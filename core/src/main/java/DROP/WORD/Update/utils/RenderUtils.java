package DROP.WORD.Update.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import DROP.WORD.Update.Constantes;

/**
 * Classe utilitária para centralizar o desenho de bordas, botões e textos,
 * garantindo alinhamento nítido e contornos limpos sem artefatos visuais.
 */
public class RenderUtils {

    private static final GlyphLayout LAYOUT_COMPARTILHADO = new GlyphLayout();

    private RenderUtils() {
    }

    /**
     * Desenha uma borda retangular usando um ShapeRenderer (ShapeType.Filled).
     */
    public static void desenharBorda(ShapeRenderer shape, float x, float y, float w, float h, float espessura, Color cor) {
        shape.setColor(cor);
        shape.rect(x, y, w, espessura);
        shape.rect(x, y + h - espessura, w, espessura);
        shape.rect(x, y, espessura, h);
        shape.rect(x + w - espessura, y, espessura, h);
    }

    public static void desenharBorda(ShapeRenderer shape, Rectangle rect, float espessura, Color cor) {
        desenharBorda(shape, rect.x, rect.y, rect.width, rect.height, espessura, cor);
    }

    /**
     * Desenha o fundo e a borda de um botão interativo.
     */
    public static void desenharCaixaBotao(ShapeRenderer shape, Rectangle botao, boolean hover, float espessuraBorda) {
        shape.setColor(hover ? Constantes.COR_BOTAO_HOVER : Constantes.COR_BOTAO);
        shape.rect(botao.x, botao.y, botao.width, botao.height);

        Color corBorda = hover ? Constantes.COR_BOTAO_BORDA_HOVER : Constantes.COR_BOTAO_BORDA;
        desenharBorda(shape, botao, espessuraBorda, corBorda);
    }

    /**
     * Desenha o texto centralizado perfeitamente dentro de uma caixa retangular.
     */
    public static void desenharTextoCentralizado(SpriteBatch batch, BitmapFont font, String texto, Rectangle caixa, Color cor) {
        LAYOUT_COMPARTILHADO.setText(font, texto);
        float textX = (float) Math.floor(caixa.x + (caixa.width - LAYOUT_COMPARTILHADO.width) / 2f);
        float textY = (float) Math.floor(caixa.y + (caixa.height - LAYOUT_COMPARTILHADO.height) / 2f);

        font.setColor(cor);
        font.draw(batch, texto, textX, textY);
    }

    /**
     * Desenha texto com contorno nítido de 1 pixel em cruz (cima, baixo, esquerda, direita).
     * Evita sobreposições diagonais excessivas que borravam a fonte.
     */
    public static void desenharTextoComBorda(SpriteBatch batch, BitmapFont font, String texto,
                                             Color corTexto, Color corBorda, int grossura,
                                             float centerX, float centerY) {
        LAYOUT_COMPARTILHADO.setText(font, texto);
        float textX = (float) Math.floor(centerX - LAYOUT_COMPARTILHADO.width / 2f);
        float textY = (float) Math.floor(centerY - LAYOUT_COMPARTILHADO.height / 2f);

        if (grossura > 0 && corBorda != null) {
            font.setColor(corBorda);
            // 4 direções ortogonais limpas
            font.draw(batch, texto, textX - grossura, textY);
            font.draw(batch, texto, textX + grossura, textY);
            font.draw(batch, texto, textX, textY - grossura);
            font.draw(batch, texto, textX, textY + grossura);
        }

        font.setColor(corTexto);
        font.draw(batch, texto, textX, textY);
    }
}
