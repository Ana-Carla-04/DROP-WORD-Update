package DROP.WORD.Update;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
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

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Cria as fontes Bitmap nativas com o eixo vertical correto (true = yDown)
        fontPadrao = new BitmapFont(true);
        fontTitulo = new BitmapFont(true);
        fontLetras = new BitmapFont(true);

        // Ativa filtro Linear para deixar o texto liso e nítido
        fontPadrao.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontTitulo.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontLetras.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        setScreen(new TelaMenu(this));
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (fontPadrao != null) fontPadrao.dispose();
        if (fontTitulo != null) fontTitulo.dispose();
        if (fontLetras != null) fontLetras.dispose();
        super.dispose(); // <--- AGORA O MÉTODO FECHA CORRETAMENTE AQUI
    }
}
