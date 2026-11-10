package DROP.WORD.Update.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import DROP.WORD.Update.Constantes;
import DROP.WORD.Update.structures.Fila;
import DROP.WORD.Update.utils.RenderUtils;

import java.util.*;

public class GerenciadorLetras {
    private final int x;
    private final int y;
    private final int largura;
    private final int altura;
    private final GerenciadorPalavras gp;
    private final int cellW;
    private final int cellH;
    private final float velocidadeQuedaBase;

    private final Character[][] grid;
    private final Color[][] gridCores;
    private LetraCaindo atual;

    private boolean pausadoPorPalavra = false;
    private String palavraEmPausa = null;
    private float tempoPausaAcumulado = 0f;
    private int ultimaLinhaValida;

    // Fila genérica (baseada em ListaEncadeada<LetraInfo>) para controlar as letras que vão cair
    private final Fila<LetraInfo> filaDeLetras = new Fila<>();

    private boolean colInvalida = false;
    private String motivoDerrota = null;
    private int letrasCorretasPosicionadas = 0;

    public GerenciadorLetras(int x, int y, int largura, int altura, GerenciadorPalavras gp, float velocidadeBase) {
        this.x = x;
        this.y = y;
        this.largura = largura + Constantes.AJUSTE_BORDA_GRADE;
        this.altura = altura + Constantes.AJUSTE_BORDA_GRADE;
        this.gp = gp;
        this.velocidadeQuedaBase = velocidadeBase;

        this.cellW = largura / Constantes.GRID_COLS;
        this.cellH = altura / Constantes.GRID_ROWS;

        this.grid = new Character[Constantes.GRID_ROWS][Constantes.GRID_COLS];
        this.gridCores = new Color[Constantes.GRID_ROWS][Constantes.GRID_COLS];

        this.ultimaLinhaValida = Constantes.GRID_ROWS - 1;

        gerarSequenciaDeLetras();
        gerarNovaLetra();
    }

    public void gerarSequenciaDeLetras() {
        filaDeLetras.limpar();
        List<String> palavras = gp.getPalavras();
        int numPalavras = palavras.size();

        // Só embaralha e define cores no início (quando corPalavras está vazio)
        boolean inicial = gp.getCorPalavra(palavras.get(0)) == null;
        if (inicial) {
            Collections.shuffle(palavras);
            Color[] cores = new Color[numPalavras];
            for (int i = 0; i < numPalavras; i++) {
                cores[i] = Constantes.CORES_TEXTO[i % Constantes.CORES_TEXTO.length];
                gp.definirCorPalavra(palavras.get(i), cores[i]);
            }
        }

        // Descobre quais índices de palavras ainda estão ativas (não completadas)
        List<Integer> indicesAtivos = new ArrayList<>();
        for (int i = 0; i < numPalavras; i++) {
            if (!gp.isPalavraCompletada(palavras.get(i))) {
                indicesAtivos.add(i);
            }
        }

        if (indicesAtivos.isEmpty()) {
            return;
        }

        int totalAtivas = indicesAtivos.size();
        boolean[][] positions = new boolean[totalAtivas + 1][Constantes.GRID_COLS];
        Arrays.fill(positions[0], true);

        Random rnd = new Random();
        for (int idxSeq = 0; idxSeq < totalAtivas; idxSeq++) {
            int palIdx = indicesAtivos.get(idxSeq);
            String palavra = palavras.get(palIdx);
            Color cor = gp.getCorPalavra(palavra);

            List<Integer> valides = new ArrayList<>();
            for (int col = 0; col < Constantes.GRID_COLS; col++) {
                if (positions[idxSeq][col]) valides.add(col);
            }

            if (!valides.isEmpty()) {
                int pos = valides.get(rnd.nextInt(valides.size()));
                filaDeLetras.enfileirar(new LetraInfo(palavra.charAt(pos), palIdx, pos, cor));
                positions[idxSeq][pos] = false;
                positions[idxSeq + 1][pos] = true;
            }

            while (anyTrue(positions[idxSeq])) {
                List<int[]> subValides = new ArrayList<>();
                for (int k = idxSeq; k < Math.min(idxSeq + 4, totalAtivas); k++) {
                    for (int l = 0; l < Constantes.GRID_COLS; l++) {
                        if (positions[k][l]) subValides.add(new int[]{k, l});
                    }
                }
                if (!subValides.isEmpty()) {
                    int[] pick = subValides.get(rnd.nextInt(subValides.size()));
                    int k = pick[0];
                    int l = pick[1];
                    int subPalIdx = indicesAtivos.get(k);
                    String subPal = palavras.get(subPalIdx);
                    Color subCor = gp.getCorPalavra(subPal);

                    filaDeLetras.enfileirar(new LetraInfo(subPal.charAt(l), subPalIdx, l, subCor));
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
        if (filaDeLetras.estaVazia()) {
            gerarSequenciaDeLetras();
        }
        LetraInfo info = filaDeLetras.desenfileirar();
        if (info == null) return;

        // Inicia a letra logo acima do topo visível do tabuleiro (emergindo de cima suavemente)
        float startY = this.y - cellH + Constantes.CELL_PADDING;

        this.atual = new LetraCaindo(
            info.getLetra(),
            info.getPalavraIndex(),
            info.getPos(),
            Constantes.COLUNA_INICIAL_LETRA,
            startY,
            velocidadeQuedaBase,
            info.getCor()
        );
    }

    public LetraInfo espiarProximaLetra() {
        return filaDeLetras.espiar();
    }

    public void moverLetraEsquerda() {
        if (atual == null || pausadoPorPalavra) return;
        if (atual.getCol() > 0) {
            int novaCol = atual.getCol() - 1;
            int linhaPouso = getLinhaPouso(novaCol);
            if (linhaPouso >= 0 && atual.getY() <= y + linhaPouso * cellH) {
                atual.setCol(novaCol);
            }
        }
    }

    public void moverLetraDireita() {
        if (atual == null || pausadoPorPalavra) return;
        if (atual.getCol() < Constantes.GRID_COLS - 1) {
            int novaCol = atual.getCol() + 1;
            int linhaPouso = getLinhaPouso(novaCol);
            if (linhaPouso >= 0 && atual.getY() <= y + linhaPouso * cellH) {
                atual.setCol(novaCol);
            }
        }
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
        this.pausadoPorPalavra = true;
        this.palavraEmPausa = palavra;
        this.tempoPausaAcumulado = 0f;
    }

    public void update(float dt, boolean acelerandoQueda) {
        boolean allCompletas = gp.todasCompletadas();
        boolean topoAtingido = verificarTopoAtingido();

        if (topoAtingido && motivoDerrota == null) {
            motivoDerrota = "A PILHA DE LETRAS ATINGIU O TOPO DA GRADE!";
        }

        if (pausadoPorPalavra) {
            tempoPausaAcumulado += dt;
            if (tempoPausaAcumulado >= Constantes.TEMPO_PAUSA_PALAVRA_SEG) {
                pausadoPorPalavra = false;
                palavraEmPausa = null;
                tempoPausaAcumulado = 0f;
            }
            return;
        }

        if (atual == null && !allCompletas && !topoAtingido) {
            gerarNovaLetra();
            return;
        }

        if (allCompletas || topoAtingido || atual == null) return;

        float multiplicador = acelerandoQueda ? Constantes.VELOCIDADE_QUEDA_RAPIDA_MULTIPLICADOR : 1.0f;
        atual.adicionarY(atual.getVel() * multiplicador * dt);

        int col = atual.getCol();
        int linhaPouso = getLinhaPouso(col);
        if (linhaPouso >= 0) {
            float alvoY = y + linhaPouso * cellH;
            if (atual.getY() >= alvoY) {
                atual.setY(alvoY);
                fixarAtualNaColuna(col);
            }
        } else {
            fixarAtualNaColuna(col);
        }
    }

    public void fixarAtualNaColuna(int col) {
        if (atual == null) return;

        for (int r = Constantes.GRID_ROWS - 1; r >= 0; r--) {
            if (grid[r][col] == null) {
                grid[r][col] = atual.getCharacter();
                gridCores[r][col] = atual.getCor();

                int colHumana = col + 1;
                GerenciadorPalavras.PalavraPosicao match = gp.encontrarPalavraPosicaoParaLetra(atual.getCharacter(), colHumana);

                if (match != null) {
                    gp.liberarPosicaoAposLetra(atual.getCharacter(), match.getPalavra(), match.getPosicao());
                    letrasCorretasPosicionadas++;
                } else if (col == atual.getPosOrigem() || gp.letraPertenceAColunaDeAlgumaPalavraAtiva(atual.getCharacter(), col)) {
                    // Caiu na coluna certa de uma palavra ativa (ex.: antes da posição anterior ser liberada):
                    // libera a posição na própria palavra de origem para não virar lixo sem progresso
                    String palavraOrig = gp.getPalavras().get(atual.getPalavraOrigem());
                    if (!gp.isPalavraCompletada(palavraOrig) && palavraOrig.charAt(col) == Character.toLowerCase(atual.getCharacter())) {
                        gp.liberarPosicaoAposLetra(atual.getCharacter(), palavraOrig, colHumana);
                        letrasCorretasPosicionadas++;
                    }
                } else {
                    colInvalida = true;
                    motivoDerrota = "A LETRA '" + Character.toUpperCase(atual.getCharacter())
                        + "' CAIU NA COLUNA ERRADA (COLUNA " + colHumana + ")!";
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

    public void avancarLinhaInvalida() {
        ultimaLinhaValida--;
    }

    public boolean verificarTopoAtingido() {
        for (int c = 0; c < Constantes.GRID_COLS; c++) {
            if (grid[0][c] != null) {
                return true;
            }
        }
        return false;
    }

    public boolean verificarDerrota() {
        return verificarTopoAtingido() || colInvalida || ultimaLinhaValida < 0;
    }

    public String getMotivoDerrota() {
        if (motivoDerrota != null) return motivoDerrota;
        if (ultimaLinhaValida < 0) return "TODAS AS LINHAS FICARAM BLOQUEADAS!";
        return "FIM DE JOGO!";
    }

    public boolean isPausadoPorPalavra() {
        return pausadoPorPalavra;
    }

    public String getPalavraEmPausa() {
        return palavraEmPausa;
    }

    public LetraCaindo getAtual() {
        return atual;
    }

    public int getLetrasCorretasPosicionadas() {
        return letrasCorretasPosicionadas;
    }

    public int getLinhaPouso(int col) {
        for (int r = Constantes.GRID_ROWS - 1; r >= 0; r--) {
            if (grid[r][col] == null) {
                return r;
            }
        }
        return -1;
    }

    public void desenharPreviaQueda(ShapeRenderer shape) {
        if (atual == null || pausadoPorPalavra) return;

        int col = atual.getCol();
        int linhaPouso = getLinhaPouso(col);
        if (linhaPouso < 0) return;

        int rx = x + col * cellW + Constantes.CELL_PADDING;
        int ry = y + linhaPouso * cellH + Constantes.CELL_PADDING;
        int rw = cellW - 2 * Constantes.CELL_PADDING;
        int rh = cellH - 2 * Constantes.CELL_PADDING;

        // Destaque suave semi-transparente na célula de destino
        shape.setColor(1f, 1f, 1f, 0.28f);
        shape.rect(rx, ry, rw, rh);

        // Borda pontilhada/destacada na cor da letra
        Color corBorda = new Color(atual.getCor().r, atual.getCor().g, atual.getCor().b, 0.75f);
        RenderUtils.desenharBorda(shape, rx, ry, rw, rh, 2, corBorda);
    }

    public void desenharGradeVazia(ShapeRenderer shape) {
        for (int c = 0; c < Constantes.GRID_COLS; c++) {
            for (int r = 0; r < Constantes.GRID_ROWS; r++) {
                if (grid[r][c] == null) {
                    int rx = x + c * cellW + Constantes.CELL_PADDING;
                    int ry = y + r * cellH + Constantes.CELL_PADDING;
                    int rw = cellW - 2 * Constantes.CELL_PADDING;
                    int rh = cellH - 2 * Constantes.CELL_PADDING;

                    shape.setColor(Constantes.COR_CELULA_VAZIA);
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

                    shape.setColor(Constantes.COR_CARTAO_LETRA);
                    shape.rect(rx, ry, rw, rh);
                    RenderUtils.desenharBorda(shape, rx, ry, rw, rh, 4, Constantes.COR_BORDA_CARTAO);
                }
            }
        }

        if (atual != null) {
            int c = atual.getCol();
            int rx = x + c * cellW + Constantes.CELL_PADDING;
            int ry = (int) atual.getY() + Constantes.CELL_PADDING;
            int rw = cellW - 2 * Constantes.CELL_PADDING;
            int rh = cellH - 2 * Constantes.CELL_PADDING;

            shape.setColor(Constantes.COR_CARTAO_LETRA);
            shape.rect(rx, ry, rw, rh);
            RenderUtils.desenharBorda(shape, rx, ry, rw, rh, 4, Constantes.COR_BORDA_CARTAO);
        }
    }

    public void desenharTextos(SpriteBatch batch, BitmapFont font) {
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

                    RenderUtils.desenharTextoComBorda(batch, font, texto, cor, Color.BLACK, 1,
                        rx + rw / 2f, ry + rh / 2f);
                }
            }
        }

        if (atual != null) {
            int c = atual.getCol();
            int rx = x + c * cellW + Constantes.CELL_PADDING;
            int ry = (int) atual.getY() + Constantes.CELL_PADDING;
            int rw = cellW - 2 * Constantes.CELL_PADDING;
            int rh = cellH - 2 * Constantes.CELL_PADDING;

            String texto = String.valueOf(atual.getCharacter()).toUpperCase();
            RenderUtils.desenharTextoComBorda(batch, font, texto, atual.getCor(), Color.BLACK, 1,
                rx + rw / 2f, ry + rh / 2f);
        }
    }
}
