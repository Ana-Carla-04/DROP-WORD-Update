# Word Drop 🔤

Um jogo de palavras e raciocínio ágil desenvolvido em **Java** com o framework **libGDX**. 
Em **Word Drop**, letras caem continuamente em uma grade 5×6 e o jogador deve posicioná-las na coluna correspondente para formar e limpar 12 palavras-alvo (ou qualquer palavra de 5 letras válida do dicionário).

---

## 🎮 Como Jogar & Regras

1. **Grade e Colunas**: A grade possui 5 colunas (correspondentes às posições da 1ª à 5ª letra de cada palavra) e 6 linhas.
2. **Posicionamento**: Letras caem do topo. Posicione a letra na coluna correta da palavra a que ela pertence.
3. **Formando Palavras**: 
   - Quando uma linha completa forma uma das **12 palavras-alvo**, a palavra é marcada como completada, a linha é removida da grade e você ganha uma grande pontuação!
   - **Palavras Bônus**: Qualquer palavra válida de 5 letras presente no dicionário (`palavras.txt`) que se formar na linha também limpa a linha e garante bônus de pontos!
4. **Condição de Derrota**:
   - A pilha de letras atinge o topo da grade (linha 0), ou
   - Uma letra cai em uma coluna onde não pode se encaixar em nenhuma palavra ativa.
5. **Condição de Vitória**:
   - Todas as 12 palavras sorteadas são formadas e limpas da grade.

---

## ⌨️ Controles

| Tecla / Ação | Função |
|---|---|
| **Setas Esquerda / Direita** (ou **A / D**) | Move a letra caindo para a esquerda ou direita |
| **Seta para Baixo** (ou **S**) | Segure para acelerar a velocidade de queda da letra |
| **P** | Pausa / Despausa o jogo com efeito visual de desfoque |
| **M** | Liga / Muta os efeitos sonoros |
| **R** | Reinicia a partida imediatamente com as mesmas palavras |
| **ESC** | Volta ao Menu Principal |
| **Enter / Espaço / Mouse** | Navega e confirma botões e seleção de nome |

---

## 🏆 Modo Competição & Ranking

- **Nome Personalizável**: Clique no campo **`JOGADOR`** no Menu ou digite seu nome ao final da partida.
- **Top 8 Ranking**: Pontuação, tempo de jogo, nível de dificuldade e nome são persistidos localmente em arquivo (`ranking_word_drop.txt`) e `Preferences`.
- **Botão Zerar**: Permite reiniciar o ranking para torneios presenciais ou competições entre amigos.

---

## 🧠 Estruturas de Dados e Algoritmos Utilizados

O projeto implementa manualmente as estruturas de dados e algoritmos requisitados, aplicando conceitos avançados de computação:

1. **Lista Encadeada Genérica (`ListaEncadeada<T>`)**:
   - Localização: `core/src/main/java/DROP/WORD/Update/structures/ListaEncadeada.java`
   - Implementação própria com nós encadeados (`No<T>`), controle de cabeça/cauda e suporte a `Iterable<T>`.
2. **Fila Genérica (`Fila<T>`)**:
   - Localização: `core/src/main/java/DROP/WORD/Update/structures/Fila.java`
   - Implementa política FIFO estrita em cima da lista encadeada. Usada em `GerenciadorLetras` para enfileirar e consumir a sequência de letras que caem.
3. **Pilha Genérica (`Pilha<T>`)**:
   - Localização: `core/src/main/java/DROP/WORD/Update/structures/Pilha.java`
   - Implementa política LIFO estrita em cima da lista encadeada. Usada em `GerenciadorPalavras` para registrar o histórico de palavras formadas e consultar a última palavra no HUD.
4. **QuickSort 3-Way com Tail Call Optimization (`Algoritmos.quickSort`)**:
   - Localização: `core/src/main/java/DROP/WORD/Update/algorithms/Algoritmos.java`
   - Partição holandesa de Dijkstra genérica (`<T extends Comparable<T>>`) imune a estouro de pilha com elementos repetidos ($O(n)$ no melhor caso e $O(\log n)$ de espaço de recursão). Usado para ordenar o dicionário de palavras.
5. **Insertion Sort Genérico (`Algoritmos.insertionSort`)**:
   - Localização: `core/src/main/java/DROP/WORD/Update/algorithms/Algoritmos.java`
   - Ordena o Ranking de pontuações de forma estável com comparador customizado (`Comparator<T>`).
6. **Busca Binária Genérica (`Algoritmos.buscaBinaria`)**:
   - Localização: `core/src/main/java/DROP/WORD/Update/algorithms/Algoritmos.java`
   - Busca em $O(\log n)$ sobre a lista ordenada do dicionário para validar instantaneamente qualquer palavra de 5 letras formada na grade.
7. **Manipulação de Arquivos e Persistência**:
   - Localização: `core/src/main/java/DROP/WORD/Update/utils/GerenciadorArquivos.java`
   - Carrega `assets/palavras.txt` via `Gdx.files.internal`, salva ranking em `Gdx.files.local("ranking_word_drop.txt")` e recordes via `Preferences`.

---

## 🛠️ Como Executar

### Pré-requisitos
- **Java JDK 21** ou superior instalado e configurado no `PATH`.

### Executando pelo Gradle
Abra um terminal na raiz do projeto e execute:

```bash
# No Windows:
.\gradlew.bat lwjgl3:run

# No Linux / macOS:
./gradlew lwjgl3:run
```

Para apenas compilar e validar os códigos:
```bash
.\gradlew.bat compileJava
```
