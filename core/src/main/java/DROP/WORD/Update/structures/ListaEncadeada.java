package DROP.WORD.Update.structures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementação própria de Lista Encadeada Genérica (Singly Linked List).
 * Cobre os itens de Lista Encadeada e Generic do checklist.
 *
 * @param <T> Tipo dos elementos armazenados na lista.
 */
public class ListaEncadeada<T> implements Iterable<T> {

    private static class No<T> {
        T dado;
        No<T> proximo;

        No(T dado) {
            this.dado = dado;
            this.proximo = null;
        }
    }

    private No<T> cabeca;
    private No<T> cauda;
    private int tamanho;

    public ListaEncadeada() {
        this.cabeca = null;
        this.cauda = null;
        this.tamanho = 0;
    }

    public void adicionarInicio(T elemento) {
        No<T> novoNo = new No<>(elemento);
        if (estaVazia()) {
            cabeca = novoNo;
            cauda = novoNo;
        } else {
            novoNo.proximo = cabeca;
            cabeca = novoNo;
        }
        tamanho++;
    }

    public void adicionarFim(T elemento) {
        No<T> novoNo = new No<>(elemento);
        if (estaVazia()) {
            cabeca = novoNo;
            cauda = novoNo;
        } else {
            cauda.proximo = novoNo;
            cauda = novoNo;
        }
        tamanho++;
    }

    public T removerInicio() {
        if (estaVazia()) {
            throw new NoSuchElementException("A lista está vazia.");
        }
        T dado = cabeca.dado;
        cabeca = cabeca.proximo;
        tamanho--;
        if (cabeca == null) {
            cauda = null;
        }
        return dado;
    }

    public T obterPrimeiro() {
        if (estaVazia()) {
            return null;
        }
        return cabeca.dado;
    }

    public T obterUltimo() {
        if (estaVazia()) {
            return null;
        }
        return cauda.dado;
    }

    public T obter(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice inválido: " + indice);
        }
        No<T> atual = cabeca;
        for (int i = 0; i < indice; i++) {
            atual = atual.proximo;
        }
        return atual.dado;
    }

    public boolean contem(T elemento) {
        No<T> atual = cabeca;
        while (atual != null) {
            if (atual.dado == null ? elemento == null : atual.dado.equals(elemento)) {
                return true;
            }
            atual = atual.proximo;
        }
        return false;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void limpar() {
        cabeca = null;
        cauda = null;
        tamanho = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private No<T> atual = cabeca;

            @Override
            public boolean hasNext() {
                return atual != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T dado = atual.dado;
                atual = atual.proximo;
                return dado;
            }
        };
    }
}
