package org.toilelibre.libe.soundtransform.infrastructure.service.sound2note;

import java.util.Arrays;

import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.util.MathArrays;
import org.toilelibre.libe.soundtransform.infrastructure.service.transforms.ADSREnveloppeSoundTransformation;
import org.toilelibre.libe.soundtransform.infrastructure.service.transforms.ReverseSoundTransformation;
import org.toilelibre.libe.soundtransform.model.converted.sound.Sound;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;
import org.toilelibre.libe.soundtransform.model.library.note.ADSRHelper;

public class MagnitudeADSRHelper implements ADSRHelper {

    private static final int ACCURATE_THRESHOLD_FOR_ADSR_HELPER = 100;

    @Override
    public int findDecay (final Sound channel1, final int attack) throws SoundTransformException {
        int decayIndex = attack;

        final double [] magnitude = this.getMagnitudeArray (channel1, MagnitudeADSRHelper.ACCURATE_THRESHOLD_FOR_ADSR_HELPER);

        try {
            MathArrays.checkOrder (Arrays.copyOfRange (magnitude, attack, magnitude.length), MathArrays.OrderDirection.INCREASING, true);
        } catch (final NonMonotonicSequenceException nmse) {
            decayIndex = (nmse.getIndex () - 1) * MagnitudeADSRHelper.ACCURATE_THRESHOLD_FOR_ADSR_HELPER;
        }
        return decayIndex;
    }

    @Override
    public int findRelease (final Sound channel1) throws SoundTransformException {
        final Sound reversed = new ReverseSoundTransformation ().transform (channel1);
        int releaseIndexFromReversed = 0;

        final double [] magnitude = this.getMagnitudeArray (reversed, MagnitudeADSRHelper.ACCURATE_THRESHOLD_FOR_ADSR_HELPER);

        try {
            MathArrays.checkOrder (magnitude, MathArrays.OrderDirection.INCREASING, true);
        } catch (final NonMonotonicSequenceException nmse) {
            releaseIndexFromReversed = (nmse.getIndex () - 1) * MagnitudeADSRHelper.ACCURATE_THRESHOLD_FOR_ADSR_HELPER;
        }
        return channel1.getSamples ().length - releaseIndexFromReversed;
    }

    @Override
    public int findSustain (final Sound channel1, final int decay) throws SoundTransformException {
        int sustainIndex = decay;

        final double [] magnitude = this.getMagnitudeArray (channel1, MagnitudeADSRHelper.ACCURATE_THRESHOLD_FOR_ADSR_HELPER);

        try {
            MathArrays.checkOrder (Arrays.copyOfRange (magnitude, decay / MagnitudeADSRHelper.ACCURATE_THRESHOLD_FOR_ADSR_HELPER, magnitude.length), MathArrays.OrderDirection.DECREASING, true);
        } catch (final NonMonotonicSequenceException nmse) {
            sustainIndex = (nmse.getIndex () - 1) * MagnitudeADSRHelper.ACCURATE_THRESHOLD_FOR_ADSR_HELPER;
        }
        return sustainIndex;
    }

    private double [] getMagnitudeArray (Sound sound, int threshold) {

        final ADSREnveloppeSoundTransformation soundTransform = new ADSREnveloppeSoundTransformation (threshold);
        soundTransform.transform (sound);
        return soundTransform.getMagnitude ();
    }
}
