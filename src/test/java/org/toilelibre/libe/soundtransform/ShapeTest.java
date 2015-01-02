package org.toilelibre.libe.soundtransform;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.Test;
import org.toilelibre.libe.soundtransform.infrastructure.service.appender.ConvertedSoundAppender;
import org.toilelibre.libe.soundtransform.infrastructure.service.observer.PrintlnTransformObserver;
import org.toilelibre.libe.soundtransform.infrastructure.service.transforms.ShapeSoundTransformation;
import org.toilelibre.libe.soundtransform.model.TransformSoundService;
import org.toilelibre.libe.soundtransform.model.converted.sound.Sound;
import org.toilelibre.libe.soundtransform.model.inputstream.ConvertAudioFileService;
import org.toilelibre.libe.soundtransform.model.library.Library;
import org.toilelibre.libe.soundtransform.model.library.note.Sound2NoteService;

public class ShapeTest {

    @Test
    public void testAppendSoundsWithDifferentNbBytes () throws IOException, UnsupportedAudioFileException {
        final ClassLoader classLoader = Thread.currentThread ().getContextClassLoader ();
        final File input1 = new File (classLoader.getResource ("notes/Piano2-D.wav").getFile ());
        final File input2 = new File (classLoader.getResource ("notes/g-piano3.wav").getFile ());
        final Sound [] s1 = new TransformSoundService ().fromInputStream (new ConvertAudioFileService ().callConverter (input1));
        final Sound [] s2 = new TransformSoundService ().fromInputStream (new ConvertAudioFileService ().callConverter (input2));
        new ConvertedSoundAppender ().append (s2 [0], 1000, s1 [0]);
    }

    @Test
    public void testShapeASimplePianoNoteAsAChordNote () {

        try {
            System.out.println ("Loading packs");
            final Library packsList = Library.getInstance ();
            final ClassLoader classLoader = Thread.currentThread ().getContextClassLoader ();
            final File input = new File (classLoader.getResource ("notes/Piano5-G.wav").getFile ());
            final File output = new File (new File (classLoader.getResource ("before.wav").getFile ()).getParent () + "/after.wav");
            final AudioInputStream outputStream = new TransformSoundService (new PrintlnTransformObserver ()).transformAudioStream (new ConvertAudioFileService ().callConverter (input),
                    new ShapeSoundTransformation (packsList.defaultPack, "chord_piano"));

            AudioSystem.write (outputStream, AudioFileFormat.Type.WAVE, output);

            final int frequency = Sound2NoteService.convert ("output chord_note",
                    new TransformSoundService (new PrintlnTransformObserver ()).fromInputStream (new ConvertAudioFileService ().callConverter (output))).getFrequency ();
            System.out.println ("Output chord note should be around 387Hz, but is " + frequency + "Hz");
        } catch (final UnsupportedAudioFileException e) {
            e.printStackTrace ();
        } catch (final IOException e) {
            e.printStackTrace ();
        }
    }

    @Test
    public void testShapeASimplePianoNoteAsAChordNoteSameFrequency () {

        try {
            System.out.println ("Loading packs");
            final Library packsList = Library.getInstance ();
            final ClassLoader classLoader = Thread.currentThread ().getContextClassLoader ();
            final File input = new File (classLoader.getResource ("notes/Piano3-E.wav").getFile ());
            final File output = new File (new File (classLoader.getResource ("before.wav").getFile ()).getParent () + "/after.wav");
            final AudioInputStream outputStream = new TransformSoundService (new PrintlnTransformObserver ()).transformAudioStream (new ConvertAudioFileService ().callConverter (input),
                    new ShapeSoundTransformation (packsList.defaultPack, "chord_piano"));

            AudioSystem.write (outputStream, AudioFileFormat.Type.WAVE, output);

            final int frequency = Sound2NoteService.convert ("output chord_note",
                    new TransformSoundService (new PrintlnTransformObserver ()).fromInputStream (new ConvertAudioFileService ().callConverter (output))).getFrequency ();
            System.out.println ("Output chord note should be around 332Hz, but is " + frequency + "Hz");
        } catch (final UnsupportedAudioFileException e) {
            e.printStackTrace ();
        } catch (final IOException e) {
            e.printStackTrace ();
        }
    }
}