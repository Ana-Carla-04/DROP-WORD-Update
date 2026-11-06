package DROP.WORD.Update.structures;

/**
 * Implementação própria de Pilha Genérica (LIFO - Last In, First Out)
 * baseada em ListaEncadeada<T>.
 *
 * @param <T> Tipo dos elementos na pilha.
 */
public class Pilha<T> {
    private final ListaEncadeada<T> lista = new ListaEncadeada<>();

    public void empilhar(T elemento) {
        lista.adicionarInicio(elemento);
    }

    public T desempilhar() {
        if (estaVazia()) {
            return null;
        }
        return lista.removerInicio();
    }

    public T topo() {
        return lista.obterPrimeiro();
    }

    public boolean estaVazia() {
        return lista.estaVazia();
    }

    public int getTamanho() {
        return lista.getTamanho();
    }

    public void limpar() {
        lista.limpar();
    }
}
