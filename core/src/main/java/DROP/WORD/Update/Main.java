package DROP.WORD.Update;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import DROP.WORD.Update.screens.TelaMenu;
import DROP.WORD.Update.utils.GeradorSons;
import DROP.WORD.Update.utils.GerenciadorArquivos;

import java.util.List;

public class Main extends Game {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont fontPadrao;
    private BitmapFont fontTitulo;
    private BitmapFont fontLetras;
    private BitmapFont fontPequena;

    private List<String> dicionarioOrdenado;
    private Constantes.Dificuldade dificuldadeAtual = Constantes.Dificuldade.NORMAL;
    private String nomeJogadorAtual = "JOGADOR 1";

    // Sons do jogo
    private Sound somMover;
    private Sound somPouso;
    private Sound somPalavra;
    private Sound somErro;
    private Sound somVitoria;
    private boolean audioAtivado = true;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        inicializarFontes();
        inicializarSons();

        // Carrega e ordena o dicionário de palavras a partir de assets/palavras.txt
        dicionarioOrdenado = GerenciadorArquivos.carregarDicionarioOrdenado();
        nomeJogadorAtual = GerenciadorArquivos.obterUltimoNomeJogador();

        setScreen(new TelaMenu(this));
    }

    private void inicializarFontes() {
        FileHandle fontFile = Gdx.files.internal("arialbd.ttf");
        if (fontFile.exists()) {
            try {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
                FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
                param.flip = true; // yDown para compatibilidade com o sistema de coordenadas do jogo
                param.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                    + "ÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÑÒÓÔÕÖÙÚÛÜÝàáâãäåçèéêëìíîïñòóôõöùúûüýÿªº•–—#><";
                param.minFilter = Texture.TextureFilter.Linear;
                param.magFilter = Texture.TextureFilter.Linear;

                // Título principal (WORD DROP, cabeçalhos das telas finais)
                param.size = 36;
                fontTitulo = generator.generateFont(param);

                // Letras do grid e cartões caindo (grande e nítido)
                param.size = 42;
                fontLetras = generator.generateFont(param);

                // Botões, palavras da lista e textos principais
                param.size = 22;
                fontPadrao = generator.generateFont(param);

                // HUD, instruções e ranking
                param.size = 16;
                fontPequena = generator.generateFont(param);

                generator.dispose();
                return;
            } catch (Exception e) {
                Gdx.app.error("Main", "Erro ao carregar FreeTypeFont, usando fallback", e);
            }
        }

        // Fallback nativo
        fontPadrao = new BitmapFont(true);
        fontTitulo = new BitmapFont(true);
        fontLetras = new BitmapFont(true);
        fontPequena = new BitmapFont(true);

        fontPadrao.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontTitulo.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontLetras.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontPequena.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private void inicializarSons() {
        try {
            somMover = GeradorSons.gerarSomMover();
            somPouso = GeradorSons.gerarSomPouso();
            somPalavra = GeradorSons.gerarSomPalavra();
            somErro = GeradorSons.gerarSomErro();
            somVitoria = GeradorSons.gerarSomVitoria();
        } catch (Exception e) {
            Gdx.app.error("Main", "Não foi possível carregar os sons", e);
        }
    }

    public void tocarSomMover() {
        if (audioAtivado && somMover != null) somMover.play(0.4f);
    }

    public void tocarSomPouso() {
        if (audioAtivado && somPouso != null) somPouso.play(0.5f);
    }

    public void tocarSomPalavra() {
        if (audioAtivado && somPalavra != null) somPalavra.play(0.8f);
    }

    public void tocarSomErro() {
        if (audioAtivado && somErro != null) somErro.play(0.7f);
    }

    public void tocarSomVitoria() {
        if (audioAtivado && somVitoria != null) somVitoria.play(0.85f);
    }

    public boolean isAudioAtivado() {
        return audioAtivado;
    }

    public void alternarAudio() {
        this.audioAtivado = !this.audioAtivado;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public BitmapFont getFontPadrao() {
        return fontPadrao;
    }

    public BitmapFont getFontTitulo() {
        return fontTitulo;
    }

    public BitmapFont getFontLetras() {
        return fontLetras;
    }

    public BitmapFont getFontPequena() {
        return fontPequena != null ? fontPequena : fontPadrao;
    }

    public List<String> getDicionarioOrdenado() {
        return dicionarioOrdenado;
    }

    public Constantes.Dificuldade getDificuldadeAtual() {
        return dificuldadeAtual;
    }

    public void setDificuldadeAtual(Constantes.Dificuldade dificuldadeAtual) {
        this.dificuldadeAtual = dificuldadeAtual;
    }

    public String getNomeJogadorAtual() {
        return nomeJogadorAtual;
    }

    public void setNomeJogadorAtual(String nomeJogadorAtual) {
        this.nomeJogadorAtual = GerenciadorArquivos.normalizarNome(nomeJogadorAtual);
        GerenciadorArquivos.salvarUltimoNomeJogador(this.nomeJogadorAtual);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (fontPadrao != null) fontPadrao.dispose();
        if (fontTitulo != null) fontTitulo.dispose();
        if (fontLetras != null) fontLetras.dispose();
        if (fontPequena != null) fontPequena.dispose();

        if (somMover != null) somMover.dispose();
        if (somPouso != null) somPouso.dispose();
        if (somPalavra != null) somPalavra.dispose();
        if (somErro != null) somErro.dispose();
        if (somVitoria != null) somVitoria.dispose();
        super.dispose();
    }
}
