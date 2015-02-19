package org.toilelibre.libe.soundtransform.model.library.note;

import org.toilelibre.libe.soundtransform.model.converted.sound.Sound;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;

public class Sound2NoteService {

    private final ADSRHelper      adsrHelper;

    private final FrequencyHelper frequencyHelper;

    public Sound2NoteService (final ADSRHelper helper1, final FrequencyHelper helper2) {
        this.adsrHelper = helper1;
        this.frequencyHelper = helper2;
    }

    public Note convert (final String fileName, final Sound [] channels) throws SoundTransformException {
        return this.convert (fileName, channels, this.frequencyHelper.findFrequency (channels));
    }

    public Note convert (final String fileName, final Sound [] channels, final float frequency) throws SoundTransformException {
        final Sound channel1 = channels [0];

        final int attack = 0;
        final int decay = this.adsrHelper.findDecay (channel1, attack);
        final int sustain = this.adsrHelper.findSustain (channel1, decay);
        final int release = this.adsrHelper.findRelease (channel1);

        return new SimpleNote (fileName, channels, frequency, attack, decay, sustain, release);

    }
}
