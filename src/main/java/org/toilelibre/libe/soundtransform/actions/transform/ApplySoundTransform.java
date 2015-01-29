package org.toilelibre.libe.soundtransform.actions.transform;

import java.io.InputStream;

import org.toilelibre.libe.soundtransform.actions.Action;
import org.toilelibre.libe.soundtransform.model.converted.SoundTransformation;
import org.toilelibre.libe.soundtransform.model.converted.sound.Sound;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;

public final class ApplySoundTransform extends Action {

    public Sound [] apply (final Sound [] sounds, final SoundTransformation transform) throws SoundTransformException {
        return this.transformSound.apply (sounds, transform);
    }

    public Sound [] convertAndApply (final InputStream ais, final SoundTransformation transform) throws SoundTransformException {
        return this.transformSound.convertAndApply (ais, transform);
    }
}