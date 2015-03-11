package org.toilelibre.libe.soundtransform.ioc.javax;

import org.toilelibre.libe.soundtransform.infrastructure.service.audioformat.javax.JavazoomAudioFileHelper;
import org.toilelibre.libe.soundtransform.infrastructure.service.audioformat.javax.WavAudioFormatParser;
import org.toilelibre.libe.soundtransform.infrastructure.service.play.javax.LineListenerPlaySoundProcessor;
import org.toilelibre.libe.soundtransform.infrastructure.service.sound2note.javax.ErrorContextLoader;
import org.toilelibre.libe.soundtransform.ioc.ImplAgnosticRootModule;
import org.toilelibre.libe.soundtransform.model.inputstream.AudioFileHelper;
import org.toilelibre.libe.soundtransform.model.inputstream.AudioFormatParser;
import org.toilelibre.libe.soundtransform.model.library.pack.ContextLoader;
import org.toilelibre.libe.soundtransform.model.play.PlaySoundProcessor;

public class JavaXRootModule extends ImplAgnosticRootModule {

    @Override
    protected AudioFileHelper provideAudioFileHelper () {
        return new JavazoomAudioFileHelper ();
    }

    @Override
    protected AudioFormatParser provideAudioFormatParser () {
        return new WavAudioFormatParser ();
    }

    @Override
    protected ContextLoader provideContextLoader () {
        return new ErrorContextLoader ();
    }

    @Override
    protected PlaySoundProcessor providePlaySoundProcessor () {
        return new LineListenerPlaySoundProcessor ();
    }

}
