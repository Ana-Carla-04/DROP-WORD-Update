package DROP.WORD.Update.entities;

import com.badlogic.gdx.graphics.Color;

public class LetraInfo {
    public char letra;
    public int palavraIndex;
    public int pos;
    public Color cor;

    public LetraInfo(char letra, int palavraIndex, int pos, Color cor) {
        this.letra = letra;
        this.palavraIndex = palavraIndex;
        this.pos = pos;
        this.cor = cor;
    }
}
