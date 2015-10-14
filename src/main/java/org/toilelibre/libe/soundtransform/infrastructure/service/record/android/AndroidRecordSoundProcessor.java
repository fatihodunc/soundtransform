package org.toilelibre.libe.soundtransform.infrastructure.service.record.android;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.toilelibre.libe.soundtransform.ioc.ApplicationInjector.$;
import org.toilelibre.libe.soundtransform.model.exception.ErrorCode;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformException;
import org.toilelibre.libe.soundtransform.model.exception.SoundTransformRuntimeException;
import org.toilelibre.libe.soundtransform.model.inputstream.StreamInfo;
import org.toilelibre.libe.soundtransform.model.logging.AbstractLogAware;
import org.toilelibre.libe.soundtransform.model.logging.EventCode;
import org.toilelibre.libe.soundtransform.model.logging.LogEvent.LogLevel;
import org.toilelibre.libe.soundtransform.model.record.RecordSoundProcessor;
import org.toilelibre.libe.soundtransform.model.record.exporter.BytesExporterFromThread;
import org.toilelibre.libe.soundtransform.model.record.exporter.OutputAsByteArrayOutputStream;
import org.toilelibre.libe.soundtransform.model.record.exporter.OutputAsByteBuffer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

final class AndroidRecordSoundProcessor extends AbstractLogAware<AndroidRecordSoundProcessor> implements RecordSoundProcessor {

    private static class StopProperlyThread extends Thread {
        private final RecordSoundProcessor processor;
        private final Object               stop;

        private StopProperlyThread (final RecordSoundProcessor processor, final Object stop) {
            this.processor = processor;
            this.stop = stop;
            this.setName (this.getClass ().getSimpleName ());
        }

        @Override
        public void run () {
            try {
                this.processor.stopProperly (this.stop);
            } catch (final SoundTransformException soundTransformException) {
                throw new SoundTransformRuntimeException (soundTransformException);
            }
        }
    }

    public enum AndroidRecordSoundProcessorEvent implements EventCode {

        NOT_ABLE_TO_READ (LogLevel.ERROR, "Not able to read the recorded data");

        private final String   messageFormat;
        private final LogLevel logLevel;

        AndroidRecordSoundProcessorEvent (final LogLevel ll, final String mF) {
            this.messageFormat = mF;
            this.logLevel = ll;
        }

        @Override
        public String getMessageFormat () {
            return this.messageFormat;
        }

        @Override
        public LogLevel getLevel () {
            return this.logLevel;
        }
    }

    public enum AndroidRecordSoundProcessorErrorCode implements ErrorCode {

        NOT_READY ("Not ready to record a sound"), STREAM_INFO_NOT_SUPPORTED ("Stream Info not supported by Recorder"), STREAM_INFO_EXPECTED ("A stream info was expected");

        private final String messageFormat;

        AndroidRecordSoundProcessorErrorCode (final String mF) {
            this.messageFormat = mF;
        }

        @Override
        public String getMessageFormat () {
            return this.messageFormat;
        }
    }

    private static final int           TWICE = 2;

    private int                        bufferSize;
    private AudioRecord                recorder;

    private AndroidRecorderThread      recordingThread;
    private BytesExporterFromThread<?> bytesExporter;

    public AudioRecord findAudioRecorder (final StreamInfo streamInfo) throws SoundTransformException {
        final int audioFormat = streamInfo.getSampleSize () == 1 ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;
        final int channelConfig = streamInfo.getChannels () == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        final int rate = (int) streamInfo.getSampleRate ();
        this.bufferSize = AudioRecord.getMinBufferSize (rate, channelConfig, audioFormat);
        final AudioRecord candidateRecorder = new AudioRecord (AudioSource.DEFAULT, rate, channelConfig, audioFormat, this.bufferSize);

        if (this.bufferSize != AudioRecord.ERROR_BAD_VALUE && candidateRecorder.getState () == AudioRecord.STATE_INITIALIZED) {
            // check if we can instantiate and have a success
            return candidateRecorder;
        }

        throw new SoundTransformException (AndroidRecordSoundProcessorErrorCode.STREAM_INFO_NOT_SUPPORTED, new UnsupportedOperationException (), streamInfo);
    }

    @Override
    public InputStream recordRawInputStream (final Object streamInfo1, final Object stop) throws SoundTransformException {
        if (! (streamInfo1 instanceof StreamInfo)) {
            throw new SoundTransformException (AndroidRecordSoundProcessorErrorCode.STREAM_INFO_EXPECTED, new IllegalArgumentException ());
        }
        final StreamInfo streamInfo = (StreamInfo) streamInfo1;
        this.startRecording (streamInfo);
        this.stopProperly (stop);
        return new ByteArrayInputStream ( ((ByteArrayOutputStream) this.bytesExporter.getOutput ()).toByteArray ());
    }

    private void waitForStop (final Object stop) throws SoundTransformException {
        boolean stopped = false;
        synchronized (stop) {
            try {
                while (!stopped) {
                    stop.wait ();
                    stopped = true;
                }
            } catch (final InterruptedException e) {
                throw new SoundTransformException (AndroidRecordSoundProcessorErrorCode.NOT_READY, e);
            }
        }
    }

    private void startRecording (final StreamInfo streamInfo) throws SoundTransformException {

        this.recorder = this.findAudioRecorder (streamInfo);
        this.bytesExporter = $.select (OutputAsByteArrayOutputStream.class);
        this.recordingThread = new AndroidRecorderThread (this.recorder, this.bytesExporter);
        this.bytesExporter.init (AndroidRecordSoundProcessor.TWICE * this.bufferSize);
        this.recorder.startRecording ();
        this.recordingThread.start ();
    }

    private void stopRecording () {
        // stops the recording activity
        if (this.recorder != null) {
            this.recorder.stop ();
            this.recorder.release ();
        }
        this.recordingThread.stopRecording ();
    }

    @Override
    public void stopProperly (final Object stop) throws SoundTransformException {
        this.waitForStop (stop);
        this.stopRecording ();
    }

    @Override
    public ByteBuffer startRecordingAndReturnByteBuffer (final Object audioFormat, final Object stop) throws SoundTransformException {
        final RecordSoundProcessor processor = this;
        if (! (audioFormat instanceof StreamInfo)) {
            throw new SoundTransformException (AndroidRecordSoundProcessorErrorCode.STREAM_INFO_EXPECTED, new IllegalArgumentException ());
        }
        final StreamInfo streamInfo = (StreamInfo) audioFormat;
        this.recorder = this.findAudioRecorder (streamInfo);
        this.bytesExporter = $.select (OutputAsByteBuffer.class);
        this.bytesExporter.init (this.bufferSize);
        this.recordingThread = new AndroidRecorderThread (this.recorder, this.bytesExporter);
        this.recorder.startRecording ();
        this.recordingThread.start ();
        new StopProperlyThread (processor, stop).start ();
        return (ByteBuffer) this.bytesExporter.getOutput ();
    }
}
