package DROP.WORD.Update.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import DROP.WORD.Update.Constantes;
import DROP.WORD.Update.Main;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return;

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Word Drop");
        config.setWindowedMode(Constantes.LARGURA_TELA, Constantes.ALTURA_TELA);
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(Constantes.FPS);

        // Se você não tiver os ícones na pasta assets, deixe esta linha comentada:
        // config.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        new Lwjgl3Application(new Main(), config);
    }
}
