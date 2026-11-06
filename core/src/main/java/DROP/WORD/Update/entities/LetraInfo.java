package DROP.WORD.Update.entities;

import com.badlogic.gdx.graphics.Color;

public class LetraInfo {
    private final char letra;
    private final int palavraIndex;
    private final int pos;
    private final Color cor;

    public LetraInfo(char letra, int palavraIndex, int pos, Color cor) {
        this.letra = letra;
        this.palavraIndex = palavraIndex;
        this.pos = pos;
        this.cor = cor;
    }

    public char getLetra() {
        return letra;
    }

    public int getPalavraIndex() {
        return palavraIndex;
    }

    public int getPos() {
        return pos;
    }

    public Color getCor() {
        return cor;
    }
}
