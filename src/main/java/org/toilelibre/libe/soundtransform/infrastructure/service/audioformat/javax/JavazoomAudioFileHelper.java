package org.toilelibre.libe.soundtransform.infrastructure.service.audioformat.javax;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

import org.toilelibre.libe.soundtransform.infrastructure.service.audioformat.WriteInputStreamToBuffer;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;
import org.toilelibre.libe.soundtransform.model.inputstream.AudioFileHelper;

public class JavazoomAudioFileHelper implements AudioFileHelper {

    private static final int HIGH_SAMPLE_RATE = 48000;
    private static final int TWO_BYTES_SAMPLE = 2 * Byte.SIZE;
    private static final int STEREO           = 2;

    private void convertIntoWavFile (final File inputFile, final File tempFile) throws SoundTransformException {
        try {
            AudioFileReader afr = this.getMpegAudioFileReader ();
            final AudioInputStream ais = afr.getAudioInputStream (inputFile);
            final AudioFormat cdFormat = new AudioFormat (JavazoomAudioFileHelper.HIGH_SAMPLE_RATE, JavazoomAudioFileHelper.TWO_BYTES_SAMPLE, JavazoomAudioFileHelper.STEREO, true, false);
            final AudioInputStream decodedAis = this.createNewInstance (this.getDecodedAudioInputStreamClassConstructor (), cdFormat, ais);
            AudioSystem.write (decodedAis, AudioFileFormat.Type.WAVE, tempFile);
        } catch (final IOException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.COULD_NOT_CONVERT, e, inputFile.getName ());
        } catch (UnsupportedAudioFileException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.COULD_NOT_CONVERT, e, inputFile.getName ());
        }
    }

    private AudioInputStream createNewInstance (Constructor<AudioInputStream> decodedAudioInputStreamClassConstructor, AudioFormat cdFormat, AudioInputStream ais) throws SoundTransformException {
        try {
            return decodedAudioInputStreamClassConstructor.newInstance (cdFormat, ais);
        } catch (IllegalArgumentException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.MP3_CONVERSION_FAILED, e);
        } catch (InstantiationException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.MP3_CONVERSION_FAILED, e);
        } catch (IllegalAccessException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.MP3_CONVERSION_FAILED, e);
        } catch (InvocationTargetException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.MP3_CONVERSION_FAILED, e);
        }
    }

    @SuppressWarnings ("unchecked")
    private <T extends AudioInputStream> Constructor<T> getDecodedAudioInputStreamClassConstructor () throws SoundTransformException {
        try {
            return (Constructor<T>) Class.forName ("javazoom.spi.mpeg.sampled.convert.DecodedMpegAudioInputStream").getConstructor (AudioFormat.class, AudioInputStream.class);
        } catch (ClassNotFoundException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.PROBLEM_IN_THE_LIBRARY, e);
        } catch (SecurityException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.PROBLEM_IN_THE_LIBRARY, e);
        } catch (NoSuchMethodException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.PROBLEM_IN_THE_LIBRARY, e);
        }
    }

    private AudioFileReader getMpegAudioFileReader () throws SoundTransformException {
        try {
            return (AudioFileReader) Class.forName ("javazoom.spi.mpeg.sampled.file.MpegAudioFileReader").newInstance ();
        } catch (IllegalAccessException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.PROBLEM_IN_THE_LIBRARY, e);
        } catch (InstantiationException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.PROBLEM_IN_THE_LIBRARY, e);
        } catch (ClassNotFoundException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.PROBLEM_IN_THE_LIBRARY, e);
        }
    }

    private InputStream getAudioInputSreamFromWavFile (final File readFile) throws SoundTransformException {
        try {
            return AudioSystem.getAudioInputStream (readFile);
        } catch (final UnsupportedAudioFileException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.COULD_NOT_CONVERT, e, readFile.getName ());
        } catch (final IOException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.COULD_NOT_CONVERT, e, readFile.getName ());
        }
    }

    @Override
    public InputStream getAudioInputStream (final File inputFile) throws SoundTransformException {
        File readFile = inputFile;
        if (inputFile.getName ().toLowerCase (Locale.getDefault ()).endsWith (".mp3")) {
            File tempFile;
            try {
                tempFile = File.createTempFile ("soundtransform", ".wav");
                this.convertIntoWavFile (inputFile, tempFile);
                readFile = tempFile;
                return AudioSystem.getAudioInputStream (readFile);
            } catch (final IOException e) {
                throw new SoundTransformException (AudioFileHelperErrorCode.COULD_NOT_CREATE_A_TEMP_FILE, e);
            } catch (UnsupportedAudioFileException e) {
                throw new SoundTransformException (AudioFileHelperErrorCode.COULD_NOT_CONVERT, e, inputFile.getName ());
            }

        }
        return this.getAudioInputSreamFromWavFile (readFile);
    }

    @Override
    public InputStream toStream (final byte [] byteArray, final Object audioFormat1) throws SoundTransformException {
        if (!(audioFormat1 instanceof AudioFormat)) {
            throw new SoundTransformException (AudioFileHelperErrorCode.AUDIO_FORMAT_COULD_NOT_BE_READ, new IllegalArgumentException ("" + audioFormat1));
        }
        final AudioFormat audioFormat = (AudioFormat) audioFormat1;
        final ByteArrayInputStream bais = new ByteArrayInputStream (byteArray);
        return new AudioInputStream (bais, audioFormat, byteArray.length / audioFormat.getFrameSize ());
    }


    @Override
    public InputStream getAudioInputStream (InputStream rawInputStream) throws SoundTransformException {
        try {
            return AudioSystem.getAudioInputStream (rawInputStream);
        } catch (UnsupportedAudioFileException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.COULD_NOT_CONVERT, e);
        } catch (IOException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.COULD_NOT_CONVERT, e);
        }
    }
    
    @Override
    public InputStream toStream (final InputStream is, final Object audioFormat1) throws SoundTransformException {
        if (!(audioFormat1 instanceof AudioFormat)) {
            throw new SoundTransformException (AudioFileHelperErrorCode.AUDIO_FORMAT_COULD_NOT_BE_READ, new IllegalArgumentException ("" + audioFormat1));
        }
        final AudioFormat audioFormat = (AudioFormat) audioFormat1;
        byte [] byteArray;
        try {
            byteArray = new WriteInputStreamToBuffer ().write (is);
        } catch (final IOException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.COULD_NOT_CONVERT, e);
        }
        final ByteArrayInputStream bais = new ByteArrayInputStream (byteArray);
        return new AudioInputStream (bais, audioFormat, byteArray.length / audioFormat.getFrameSize ());
    }

    @Override
    public void writeInputStream (final InputStream ais, final File fDest) throws SoundTransformException {
        if (!(ais instanceof AudioInputStream)) {
            throw new SoundTransformException (AudioFileHelperErrorCode.COULD_NOT_CONVERT, new IllegalArgumentException ("" + ais), ais);
        }
        try {
            AudioSystem.write ((AudioInputStream) ais, AudioFileFormat.Type.WAVE, fDest);
        } catch (final IOException e) {
            throw new SoundTransformException (AudioFileHelperErrorCode.COULD_NOT_CONVERT, e, fDest.getName ());
        }
    }
}
