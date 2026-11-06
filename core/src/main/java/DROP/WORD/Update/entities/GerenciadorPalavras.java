package DROP.WORD.Update.entities;

import com.badlogic.gdx.graphics.Color;
import DROP.WORD.Update.Constantes;
import DROP.WORD.Update.algorithms.Algoritmos;
import DROP.WORD.Update.structures.Pilha;

import java.util.*;

public class GerenciadorPalavras {
    private final List<String> palavras;
    private final List<String> dicionarioOrdenado;
    private final Map<String, Color> corPalavras = new HashMap<>();
    private final Map<String, Boolean> completadas = new HashMap<>();
    private final Map<String, Set<Integer>> liberadas = new HashMap<>();
    private final Map<String, Integer> progresso = new HashMap<>();
    private final Map<String, Map<Integer, Character>> letrasColocadas = new HashMap<>();

    // Pilha genérica para registrar o histórico de palavras formadas (LIFO)
    private final Pilha<String> historicoPalavrasFormadas = new Pilha<>();

    public GerenciadorPalavras(List<String> palavrasAtivas) {
        this(palavrasAtivas, null);
    }

    public GerenciadorPalavras(List<String> palavrasAtivas, List<String> dicionarioCompleto) {
        if (palavrasAtivas == null || palavrasAtivas.isEmpty()) {
            palavrasAtivas = Arrays.asList("bravo", "clima", "festa", "hotel");
        }
        this.palavras = new ArrayList<>();
        for (String p : palavrasAtivas) {
            if (p != null && p.trim().length() == Constantes.GRID_COLS) {
                this.palavras.add(p.trim().toLowerCase());
            }
        }

        // Prepara e ordena o dicionário completo com nosso QuickSort para uso na Busca Binária
        this.dicionarioOrdenado = new ArrayList<>();
        if (dicionarioCompleto != null) {
            for (String d : dicionarioCompleto) {
                if (d != null && d.trim().length() == Constantes.GRID_COLS) {
                    this.dicionarioOrdenado.add(d.trim().toLowerCase());
                }
            }
        }
        for (String p : this.palavras) {
            if (!this.dicionarioOrdenado.contains(p)) {
                this.dicionarioOrdenado.add(p);
            }
        }
        Algoritmos.quickSort(this.dicionarioOrdenado);

        for (String p : this.palavras) {
            this.completadas.put(p, false);
            Set<Integer> initSet = new HashSet<>();
            initSet.add(1);
            this.liberadas.put(p, initSet);
            this.progresso.put(p, 0);
            this.letrasColocadas.put(p, new HashMap<>());
        }
    }

    public List<String> getPalavras() {
        return palavras;
    }

    public List<String> getDicionarioOrdenado() {
        return Collections.unmodifiableList(dicionarioOrdenado);
    }

    public Color getCorPalavra(String palavra) {
        return corPalavras.get(palavra);
    }

    public void definirCorPalavra(String palavra, Color cor) {
        corPalavras.put(palavra, cor);
    }

    public boolean isPalavraCompletada(String palavra) {
        Boolean comp = completadas.get(palavra);
        return comp != null && comp;
    }

    public boolean todasCompletadas() {
        for (boolean c : completadas.values()) {
            if (!c) return false;
        }
        return !completadas.isEmpty();
    }

    public int getQuantidadeCompletadas() {
        int cont = 0;
        for (boolean c : completadas.values()) {
            if (c) cont++;
        }
        return cont;
    }

    public Pilha<String> getHistoricoPalavrasFormadas() {
        return historicoPalavrasFormadas;
    }

    public void liberarPosicaoAposLetra(char letra, String palavraOrigem, int posOrigem) {
        char ch = Character.toLowerCase(letra);
        this.letrasColocadas.get(palavraOrigem).put(posOrigem, ch);

        if (posOrigem > this.progresso.get(palavraOrigem)) {
            this.progresso.put(palavraOrigem, posOrigem);
        }

        int maxGlobal = 0;
        for (int v : this.progresso.values()) {
            if (v > maxGlobal) maxGlobal = v;
        }

        for (String p : this.palavras) {
            if (isPalavraCompletada(p)) continue;

            Set<Integer> novas = new HashSet<>();
            novas.add(1);
            int limite = Math.min(maxGlobal + 1, Constantes.GRID_COLS);

            for (int pos = 2; pos <= limite; pos++) {
                if (p.equals(palavraOrigem) || this.progresso.get(p) >= pos - 1) {
                    novas.add(pos);
                }
            }
            this.liberadas.put(p, novas);
        }
    }

    public static class PalavraPosicao {
        private final String palavra;
        private final int posicao;

        public PalavraPosicao(String p, int pos) {
            this.palavra = p;
            this.posicao = pos;
        }

        public String getPalavra() {
            return palavra;
        }

        public int getPosicao() {
            return posicao;
        }
    }

    public PalavraPosicao encontrarPalavraPosicaoParaLetra(char letra, int coluna) {
        char ch = Character.toLowerCase(letra);
        int posAlvo = coluna;

        for (String p : this.palavras) {
            if (!isPalavraCompletada(p) &&
                this.liberadas.get(p).contains(posAlvo) &&
                p.charAt(posAlvo - 1) == ch &&
                !this.letrasColocadas.get(p).containsKey(posAlvo)) {
                return new PalavraPosicao(p, posAlvo);
            }
        }
        return null;
    }

    /**
     * Verifica se uma letra pertence à coluna indicada em qualquer palavra ativa ainda não completada
     * (mesmo que a posição já tenha sido liberada anteriormente), evitando que letras na coluna certa
     * virem lixo sem motivo ou causem derrota injusta.
     */
    public boolean letraPertenceAColunaDeAlgumaPalavraAtiva(char letra, int colunaZeroBased) {
        char ch = Character.toLowerCase(letra);
        for (String p : this.palavras) {
            if (!isPalavraCompletada(p) && p.charAt(colunaZeroBased) == ch) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se a linha forma uma palavra da lista alvo ou qualquer palavra válida do dicionário
     * usando Busca Binária (Algoritmos.buscaBinaria).
     */
    public String verificarPalavraFormada(List<Character> letrasLinha) {
        if (letrasLinha == null || letrasLinha.size() != Constantes.GRID_COLS) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Character c : letrasLinha) {
            sb.append(Character.toLowerCase(c));
        }
        String candidata = sb.toString();

        // 1. Verifica se é uma das palavras-alvo ainda não completadas
        for (String p : this.palavras) {
            if (candidata.equals(p) && !isPalavraCompletada(p)) {
                return p;
            }
        }

        // 2. Valida no dicionário ordenado usando Busca Binária explícita
        int indiceDicionario = Algoritmos.buscaBinaria(this.dicionarioOrdenado, candidata);
        if (indiceDicionario >= 0) {
            return candidata;
        }

        return null;
    }

    /**
     * Marca a palavra formada e empilha no histórico (Pilha<String>).
     * Retorna true se era uma palavra-alvo da partida, ou false se foi palavra bônus do dicionário.
     */
    public boolean registrarPalavraFormada(String palavra) {
        historicoPalavrasFormadas.empilhar(palavra.toUpperCase());
        if (completadas.containsKey(palavra) && !completadas.get(palavra)) {
            completadas.put(palavra, true);
            liberadas.get(palavra).clear();
            return true;
        }
        return false;
    }
}
