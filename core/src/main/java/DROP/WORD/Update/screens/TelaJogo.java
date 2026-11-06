package DROP.WORD.Update.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import DROP.WORD.Update.Constantes;
import DROP.WORD.Update.Main;
import DROP.WORD.Update.entities.GerenciadorLetras;
import DROP.WORD.Update.entities.GerenciadorPalavras;

import java.util.List;

public class TelaJogo extends ScreenAdapter {
    private final Main game;
    private final OrthographicCamera camera;

    private GerenciadorPalavras gp;
    private GerenciadorLetras gl;
    private final List<String> palavrasIniciais;

    private final int xAreaJogo = 380;
    private final int yAreaJogo = 30;
    private final int larguraAreaJogo = 430;
    private final int alturaAreaJogo = 540;

    private final GlyphLayout layout = new GlyphLayout();

    public TelaJogo(Main game, List<String> palavras) {
        this.game = game;
        this.palavrasIniciais = palavras;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(true, Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);

        iniciarJogo(palavras);
    }

    private void iniciarJogo(List<String> palavras) {
        this.gp = new GerenciadorPalavras(palavras);
        this.gl = new GerenciadorLetras(xAreaJogo, yAreaJogo, larguraAreaJogo, alturaAreaJogo, gp);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.setScreen(new TelaMenu(game));
                    return true;
                } else if (keycode == Input.Keys.LEFT && !gl.pausado) {
                    gl.moverLetraEsquerda();
                    return true;
                } else if (keycode == Input.Keys.RIGHT && !gl.pausado) {
                    gl.moverLetraDireita();
                    return true;
                } else if (keycode == Input.Keys.DOWN) {
                    if (gl.atual != null && !gl.pausado) {
                        gl.atual.y += 20;
                    }
                    return true;
                } else if (keycode == Input.Keys.R) {
                    iniciarJogo(palavrasIniciais);
                    return true;
                }
                return false;
            }
        });
    }

    public boolean verificarDerrota() {
        if (gl == null) return false;
        boolean col0 = false;
        for (int c = 0; c < Constantes.GRID_COLS; c++) {
            if (gl.grid[0][c] != null) { col0 = true; break; }
        }
        return col0 || gl.colInvalida;
    }

    public boolean verificarVitoria() {
        if (gp == null) return false;
        for (boolean b : gp.completadas.values()) {
            if (!b) return false;
        }
        return true;
    }

    public void verificarPalavrasFormadas() {
        List<Character> letras = gl.getLetrasLinhaInferior();
        if (letras.size() == Constantes.GRID_COLS) {
            String formata = gp.verificarPalavraFormada(letras);
            if (formata != null) {
                gp.marcarPalavraCompleta(formata);
                gl.pausarPorPalavra(formata);
                gl.removerLinhaInferior();
            } else {
                gl.ultimaLinhaValida--;
            }
        }
    }

    @Override
    public void render(float delta) {
        gl.update(delta);
        if (!gl.pausado) {
            verificarPalavrasFormadas();
        }

        if (verificarDerrota()) {
            game.setScreen(new TelaGameOver(game));
            return;
        } else if (verificarVitoria()) {
            game.setScreen(new TelaVitoria(game));
            return;
        }

        Gdx.gl.glClearColor(Constantes.COR_FUNDO.r, Constantes.COR_FUNDO.g, Constantes.COR_FUNDO.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.shapeRenderer.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(Constantes.COR_CABECALHO);
        game.shapeRenderer.rect(18, 30, 330, 90);

        int numCols = 2;
        int larguraColuna = 120;
        int espacamento = 40;
        int larguraTotal = numCols * (larguraColuna + 5) + (numCols - 1) * espacamento;
        int xBase = (Constantes.LARGURA_TELA - (2 * larguraTotal)) / 8;
        int yBase = 150;

        for (int i = 0; i < gp.palavras.size(); i++) {
            String p = gp.palavras.get(i);
            int col = i % numCols;
            int lin = i / numCols;
            int px = xBase + col * (larguraColuna + espacamento + 5);
            int py = yBase + lin * 70;

            Color corFundo = gp.completadas.get(p) ? Constantes.COR_PALAVRA_COMPLETA : Constantes.COR_CABECALHO;
            game.shapeRenderer.setColor(corFundo);
            game.shapeRenderer.rect(px, py, larguraColuna + 20, 45);
        }

        game.shapeRenderer.setColor(Constantes.COR_CABECALHO);
        game.shapeRenderer.rect(xAreaJogo - 2, yAreaJogo - 2, larguraAreaJogo + 4, alturaAreaJogo + 4);

        gl.desenharGradeVazia(game.shapeRenderer);
        gl.desenharBlocosDeLetras(game.shapeRenderer);

        desenharBorda(game.shapeRenderer, 18, 30, 330, 90, 4, Constantes.COR_BORDA);

        for (int i = 0; i < gp.palavras.size(); i++) {
            int col = i % numCols;
            int lin = i / numCols;
            int px = xBase + col * (larguraColuna + espacamento + 5);
            int py = yBase + lin * 70;
            desenharBorda(game.shapeRenderer, px, py, larguraColuna + 20, 45, 3, Constantes.COR_BORDA);
        }

        desenharBorda(game.shapeRenderer, xAreaJogo - 2, yAreaJogo - 2, larguraAreaJogo + 4, alturaAreaJogo + 4, 3, Constantes.COR_BORDA);

        int lq = larguraAreaJogo / Constantes.GRID_COLS;
        int hq = alturaAreaJogo / Constantes.GRID_ROWS;
        for (int col = 0; col < Constantes.GRID_COLS; col++) {
            for (int lin = 0; lin < Constantes.GRID_ROWS; lin++) {
                desenharBorda(game.shapeRenderer, xAreaJogo + col * lq, yAreaJogo + lin * hq, lq, hq, 1, Constantes.COR_BORDA);
            }
        }

        game.shapeRenderer.end();

        game.batch.begin();

        // Título "WORD DROP"
        game.fontTitulo.getData().setScale(2.1f);
        game.fontTitulo.setColor(Constantes.COR_TEXTO);
        layout.setText(game.fontTitulo, "WORD DROP");
        game.fontTitulo.draw(game.batch, "WORD DROP",
            18 + (330 - layout.width) / 2f,
            30 + (90 - layout.height) / 2f);

        // Palavras da lista com contorno branco usando a nova função estática
        game.fontPadrao.getData().setScale(1.6f);
        for (int i = 0; i < gp.palavras.size(); i++) {
            String p = gp.palavras.get(i);
            int col = i % numCols;
            int lin = i / numCols;
            int px = xBase + col * (larguraColuna + espacamento + 5);
            int py = yBase + lin * 70;

            Color corTexto = gp.completadas.get(p) ? Constantes.COR_TEXTO : gp.corPalavras.get(p);
            Rectangle rect = new Rectangle(px, py, larguraColuna + 20, 45);

            GerenciadorLetras.desenharTextoComBordaGrossa(
                game.batch, game.fontPadrao, p.toUpperCase(),
                corTexto, Color.WHITE, 1,
                rect.x + rect.width / 2f, rect.y + rect.height / 2f
            );
        }

        // Aumentando um pouquinho a fonte das letras do grid para ficar imponente igual a imagem
        game.fontLetras.getData().setScale(2.5f);
        gl.desenharTextos(game.batch, game.fontLetras);

        game.batch.end();
    }

    private void desenharBorda(ShapeRenderer shape, float x, float y, float w, float h, float espessura, Color cor) {
        shape.setColor(cor);
        shape.rect(x, y, w, espessura);
        shape.rect(x, y + h - espessura, w, espessura);
        shape.rect(x, y, espessura, h);
        shape.rect(x + w - espessura, y, espessura, h);
    }
}
