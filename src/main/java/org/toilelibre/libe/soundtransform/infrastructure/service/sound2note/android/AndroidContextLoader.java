package org.toilelibre.libe.soundtransform.infrastructure.service.sound2note.android;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.toilelibre.libe.soundtransform.model.exception.ErrorCode;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;
import org.toilelibre.libe.soundtransform.model.library.pack.ContextLoader;

public class AndroidContextLoader implements ContextLoader {

    public enum AndroidContextReaderErrorCode implements ErrorCode {
        WRONG_CONTEXT_CLASS ("Expected an Android context"), COULD_NOT_READ_ID ("Could not read id : %1s"),
        COULD_NOT_FIND_ID ("Could not find id : %1s"),
        COULD_NOT_USE_CONTEXT ("Could not use context : %1s");

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
            return rClass.getDeclaredField (idName).getInt (null);
        } catch (final IllegalArgumentException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_READ_ID, e, idName);
        } catch (final IllegalAccessException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_READ_ID, e, idName);
        } catch (final NoSuchFieldException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_READ_ID, e, idName);
        } catch (final SecurityException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_READ_ID, e, idName);
        }
    }

    private Object getResources (final Object context) throws SoundTransformException {
        try {
            return context.getClass ().getMethod ("getResources").invoke (context);
        } catch (final IllegalArgumentException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_USE_CONTEXT, e, context);
        } catch (final SecurityException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_USE_CONTEXT, e, context);
        } catch (final IllegalAccessException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_USE_CONTEXT, e, context);
        } catch (final InvocationTargetException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_USE_CONTEXT, e, context);
        } catch (final NoSuchMethodException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.WRONG_CONTEXT_CLASS, e);
        }
    }

    private InputStream openRawResource (final Object resources, final int id) throws SoundTransformException {
        try {
            return (InputStream) resources.getClass ().getDeclaredMethod ("openRawResource", int.class).invoke (resources, id);
        } catch (final IllegalArgumentException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_FIND_ID, e, id);
        } catch (final SecurityException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_FIND_ID, e, id);
        } catch (final IllegalAccessException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_FIND_ID, e, id);
        } catch (final InvocationTargetException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_FIND_ID, e, id);
        } catch (final NoSuchMethodException e) {
            throw new SoundTransformException (AndroidContextReaderErrorCode.COULD_NOT_FIND_ID, e, id);
        }
    }

    @Override
    public InputStream read (final Object context, final Class<?> rClass, final int id) throws SoundTransformException {
        return this.openRawResource (this.getResources (context), id);
    }

    @Override
    public InputStream read (final Object context, final Class<?> rClass, final String idName) throws SoundTransformException {
        final int id = this.getIdFromIdName (rClass, idName);

        return this.openRawResource (this.getResources (context), id);
    }

}
