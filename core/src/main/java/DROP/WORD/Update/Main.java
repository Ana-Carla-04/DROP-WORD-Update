package DROP.WORD.Update;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import DROP.WORD.Update.screens.TelaMenu;

public class Main extends Game {
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont fontPadrao;
    public BitmapFont fontTitulo;
    public BitmapFont fontLetras;
    public BitmapFont fontPalavrasLista;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Fonte padrão do LibGDX
        fontTitulo = new BitmapFont();
        fontPadrao = new BitmapFont();
        fontPalavrasLista = new BitmapFont();
        fontLetras = new BitmapFont();

        // Ajusta o tamanho
        fontTitulo.getData().setScale(3f);
        fontPadrao.getData().setScale(2f);
        fontPalavrasLista.getData().setScale(2f);
        fontLetras.getData().setScale(3f);

        setScreen(new TelaMenu(this));
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (fontPadrao != null) fontPadrao.dispose();
        if (fontTitulo != null) fontTitulo.dispose();
        if (fontLetras != null) fontLetras.dispose();
        if (fontPalavrasLista != null) fontPalavrasLista.dispose();
        super.dispose();
    }
}
