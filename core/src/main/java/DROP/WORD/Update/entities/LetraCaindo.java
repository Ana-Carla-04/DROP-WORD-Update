package DROP.WORD.Update.entities;

import com.badlogic.gdx.graphics.Color;
import DROP.WORD.Update.Constantes;

public class LetraCaindo {
    public char character;
    public int palavraOrigem;
    public int posOrigem;
    public int col;
    public float y;
    public float vel;
    public Color cor;

    public LetraCaindo(char character, int palavraOrigem, int posOrigem, int col, float rowPx, float velocidade, Color cor) {
        this.character = character;
        this.palavraOrigem = palavraOrigem;
        this.posOrigem = posOrigem;
        this.col = col;
        this.y = rowPx;
        this.vel = velocidade;
        this.cor = cor != null ? cor : Constantes.COR_LETRA_ATUAL;
    }
}
