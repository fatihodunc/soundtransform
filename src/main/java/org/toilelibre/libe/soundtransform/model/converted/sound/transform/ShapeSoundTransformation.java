package org.toilelibre.libe.soundtransform.model.converted.sound.transform;

import org.toilelibre.libe.soundtransform.ioc.ApplicationInjector.$;
import org.toilelibre.libe.soundtransform.model.converted.SoundTransformation;
import org.toilelibre.libe.soundtransform.model.converted.sound.Sound;
import org.toilelibre.libe.soundtransform.model.converted.sound.SoundAppender;
import org.toilelibre.libe.soundtransform.model.exception.ErrorCode;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;
import org.toilelibre.libe.soundtransform.model.library.Library;
import org.toilelibre.libe.soundtransform.model.library.note.Note;
import org.toilelibre.libe.soundtransform.model.library.note.Silence;
import org.toilelibre.libe.soundtransform.model.library.pack.Pack;
import org.toilelibre.libe.soundtransform.model.observer.AbstractLogAware;
import org.toilelibre.libe.soundtransform.model.observer.LogEvent;
import org.toilelibre.libe.soundtransform.model.observer.LogEvent.LogLevel;

public class ShapeSoundTransformation extends AbstractLogAware<ShapeSoundTransformation> implements SoundTransformation {
    public enum ShapeSoundTransformationErrorCode implements ErrorCode {

        NOT_AN_INSTRUMENT ("%1s is not a valid instrument"), NO_PACK_IN_PARAMETER ("No pack in parameter. Please instantiate a ShapeSoundTransformation with a not null Pack");

        private final String messageFormat;

        ShapeSoundTransformationErrorCode (final String mF) {
            this.messageFormat = mF;
        }

        @Override
        public String getMessageFormat () {
            return this.messageFormat;
        }
    }

    private final Pack          pack;
    private final String        instrument;
    private final SoundAppender soundAppender;
    private final Silence       silence;
    private int []              freqs;
    private int                 nbBytesPerSample;
    private int                 soundLength;
    private int                 sampleRate;

    public ShapeSoundTransformation (final String packName, final String instrument) {
        this.silence = new Silence ();
        this.pack = $.select (Library.class).getPack (packName);
        this.instrument = instrument;
        this.soundAppender = $.select (SoundAppender.class);
    }

    public ShapeSoundTransformation (final String packName, final String instrument, final int [] freqs, final int soundLength1, final int nbBytesPerSample1, final int sampleRate1) {
        this (packName, instrument);
        this.freqs = freqs;
        this.soundLength = soundLength1;
        this.nbBytesPerSample = nbBytesPerSample1;
        this.sampleRate = sampleRate1;
    }

    private Note findNote (final double lastFreq, final int sampleRate, final int i, final int lastBegining) throws SoundTransformException {
        Note note = this.silence;
        if (lastFreq > 50 && Math.abs (sampleRate - lastFreq) > 100) {
            this.log (new LogEvent (LogLevel.VERBOSE, "Note (" + lastFreq + "Hz) between " + lastBegining + "/" + this.freqs.length + " and " + i + "/" + this.freqs.length));
            if (!this.pack.containsKey (this.instrument)) {
                throw new SoundTransformException (ShapeSoundTransformationErrorCode.NOT_AN_INSTRUMENT, new NullPointerException (), this.instrument);
            }
            note = this.pack.get (this.instrument).getNearestNote ((int) lastFreq);
        }
        return note;
    }

    private boolean freqHasChanged (final int freq1, final int freq2) {
        return Math.abs (freq1 - freq2) > freq1 * 5.0 / 100;
    }

    private int [] getLoudestFreqs (final Sound sound, final int threshold) {
        final PeakFindWithHPSSoundTransformation<?> peak = $.create (PeakFindWithHPSSoundTransformation.class, threshold, -1);
        peak.setObservers (this.observers).transform (sound);
        return peak.getLoudestFreqs ();
    }

    public Sound transform (final int threshold) throws SoundTransformException {
        return this.transform (threshold, 1);
    }

    public Sound transform (final int threshold, final int channelNum) throws SoundTransformException {
        final Sound builtSound = new Sound (new long [this.soundLength], this.nbBytesPerSample, this.sampleRate, channelNum);
        int lastBegining = 0;
        int lastFreq = 0;
        boolean firstNote = true;
        for (int i = 4 ; i < this.freqs.length ; i++) {
            final boolean freqChanged = this.freqHasChanged (this.freqs [i - 1], this.freqs [i]);
            final boolean freqChangedBefore = this.freqHasChanged (this.freqs [i - 2], this.freqs [i - 1]);
            final boolean freqChangedBeforeBefore = this.freqHasChanged (this.freqs [i - 3], this.freqs [i - 2]);
            final boolean freqChangedBeforeBeforeBefore = this.freqHasChanged (this.freqs [i - 4], this.freqs [i - 3]);
            final boolean freqChangedFromLastNote = this.freqHasChanged (this.freqs [i], lastFreq);
            final boolean newNote = !freqChanged && !freqChangedBefore && !freqChangedBeforeBefore && freqChangedBeforeBeforeBefore && (!freqChangedFromLastNote || firstNote);
            if (i == this.freqs.length - 1 || newNote) {
                final int endOfNoteIndex = i == this.freqs.length - 1 ? i : i - 4;
                final float lengthInSeconds = (endOfNoteIndex - lastBegining < 1 ? this.freqs [i] * threshold : (endOfNoteIndex - 1 - lastBegining) * threshold * 1.0f) / this.sampleRate;
                final Note note = this.findNote (this.freqs [endOfNoteIndex], this.sampleRate, endOfNoteIndex + 1, lastBegining + 1);
                this.soundAppender.appendNote (builtSound, note, this.freqs [endOfNoteIndex], threshold * lastBegining, channelNum, lengthInSeconds);
                lastBegining = endOfNoteIndex;
                lastFreq = this.freqs [endOfNoteIndex];
                firstNote = false;
            }
        }

        this.freqs = null;
        return builtSound;
    }

    @Override
    public Sound transform (final Sound sound) throws SoundTransformException {
        if (this.pack == null) {
            throw new SoundTransformException (ShapeSoundTransformationErrorCode.NO_PACK_IN_PARAMETER, new NullPointerException ());
        }
        final int threshold = 100;
        int channelNum = 1;

        this.log (new LogEvent (LogLevel.VERBOSE, "Finding loudest frequencies"));

        if (this.freqs == null) {
            this.soundLength = sound.getSamples ().length;
            this.nbBytesPerSample = sound.getNbBytesPerSample ();
            this.sampleRate = sound.getSampleRate ();
            this.freqs = this.getLoudestFreqs (sound, threshold);
            channelNum = sound.getChannelNum ();
        }
        return this.transform (threshold, channelNum);
    }

}