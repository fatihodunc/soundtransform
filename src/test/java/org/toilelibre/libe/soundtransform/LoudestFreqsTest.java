package org.toilelibre.libe.soundtransform;

import java.util.Collections;

import org.junit.Test;
import org.toilelibre.libe.soundtransform.actions.fluent.FluentClient;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;

public class LoudestFreqsTest {

    @Test
    public void adjustFreqs () {
        final float [] array = { 225.32f, 799.2f, 146.11f, 332.74f, 55f, 139f, 1010f };
        final float [] array2 = { 220f, 783.991f, 146.832f, 329.628f, 55f, 146.832f, 987.767f };
        final float [] result = FluentClient.start ().withFreqs (Collections.singletonList (array)).adjust ().stopWithFreqs ().get (0);
        org.junit.Assert.assertArrayEquals (result, array2, 0);
    }

    @Test
    public void changeOctaveDown () {
        final float [] array = { 1, 2, 3, -2, -6, 10, 12 };
        final float [] array2 = { 0.5f, 1, 1.5f, -1, -3, 5, 6 };
        final float [] result = FluentClient.start ().withFreqs (Collections.singletonList (array)).octaveDown ().stopWithFreqs ().get (0);
        org.junit.Assert.assertArrayEquals (result, array2, 0);
    }

    @Test
    public void changeOctaveUp () {
        final float [] array = { 1, 2, 3, -2, -6, 10, 12 };
        final float [] array2 = { 2, 4, 6, -4, -12, 20, 24 };
        final float [] result = FluentClient.start ().withFreqs (Collections.singletonList (array)).octaveUp ().stopWithFreqs ().get (0);
        org.junit.Assert.assertArrayEquals (result, array2, 0);
    }

    @Test
    public void operationShouldBeReversibleEvenWithALotOfOctaveDown () {
        final float [] array = { 1, 2, 3, -2, -6, 10, 12 };
        final float [] result = FluentClient.start ().withFreqs (Collections.singletonList (array)).octaveDown ().octaveDown ().octaveDown ().octaveDown ().octaveDown ().octaveUp ().octaveUp ().octaveUp ().octaveUp ().octaveUp ().stopWithFreqs ().get (0);
        org.junit.Assert.assertArrayEquals (result, array, 0);
    }

    @Test
    public void surroundInRange () throws SoundTransformException {
        final float [] array = { 225.32f, 799.2f, 146.11f, 332.74f, 55f, 139f, 1010f };
        final float [] array2 = { 225.32f, 199.8f, 146.11f, 166.37f, 220f, 139f, 189.375f };
        final float [] result = FluentClient.start ().withFreqs (Collections.singletonList (array)).surroundInRange (130.81f, 246.94f).stopWithFreqs ().get (0);
        org.junit.Assert.assertArrayEquals (result, array2, 0);
    }
}
