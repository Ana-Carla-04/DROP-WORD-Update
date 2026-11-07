package DROP.WORD.Update.structures;

/**
 * Implementação própria de Fila Genérica (FIFO - First In, First Out)
 * baseada em ListaEncadeada<T>.
 *
 * @param <T> Tipo dos elementos na fila.
 */
public class Fila<T> {
    private final ListaEncadeada<T> lista = new ListaEncadeada<>();

    public void enfileirar(T elemento) {
        lista.adicionarFim(elemento);
    }

    public T desenfileirar() {
        if (estaVazia()) {
            return null;
        }
        return lista.removerInicio();
    }

    public T espiar() {
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
