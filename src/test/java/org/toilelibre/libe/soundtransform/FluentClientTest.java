package org.toilelibre.libe.soundtransform;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Test;
import org.toilelibre.libe.soundtransform.actions.fluent.FluentClient;
import org.toilelibre.libe.soundtransform.ioc.SoundTransformTest;
import org.toilelibre.libe.soundtransform.model.converted.sound.transform.EightBitsSoundTransformation;
import org.toilelibre.libe.soundtransform.model.converted.sound.transform.NoOpSoundTransformation;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;
import org.toilelibre.libe.soundtransform.model.inputstream.InputStreamInfo;

public class FluentClientTest extends SoundTransformTest {

    @Test
    public void backAndForth () throws SoundTransformException {
        FluentClient.start ().withClasspathResource ("before.wav").convertIntoSound ().apply (new NoOpSoundTransformation ()).exportToClasspathResource ("before.wav").convertIntoSound ();
    }

    // Plays the sound three times, therefore too long time consuming test
    // @Test
    public void playIt () throws SoundTransformException {
        FluentClient.start ().withClasspathResource ("before.wav").playIt ().convertIntoSound ().playIt ().exportToStream ().playIt ();
    }

    @Test
    public void readRawInputStream () throws SoundTransformException {
        final RandomDataGenerator rdg = new RandomDataGenerator ();
        final byte [] data = new byte [65536];
        for (int i = 0 ; i < data.length ; i++) {
            data [i] = (byte) rdg.nextInt (Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        final InputStream is = new ByteArrayInputStream (data);
        final InputStreamInfo isi = new InputStreamInfo (1, 32768, 2, 8000, false, true);

        FluentClient.start ().withRawInputStream (is, isi).importToSound ().exportToClasspathResourceWithSiblingResource ("after.wav", "before.wav");
    }

    @Test
    public void simpleLifeCycle () throws SoundTransformException {
        FluentClient.start ().withClasspathResource ("before.wav").convertIntoSound ().apply (new EightBitsSoundTransformation (25)).exportToClasspathResource ("after.wav");
    }

    @Test
    public void testImportHPSFreqs () throws SoundTransformException {
        final int [] freqs = new int [(int) Math.random () * 2000 + 4000];
        int i = 0;
        while (i < freqs.length) {
            final int length = Math.min ((int) (Math.random () * 200 + 400), freqs.length - i);
            final int currentFreq = (int) (Math.random () * 150 + 160);
            for (int j = 0 ; j < length ; j++) {
                freqs [i++] = currentFreq;
            }
        }
        final InputStream packInputStream = Thread.currentThread ().getContextClassLoader ().getResourceAsStream ("defaultPack.json");
        final InputStreamInfo isi = new InputStreamInfo (1, freqs.length * 100, 2, 48000, false, true);
        FluentClient.start ().withAPack ("default", packInputStream).withFreqs (freqs).shapeIntoSound ("default", "simple_piano", isi).exportToClasspathResourceWithSiblingResource ("after.wav", "before.wav");
    }

    @Test
    public void twoTimesInOneInstruction () throws SoundTransformException {
        FluentClient.start ().withClasspathResource ("before.wav").convertIntoSound ().andAfterStart ().withClasspathResource ("before.wav").convertIntoSound ();
    }
}
