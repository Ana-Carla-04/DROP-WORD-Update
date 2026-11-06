package DROP.WORD.Update.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * Captura a cena atual numa imagem e aplica um desfoque gaussiano (blur) nela.
 *
 * Como o jogo fica congelado durante a pausa, o blur é calculado UMA vez (em capturar())
 * e depois só a imagem pronta é desenhada a cada frame (desenhar()), o que é barato.
 *
 * O blur é separável: uma passada horizontal e outra vertical, repetidas algumas vezes.
 * A imagem é processada em resolução reduzida, o que deixa o desfoque mais forte e mais rápido.
 */
public class DesfoqueFundo implements Disposable {

    private static final String VERTEX =
        "attribute vec4 a_position;\n" +
        "attribute vec4 a_color;\n" +
        "attribute vec2 a_texCoord0;\n" +
        "uniform mat4 u_projTrans;\n" +
        "varying vec4 v_color;\n" +
        "varying vec2 v_texCoords;\n" +
        "void main() {\n" +
        "    v_color = a_color;\n" +
        "    v_texCoords = a_texCoord0;\n" +
        "    gl_Position = u_projTrans * a_position;\n" +
        "}\n";

    // Gaussiana de 9 amostras (5 leituras de textura, aproveitando a interpolação linear)
    private static final String FRAGMENT =
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "varying vec4 v_color;\n" +
        "varying vec2 v_texCoords;\n" +
        "uniform sampler2D u_texture;\n" +
        "uniform vec2 u_direcao;\n" +
        "void main() {\n" +
        "    vec4 soma = texture2D(u_texture, v_texCoords) * 0.2270270270;\n" +
        "    soma += texture2D(u_texture, v_texCoords + u_direcao * 1.3846153846) * 0.3162162162;\n" +
        "    soma += texture2D(u_texture, v_texCoords - u_direcao * 1.3846153846) * 0.3162162162;\n" +
        "    soma += texture2D(u_texture, v_texCoords + u_direcao * 3.2307692308) * 0.0702702703;\n" +
        "    soma += texture2D(u_texture, v_texCoords - u_direcao * 3.2307692308) * 0.0702702703;\n" +
        "    gl_FragColor = soma;\n" +
        "}\n";

    private final int largura;
    private final int altura;
    private final int passadas;
    private final float brilho;

    private final FrameBuffer bufferA;
    private final FrameBuffer bufferB;
    private final ShaderProgram shader;
    private final Matrix4 projecao = new Matrix4();

    private boolean temImagem = false;

    /**
     * @param larguraCena largura da cena (em pixels do mundo do jogo)
     * @param alturaCena  altura da cena
     * @param escala      fração da resolução usada no blur (0.5 = metade; menor = mais borrado)
     * @param passadas    quantas vezes repetir horizontal+vertical (mais = mais borrado)
     * @param brilho      1 = sem escurecer; 0.8 escurece levemente a imagem borrada
     */
    public DesfoqueFundo(int larguraCena, int alturaCena, float escala, int passadas, float brilho) {
        this.largura = Math.max(1, Math.round(larguraCena * escala));
        this.altura = Math.max(1, Math.round(alturaCena * escala));
        this.passadas = Math.max(1, passadas);
        this.brilho = brilho;

        this.bufferA = new FrameBuffer(Pixmap.Format.RGBA8888, largura, altura, false);
        this.bufferB = new FrameBuffer(Pixmap.Format.RGBA8888, largura, altura, false);
        bufferA.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        bufferB.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        this.shader = new ShaderProgram(VERTEX, FRAGMENT);
        if (!shader.isCompiled()) {
            Gdx.app.error("DesfoqueFundo", "Shader de blur não compilou: " + shader.getLog());
        }
        projecao.setToOrtho2D(0, 0, largura, altura);
    }

    public DesfoqueFundo(int larguraCena, int alturaCena) {
        this(larguraCena, alturaCena, 0.5f, 2, 0.85f);
    }

    public boolean temImagem() {
        return temImagem;
    }

    /** Esquece a imagem capturada (chame ao despausar). */
    public void descartar() {
        temImagem = false;
    }

    /**
     * Desenha a cena dentro do buffer (usando o Runnable) e aplica o blur.
     * Não pode ser chamado com o batch ou ShapeRenderer em begin().
     * Depois de chamar, restaure o viewport da janela (por exemplo viewport.apply()),
     * pois FrameBuffer.end() deixa o viewport ocupando a janela inteira.
     */
    public void capturar(SpriteBatch batch, Runnable desenharCena) {
        bufferA.begin();
        desenharCena.run();
        bufferA.end();

        if (shader.isCompiled()) {
            for (int i = 0; i < passadas; i++) {
                passada(batch, bufferA, bufferB, 1f / largura, 0f); // horizontal
                passada(batch, bufferB, bufferA, 0f, 1f / altura);  // vertical
            }
        }
        temImagem = true;
    }

    private void passada(SpriteBatch batch, FrameBuffer origem, FrameBuffer destino, float dx, float dy) {
        destino.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(projecao);
        batch.setShader(shader);
        batch.disableBlending(); // copia os pixels como estão, sem misturar com o fundo
        batch.begin();
        shader.setUniformf("u_direcao", dx, dy);
        batch.draw(origem.getColorBufferTexture(), 0, 0, largura, altura);
        batch.end();
        batch.enableBlending();
        batch.setShader(null);
        destino.end();
    }

    /**
     * Desenha a imagem borrada ocupando o retângulo informado.
     * O batch precisa estar em begin() e com a projeção da tela já configurada.
     */
    public void desenhar(SpriteBatch batch, float x, float y, float w, float h) {
        batch.setColor(brilho, brilho, brilho, 1f);
        batch.draw(bufferA.getColorBufferTexture(), x, y, w, h);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        bufferA.dispose();
        bufferB.dispose();
        shader.dispose();
    }
}