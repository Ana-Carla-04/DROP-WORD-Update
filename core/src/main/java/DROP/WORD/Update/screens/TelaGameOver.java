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
import DROP.WORD.Update.entities.GerenciadorLetras;

public class TelaGameOver extends ScreenAdapter {
    private final Main game;
    private final OrthographicCamera camera;

    private final Rectangle caixaPrincipal = new Rectangle(20, 20, Constantes.LARGURA_TELA - 40, Constantes.ALTURA_TELA - 40);
    private final Rectangle caixaTitulo = new Rectangle(Constantes.LARGURA_TELA / 7f, 100, 600, 100);
    private final Rectangle botaoMenu = new Rectangle(Constantes.LARGURA_TELA / 2f - 100, 400, 200, 60);

    private final Color corFundoExterno = Color.valueOf("dfb0b0");
    private final Color corFundoInterno = Color.valueOf("ef7b7b");
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

    public TelaGameOver(Main game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(true, Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);
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

        // 1. FORMAS PREENCHIDAS
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(corFundoInterno);
        game.shapeRenderer.rect(caixaPrincipal.x, caixaPrincipal.y, caixaPrincipal.width, caixaPrincipal.height);

        game.shapeRenderer.setColor(corTituloCaixa);
        game.shapeRenderer.rect(caixaTitulo.x, caixaTitulo.y, caixaTitulo.width, caixaTitulo.height);

        game.shapeRenderer.setColor(estaSobreBotao ? corBotaoHover : corBotao);
        game.shapeRenderer.rect(botaoMenu.x, botaoMenu.y, botaoMenu.width, botaoMenu.height);

        // 2. BORDAS RETAS
        desenharBorda(game.shapeRenderer, caixaPrincipal.x, caixaPrincipal.y, caixaPrincipal.width, caixaPrincipal.height, 3, corBordaInterna);
        desenharBorda(game.shapeRenderer, caixaTitulo.x, caixaTitulo.y, caixaTitulo.width, caixaTitulo.height, 3, corTituloBorda);

        Color bordaMenu = estaSobreBotao ? corBotaoBordaHover : corBotaoBorda;
        desenharBorda(game.shapeRenderer, botaoMenu.x, botaoMenu.y, botaoMenu.width, botaoMenu.height, 3, bordaMenu);
        game.shapeRenderer.end();

        // 3. TEXTOS
        game.batch.begin();
        game.fontTitulo.getData().setScale(3.0f);
        layout.setText(game.fontTitulo, "VOCÊ PERDEU");

        // Sombra de 3px
        game.fontTitulo.setColor(corFundoInterno);
        game.fontTitulo.draw(game.batch, "VOCÊ PERDEU",
            caixaTitulo.x + (caixaTitulo.width - layout.width) / 2f + 3,
            caixaTitulo.y + (caixaTitulo.height - layout.height) / 2f + 3);

        // Texto principal
        game.fontTitulo.setColor(Color.BLACK);
        game.fontTitulo.draw(game.batch, "VOCÊ PERDEU",
            caixaTitulo.x + (caixaTitulo.width - layout.width) / 2f,
            caixaTitulo.y + (caixaTitulo.height - layout.height) / 2f);

        // Mensagens
        game.fontPadrao.getData().setScale(2.0f);
        game.fontPadrao.setColor(Color.BLACK);

        layout.setText(game.fontPadrao, "VOLTE AO MENU E  ");
        game.fontPadrao.draw(game.batch, "VOLTE AO MENU E  ", Constantes.LARGURA_TELA / 2f - layout.width / 2f, 250);

        layout.setText(game.fontPadrao, "TENTE NOVAMENTE!");
        game.fontPadrao.draw(game.batch, "TENTE NOVAMENTE!", Constantes.LARGURA_TELA / 2f - layout.width / 2f - 5, 320);

        // Botão MENU
        game.fontPadrao.getData().setScale(1.8f);
        String txtMenu = "MENU";
        layout.setText(game.fontPadrao, txtMenu);
        game.fontPadrao.setColor(estaSobreBotao ? corTextoBotaoHover : corTextoBotao);
        game.fontPadrao.draw(game.batch, txtMenu,
            botaoMenu.x + (botaoMenu.width - layout.width) / 2f,
            botaoMenu.y + (botaoMenu.height - layout.height) / 2f);

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
