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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import DROP.WORD.Update.Constantes;
import DROP.WORD.Update.Main;
import DROP.WORD.Update.entities.GerenciadorLetras;
import DROP.WORD.Update.entities.GerenciadorPalavras;
import DROP.WORD.Update.entities.LetraInfo;
import DROP.WORD.Update.utils.DesfoqueFundo;
import DROP.WORD.Update.utils.RenderUtils;

import java.util.List;

public class TelaJogo extends ScreenAdapter {
    private final Main game;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private GerenciadorPalavras gp;
    private GerenciadorLetras gl;
    private final List<String> palavrasIniciais;

    private final int xAreaJogo = 380;
    private final int yAreaJogo = 65;
    private final int larguraAreaJogo = 430;
    private final int alturaAreaJogo = 505;

    private final Rectangle caixaCabecalho = new Rectangle(18, 20, 330, 62);
    private final Rectangle caixaStatus = new Rectangle(18, 90, 330, 50);
    private final Rectangle caixaHudTopo = new Rectangle(380, 18, 430, 42);
    private final Rectangle cartaoProximaLetra = new Rectangle(388, 22, 34, 34);

    private final GlyphLayout layout = new GlyphLayout();

    private boolean jogoPausadoManual = false;
    private DesfoqueFundo desfoque;
    private float tempoDecorridoSegundos = 0f;
    private int pontuacaoPalavras = 0;

    public TelaJogo(Main game, List<String> palavras) {
        this.game = game;
        this.palavrasIniciais = palavras;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(true, Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);
        this.viewport = new FitViewport(Constantes.LARGURA_TELA, Constantes.ALTURA_TELA, camera);

        iniciarJogo(palavras);
    }

    private void iniciarJogo(List<String> palavras) {
        this.gp = new GerenciadorPalavras(palavras, game.getDicionarioOrdenado());
        float velBase = game.getDificuldadeAtual().getVelocidadeBase();
        this.gl = new GerenciadorLetras(xAreaJogo, yAreaJogo, larguraAreaJogo, alturaAreaJogo, gp, velBase);
        this.jogoPausadoManual = false;
        this.tempoDecorridoSegundos = 0f;
        this.pontuacaoPalavras = 0;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.setScreen(new TelaMenu(game));
                    return true;
                } else if (keycode == Input.Keys.P) {
                    jogoPausadoManual = !jogoPausadoManual;
                    return true;
                } else if (keycode == Input.Keys.M) {
                    game.alternarAudio();
                    return true;
                } else if (keycode == Input.Keys.R) {
                    iniciarJogo(palavrasIniciais);
                    return true;
                }

                if (jogoPausadoManual || gl.isPausadoPorPalavra()) {
                    return false;
                }

                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
                    gl.moverLetraEsquerda();
                    game.tocarSomMover();
                    return true;
                } else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
                    gl.moverLetraDireita();
                    game.tocarSomMover();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void pause() {
        // Pausa automática ao minimizar ou perder foco
        this.jogoPausadoManual = true;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public int calcularPontuacaoTotal() {
        int mult = game.getDificuldadeAtual().getMultiplicadorPontos();
        int ptsLetras = gl.getLetrasCorretasPosicionadas() * Constantes.PONTOS_LETRA_CORRETA * mult;
        return pontuacaoPalavras + ptsLetras;
    }

    public boolean verificarDerrota() {
        return gl != null && gl.verificarDerrota();
    }

    public boolean verificarVitoria() {
        return gp != null && gp.todasCompletadas();
    }

    public void verificarPalavrasFormadas() {
        List<Character> letras = gl.getLetrasLinhaInferior();
        if (letras.size() == Constantes.GRID_COLS) {
            String formada = gp.verificarPalavraFormada(letras);
            if (formada != null) {
                boolean eraPalavraAlvo = gp.registrarPalavraFormada(formada);
                int mult = game.getDificuldadeAtual().getMultiplicadorPontos();
                if (eraPalavraAlvo) {
                    pontuacaoPalavras += Constantes.PONTOS_PALAVRA_ALVO * mult;
                } else {
                    pontuacaoPalavras += Constantes.PONTOS_PALAVRA_BONUS_DICIONARIO * mult;
                }
                gl.pausarPorPalavra(formada);
                gl.removerLinhaInferior();
                game.tocarSomPalavra();
            } else {
                gl.avancarLinhaInvalida();
                game.tocarSomPouso();
            }
        }
    }

    public void update(float delta) {
        if (jogoPausadoManual) {
            return;
        }

        tempoDecorridoSegundos += delta;

        boolean segurandoBaixo = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
        gl.update(delta, segurandoBaixo);

        if (!gl.isPausadoPorPalavra()) {
            verificarPalavrasFormadas();
        }

        if (verificarDerrota()) {
            int ptsFinal = calcularPontuacaoTotal();
            game.setScreen(new TelaFimDeJogo(
                game,
                false,
                gl.getMotivoDerrota(),
                ptsFinal,
                tempoDecorridoSegundos,
                gp.getQuantidadeCompletadas(),
                gp.getPalavras().size(),
                palavrasIniciais
            ));
        } else if (verificarVitoria()) {
            int ptsFinal = calcularPontuacaoTotal();
            game.setScreen(new TelaFimDeJogo(
                game,
                true,
                null,
                ptsFinal,
                tempoDecorridoSegundos,
                gp.getQuantidadeCompletadas(),
                gp.getPalavras().size(),
                palavrasIniciais
            ));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        if (game.getScreen() != this) {
            return;
        }

        camera.update();

        if (jogoPausadoManual) {
            desenharPausa();
        } else {
            if (desfoque != null) {
                desfoque.descartar();
            }
            desenharCena();
        }
    }

    private void desenharPausa() {
        SpriteBatch batch = game.getBatch();

        if (desfoque == null) {
            desfoque = new DesfoqueFundo(Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);
        }
        if (!desfoque.temImagem()) {
            desfoque.capturar(batch, this::desenharCena);
            viewport.apply();
        }

        Gdx.gl.glClearColor(Constantes.COR_FUNDO.r, Constantes.COR_FUNDO.g, Constantes.COR_FUNDO.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        desfoque.desenhar(batch, 0, 0, Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);

        BitmapFont fontTitulo = game.getFontTitulo();
        BitmapFont fontPadrao = game.getFontPadrao();
        float cx = Constantes.LARGURA_TELA / 2f;
        float cy = Constantes.ALTURA_TELA / 2f;

        RenderUtils.desenharTextoComBorda(batch, fontTitulo, "PAUSADO",
            Color.WHITE, Color.BLACK, 2, cx, cy - 20);

        RenderUtils.desenharTextoComBorda(batch, fontPadrao, "[P] CONTINUAR    [R] REINICIAR    [ESC] MENU",
            Color.WHITE, Color.BLACK, 1, cx, cy + 40);

        batch.end();
    }

    @Override
    public void hide() {
        if (desfoque != null) {
            desfoque.dispose();
            desfoque = null;
        }
    }

    private void desenharCena() {
        Gdx.gl.glClearColor(Constantes.COR_FUNDO.r, Constantes.COR_FUNDO.g, Constantes.COR_FUNDO.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ShapeRenderer sr = game.getShapeRenderer();
        SpriteBatch batch = game.getBatch();
        BitmapFont fontTitulo = game.getFontTitulo();
        BitmapFont fontPadrao = game.getFontPadrao();
        BitmapFont fontLetras = game.getFontLetras();
        BitmapFont fontPequena = game.getFontPequena();

        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        int numCols = 2;
        int larguraColuna = 120;
        int espacamento = 40;
        int larguraTotal = numCols * (larguraColuna + 5) + (numCols - 1) * espacamento;
        int xBase = (Constantes.LARGURA_TELA - (2 * larguraTotal)) / 8;
        int yBase = 152;
        int espacamentoLinhas = 68;

        // Descobre a palavra dona da letra atual para destaque
        int idxPalavraDona = (gl.getAtual() != null) ? gl.getAtual().getPalavraOrigem() : -1;

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Cabeçalho e Status
        sr.setColor(Constantes.COR_CABECALHO);
        sr.rect(caixaCabecalho.x, caixaCabecalho.y, caixaCabecalho.width, caixaCabecalho.height);
        RenderUtils.desenharBorda(sr, caixaCabecalho, 4, Constantes.COR_BORDA);

        sr.setColor(Constantes.COR_CABECALHO);
        sr.rect(caixaStatus.x, caixaStatus.y, caixaStatus.width, caixaStatus.height);
        RenderUtils.desenharBorda(sr, caixaStatus, 3, Constantes.COR_BORDA);

        sr.setColor(Constantes.COR_CABECALHO);
        sr.rect(caixaHudTopo.x, caixaHudTopo.y, caixaHudTopo.width, caixaHudTopo.height);
        RenderUtils.desenharBorda(sr, caixaHudTopo, 3, Constantes.COR_BORDA);

        // Bloco/Cartão de Destaque da Próxima Letra (estilo o bloco de letra caindo)
        LetraInfo proxLetra = gl.espiarProximaLetra();
        sr.setColor(Constantes.COR_CARTAO_LETRA);
        sr.rect(cartaoProximaLetra.x, cartaoProximaLetra.y, cartaoProximaLetra.width, cartaoProximaLetra.height);
        Color corBordaCartaoProx = (proxLetra != null && game.getDificuldadeAtual() != Constantes.Dificuldade.DIFICIL)
            ? proxLetra.getCor() : Constantes.COR_BORDA_CARTAO;
        RenderUtils.desenharBorda(sr, cartaoProximaLetra, 2, corBordaCartaoProx);

        // Caixas das 12 palavras
        List<String> palavras = gp.getPalavras();
        for (int i = 0; i < palavras.size(); i++) {
            String p = palavras.get(i);
            int col = i % numCols;
            int lin = i / numCols;
            int px = xBase + col * (larguraColuna + espacamento + 5);
            int py = yBase + lin * espacamentoLinhas;

            boolean ehDona = (i == idxPalavraDona && !gp.isPalavraCompletada(p));
            Color corFundo = gp.isPalavraCompletada(p) ? Constantes.COR_PALAVRA_COMPLETA : (ehDona ? Color.valueOf("fff1f1") : Constantes.COR_CABECALHO);
            sr.setColor(corFundo);
            sr.rect(px, py, larguraColuna + 20, 44);

            // Se for a palavra dona da letra caindo, desenha borda mais espessa e vibrante
            Color bordaCor = ehDona ? gp.getCorPalavra(p) : Constantes.COR_BORDA;
            RenderUtils.desenharBorda(sr, px, py, larguraColuna + 20, 44, ehDona ? 4 : 3, bordaCor);
        }

        // Fundo e bordas da grade
        sr.setColor(Constantes.COR_CABECALHO);
        sr.rect(xAreaJogo - 2, yAreaJogo - 2, larguraAreaJogo + 4, alturaAreaJogo + 4);

        gl.desenharGradeVazia(sr);
        gl.desenharPreviaQueda(sr); // Prévia de queda destacando a célula de destino!
        gl.desenharBlocosDeLetras(sr);

        RenderUtils.desenharBorda(sr, xAreaJogo - 2, yAreaJogo - 2, larguraAreaJogo + 4, alturaAreaJogo + 4, 3, Constantes.COR_BORDA);

        int lq = larguraAreaJogo / Constantes.GRID_COLS;
        int hq = alturaAreaJogo / Constantes.GRID_ROWS;
        for (int col = 0; col < Constantes.GRID_COLS; col++) {
            for (int lin = 0; lin < Constantes.GRID_ROWS; lin++) {
                RenderUtils.desenharBorda(sr, xAreaJogo + col * lq, yAreaJogo + lin * hq, lq, hq, 1, Constantes.COR_BORDA);
            }
        }

        sr.end();

        batch.begin();

        // Título "WORD DROP"
        RenderUtils.desenharTextoCentralizado(batch, fontTitulo, "WORD DROP", caixaCabecalho, Constantes.COR_TEXTO);

        // Status Esquerdo (Pontuação, Tempo, Jogador)
        fontPequena.setColor(Constantes.COR_TEXTO);
        String txtPontos = String.format("PONTOS: %d   |   TEMPO: %02d:%02d",
            calcularPontuacaoTotal(),
            (int) (tempoDecorridoSegundos / 60),
            (int) (tempoDecorridoSegundos % 60));
        fontPequena.draw(batch, txtPontos, caixaStatus.x + 12, caixaStatus.y + 6);

        String ultimaFormada = gp.getHistoricoPalavrasFormadas().topo();
        String txtSub = "JOGADOR: " + game.getNomeJogadorAtual()
            + "  |  ÚLTIMA: " + (ultimaFormada != null ? ultimaFormada : "---");
        fontPequena.draw(batch, txtSub, caixaStatus.x + 12, caixaStatus.y + 24);

        // HUD Topo Direito com Letra em Destaque Visual
        LetraInfo prox = gl.espiarProximaLetra();
        Constantes.Dificuldade dif = game.getDificuldadeAtual();

        // 1. Letra destacada dentro do mini cartão
        String charProx;
        Color corProx;
        if (prox == null) {
            charProx = "-";
            corProx = Color.GRAY;
        } else if (dif == Constantes.Dificuldade.DIFICIL) {
            charProx = "?";
            corProx = Color.DARK_GRAY;
        } else {
            charProx = String.valueOf(Character.toUpperCase(prox.getLetra()));
            corProx = prox.getCor();
        }

        RenderUtils.desenharTextoComBorda(
            batch, fontPadrao, charProx,
            corProx, Color.BLACK, 1,
            cartaoProximaLetra.x + cartaoProximaLetra.width / 2f,
            cartaoProximaLetra.y + cartaoProximaLetra.height / 2f
        );

        // 2. Rótulo "PRÓXIMA" e detalhe de coluna (no Fácil)
        fontPequena.setColor(Constantes.COR_TEXTO);
        String txtRotulo = "PRÓXIMA";
        if (dif == Constantes.Dificuldade.FACIL && prox != null) {
            txtRotulo = "PRÓXIMA (col " + (prox.getPos() + 1) + "):";
        } else {
            txtRotulo = "PRÓXIMA:";
        }
        fontPequena.draw(batch, txtRotulo, cartaoProximaLetra.x + cartaoProximaLetra.width + 8, caixaHudTopo.y + 14);

        // 3. Atalhos do HUD alinhados à direita
        String atalhosTxt = "[P] PAUSA  [M] SOM  [R] REINICIAR";
        layout.setText(fontPequena, atalhosTxt);
        fontPequena.draw(batch, atalhosTxt, caixaHudTopo.x + caixaHudTopo.width - layout.width - 12, caixaHudTopo.y + 14);

        // Palavras da lista
        for (int i = 0; i < palavras.size(); i++) {
            String p = palavras.get(i);
            int col = i % numCols;
            int lin = i / numCols;
            int px = xBase + col * (larguraColuna + espacamento + 5);
            int py = yBase + lin * espacamentoLinhas;

            Color corTexto = gp.isPalavraCompletada(p) ? Constantes.COR_TEXTO : gp.getCorPalavra(p);
            RenderUtils.desenharTextoComBorda(
                batch, fontPadrao, p.toUpperCase(),
                corTexto, Color.BLACK, 1,
                px + (larguraColuna + 20) / 2f, py + 44 / 2f
            );
        }

        // Letras da grade
        gl.desenharTextos(batch, fontLetras);

        // Mensagem de palavra formada em pausa de comemoração
        if (gl.isPausadoPorPalavra() && gl.getPalavraEmPausa() != null) {
            String msg = "PALAVRA: " + gl.getPalavraEmPausa().toUpperCase() + "!";
            RenderUtils.desenharTextoComBorda(
                batch, fontTitulo, msg,
                Constantes.COR_PALAVRA_COMPLETA, Color.BLACK, 2,
                xAreaJogo + larguraAreaJogo / 2f, yAreaJogo + alturaAreaJogo / 2f
            );
        }

        batch.end();
    }
}
