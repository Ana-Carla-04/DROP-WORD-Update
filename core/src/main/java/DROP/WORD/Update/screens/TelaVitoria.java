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

public class TelaVitoria extends ScreenAdapter {
    private final Main game;
    private final OrthographicCamera camera;

    private final Rectangle caixaPrincipal = new Rectangle(20, 20, Constantes.LARGURA_TELA - 40, Constantes.ALTURA_TELA - 40);

    // INVERTIDO: caixaTitulo estava em Y=100 (baixo), agora está no topo
    private final Rectangle caixaTitulo = new Rectangle(Constantes.LARGURA_TELA / 7f, Constantes.ALTURA_TELA - 200, 600, 100);

    // INVERTIDO: botaoMenu estava em Y=400 (meio-alto), agora está mais para baixo
    private final Rectangle botaoMenu = new Rectangle(Constantes.LARGURA_TELA / 2f - 100, 150, 200, 60);

    private final Color corFundoExterno = Color.valueOf("dfb0b0");
    private final Color corFundoInterno = Color.valueOf("a2c8a5");
    private final Color corBordaInterna = Color.valueOf("ffffff");
    private final Color corTituloCaixa  = Color.valueOf("f4ddde");
    private final Color corTituloBorda  = Color.valueOf("b26969");

    private final Color corBotao        = Color.valueOf("dfb0b0");
    private final Color corBotaoBorda   = Color.valueOf("b26969");
    private final Color corTextoBotao   = Color.valueOf("000000");

    private final Color corBotaoHover      = Color.valueOf("b26969");
    private final Color corBotaoBordaHover = Color.valueOf("f4ddde");
    private final Color corTextoBotaoHover = Color.valueOf("f4ddde");

    private final Vector3 mousePos = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();

    public TelaVitoria(Main game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                mousePos.set(screenX, screenY, 0);
                camera.unproject(mousePos);

                if (botaoMenu.contains(mousePos.x, mousePos.y)) {
                    game.setScreen(new TelaMenu(game));
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
        boolean estaSobreBotao = botaoMenu.contains(mousePos.x, mousePos.y);

        camera.update();
        game.shapeRenderer.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(corFundoInterno);
        game.shapeRenderer.rect(caixaPrincipal.x, caixaPrincipal.y, caixaPrincipal.width, caixaPrincipal.height);

        game.shapeRenderer.setColor(corTituloCaixa);
        game.shapeRenderer.rect(caixaTitulo.x, caixaTitulo.y, caixaTitulo.width, caixaTitulo.height);

        game.shapeRenderer.setColor(estaSobreBotao ? corBotaoHover : corBotao);
        game.shapeRenderer.rect(botaoMenu.x, botaoMenu.y, botaoMenu.width, botaoMenu.height);

        desenharBorda(game.shapeRenderer, caixaPrincipal.x, caixaPrincipal.y, caixaPrincipal.width, caixaPrincipal.height, 3, corBordaInterna);
        desenharBorda(game.shapeRenderer, caixaTitulo.x, caixaTitulo.y, caixaTitulo.width, caixaTitulo.height, 3, corTituloBorda);

        Color bordaMenu = estaSobreBotao ? corBotaoBordaHover : corBotaoBorda;
        desenharBorda(game.shapeRenderer, botaoMenu.x, botaoMenu.y, botaoMenu.width, botaoMenu.height, 3, bordaMenu);
        game.shapeRenderer.end();

        game.batch.begin();
        game.fontTitulo.setColor(Color.BLACK);
        layout.setText(game.fontTitulo, "PARABÉNS!");
        game.fontTitulo.draw(game.batch, "PARABÉNS!",
            caixaTitulo.x + (caixaTitulo.width - layout.width) / 2f,
            caixaTitulo.y + (caixaTitulo.height + layout.height) / 2f);

        game.fontPadrao.setColor(Color.BLACK);
        layout.setText(game.fontPadrao, "VOCÊ GANHOU!");
        // INVERTIDO: Y mudou de 300 (baixo) para 400 (meio-alto)
        game.fontPadrao.draw(game.batch, "VOCÊ GANHOU!",
            Constantes.LARGURA_TELA / 2f - layout.width / 2f,
            Constantes.ALTURA_TELA / 2f);

        game.fontPadrao.setColor(estaSobreBotao ? corTextoBotaoHover : corTextoBotao);
        layout.setText(game.fontPadrao, "MENU");
        game.fontPadrao.draw(game.batch, "MENU",
            botaoMenu.x + (botaoMenu.width - layout.width) / 2f,
            botaoMenu.y + (botaoMenu.height + layout.height) / 2f);

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
