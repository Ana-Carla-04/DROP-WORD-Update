package DROP.WORD.Update.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import DROP.WORD.Update.Constantes;

import java.util.*;

public class GerenciadorLetras {
    public int x, y, largura, altura;
    public GerenciadorPalavras gp;
    public int numPalavras;
    public int cellW, cellH;

    public Character[][] grid;
    public Color[][] gridCores;
    public LetraCaindo atual;

    public boolean pausado = false;
    public String palavraEmPausa = null;
    public long pausaInicio = 0;
    public int ultimaLinhaValida;

    public List<LetraInfo> sequenciaDeLetras = new ArrayList<>();
    public int proximoIndiceLetra = 0;
    public boolean colInvalida = false;

    private final Color corFundoCélulaVazia = Color.valueOf("ddc1c2");
    private final Color corFundoCartaoLetra = Color.valueOf("f4ddde");
    private final Color corBordaCartao      = Color.valueOf("a8a3a3");

    private final GlyphLayout layout = new GlyphLayout();

    public GerenciadorLetras(int x, int y, int largura, int altura, GerenciadorPalavras gp) {
        this.x = x;
        this.y = y;
        this.largura = largura + 3;
        this.altura = altura + 3;
        this.gp = gp;
        this.numPalavras = gp.palavras.size();

        this.cellW = largura / Constantes.GRID_COLS;
        this.cellH = altura / Constantes.GRID_ROWS;

        this.grid = new Character[Constantes.GRID_ROWS][Constantes.GRID_COLS];
        this.gridCores = new Color[Constantes.GRID_ROWS][Constantes.GRID_COLS];

        this.ultimaLinhaValida = Constantes.GRID_ROWS - 1;

        gerarSequenciaDeLetras();
        gerarNovaLetra();
    }

    public void gerarSequenciaDeLetras() {
        this.numPalavras = gp.palavras.size();
        Collections.shuffle(gp.palavras);

        Color[] cores = new Color[numPalavras];
        for (int i = 0; i < numPalavras; i++) {
            cores[i] = Constantes.CORES_TEXTO[i % Constantes.CORES_TEXTO.length];
            gp.corPalavras.put(gp.palavras.get(i), cores[i]);
        }

        boolean[][] positions = new boolean[numPalavras + 1][Constantes.GRID_COLS];
        Arrays.fill(positions[0], true);

        Random rnd = new Random();
        for (int i = 0; i < numPalavras; i++) {
            List<Integer> valides = new ArrayList<>();
            for (int idx = 0; idx < Constantes.GRID_COLS; idx++) {
                if (positions[i][idx]) valides.add(idx);
            }

            int pos = valides.get(rnd.nextInt(valides.size()));
            sequenciaDeLetras.add(new LetraInfo(gp.palavras.get(i).charAt(pos), i, pos, cores[i]));
            positions[i][pos] = false;
            positions[i + 1][pos] = true;

            while (anyTrue(positions[i])) {
                List<int[]> subValides = new ArrayList<>();
                for (int k = i; k < Math.min(i + 4, numPalavras); k++) {
                    for (int l = 0; l < Constantes.GRID_COLS; l++) {
                        if (positions[k][l]) subValides.add(new int[]{k, l});
                    }
                }
                if (!subValides.isEmpty()) {
                    int[] pick = subValides.get(rnd.nextInt(subValides.size()));
                    int k = pick[0];
                    int l = pick[1];
                    sequenciaDeLetras.add(new LetraInfo(gp.palavras.get(k).charAt(l), k, l, cores[k]));
                    positions[k][l] = false;
                    positions[k + 1][l] = true;
                }
            }
        }
    }

    private boolean anyTrue(boolean[] arr) {
        for (boolean b : arr) if (b) return true;
        return false;
    }

    public void gerarNovaLetra() {
        if (sequenciaDeLetras.isEmpty()) return;
        int totalLetras = Constantes.GRID_COLS * gp.palavras.size();
        LetraInfo info = sequenciaDeLetras.get(proximoIndiceLetra % totalLetras);
        proximoIndiceLetra++;
        int startXCol = 2;
        this.atual = new LetraCaindo(info.letra, info.palavraIndex, info.pos, startXCol, this.y + 10, 80, info.cor);
    }

    public void moverLetraEsquerda() {
        if (atual == null || pausado) return;
        if (atual.col > 0) atual.col--;
    }

    public void moverLetraDireita() {
        if (atual == null || pausado) return;
        if (atual.col < Constantes.GRID_COLS - 1) atual.col++;
    }

    public List<Character> getLetrasLinhaInferior() {
        List<Character> linha = new ArrayList<>();
        int r = ultimaLinhaValida;
        if (r < 0 || r >= Constantes.GRID_ROWS) return linha;
        for (int c = 0; c < Constantes.GRID_COLS; c++) {
            if (grid[r][c] == null) return Collections.emptyList();
            linha.add(grid[r][c]);
        }
        return linha;
    }

    public void pausarPorPalavra(String palavra) {
        this.pausado = true;
        this.palavraEmPausa = palavra;
        this.pausaInicio = TimeUtils.millis();
    }

    public void update(float dt) {
        boolean allCompletas = true;
        for (boolean c : gp.completadas.values()) if (!c) allCompletas = false;

        boolean any0 = false;
        for (int c = 0; c < Constantes.GRID_COLS; c++) {
            if (grid[0][c] != null) { any0 = true; break; }
        }

        if (pausado) {
            if (TimeUtils.millis() - pausaInicio >= 1000) {
                pausado = false;
                palavraEmPausa = null;
                pausaInicio = 0;
            }
            return;
        }

        if (atual == null && !allCompletas && !any0 && !pausado) {
            gerarNovaLetra();
            return;
        }

        if (allCompletas || any0 || atual == null) return;

        atual.y += atual.vel * dt;

        float bottomY = y + altura - cellH / 2.0f;
        if (atual.y >= bottomY) {
            fixarAtualNaColuna(atual.col);
            return;
        }

        int row = (int) ((atual.y - y) / cellH);
        if (row >= Constantes.GRID_ROWS - 1) {
            fixarAtualNaColuna(atual.col);
            return;
        }
        if (grid[row + 1][atual.col] != null) {
            fixarAtualNaColuna(atual.col);
        }
    }

    public void fixarAtualNaColuna(int col) {
        if (atual == null) return;

        for (int r = Constantes.GRID_ROWS - 1; r >= 0; r--) {
            if (grid[r][col] == null) {
                grid[r][col] = atual.character;
                gridCores[r][col] = atual.cor;

                int colHumana = col + 1;
                GerenciadorPalavras.PalavraPosicao match = gp.encontrarPalavraPosicaoParaLetra(atual.character, colHumana);

                if (match != null) {
                    gp.liberarPosicaoAposLetra(atual.character, match.palavra, match.posicao);
                } else if (col != atual.posOrigem) {
                    colInvalida = true;
                }
                break;
            }
        }
        atual = null;
    }

    public void removerLinha(int row) {
        for (int r = row; r > 0; r--) {
            for (int c = 0; c < Constantes.GRID_COLS; c++) {
                grid[r][c] = grid[r - 1][c];
                gridCores[r][c] = gridCores[r - 1][c];
            }
        }
        for (int c = 0; c < Constantes.GRID_COLS; c++) {
            grid[0][c] = null;
            gridCores[0][c] = null;
        }
    }

    public void removerLinhaInferior() {
        removerLinha(ultimaLinhaValida);
    }

    public void desenharGradeVazia(ShapeRenderer shape) {
        for (int c = 0; c < Constantes.GRID_COLS; c++) {
            for (int r = 0; r < Constantes.GRID_ROWS; r++) {
                if (grid[r][c] == null) {
                    int rx = x + c * cellW + Constantes.CELL_PADDING;
                    int ry = y + r * cellH + Constantes.CELL_PADDING;
                    int rw = cellW - 2 * Constantes.CELL_PADDING;
                    int rh = cellH - 2 * Constantes.CELL_PADDING;

                    shape.setColor(corFundoCélulaVazia);
                    shape.rect(rx, ry, rw, rh);
                }
            }
        }
    }

    public void desenharBlocosDeLetras(ShapeRenderer shape) {
        for (int c = 0; c < Constantes.GRID_COLS; c++) {
            for (int r = 0; r < Constantes.GRID_ROWS; r++) {
                if (grid[r][c] != null) {
                    int rx = x + c * cellW + Constantes.CELL_PADDING;
                    int ry = y + r * cellH + Constantes.CELL_PADDING;
                    int rw = cellW - 2 * Constantes.CELL_PADDING;
                    int rh = cellH - 2 * Constantes.CELL_PADDING;

                    shape.setColor(corFundoCartaoLetra);
                    shape.rect(rx, ry, rw, rh);
                    desenharBorda(shape, rx, ry, rw, rh, 4, corBordaCartao);
                }
            }
        }

        if (atual != null) {
            int c = atual.col;
            int rx = x + c * cellW + Constantes.CELL_PADDING;
            int ry = (int) atual.y + Constantes.CELL_PADDING;
            int rw = cellW - 2 * Constantes.CELL_PADDING;
            int rh = cellH - 2 * Constantes.CELL_PADDING;

            shape.setColor(corFundoCartaoLetra);
            shape.rect(rx, ry, rw, rh);
            desenharBorda(shape, rx, ry, rw, rh, 4, corBordaCartao);
        }
    }

    public void desenharTextos(SpriteBatch batch, BitmapFont font) {
        // Letras fixas no grid
        for (int c = 0; c < Constantes.GRID_COLS; c++) {
            for (int r = 0; r < Constantes.GRID_ROWS; r++) {
                Character val = grid[r][c];
                if (val != null) {
                    int rx = x + c * cellW + Constantes.CELL_PADDING;
                    int ry = y + r * cellH + Constantes.CELL_PADDING;
                    int rw = cellW - 2 * Constantes.CELL_PADDING;
                    int rh = cellH - 2 * Constantes.CELL_PADDING;

                    Color cor = gridCores[r][c] != null ? gridCores[r][c] : Constantes.COR_LETRA_FIXA;
                    String texto = String.valueOf(val).toUpperCase();

                    // Usa a nova função de contorno
                    desenharTextoComBordaGrossa(batch, font, texto, cor, Color.WHITE, 2,
                        rx + rw / 2f, ry + rh / 2f);
                }
            }
        }

        // Letra em queda
        if (atual != null) {
            int c = atual.col;
            int rx = x + c * cellW + Constantes.CELL_PADDING;
            int ry = (int) atual.y + Constantes.CELL_PADDING;
            int rw = cellW - 2 * Constantes.CELL_PADDING;
            int rh = cellH - 2 * Constantes.CELL_PADDING;

            String texto = String.valueOf(atual.character).toUpperCase();
            desenharTextoComBordaGrossa(batch, font, texto, atual.cor, Color.WHITE, 2,
                rx + rw / 2f, ry + rh / 2f);
        }
    }

    // MÉTODO RECUPERADO: Desenha texto com contorno grosso
    public static void desenharTextoComBordaGrossa(SpriteBatch batch, BitmapFont font, String texto,
                                                   Color corTexto, Color corBorda, int grossura,
                                                   float centerX, float centerY) {
        // CORREÇÃO: Criamos um GlyphLayout local em vez de usar o da classe
        GlyphLayout layoutLocal = new GlyphLayout(font, texto);
        float textX = centerX - layoutLocal.width / 2f;
        float textY = centerY - layoutLocal.height / 2f;

        // Desenha a borda (offset em 8 direções)
        font.setColor(corBorda);
        for (int dx = -grossura; dx <= grossura; dx++) {
            for (int dy = -grossura; dy <= grossura; dy++) {
                if (dx != 0 || dy != 0) {
                    font.draw(batch, texto, textX + dx, textY + dy);
                }
            }
        }

        // Desenha o texto principal
        font.setColor(corTexto);
        font.draw(batch, texto, textX, textY);
    }

    private void desenharBorda(ShapeRenderer shape, float x, float y, float w, float h, float espessura, Color cor) {
        shape.setColor(cor);
        shape.rect(x, y, w, espessura);
        shape.rect(x, y + h - espessura, w, espessura);
        shape.rect(x, y, espessura, h);
        shape.rect(x + w - espessura, y, espessura, h);
    }
}
