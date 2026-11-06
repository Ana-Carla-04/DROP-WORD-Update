package DROP.WORD.Update.utils;

import com.badlogic.gdx.audio.Sound;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Gerador de efeitos sonoros sintéticos (WAV 16-bit PCM 44.1kHz) em runtime.
 * Permite ter áudio imediato e sem dependências externas de download.
 */
public class GeradorSons {

    public static Sound gerarSomMover() {
        return sintetizarSom((sampleRate, samples) -> {
            // "blip" rápido de 40ms subindo de 400Hz para 600Hz
            int total = (int) (sampleRate * 0.04f);
            short[] buffer = new short[total];
            for (int i = 0; i < total; i++) {
                float t = (float) i / total;
                float freq = 400f + 200f * t;
                float envelope = (1f - t);
                buffer[i] = (short) (Math.sin(2.0 * Math.PI * freq * (i / (double) sampleRate)) * envelope * 8000);
            }
            return buffer;
        });
    }

    public static Sound gerarSomPouso() {
        return sintetizarSom((sampleRate, samples) -> {
            // "thud" de 60ms em baixa frequência (180Hz descendo para 80Hz)
            int total = (int) (sampleRate * 0.06f);
            short[] buffer = new short[total];
            for (int i = 0; i < total; i++) {
                float t = (float) i / total;
                float freq = 180f - 100f * t;
                float envelope = (1f - t) * (1f - t);
                buffer[i] = (short) (Math.sin(2.0 * Math.PI * freq * (i / (double) sampleRate)) * envelope * 12000);
            }
            return buffer;
        });
    }

    public static Sound gerarSomPalavra() {
        return sintetizarSom((sampleRate, samples) -> {
            // acorde ascendente alegre de 250ms (Dó - Mi - Sol: 523Hz, 659Hz, 784Hz)
            int total = (int) (sampleRate * 0.28f);
            short[] buffer = new short[total];
            for (int i = 0; i < total; i++) {
                float t = (float) i / total;
                float envelope = 1f - t;
                double s1 = Math.sin(2.0 * Math.PI * 523.25 * (i / (double) sampleRate));
                double s2 = (i > total * 0.25f) ? Math.sin(2.0 * Math.PI * 659.25 * (i / (double) sampleRate)) : 0;
                double s3 = (i > total * 0.50f) ? Math.sin(2.0 * Math.PI * 783.99 * (i / (double) sampleRate)) : 0;
                buffer[i] = (short) (((s1 + s2 + s3) / 3.0) * envelope * 15000);
            }
            return buffer;
        });
    }

    public static Sound gerarSomErro() {
        return sintetizarSom((sampleRate, samples) -> {
            // "buzzer" de 200ms com onda quadrada rápida de 150Hz
            int total = (int) (sampleRate * 0.22f);
            short[] buffer = new short[total];
            for (int i = 0; i < total; i++) {
                float t = (float) i / total;
                float freq = 140f - 30f * t;
                double sin = Math.sin(2.0 * Math.PI * freq * (i / (double) sampleRate));
                double sq = sin >= 0 ? 1.0 : -1.0;
                float envelope = 1f - t;
                buffer[i] = (short) (sq * envelope * 10000);
            }
            return buffer;
        });
    }

    public static Sound gerarSomVitoria() {
        return sintetizarSom((sampleRate, samples) -> {
            // Fanfarra triunfal de 400ms
            int total = (int) (sampleRate * 0.45f);
            short[] buffer = new short[total];
            for (int i = 0; i < total; i++) {
                float t = (float) i / total;
                float envelope = 1f - (t * 0.7f);
                double s1 = Math.sin(2.0 * Math.PI * 587.33 * (i / (double) sampleRate));
                double s2 = Math.sin(2.0 * Math.PI * 880.00 * (i / (double) sampleRate));
                buffer[i] = (short) (((s1 + s2) / 2.0) * envelope * 14000);
            }
            return buffer;
        });
    }

    private interface GeradorWave {
        short[] gerar(int sampleRate, int totalSamples);
    }

    private static Sound sintetizarSom(GeradorWave waveGen) {
        int sampleRate = 44100;
        short[] pcm = waveGen.gerar(sampleRate, 0);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            // Cabeçalho RIFF WAV
            out.writeBytes("RIFF");
            out.writeInt(Integer.reverseBytes(36 + pcm.length * 2));
            out.writeBytes("WAVE");
            out.writeBytes("fmt ");
            out.writeInt(Integer.reverseBytes(16)); // Chunk size 16
            out.writeShort(Short.reverseBytes((short) 1)); // PCM format
            out.writeShort(Short.reverseBytes((short) 1)); // Mono
            out.writeInt(Integer.reverseBytes(sampleRate));
            out.writeInt(Integer.reverseBytes(sampleRate * 2)); // Byte rate
            out.writeShort(Short.reverseBytes((short) 2)); // Block align
            out.writeShort(Short.reverseBytes((short) 16)); // Bits per sample
            out.writeBytes("data");
            out.writeInt(Integer.reverseBytes(pcm.length * 2));

            for (short s : pcm) {
                out.writeShort(Short.reverseBytes(s));
            }
            out.flush();

            File tempFile = File.createTempFile("snd_drop_", ".wav");
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(baos.toByteArray());
            }

            return com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.absolute(tempFile.getAbsolutePath()));
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error("GeradorSons", "Falha ao sintetizar efeito sonoro", e);
            return null;
        }
    }
}
