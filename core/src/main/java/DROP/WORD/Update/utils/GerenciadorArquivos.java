package DROP.WORD.Update.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import DROP.WORD.Update.Constantes;
import DROP.WORD.Update.algorithms.Algoritmos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Responsável pela leitura de arquivos de dicionário (assets/palavras.txt) via Gdx.files.internal
 * e persistência do Ranking / Recordes personalizados por jogador via Gdx.files.local e Preferences.
 */
public class GerenciadorArquivos {

    private static final String ARQUIVO_PALAVRAS = "palavras.txt";
    private static final String ARQUIVO_RANKING = "ranking_word_drop.txt";
    private static final String PREFS_NAME = "WordDropPreferences";
    private static final String KEY_HIGH_SCORE = "highScore";
    private static final String KEY_LAST_PLAYER = "lastPlayerName";
    private static final int MAX_RANKING_ENTRIES = 8;

    /**
     * Representa um registro de pontuação personalizada no ranking.
     */
    public static class RegistroRanking implements Comparable<RegistroRanking> {
        private final String nomeJogador;
        private final int pontuacao;
        private final float tempoSegundos;
        private final String dificuldade;

        public RegistroRanking(String nomeJogador, int pontuacao, float tempoSegundos, String dificuldade) {
            this.nomeJogador = normalizarNome(nomeJogador);
            this.pontuacao = pontuacao;
            this.tempoSegundos = tempoSegundos;
            this.dificuldade = dificuldade;
        }

        public String getNomeJogador() {
            return nomeJogador;
        }

        public int getPontuacao() {
            return pontuacao;
        }

        public float getTempoSegundos() {
            return tempoSegundos;
        }

        public String getDificuldade() {
            return dificuldade;
        }

        @Override
        public int compareTo(RegistroRanking outro) {
            if (this.pontuacao != outro.pontuacao) {
                return Integer.compare(outro.pontuacao, this.pontuacao); // Ordem decrescente de pontos
            }
            return Float.compare(this.tempoSegundos, outro.tempoSegundos); // Menor tempo desempata
        }

        public String serializar() {
            return nomeJogador + ";" + pontuacao + ";"
                + String.format(java.util.Locale.US, "%.1f", tempoSegundos) + ";" + dificuldade;
        }

        public static RegistroRanking desserializar(String linha) {
            try {
                String[] partes = linha.trim().split(";");
                if (partes.length >= 4) {
                    String nome = partes[0];
                    int pts = Integer.parseInt(partes[1]);
                    float tempo = Float.parseFloat(partes[2]);
                    String dif = partes[3];
                    return new RegistroRanking(nome, pts, tempo, dif);
                } else if (partes.length == 3) {
                    // Compatibilidade com formato antigo sem nome
                    int pts = Integer.parseInt(partes[0]);
                    float tempo = Float.parseFloat(partes[1]);
                    String dif = partes[2];
                    return new RegistroRanking("JOGADOR", pts, tempo, dif);
                }
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    public static String normalizarNome(String nome) {
        if (nome == null) return "JOGADOR";
        String limpo = nome.replace(";", "").trim().toUpperCase();
        if (limpo.isEmpty()) return "JOGADOR";
        if (limpo.length() > 10) limpo = limpo.substring(0, 10);
        return limpo;
    }

    /**
     * Lê todas as palavras de 5 letras de assets/palavras.txt e retorna a lista ordenada com QuickSort.
     */
    public static List<String> carregarDicionarioOrdenado() {
        List<String> dicionario = new ArrayList<>();
        try {
            if (Gdx.files != null) {
                FileHandle file = Gdx.files.internal(ARQUIVO_PALAVRAS);
                if (file.exists()) {
                    String conteudo = file.readString("UTF-8");
                    String[] linhas = conteudo.split("\\r?\\n");
                    for (String linha : linhas) {
                        String limpa = linha.trim().toLowerCase();
                        if (limpa.length() == Constantes.GRID_COLS) {
                            dicionario.add(limpa);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("GerenciadorArquivos", "Erro ao ler " + ARQUIVO_PALAVRAS, e);
        }

        if (dicionario.isEmpty()) {
            dicionario.addAll(Arrays.asList(
                "bravo", "clima", "festa", "hotel",
                "fluir", "poder", "quero", "tango",
                "renda", "zebra", "grupo", "lucro"
            ));
        }

        // Ordena o dicionário com o QuickSort 3-way implementado manualmente
        Algoritmos.quickSort(dicionario);

        // Remove eventuais duplicatas mantendo a ordenação
        List<String> unicas = new ArrayList<>(dicionario.size());
        String anterior = null;
        for (String p : dicionario) {
            if (!p.equals(anterior)) {
                unicas.add(p);
                anterior = p;
            }
        }
        return unicas;
    }

    /**
     * Sorteia N palavras-alvo para a partida a partir do dicionário carregado.
     */
    public static List<String> sortearPalavrasPartida(List<String> dicionarioOrdenado, int quantidade) {
        List<String> copia = new ArrayList<>(dicionarioOrdenado);
        Collections.shuffle(copia);
        int limite = Math.min(quantidade, copia.size());
        return new ArrayList<>(copia.subList(0, limite));
    }

    /**
     * Atualiza a pontuação do jogador no ranking:
     * - Se o jogador já tem registro, atualiza se a nova pontuação for maior (ou menor tempo no empate).
     * - Se o jogador não tem registro, adiciona uma nova entrada.
     */
    public static void salvarPontuacao(String nomeJogador, int pontuacao, float tempoSegundos, String dificuldade) {
        if (Gdx.app == null) return;

        String nomeNorm = normalizarNome(nomeJogador);

        // Atualiza recorde geral e último jogador no Preferences
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putString(KEY_LAST_PLAYER, nomeNorm);
        int recordeAtual = prefs.getInteger(KEY_HIGH_SCORE, 0);
        if (pontuacao > recordeAtual) {
            prefs.putInteger(KEY_HIGH_SCORE, pontuacao);
        }
        prefs.flush();

        // Carrega ranking atual do arquivo local
        List<RegistroRanking> ranking = carregarRanking();
        int indiceExistente = -1;
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getNomeJogador().equalsIgnoreCase(nomeNorm)) {
                indiceExistente = i;
                break;
            }
        }

        RegistroRanking novoRegistro = new RegistroRanking(nomeNorm, pontuacao, tempoSegundos, dificuldade);

        if (indiceExistente >= 0) {
            RegistroRanking atual = ranking.get(indiceExistente);
            // Só atualiza se for melhor pontuação (ou menor tempo se mesma pontuação)
            if (novoRegistro.compareTo(atual) < 0) {
                ranking.set(indiceExistente, novoRegistro);
            }
        } else {
            ranking.add(novoRegistro);
        }

        // Ordena com Insertion Sort
        Algoritmos.insertionSort(ranking, RegistroRanking::compareTo);

        if (ranking.size() > MAX_RANKING_ENTRIES) {
            ranking = new ArrayList<>(ranking.subList(0, MAX_RANKING_ENTRIES));
        }

        escreverRankingNoArquivo(ranking);
    }

    public static void limparRanking() {
        if (Gdx.app == null) return;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger(KEY_HIGH_SCORE, 0);
        prefs.flush();
        try {
            FileHandle localFile = Gdx.files.local(ARQUIVO_RANKING);
            if (localFile.exists()) {
                localFile.delete();
            }
        } catch (Exception ignored) {
        }
    }

    private static void escreverRankingNoArquivo(List<RegistroRanking> ranking) {
        try {
            FileHandle localFile = Gdx.files.local(ARQUIVO_RANKING);
            StringBuilder sb = new StringBuilder();
            for (RegistroRanking reg : ranking) {
                sb.append(reg.serializar()).append("\n");
            }
            localFile.writeString(sb.toString(), false, "UTF-8");
        } catch (Exception e) {
            Gdx.app.error("GerenciadorArquivos", "Não foi possível salvar arquivo de ranking local", e);
        }
    }

    public static int obterRecorde() {
        if (Gdx.app == null) return 0;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        return prefs.getInteger(KEY_HIGH_SCORE, 0);
    }

    public static String obterUltimoNomeJogador() {
        if (Gdx.app == null) return "JOGADOR 1";
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        return prefs.getString(KEY_LAST_PLAYER, "JOGADOR 1");
    }

    public static void salvarUltimoNomeJogador(String nome) {
        if (Gdx.app == null) return;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putString(KEY_LAST_PLAYER, normalizarNome(nome));
        prefs.flush();
    }

    public static List<RegistroRanking> carregarRanking() {
        List<RegistroRanking> ranking = new ArrayList<>();
        if (Gdx.files == null) return ranking;

        try {
            FileHandle localFile = Gdx.files.local(ARQUIVO_RANKING);
            if (localFile.exists()) {
                String conteudo = localFile.readString("UTF-8");
                String[] linhas = conteudo.split("\\r?\\n");
                for (String linha : linhas) {
                    RegistroRanking reg = RegistroRanking.desserializar(linha);
                    if (reg != null) {
                        ranking.add(reg);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        Algoritmos.insertionSort(ranking, RegistroRanking::compareTo);
        return ranking;
    }
}
