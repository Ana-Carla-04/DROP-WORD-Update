package DROP.WORD.Update.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import DROP.WORD.Update.Constantes;
import DROP.WORD.Update.Main;

import java.util.Arrays;
import java.util.List;

public class TelaMenu extends ScreenAdapter {
    private final Main game;
    private final OrthographicCamera camera;

    // Retângulos com Y ajustado para a câmera padrão (Y para cima)
    // Título em cima (Y=400), Play no meio (Y=260), Exit embaixo (Y=120)
    private final Rectangle caixaPrincipal = new Rectangle(20, 20, Constantes.LARGURA_TELA - 40, Constantes.ALTURA_TELA - 40);
    private final Rectangle caixaTitulo = new Rectangle(Constantes.LARGURA_TELA / 7f, 400, 600, 100);
    private final Rectangle botaoPlay = new Rectangle(Constantes.LARGURA_TELA / 2f - 130, 260, 260, 80);
    private final Rectangle botaoExit = new Rectangle(Constantes.LARGURA_TELA / 2f - 110, 120, 220, 70);

    private final Color corFundoExterno = Color.valueOf("dfb0b0");
    private final Color corFundoInterno = Color.valueOf("f4d8d9");
    private final Color corBordaInterna = Color.valueOf("8f8b8b");
    private final Color corTituloCaixa  = Color.valueOf("f4ddde");
    private final Color corTituloBorda  = Color.valueOf("b26969");
    private final Color corTextoTitulo  = Color.valueOf("000000");

    private final Color corBotao        = Color.valueOf("dfb0b0");
    private final Color corBotaoBorda   = Color.valueOf("b26969");
    private final Color corTextoBotao   = Color.valueOf("000000");

    private final Color corBotaoHover      = Color.valueOf("b26969");
    private final Color corBotaoBordaHover = Color.valueOf("f4ddde");
    private final Color corTextoBotaoHover = Color.valueOf("f4ddde");

    private final Vector3 mousePos = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();

    public TelaMenu(Main game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        // CÂMERA PADRÃO (Y para cima)
        this.camera.setToOrtho(false, Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                mousePos.set(screenX, screenY, 0);
                camera.unproject(mousePos);


                if (botaoPlay.contains(mousePos.x, mousePos.y)) {
                    List<String> palavras = Arrays.asList(
                        "bravo", "clima", "festa", "hotel",
                        "fluir", "poder", "quero", "tango",
                        "renda", "zebra", "grupo", "lucro"
                    );
                    game.setScreen(new TelaJogo(game, palavras));
                    return true;
                } else if (botaoExit.contains(mousePos.x, mousePos.y)) {
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {


        Gdx.gl.glClearColor(corFundoExterno.r, corFundoExterno.g, corFundoExterno.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);


        boolean hoverPlay = botaoPlay.contains(mousePos.x, mousePos.y);
        boolean hoverExit = botaoExit.contains(mousePos.x, mousePos.y);

        camera.update();
        game.shapeRenderer.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        game.shapeRenderer.setColor(corFundoInterno);
        game.shapeRenderer.rect(caixaPrincipal.x, caixaPrincipal.y, caixaPrincipal.width, caixaPrincipal.height);

        game.shapeRenderer.setColor(corTituloCaixa);
        game.shapeRenderer.rect(caixaTitulo.x, caixaTitulo.y, caixaTitulo.width, caixaTitulo.height);

        game.shapeRenderer.setColor(hoverPlay ? corBotaoHover : corBotao);
        game.shapeRenderer.rect(botaoPlay.x, botaoPlay.y, botaoPlay.width, botaoPlay.height);

        game.shapeRenderer.setColor(hoverExit ? corBotaoHover : corBotao);
        game.shapeRenderer.rect(botaoExit.x, botaoExit.y, botaoExit.width, botaoExit.height);

        desenharBordaRetangulo(game.shapeRenderer, caixaPrincipal.x, caixaPrincipal.y, caixaPrincipal.width, caixaPrincipal.height, 3, corBordaInterna);
        desenharBordaRetangulo(game.shapeRenderer, caixaTitulo.x, caixaTitulo.y, caixaTitulo.width, caixaTitulo.height, 6, corTituloBorda);

        Color bordaPlay = hoverPlay ? corBotaoBordaHover : corBotaoBorda;
        desenharBordaRetangulo(game.shapeRenderer, botaoPlay.x, botaoPlay.y, botaoPlay.width, botaoPlay.height, 3, bordaPlay);

        Color bordaExit = hoverExit ? corBotaoBordaHover : corBotaoBorda;
        desenharBordaRetangulo(game.shapeRenderer, botaoExit.x, botaoExit.y, botaoExit.width, botaoExit.height, 3, bordaExit);

        game.shapeRenderer.end();
        game.batch.begin();

        // Título (Y centralizado com sinal de MENOS)
        game.fontTitulo.setColor(corTextoTitulo);
        layout.setText(game.fontTitulo, "WORD DROP");
        game.fontTitulo.draw(game.batch, "WORD DROP",
            caixaTitulo.x + (caixaTitulo.width - layout.width) / 2f,
            caixaTitulo.y + (caixaTitulo.height + layout.height) / 2f);

        // PLAY (Y centralizado com sinal de MENOS)
        game.fontPadrao.setColor(hoverPlay ? corTextoBotaoHover : corTextoBotao);
        layout.setText(game.fontPadrao, "PLAY");
        game.fontPadrao.draw(game.batch, "PLAY",
            botaoPlay.x + (botaoPlay.width - layout.width) / 2f,
            botaoPlay.y + (botaoPlay.height + layout.height) / 2f);

        // EXIT (Y centralizado com sinal de MENOS)
        game.fontPadrao.setColor(hoverExit ? corTextoBotaoHover : corTextoBotao);
        layout.setText(game.fontPadrao, "EXIT");
        game.fontPadrao.draw(game.batch, "EXIT",
            botaoExit.x + (botaoExit.width - layout.width) / 2f,
            botaoExit.y + (botaoExit.height + layout.height) / 2f);

        game.batch.end();
    }

    private void desenharBordaRetangulo(ShapeRenderer shape, float x, float y, float w, float h, float espessura, Color cor) {
        shape.setColor(cor);
        shape.rect(x, y, w, espessura);
        shape.rect(x, y + h - espessura, w, espessura);
        shape.rect(x, y, espessura, h);
        shape.rect(x + w - espessura, y, espessura, h);
    }
}
