package DROP.WORD.Update.entities;

import com.badlogic.gdx.graphics.Color;
import java.util.*;

public class GerenciadorPalavras {
    public List<String> palavras;
    public Map<String, Color> corPalavras = new HashMap<>();
    public Map<String, Boolean> completadas = new HashMap<>();
    public Map<String, Set<Integer>> liberadas = new HashMap<>();
    public Map<String, Integer> progresso = new HashMap<>();
    public Map<String, Map<Integer, Character>> letrasColocadas = new HashMap<>();

    public GerenciadorPalavras(List<String> palavrasAtivas) {
        if (palavrasAtivas == null || palavrasAtivas.isEmpty()) {
            palavrasAtivas = Arrays.asList("bravo", "clima", "festa", "hotel");
        }
        this.palavras = new ArrayList<>();
        for (String p : palavrasAtivas) {
            if (p.length() == 5) {
                this.palavras.add(p.toLowerCase());
            }
        }

        for (String p : this.palavras) {
            this.completadas.put(p, false);
            Set<Integer> initSet = new HashSet<>();
            initSet.add(1);
            this.liberadas.put(p, initSet);
            this.progresso.put(p, 0);
            this.letrasColocadas.put(p, new HashMap<>());
        }
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
            if (this.completadas.get(p)) continue;

            Set<Integer> novas = new HashSet<>();
            novas.add(1);
            int limite = Math.min(maxGlobal + 1, 5);

            for (int pos = 2; pos <= limite; pos++) {
                if (p.equals(palavraOrigem) || this.progresso.get(p) >= pos - 1) {
                    novas.add(pos);
                }
            }
            this.liberadas.put(p, novas);
        }
    }

    public static class PalavraPosicao {
        public String palavra;
        public int posicao;
        public PalavraPosicao(String p, int pos) {
            this.palavra = p;
            this.posicao = pos;
        }
    }

    public PalavraPosicao encontrarPalavraPosicaoParaLetra(char letra, int coluna) {
        char ch = Character.toLowerCase(letra);
        int posAlvo = coluna;

        for (String p : this.palavras) {
            if (!this.completadas.get(p) &&
                this.liberadas.get(p).contains(posAlvo) &&
                p.charAt(posAlvo - 1) == ch &&
                !this.letrasColocadas.get(p).containsKey(posAlvo)) {
                return new PalavraPosicao(p, posAlvo);
            }
        }
        return null;
    }

    public String verificarPalavraFormada(List<Character> letrasLinha) {
        StringBuilder sb = new StringBuilder();
        for (Character c : letrasLinha) {
            sb.append(Character.toLowerCase(c));
        }
        String s = sb.toString();
        for (String p : this.palavras) {
            if (s.equals(p) && !this.completadas.get(p)) {
                return p;
            }
        }
        return null;
    }

    public void marcarPalavraCompleta(String palavra) {
        this.completadas.put(palavra, true);
        this.liberadas.get(palavra).clear();
    }
}
