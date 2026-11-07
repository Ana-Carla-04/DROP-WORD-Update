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

import java.util.ArrayList;
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
        this.camera.setToOrtho(false, Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);

        iniciarJogo(palavras);
    }

    private void iniciarJogo(List<String> palavras) {
        this.gp = new GerenciadorPalavras(palavras);
        // A área do jogo começa em Y=30 no Pygame (que é o topo).
        // No LibGDX, o topo é ALTURA - y - altura
        int yAreaJogoLibGDX = Constantes.ALTURA_TELA - yAreaJogo - alturaAreaJogo;
        this.gl = new GerenciadorLetras(xAreaJogo, yAreaJogoLibGDX, larguraAreaJogo, alturaAreaJogo, gp);
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
                        gl.atual.y -= 20; // No LibGDX, subtrai para descer
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
        // A derrota é a linha de CIMA (topo)
        int linhaDerrota = 0;
        for (int c = 0; c < Constantes.GRID_COLS; c++) {
            if (gl.grid[linhaDerrota][c] != null) return true;
        }
        return gl.colInvalida;
    }

    public boolean verificarVitoria() {
        if (gp == null) return false;
        for (boolean b : gp.completadas.values()) {
            if (!b) return false;
        }
        return true;
    }

    public void verificarPalavrasFormadas() {
        int linhaFundo = Constantes.GRID_ROWS - 1;
        List<Character> letras = new ArrayList<>();
        for (int c = 0; c < Constantes.GRID_COLS; c++) {
            if (gl.grid[linhaFundo][c] == null) return;
            letras.add(gl.grid[linhaFundo][c]);
        }

        if (letras.size() == Constantes.GRID_COLS) {
            String formata = gp.verificarPalavraFormada(letras);
            if (formata != null) {
                gp.marcarPalavraCompleta(formata);
                gl.pausarPorPalavra(formata);
                // Remove a linha do fundo
                for (int r = Constantes.GRID_ROWS - 1; r > 0; r--) {
                    for (int c = 0; c < Constantes.GRID_COLS; c++) {
                        gl.grid[r][c] = gl.grid[r - 1][c];
                        gl.gridCores[r][c] = gl.gridCores[r - 1][c];
                    }
                }
                // Limpa a linha de cima
                for (int c = 0; c < Constantes.GRID_COLS; c++) {
                    gl.grid[0][c] = null;
                    gl.gridCores[0][c] = null;
                }
            } else {
                gl.ultimaLinhaValida--; // A linha do fundo sobe
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

        // Cabeçalho (Pygame: y=30, h=90). LibGDX: ALTURA - 30 - 90
        float yCabecalho = Constantes.ALTURA_TELA - 30 - 90;
        game.shapeRenderer.rect(18, yCabecalho, 330, 90);

        int numCols = 2;
        int larguraColuna = 120;
        int espacamento = 40;
        int larguraTotal = numCols * (larguraColuna + 5) + (numCols - 1) * espacamento;
        int xBase = (Constantes.LARGURA_TELA - (2 * larguraTotal)) / 8;
        int yBase = 150;

        // Lista de palavras (Y invertido)
        for (int i = 0; i < gp.palavras.size(); i++) {
            String p = gp.palavras.get(i);
            int col = i % numCols;
            int lin = i / numCols;
            int px = xBase + col * (larguraColuna + espacamento + 5);
            int py = yBase + lin * 70;

            Color corFundo = gp.completadas.get(p) ? Constantes.COR_PALAVRA_COMPLETA : Constantes.COR_CABECALHO;
            game.shapeRenderer.setColor(corFundo);

            float pyLibGDX = Constantes.ALTURA_TELA - py - 45;
            game.shapeRenderer.rect(px, pyLibGDX, larguraColuna + 20, 45);
        }

        // Fundo da área do grid (Y invertido)
        float yAreaJogoLibGDX = Constantes.ALTURA_TELA - yAreaJogo - alturaAreaJogo;
        game.shapeRenderer.setColor(Constantes.COR_CABECALHO);
        game.shapeRenderer.rect(xAreaJogo - 2, yAreaJogoLibGDX - 2, larguraAreaJogo + 4, alturaAreaJogo + 4);

        gl.desenharGradeVazia(game.shapeRenderer);
        gl.desenharBlocosDeLetras(game.shapeRenderer);

        desenharBorda(game.shapeRenderer, 18, yCabecalho, 330, 90, 4, Constantes.COR_BORDA);

        for (int i = 0; i < gp.palavras.size(); i++) {
            int col = i % numCols;
            int lin = i / numCols;
            int px = xBase + col * (larguraColuna + espacamento + 5);
            int py = yBase + lin * 70;
            float pyLibGDX = Constantes.ALTURA_TELA - py - 45;
            desenharBorda(game.shapeRenderer, px, pyLibGDX, larguraColuna + 20, 45, 3, Constantes.COR_BORDA);
        }

        desenharBorda(game.shapeRenderer, xAreaJogo - 2, yAreaJogoLibGDX - 2, larguraAreaJogo + 4, alturaAreaJogo + 4, 3, Constantes.COR_BORDA);

        game.shapeRenderer.end();
        game.batch.begin();

        // Título "WORD DROP"
        game.fontTitulo.setColor(Constantes.COR_TEXTO);
        layout.setText(game.fontTitulo, "WORD DROP");
        game.fontTitulo.draw(game.batch, "WORD DROP",
            18 + (330 - layout.width) / 2f,
            yCabecalho + (90 + layout.height) / 2f);

        // Palavras da lista
        for (int i = 0; i < gp.palavras.size(); i++) {
            String p = gp.palavras.get(i);
            int col = i % numCols;
            int lin = i / numCols;
            int px = xBase + col * (larguraColuna + espacamento + 5);
            int py = yBase + lin * 70;
            float pyLibGDX = Constantes.ALTURA_TELA - py - 45;

            Color corTexto = gp.completadas.get(p) ? Constantes.COR_TEXTO : gp.corPalavras.get(p);
            game.fontPalavrasLista.setColor(corTexto);

            layout.setText(game.fontPalavrasLista, p.toUpperCase());
            float textX = px + ((larguraColuna + 20) - layout.width) / 2f;
            float textY = pyLibGDX + (45 + layout.height) / 2f;

            game.fontPalavrasLista.draw(game.batch, p.toUpperCase(), textX, textY);
        }

        // Letras do grid
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
