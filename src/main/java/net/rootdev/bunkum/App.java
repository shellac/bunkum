package net.rootdev.bunkum;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

/**
 * See <http://www.redcode.nl/blog/2010/06/creating-shazam-in-java/>
 *
 */
public class App {

    public static void main(String[] args) throws Exception {
        new App().run();
    }
    private boolean running;

    public void run() throws Exception {

        final AudioFormat format = getFormat(); //Fill AudioFormat with the wanted settings
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        // In another thread I start:
        byte[] buffer = new byte[4096];

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        running = true;

        try {
            while (running) {
                int count = line.read(buffer, 0, buffer.length);
                if (count > 0) {
                    out.write(buffer, 0, count);
                }
            }
            out.close();
        } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.exit(-1);
        }


        byte audio[] = out.toByteArray();

        final int totalSize = audio.length;

        int amountPossible = totalSize / Harvester.CHUNK_SIZE;

//When turning into frequency domain we'll need complex numbers:
        Complex[][] results = new Complex[amountPossible][];

//For all the chunks:
        for (int times = 0; times < amountPossible; times++) {
            Complex[] complex = new Complex[Harvester.CHUNK_SIZE];
            for (int i = 0; i < Harvester.CHUNK_SIZE; i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(audio[(times * Harvester.CHUNK_SIZE) + i], 0);
            }
            //Perform FFT analysis on the chunk:

            FastFourierTransformer fft = new FastFourierTransformer();

            results[times] = fft.transform(complex);
        }

//Done!


        for (int i = 0; i < results.length; i++) {
            int freq = 1;
            for (int line = 1; line < size; line++) {
                // To get the magnitude of the sound at a given frequency slice
                // get the abs() from the complex number.
                // In this case I use Math.log to get a more managable number (used for color)
                double magnitude = Math.log(results[i][freq].abs() + 1);

                // The more blue in the color the more intensity for a given frequency point:
                g2d.setColor(new Color(0, (int) magnitude * 10, (int) magnitude * 20));
                // Fill:
                g2d.fillRect(i * blockSizeX, (size - line) * blockSizeY, blockSizeX, blockSizeY);

                // I used a improviced logarithmic scale and normal scale:
                if (logModeEnabled && (Math.log10(line) * Math.log10(line)) > 1) {
                    freq += (int) (Math.log10(line) * Math.log10(line));
                } else {
                    freq++;
                }
            }
        }



    }

    private AudioFormat getFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 8;
        int channels = 1; //mono
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
