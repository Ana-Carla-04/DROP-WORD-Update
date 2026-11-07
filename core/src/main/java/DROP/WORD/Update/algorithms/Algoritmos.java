package DROP.WORD.Update.algorithms;

import java.util.Comparator;
import java.util.List;

/**
 * Implementação própria de algoritmos de Ordenação (QuickSort 3-way e InsertionSort)
 * e Busca (Busca Binária), utilizando métodos genéricos (<T extends Comparable<T>>).
 */
public class Algoritmos {

    /**
     * Ordena uma lista em ordem crescente usando QuickSort 3-way (partição holandesa de Dijkstra),
     * que lida em tempo O(n) com listas cheias de elementos repetidos sem estourar a pilha.
     *
     * @param lista Lista a ser ordenada.
     * @param <T>   Tipo que implementa Comparable.
     */
    public static <T extends Comparable<T>> void quickSort(List<T> lista) {
        if (lista == null || lista.size() <= 1) {
            return;
        }
        quickSort3Way(lista, 0, lista.size() - 1);
    }

    private static <T extends Comparable<T>> void quickSort3Way(List<T> lista, int inicio, int fim) {
        while (inicio < fim) {
            int meio = inicio + (fim - inicio) / 2;
            trocar(lista, inicio, meio);
            T pivo = lista.get(inicio);

            int lt = inicio;      // lista[inicio..lt-1] < pivo
            int gt = fim;         // lista[gt+1..fim] > pivo
            int i = inicio + 1;   // lista[lt..i-1] == pivo

            while (i <= gt) {
                int cmp = lista.get(i).compareTo(pivo);
                if (cmp < 0) {
                    trocar(lista, lt++, i++);
                } else if (cmp > 0) {
                    trocar(lista, i, gt--);
                } else {
                    i++;
                }
            }

            // Recorre na menor metade e itera na maior (Tail Call Elimination) -> O(log N) de pilha no pior caso
            if (lt - inicio < fim - gt) {
                quickSort3Way(lista, inicio, lt - 1);
                inicio = gt + 1;
            } else {
                quickSort3Way(lista, gt + 1, fim);
                fim = lt - 1;
            }
        }
    }

    /**
     * Ordena uma lista usando Insertion Sort com um Comparator customizado
     * (por exemplo, para ordenar o Ranking de pontuações em ordem decrescente).
     *
     * @param lista      Lista a ser ordenada.
     * @param comparador Critério de comparação.
     * @param <T>        Tipo dos elementos.
     */
    public static <T> void insertionSort(List<T> lista, Comparator<T> comparador) {
        if (lista == null || lista.size() <= 1) {
            return;
        }
        for (int i = 1; i < lista.size(); i++) {
            T chave = lista.get(i);
            int j = i - 1;
            while (j >= 0 && comparador.compare(lista.get(j), chave) > 0) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, chave);
        }
    }

    /**
     * Realiza Busca Binária iterativa em uma lista previamente ordenada.
     *
     * @param listaOrdenada Lista ordenada em ordem crescente.
     * @param alvo          Elemento procurado.
     * @param <T>           Tipo que implementa Comparable.
     * @return Índice do elemento se encontrado, ou -1 caso contrário.
     */
    public static <T extends Comparable<T>> int buscaBinaria(List<T> listaOrdenada, T alvo) {
        if (listaOrdenada == null || alvo == null) {
            return -1;
        }
        int inicio = 0;
        int fim = listaOrdenada.size() - 1;

        while (inicio <= fim) {
            int meio = inicio + (fim - inicio) / 2;
            T valorMeio = listaOrdenada.get(meio);
            int cmp = valorMeio.compareTo(alvo);

            if (cmp == 0) {
                return meio;
            } else if (cmp < 0) {
                inicio = meio + 1;
            } else {
                fim = meio - 1;
            }
        }
        return -1;
    }

    private static <T> void trocar(List<T> lista, int i, int j) {
        if (i != j) {
            T temp = lista.get(i);
            lista.set(i, lista.get(j));
            lista.set(j, temp);
        }
    }
}
