package DROP.WORD.Update.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import DROP.WORD.Update.Constantes;
import DROP.WORD.Update.Main;
import DROP.WORD.Update.utils.GerenciadorArquivos;
import DROP.WORD.Update.utils.RenderUtils;

import java.util.List;

public class TelaFimDeJogo extends ScreenAdapter {
    private final Main game;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final boolean vitoria;
    private final String motivoDerrota;
    private final int pontuacaoFinal;
    private final float tempoSegundos;
    private final int palavrasCompletadas;
    private final int totalPalavras;
    private final List<String> palavrasPartida;

    private final Rectangle caixaPrincipal = new Rectangle(20, 20, Constantes.LARGURA_TELA - 40, Constantes.ALTURA_TELA - 40);
    private final Rectangle caixaTitulo = new Rectangle(Constantes.LARGURA_TELA / 2f - 300, 42, 600, 80);
    private final Rectangle caixaResumo = new Rectangle(Constantes.LARGURA_TELA / 2f - 300, 138, 600, 185);
    private final Rectangle caixaInputNome = new Rectangle(Constantes.LARGURA_TELA / 2f - 220, 335, 440, 58);

    private final Rectangle botaoJogarNovamente = new Rectangle(Constantes.LARGURA_TELA / 2f - 260, 415, 240, 58);
    private final Rectangle botaoMenu = new Rectangle(Constantes.LARGURA_TELA / 2f + 20, 415, 240, 58);

    private final Color corFundoExterno = Constantes.COR_FUNDO;
    private final Color corFundoInterno;
    private final Color corBordaInterna = Color.valueOf("ffffff");
    private final Color corTituloCaixa  = Color.valueOf("f4ddde");
    private final Color corTituloBorda  = Color.valueOf("b26969");
    private final Color corCampoNome    = Color.valueOf("fff6f6");

    private final Vector3 mousePos = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();

    private StringBuilder bufferNome;
    private float tempoDesdeInicio = 0f;
    private static final float DELAY_BLOQUEIO_DIGITACAO = 0.5f;

    public TelaFimDeJogo(Main game, boolean vitoria, String motivoDerrota, int pontuacaoFinal,
                         float tempoSegundos, int palavrasCompletadas, int totalPalavras, List<String> palavrasPartida) {
        this.game = game;
        this.vitoria = vitoria;
        this.motivoDerrota = motivoDerrota;
        this.pontuacaoFinal = pontuacaoFinal;
        this.tempoSegundos = tempoSegundos;
        this.palavrasCompletadas = palavrasCompletadas;
        this.totalPalavras = totalPalavras;
        this.palavrasPartida = palavrasPartida;

        this.corFundoInterno = vitoria ? Color.valueOf("a2c8a5") : Color.valueOf("ef7b7b");
        this.bufferNome = new StringBuilder(game.getNomeJogadorAtual());

        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(true, Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);
        this.viewport = new FitViewport(Constantes.LARGURA_TELA, Constantes.ALTURA_TELA, camera);

        // Auto-salva logo ao entrar na tela final
        salvarPontuacao();

        if (vitoria) {
            game.tocarSomVitoria();
        } else {
            game.tocarSomErro();
        }
    }

    private void salvarPontuacao() {
        String nomeFinal = GerenciadorArquivos.normalizarNome(bufferNome.toString());
        game.setNomeJogadorAtual(nomeFinal);
        GerenciadorArquivos.salvarPontuacao(
            nomeFinal,
            pontuacaoFinal,
            tempoSegundos,
            game.getDificuldadeAtual().getRotulo()
        );
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                mousePos.set(screenX, screenY, 0);
                viewport.unproject(mousePos);

                if (botaoJogarNovamente.contains(mousePos.x, mousePos.y)) {
                    salvarPontuacao();
                    List<String> novasPalavras = GerenciadorArquivos.sortearPalavrasPartida(
                        game.getDicionarioOrdenado(),
                        Constantes.TOTAL_PALAVRAS_PARTIDA
                    );
                    game.setScreen(new TelaJogo(game, novasPalavras));
                    return true;
                } else if (botaoMenu.contains(mousePos.x, mousePos.y)) {
                    salvarPontuacao();
                    game.setScreen(new TelaMenu(game));
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.ESCAPE) {
                    salvarPontuacao();
                    game.setScreen(new TelaMenu(game));
                    return true;
                } else if (keycode == Input.Keys.BACKSPACE) {
                    if (tempoDesdeInicio >= DELAY_BLOQUEIO_DIGITACAO && bufferNome.length() > 0) {
                        bufferNome.deleteCharAt(bufferNome.length() - 1);
                        salvarPontuacao();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                if (tempoDesdeInicio < DELAY_BLOQUEIO_DIGITACAO) {
                    return false;
                }

                if (Character.isLetterOrDigit(character) || character == ' ' || character == '_' || character == '-') {
                    if (bufferNome.length() < 12) {
                        bufferNome.append(Character.toUpperCase(character));
                        salvarPontuacao();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        tempoDesdeInicio += delta;

        Gdx.gl.glClearColor(corFundoExterno.r, corFundoExterno.g, corFundoExterno.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        boolean hoverReplay = botaoJogarNovamente.contains(mousePos.x, mousePos.y);
        boolean hoverMenu = botaoMenu.contains(mousePos.x, mousePos.y);

        camera.update();
        ShapeRenderer sr = game.getShapeRenderer();
        SpriteBatch batch = game.getBatch();
        BitmapFont fontTitulo = game.getFontTitulo();
        BitmapFont fontPadrao = game.getFontPadrao();
        BitmapFont fontPequena = game.getFontPequena();

        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // 1. FORMAS E BORDAS
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(corFundoInterno);
        sr.rect(caixaPrincipal.x, caixaPrincipal.y, caixaPrincipal.width, caixaPrincipal.height);
        RenderUtils.desenharBorda(sr, caixaPrincipal, 3, corBordaInterna);

        sr.setColor(corTituloCaixa);
        sr.rect(caixaTitulo.x, caixaTitulo.y, caixaTitulo.width, caixaTitulo.height);
        RenderUtils.desenharBorda(sr, caixaTitulo, 3, corTituloBorda);

        sr.setColor(corTituloCaixa);
        sr.rect(caixaResumo.x, caixaResumo.y, caixaResumo.width, caixaResumo.height);
        RenderUtils.desenharBorda(sr, caixaResumo, 3, corTituloBorda);

        sr.setColor(corCampoNome);
        sr.rect(caixaInputNome.x, caixaInputNome.y, caixaInputNome.width, caixaInputNome.height);
        RenderUtils.desenharBorda(sr, caixaInputNome, 3, corTituloBorda);

        RenderUtils.desenharCaixaBotao(sr, botaoJogarNovamente, hoverReplay, 3);
        RenderUtils.desenharCaixaBotao(sr, botaoMenu, hoverMenu, 3);
        sr.end();

        // 2. TEXTOS
        batch.begin();

        String textoTitulo = vitoria ? "PARABÉNS! VOCÊ VENCEU!" : "VOCÊ PERDEU";
        layout.setText(fontTitulo, textoTitulo);

        fontTitulo.setColor(corFundoInterno);
        fontTitulo.draw(batch, textoTitulo,
            caixaTitulo.x + (caixaTitulo.width - layout.width) / 2f + 2,
            caixaTitulo.y + (caixaTitulo.height - layout.height) / 2f + 2);

        fontTitulo.setColor(Color.BLACK);
        fontTitulo.draw(batch, textoTitulo,
            caixaTitulo.x + (caixaTitulo.width - layout.width) / 2f,
            caixaTitulo.y + (caixaTitulo.height - layout.height) / 2f);

        if (vitoria) {
            fontPadrao.setColor(Color.valueOf("1d6323"));
            String msgVitoria = "TODAS AS " + palavrasCompletadas + " PALAVRAS FORAM FORMADAS!";
            layout.setText(fontPadrao, msgVitoria);
            fontPadrao.draw(batch, msgVitoria,
                caixaResumo.x + (caixaResumo.width - layout.width) / 2f, caixaResumo.y + 18);
        } else {
            fontPadrao.setColor(Color.valueOf("901a1a"));
            layout.setText(fontPadrao, motivoDerrota);
            fontPadrao.draw(batch, motivoDerrota,
                caixaResumo.x + (caixaResumo.width - layout.width) / 2f, caixaResumo.y + 18);
        }

        fontPadrao.setColor(Color.BLACK);
        int recorde = Math.max(GerenciadorArquivos.obterRecorde(), pontuacaoFinal);
        String linhaPts = "PONTUAÇÃO FINAL: " + pontuacaoFinal + " PTS  (RECORDE: " + recorde + " PTS)";
        layout.setText(fontPadrao, linhaPts);
        fontPadrao.draw(batch, linhaPts, caixaResumo.x + (caixaResumo.width - layout.width) / 2f, caixaResumo.y + 60);

        String linhaPal = String.format("PALAVRAS COMPLETADAS: %d / %d", palavrasCompletadas, totalPalavras);
        layout.setText(fontPadrao, linhaPal);
        fontPadrao.draw(batch, linhaPal, caixaResumo.x + (caixaResumo.width - layout.width) / 2f, caixaResumo.y + 102);

        String linhaTempo = String.format("TEMPO TOTAL: %02d:%02d  |  NÍVEL: %s",
            (int) (tempoSegundos / 60), (int) (tempoSegundos % 60), game.getDificuldadeAtual().getRotulo());
        layout.setText(fontPadrao, linhaTempo);
        fontPadrao.draw(batch, linhaTempo, caixaResumo.x + (caixaResumo.width - layout.width) / 2f, caixaResumo.y + 144);

        fontPequena.setColor(corTituloBorda);
        String labelInput = "NOME NO RANKING (CLIQUE OU DIGITE PARA ALTERAR):";
        layout.setText(fontPequena, labelInput);
        fontPequena.draw(batch, labelInput,
            caixaInputNome.x + (caixaInputNome.width - layout.width) / 2f, caixaInputNome.y + 6);

        fontPadrao.setColor(Color.BLACK);
        String cursor = ((int) (tempoDesdeInicio * 2) % 2 == 0) ? "_" : "";
        String txtNome = bufferNome.toString() + cursor;
        layout.setText(fontPadrao, txtNome);
        fontPadrao.draw(batch, txtNome,
            caixaInputNome.x + (caixaInputNome.width - layout.width) / 2f, caixaInputNome.y + 26);

        RenderUtils.desenharTextoCentralizado(batch, fontPadrao, "JOGAR NOVAMENTE", botaoJogarNovamente,
            hoverReplay ? Constantes.COR_BOTAO_TEXTO_HOVER : Constantes.COR_BOTAO_TEXTO);

        RenderUtils.desenharTextoCentralizado(batch, fontPadrao, "MENU PRINCIPAL", botaoMenu,
            hoverMenu ? Constantes.COR_BOTAO_TEXTO_HOVER : Constantes.COR_BOTAO_TEXTO);

        batch.end();
    }
}
