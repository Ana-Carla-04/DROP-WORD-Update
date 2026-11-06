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

public class TelaMenu extends ScreenAdapter {
    private final Main game;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final Rectangle caixaPrincipal = new Rectangle(20, 20, Constantes.LARGURA_TELA - 40, Constantes.ALTURA_TELA - 40);
    private final Rectangle caixaTitulo = new Rectangle(Constantes.LARGURA_TELA / 2f - 300, 36, 600, 78);

    private final Rectangle caixaNomeJogador = new Rectangle(65, 125, 270, 50);
    private final Rectangle botaoPlay = new Rectangle(65, 187, 270, 52);
    private final Rectangle botaoDificuldade = new Rectangle(65, 249, 270, 52);
    private final Rectangle botaoAudio = new Rectangle(65, 311, 270, 52);
    private final Rectangle botaoExit = new Rectangle(65, 373, 270, 48);

    private final Rectangle painelRanking = new Rectangle(365, 125, 420, 296);
    private final Rectangle botaoLimparRanking = new Rectangle(665, 392, 110, 24);
    private final Rectangle painelControles = new Rectangle(50, 436, Constantes.LARGURA_TELA - 100, 126);

    private final Color corFundoExterno = Constantes.COR_FUNDO;
    private final Color corFundoInterno = Color.valueOf("f4d8d9");
    private final Color corBordaInterna = Color.valueOf("8f8b8b");
    private final Color corTituloCaixa  = Color.valueOf("f4ddde");
    private final Color corTituloBorda  = Color.valueOf("b26969");
    private final Color corTextoTitulo  = Color.valueOf("000000");
    private final Color corCampoAtivo   = Color.valueOf("fff6f6");

    private final Vector3 mousePos = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();

    // 0 = Nome do Jogador, 1 = Jogar, 2 = Dificuldade, 3 = Áudio, 4 = Sair
    private int opcaoSelecionada = 1;
    private boolean editandoNome = false;
    private StringBuilder bufferNome = new StringBuilder();
    private float tempoCursor = 0f;

    // Rastreia se o mouse realmente se moveu para não sobrescrever o teclado
    private int ultimoMouseX = -1;
    private int ultimoMouseY = -1;

    private List<GerenciadorArquivos.RegistroRanking> ranking;
    private int recordeMaximo;

    public TelaMenu(Main game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(true, Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);
        this.viewport = new FitViewport(Constantes.LARGURA_TELA, Constantes.ALTURA_TELA, camera);
    }

    @Override
    public void show() {
        atualizarDadosRanking();
        bufferNome = new StringBuilder(game.getNomeJogadorAtual());

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                mousePos.set(screenX, screenY, 0);
                viewport.unproject(mousePos);

                if (caixaNomeJogador.contains(mousePos.x, mousePos.y)) {
                    iniciarEdicaoNome();
                    return true;
                } else {
                    if (editandoNome) {
                        confirmarEdicaoNome();
                    }
                }

                if (botaoPlay.contains(mousePos.x, mousePos.y)) {
                    iniciarPartida();
                    return true;
                } else if (botaoDificuldade.contains(mousePos.x, mousePos.y)) {
                    alternarDificuldade();
                    return true;
                } else if (botaoAudio.contains(mousePos.x, mousePos.y)) {
                    alternarAudio();
                    return true;
                } else if (botaoExit.contains(mousePos.x, mousePos.y)) {
                    Gdx.app.exit();
                    return true;
                } else if (botaoLimparRanking.contains(mousePos.x, mousePos.y)) {
                    GerenciadorArquivos.limparRanking();
                    atualizarDadosRanking();
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                // Só atualiza seleção por mouse se ele realmente se movimentou
                if (screenX != ultimoMouseX || screenY != ultimoMouseY) {
                    ultimoMouseX = screenX;
                    ultimoMouseY = screenY;
                    mousePos.set(screenX, screenY, 0);
                    viewport.unproject(mousePos);

                    if (!editandoNome) {
                        if (caixaNomeJogador.contains(mousePos.x, mousePos.y)) opcaoSelecionada = 0;
                        else if (botaoPlay.contains(mousePos.x, mousePos.y)) opcaoSelecionada = 1;
                        else if (botaoDificuldade.contains(mousePos.x, mousePos.y)) opcaoSelecionada = 2;
                        else if (botaoAudio.contains(mousePos.x, mousePos.y)) opcaoSelecionada = 3;
                        else if (botaoExit.contains(mousePos.x, mousePos.y)) opcaoSelecionada = 4;
                    }
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (editandoNome) {
                    if (keycode == Input.Keys.ENTER || keycode == Input.Keys.ESCAPE || keycode == Input.Keys.DOWN) {
                        confirmarEdicaoNome();
                        if (keycode == Input.Keys.DOWN) {
                            opcaoSelecionada = 1;
                        }
                        return true;
                    } else if (keycode == Input.Keys.BACKSPACE) {
                        if (bufferNome.length() > 0) {
                            bufferNome.deleteCharAt(bufferNome.length() - 1);
                        }
                        return true;
                    }
                    return false;
                }

                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                    opcaoSelecionada = (opcaoSelecionada + 4) % 5;
                    if (opcaoSelecionada == 0) iniciarEdicaoNome();
                    return true;
                } else if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                    opcaoSelecionada = (opcaoSelecionada + 1) % 5;
                    if (opcaoSelecionada == 0) iniciarEdicaoNome();
                    return true;
                } else if (keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
                    if (opcaoSelecionada == 2) {
                        alternarDificuldade();
                        return true;
                    } else if (opcaoSelecionada == 3) {
                        alternarAudio();
                        return true;
                    }
                } else if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    executarOpcaoSelecionada();
                    return true;
                } else if (keycode == Input.Keys.ESCAPE) {
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                if (!editandoNome) return false;

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

    private void atualizarDadosRanking() {
        this.ranking = GerenciadorArquivos.carregarRanking();
        this.recordeMaximo = GerenciadorArquivos.obterRecorde();
    }

    private void iniciarEdicaoNome() {
        this.opcaoSelecionada = 0;
        this.editandoNome = true;
    }

    private void confirmarEdicaoNome() {
        this.editandoNome = false;
        String digitado = bufferNome.toString().trim();
        if (digitado.isEmpty()) {
            digitado = "JOGADOR 1";
        }
        game.setNomeJogadorAtual(digitado);
        bufferNome = new StringBuilder(game.getNomeJogadorAtual());
    }

    private void iniciarPartida() {
        if (editandoNome) confirmarEdicaoNome();
        List<String> palavrasSorteadas = GerenciadorArquivos.sortearPalavrasPartida(
            game.getDicionarioOrdenado(),
            Constantes.TOTAL_PALAVRAS_PARTIDA
        );
        game.setScreen(new TelaJogo(game, palavrasSorteadas));
    }

    private void alternarDificuldade() {
        game.setDificuldadeAtual(game.getDificuldadeAtual().proxima());
    }

    private void alternarAudio() {
        game.alternarAudio();
    }

    private void executarOpcaoSelecionada() {
        if (opcaoSelecionada == 0) {
            iniciarEdicaoNome();
        } else if (opcaoSelecionada == 1) {
            iniciarPartida();
        } else if (opcaoSelecionada == 2) {
            alternarDificuldade();
        } else if (opcaoSelecionada == 3) {
            alternarAudio();
        } else {
            Gdx.app.exit();
        }
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

        boolean hoverNome = opcaoSelecionada == 0 || editandoNome;
        boolean hoverPlay = opcaoSelecionada == 1 && !editandoNome;
        boolean hoverDif = opcaoSelecionada == 2 && !editandoNome;
        boolean hoverAudio = opcaoSelecionada == 3 && !editandoNome;
        boolean hoverExit = opcaoSelecionada == 4 && !editandoNome;
        boolean hoverLimpar = botaoLimparRanking.contains(mousePos.x, mousePos.y);

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
        RenderUtils.desenharBorda(sr, caixaTitulo, 5, corTituloBorda);

        // Caixa editável do Nome do Jogador
        sr.setColor(editandoNome ? corCampoAtivo : corTituloCaixa);
        sr.rect(caixaNomeJogador.x, caixaNomeJogador.y, caixaNomeJogador.width, caixaNomeJogador.height);
        RenderUtils.desenharBorda(sr, caixaNomeJogador, hoverNome ? 4 : 2,
            hoverNome ? corTituloBorda : corBordaInterna);

        RenderUtils.desenharCaixaBotao(sr, botaoPlay, hoverPlay, 3);
        RenderUtils.desenharCaixaBotao(sr, botaoDificuldade, hoverDif, 3);
        RenderUtils.desenharCaixaBotao(sr, botaoAudio, hoverAudio, 3);
        RenderUtils.desenharCaixaBotao(sr, botaoExit, hoverExit, 3);

        // Painel de Ranking
        sr.setColor(corTituloCaixa);
        sr.rect(painelRanking.x, painelRanking.y, painelRanking.width, painelRanking.height);
        RenderUtils.desenharBorda(sr, painelRanking, 3, corTituloBorda);

        // Botão Zerar Ranking
        RenderUtils.desenharCaixaBotao(sr, botaoLimparRanking, hoverLimpar, 2);

        // Painel de Controles
        sr.setColor(corTituloCaixa);
        sr.rect(painelControles.x, painelControles.y, painelControles.width, painelControles.height);
        RenderUtils.desenharBorda(sr, painelControles, 3, corBordaInterna);

        sr.end();

        // 2. TEXTOS
        batch.begin();

        RenderUtils.desenharTextoCentralizado(batch, fontTitulo, "WORD DROP", caixaTitulo, corTextoTitulo);

        // Texto do campo de Nome do Jogador
        fontPequena.setColor(corTituloBorda);
        String labelJogador = editandoNome ? "DIGITANDO (ENTER CONFIRMA):" : "JOGADOR (CLIQUE P/ MUDAR):";
        fontPequena.draw(batch, labelJogador, caixaNomeJogador.x + 10, caixaNomeJogador.y + 6);

        fontPadrao.setColor(Constantes.COR_TEXTO);
        String cursor = (editandoNome && ((int) (tempoCursor * 2) % 2 == 0)) ? "_" : "";
        String textoNome = (editandoNome ? bufferNome.toString() : game.getNomeJogadorAtual()) + cursor;
        fontPadrao.draw(batch, textoNome, caixaNomeJogador.x + 10, caixaNomeJogador.y + 24);

        RenderUtils.desenharTextoCentralizado(batch, fontPadrao, "JOGAR", botaoPlay,
            hoverPlay ? Constantes.COR_BOTAO_TEXTO_HOVER : Constantes.COR_BOTAO_TEXTO);

        String txtDif = "NÍVEL: " + game.getDificuldadeAtual().getRotulo();
        RenderUtils.desenharTextoCentralizado(batch, fontPadrao, txtDif, botaoDificuldade,
            hoverDif ? Constantes.COR_BOTAO_TEXTO_HOVER : Constantes.COR_BOTAO_TEXTO);

        String txtAudio = "SOM: " + (game.isAudioAtivado() ? "LIGADO [M]" : "MUTADO [M]");
        RenderUtils.desenharTextoCentralizado(batch, fontPadrao, txtAudio, botaoAudio,
            hoverAudio ? Constantes.COR_BOTAO_TEXTO_HOVER : Constantes.COR_BOTAO_TEXTO);

        RenderUtils.desenharTextoCentralizado(batch, fontPadrao, "SAIR", botaoExit,
            hoverExit ? Constantes.COR_BOTAO_TEXTO_HOVER : Constantes.COR_BOTAO_TEXTO);

        // Textos do Ranking (Alinhamento em colunas fixas com as 8 entradas)
        fontPadrao.setColor(corTituloBorda);
        String tituloRank = "COMPETIÇÃO - TOP 8 (MÁX: " + recordeMaximo + ")";
        layout.setText(fontPadrao, tituloRank);
        fontPadrao.draw(batch, tituloRank,
            painelRanking.x + (painelRanking.width - layout.width) / 2f, painelRanking.y + 12);

        fontPequena.setColor(Constantes.COR_TEXTO);
        if (ranking == null || ranking.isEmpty()) {
            String msgVazio = "Nenhuma partida registrada ainda.";
            layout.setText(fontPequena, msgVazio);
            fontPequena.draw(batch, msgVazio,
                painelRanking.x + (painelRanking.width - layout.width) / 2f, painelRanking.y + 130);
        } else {
            // Colunas fixas para alinhamento perfeito sem extrapolar bordas
            float colPos = painelRanking.x + 12;
            float colNome = painelRanking.x + 44;
            float colPts = painelRanking.x + 165;
            float colTempo = painelRanking.x + 258;
            float colDif = painelRanking.x + 322;

            for (int i = 0; i < Math.min(8, ranking.size()); i++) {
                GerenciadorArquivos.RegistroRanking reg = ranking.get(i);
                float yItem = painelRanking.y + 40 + i * 29;

                fontPequena.draw(batch, "#" + (i + 1), colPos, yItem);
                fontPequena.draw(batch, reg.getNomeJogador(), colNome, yItem);
                fontPequena.draw(batch, reg.getPontuacao() + " pts", colPts, yItem);
                fontPequena.draw(batch, String.format("%.0fs", reg.getTempoSegundos()), colTempo, yItem);
                fontPequena.draw(batch, reg.getDificuldade(), colDif, yItem);
            }
        }

        RenderUtils.desenharTextoCentralizado(batch, fontPequena, "ZERAR", botaoLimparRanking,
            hoverLimpar ? Constantes.COR_BOTAO_TEXTO_HOVER : Constantes.COR_BOTAO_TEXTO);

        // Instruções de Controles (com acentuação completa e bem distribuída)
        fontPadrao.setColor(corTituloBorda);
        String tituloCtrl = "COMO JOGAR & CONTROLES";
        layout.setText(fontPadrao, tituloCtrl);
        fontPadrao.draw(batch, tituloCtrl,
            painelControles.x + (painelControles.width - layout.width) / 2f, painelControles.y + 6);

        fontPequena.setColor(Constantes.COR_TEXTO);
        String[] instrucoes = {
            "> COMPETIÇÃO: Clique em JOGADOR acima (ou digite no fim do jogo) para definir seu nome!",
            "> SETAS ESQ / DIR: Move a letra para a coluna correspondente (coluna 1 a 5).",
            "> SEGURAR SETA BAIXO: Acelera queda  |  [P] Pausa  |  [R] Reinicia  |  [M] Som.",
            "> DICA: Qualquer palavra de 5 letras do dicionário limpa a linha e dá bônus!"
        };
        for (int i = 0; i < instrucoes.length; i++) {
            fontPequena.draw(batch, instrucoes[i], painelControles.x + 14, painelControles.y + 30 + i * 22);
        }

        batch.end();
    }
}
