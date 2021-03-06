package org.toilelibre.libe.soundtransform.actions.transform;

import java.util.List;

import org.toilelibre.libe.soundtransform.actions.Action;
import org.toilelibre.libe.soundtransform.ioc.ApplicationInjector;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;
import org.toilelibre.libe.soundtransform.model.freqs.LoudestFreqsService;
import org.toilelibre.libe.soundtransform.model.logging.Observer;

@Action
public final class ChangeLoudestFreqs {

    private final LoudestFreqsService loudestFreqs;

    public ChangeLoudestFreqs (final Observer... observers) {
        this.loudestFreqs = ApplicationInjector.$.select (LoudestFreqsService.class);
    }

    public List<float []> adjust (final List<float []> freqs) {
        return this.loudestFreqs.adjust (freqs);
    }

    public List<float []> compress (final List<float []> freqs, final float factor) {
        return this.loudestFreqs.compress (freqs, factor);
    }

    public List<float []> filterRange (final List<float []> freqs, final float low, final float high) throws SoundTransformException {
        return this.loudestFreqs.filterRange (freqs, low, high);
    }

    public List<float []> insertPart (final List<float []> freqs, final List<float []> subFreqs, final int start) {
        return this.loudestFreqs.insertPart (freqs, subFreqs, start);
    }

    public List<float []> octaveDown (final List<float []> freqs) {
        return this.loudestFreqs.octaveDown (freqs);
    }

    public List<float []> octaveUp (final List<float []> freqs) {
        return this.loudestFreqs.octaveUp (freqs);
    }

    public List<float []> replacePart (final List<float []> freqs, final List<float []> subFreqs, final int start) {
        return this.loudestFreqs.replacePart (freqs, subFreqs, start);
    }

    public List<float []> surroundInRange (final List<float []> freqs, final float low, final float high) throws SoundTransformException {
        return this.loudestFreqs.surroundInRange (freqs, low, high);
    }
}
