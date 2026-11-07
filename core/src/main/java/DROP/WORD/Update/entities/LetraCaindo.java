package DROP.WORD.Update.entities;

import com.badlogic.gdx.graphics.Color;
import DROP.WORD.Update.Constantes;

public class LetraCaindo {
    private final char character;
    private final int palavraOrigem;
    private final int posOrigem;
    private int col;
    private float y;
    private final float vel;
    private final Color cor;

    public LetraCaindo(char character, int palavraOrigem, int posOrigem, int col, float rowPx, float velocidade, Color cor) {
        this.character = character;
        this.palavraOrigem = palavraOrigem;
        this.posOrigem = posOrigem;
        this.col = col;
        this.y = rowPx;
        this.vel = velocidade;
        this.cor = cor != null ? cor : Constantes.COR_LETRA_ATUAL;
    }

    public char getCharacter() {
        return character;
    }

    public int getPalavraOrigem() {
        return palavraOrigem;
    }

    public int getPosOrigem() {
        return posOrigem;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void adicionarY(float deltaY) {
        this.y += deltaY;
    }

    public float getVel() {
        return vel;
    }

    public Color getCor() {
        return cor;
    }
}
