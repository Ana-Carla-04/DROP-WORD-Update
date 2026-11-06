package DROP.WORD.Update;

import com.badlogic.gdx.graphics.Color;

public class Constantes {
    public static final int LARGURA_TELA = 830;
    public static final int ALTURA_TELA = 590;

    public static final Color COR_FUNDO = Color.valueOf("dfb0b0");
    public static final Color COR_CABECALHO = Color.valueOf("f4ddde");
    public static final Color COR_BORDA = Color.valueOf("a8a3a3");
    public static final Color COR_BORDA_LETRA = Color.valueOf("494646");
    public static final Color COR_TEXTO = Color.valueOf("000000");

    public static final int FPS = 60;

    public static final Color[] CORES_TEXTO = {
        Color.valueOf("6be433"), Color.valueOf("ff5757"), Color.valueOf("8c52ff"),
        Color.valueOf("ffdf55"), Color.valueOf("4244f4"), Color.valueOf("eb31ef"),
        Color.valueOf("30df96"), Color.valueOf("2cbddd"), Color.valueOf("ff9221"),
        Color.valueOf("f64283"), Color.valueOf("35af63"), Color.valueOf("7f24af")
    };

    public static final Color COR_PALAVRA_COMPLETA = new Color(100f / 255f, 220f / 255f, 100f / 255f, 1f);
    public static final Color COR_LETRA_FIXA = new Color(220f / 255f, 220f / 255f, 220f / 255f, 1f);
    public static final Color COR_LETRA_ATUAL = new Color(255f / 255f, 200f / 255f, 60f / 255f, 1f);

    public static final int GRID_COLS = 5;
    public static final int GRID_ROWS = 6;
    public static final int CELL_PADDING = 4;
}
