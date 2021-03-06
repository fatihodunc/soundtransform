package org.toilelibre.libe.soundtransform.infrastructure.service.sound2note.android;

import java.io.InputStream;
import java.lang.reflect.Field;

import org.toilelibre.libe.soundtransform.infrastructure.service.Processor;
import org.toilelibre.libe.soundtransform.model.exception.ErrorCode;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;
import org.toilelibre.libe.soundtransform.model.library.pack.ContextLoader;

import android.content.Context;
import android.content.res.Resources;

@Processor
final class AndroidContextLoader implements ContextLoader {

    private enum AndroidContextReaderErrorCode implements ErrorCode {
        WRONG_CONTEXT_CLASS ("Expected an Android context"), COULD_NOT_READ_ID ("Could not read id : %1s"), COULD_NOT_FIND_ID ("Could not find id : %1s"), COULD_NOT_USE_CONTEXT ("Could not use context : %1s");

        private final String messageFormat;

        AndroidContextReaderErrorCode (final String mF) {
            this.messageFormat = mF;
        }

        @Override
        public String getMessageFormat () {
            return this.messageFormat;
        }
    }

    private int getIdFromIdName (final Class<?> rClass, final String idName) throws SoundTransformException {
        try {
            return this.getDeclaredField (rClass, idName).getInt (null);
        } catch (final IllegalArgumentException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_READ_ID, e, idName);
        } catch (final IllegalAccessException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_READ_ID, e, idName);
        }
    }

    private Field getDeclaredField (final Class<?> rClass, final String idName) throws SoundTransformException {
        try {
            return rClass.getDeclaredField (idName);
        } catch (final NoSuchFieldException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_READ_ID, e, idName);
        }
    }

    @Override
    public InputStream read (final Object context, final int id) throws SoundTransformException {
        try {
            return ((Context) context).getResources ().openRawResource (id);
        } catch (Resources.NotFoundException nfe) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_READ_ID, nfe, id);
        }
    }

    @Override
    public InputStream read (final Object context, final Class<?> rClass, final String idName) throws SoundTransformException {
        final int id = this.getIdFromIdName (rClass, idName);

        return this.read (context, id);
    }

}
