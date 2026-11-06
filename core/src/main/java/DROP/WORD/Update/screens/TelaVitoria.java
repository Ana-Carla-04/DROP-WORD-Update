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

public class TelaVitoria extends ScreenAdapter {
    private final Main game;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final int pontuacaoFinal;
    private final float tempoSegundos;
    private final int palavrasCompletadas;

    private final Rectangle caixaPrincipal = new Rectangle(20, 20, Constantes.LARGURA_TELA - 40, Constantes.ALTURA_TELA - 40);
    private final Rectangle caixaTitulo = new Rectangle(Constantes.LARGURA_TELA / 2f - 300, 48, 600, 82);
    private final Rectangle caixaResumo = new Rectangle(Constantes.LARGURA_TELA / 2f - 300, 148, 600, 185);
    private final Rectangle caixaInputNome = new Rectangle(Constantes.LARGURA_TELA / 2f - 220, 352, 440, 62);
    private final Rectangle botaoMenu = new Rectangle(Constantes.LARGURA_TELA / 2f - 140, 435, 280, 62);

    private final Color corFundoExterno = Constantes.COR_FUNDO;
    private final Color corFundoInterno = Color.valueOf("a2c8a5");
    private final Color corBordaInterna = Color.valueOf("ffffff");
    private final Color corTituloCaixa  = Color.valueOf("f4ddde");
    private final Color corTituloBorda  = Color.valueOf("b26969");
    private final Color corCampoNome    = Color.valueOf("fff6f6");

    private final Vector3 mousePos = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();

    private StringBuilder bufferNome;
    private boolean pontuacaoSalva = false;
    private float tempoCursor = 0f;

    public TelaVitoria(Main game) {
        this(game, 0, 0f, Constantes.TOTAL_PALAVRAS_PARTIDA);
    }

    public TelaVitoria(Main game, int pontuacaoFinal, float tempoSegundos, int palavrasCompletadas) {
        this.game = game;
        this.pontuacaoFinal = pontuacaoFinal;
        this.tempoSegundos = tempoSegundos;
        this.palavrasCompletadas = palavrasCompletadas;
        this.bufferNome = new StringBuilder(game.getNomeJogadorAtual());

        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(true, Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);
        this.viewport = new FitViewport(Constantes.LARGURA_TELA, Constantes.ALTURA_TELA, camera);
    }

    private void salvarEVoltarAoMenu() {
        if (!pontuacaoSalva) {
            String nomeFinal = GerenciadorArquivos.normalizarNome(bufferNome.toString());
            game.setNomeJogadorAtual(nomeFinal);
            GerenciadorArquivos.salvarPontuacao(
                nomeFinal,
                pontuacaoFinal,
                tempoSegundos,
                game.getDificuldadeAtual().getRotulo()
            );
            pontuacaoSalva = true;
        }
        game.setScreen(new TelaMenu(game));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                mousePos.set(screenX, screenY, 0);
                viewport.unproject(mousePos);

                if (botaoMenu.contains(mousePos.x, mousePos.y)) {
                    salvarEVoltarAoMenu();
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.ESCAPE) {
                    salvarEVoltarAoMenu();
                    return true;
                } else if (keycode == Input.Keys.BACKSPACE) {
                    if (bufferNome.length() > 0) {
                        bufferNome.deleteCharAt(bufferNome.length() - 1);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                if (Character.isLetterOrDigit(character) || character == ' ' || character == '_' || character == '-') {
                    if (bufferNome.length() < 12) {
                        bufferNome.append(Character.toUpperCase(character));
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
        tempoCursor += delta;

        Gdx.gl.glClearColor(corFundoExterno.r, corFundoExterno.g, corFundoExterno.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        boolean estaSobreBotao = botaoMenu.contains(mousePos.x, mousePos.y);

        camera.update();
        ShapeRenderer sr = game.getShapeRenderer();
        SpriteBatch batch = game.getBatch();
        BitmapFont fontTitulo = game.getFontTitulo();
        BitmapFont fontPadrao = game.getFontPadrao();

        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // 1. FORMAS PREENCHIDAS E BORDAS
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

        RenderUtils.desenharCaixaBotao(sr, botaoMenu, estaSobreBotao, 3);
        sr.end();

        // 2. TEXTOS
        batch.begin();
        fontTitulo.getData().setScale(2.7f);
        layout.setText(fontTitulo, "PARABÉNS! VOCÊ VENCEU!");

        fontTitulo.setColor(corFundoInterno);
        fontTitulo.draw(batch, "PARABÉNS! VOCÊ VENCEU!",
            caixaTitulo.x + (caixaTitulo.width - layout.width) / 2f + 3,
            caixaTitulo.y + (caixaTitulo.height - layout.height) / 2f + 3);

        fontTitulo.setColor(Color.BLACK);
        fontTitulo.draw(batch, "PARABÉNS! VOCÊ VENCEU!",
            caixaTitulo.x + (caixaTitulo.width - layout.width) / 2f,
            caixaTitulo.y + (caixaTitulo.height - layout.height) / 2f);

        fontPadrao.getData().setScale(1.45f);
        fontPadrao.setColor(Color.valueOf("1d6323"));
        String msgTopo = "TODAS AS " + palavrasCompletadas + " PALAVRAS FORAM FORMADAS!";
        layout.setText(fontPadrao, msgTopo);
        fontPadrao.draw(batch, msgTopo, caixaResumo.x + (caixaResumo.width - layout.width) / 2f, caixaResumo.y + 24);

        fontPadrao.getData().setScale(1.4f);
        fontPadrao.setColor(Color.BLACK);

        int recorde = Math.max(GerenciadorArquivos.obterRecorde(), pontuacaoFinal);
        String linhaPts = "PONTUAÇÃO FINAL: " + pontuacaoFinal + " PTS  (RECORDE: " + recorde + " PTS)";
        layout.setText(fontPadrao, linhaPts);
        fontPadrao.draw(batch, linhaPts, caixaResumo.x + (caixaResumo.width - layout.width) / 2f, caixaResumo.y + 75);

        String linhaTempo = String.format("TEMPO TOTAL: %02d:%02d   |   NÍVEL: %s",
            (int) (tempoSegundos / 60), (int) (tempoSegundos % 60), game.getDificuldadeAtual().getRotulo());
        layout.setText(fontPadrao, linhaTempo);
        fontPadrao.draw(batch, linhaTempo, caixaResumo.x + (caixaResumo.width - layout.width) / 2f, caixaResumo.y + 125);

        // Caixa de Nome para o Ranking
        fontPadrao.getData().setScale(1.05f);
        fontPadrao.setColor(corTituloBorda);
        String labelInput = "DIGITE SEU NOME PARA O RANKING:";
        layout.setText(fontPadrao, labelInput);
        fontPadrao.draw(batch, labelInput,
            caixaInputNome.x + (caixaInputNome.width - layout.width) / 2f,
            caixaInputNome.y + 8);

        fontPadrao.getData().setScale(1.55f);
        fontPadrao.setColor(Color.BLACK);
        String cursor = ((int) (tempoCursor * 2) % 2 == 0) ? "_" : "";
        String txtNome = bufferNome.toString() + cursor;
        layout.setText(fontPadrao, txtNome);
        fontPadrao.draw(batch, txtNome,
            caixaInputNome.x + (caixaInputNome.width - layout.width) / 2f,
            caixaInputNome.y + 30);

        // Botão Salvar e Voltar ao Menu
        fontPadrao.getData().setScale(1.55f);
        RenderUtils.desenharTextoCentralizado(batch, fontPadrao, "SALVAR E MENU [ENTER]", botaoMenu,
            estaSobreBotao ? Constantes.COR_BOTAO_TEXTO_HOVER : Constantes.COR_BOTAO_TEXTO);

        batch.end();
    }
}
