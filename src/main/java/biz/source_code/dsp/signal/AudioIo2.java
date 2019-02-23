// Copyright 2013 Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland
// www.source-code.biz, www.inventec.ch/chdh
//
// This module is multi-licensed and may be used under the terms
// of any of the following licenses:
//
//  EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
//  LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
//
// Please contact the author if you need another license.
// This module is provided "as is", without warranties of any kind.

package biz.source_code.dsp.signal;

import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.util.AudioFormatsSupported;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Audio i/o utilities.
 */
// TODO: this class must be deleted once the refactoring is finished
@Service
public class AudioIo2 {

    public AudioInputStream getAudioInputStream(AudioSignal signal, int pos, int len) {
        AudioFormat format = new AudioFormat(signal.getSamplingRate(), 16, signal.getChannels(), true, false);
        AudioBytesPackerStream audioBytesPackerStream = new AudioBytesPackerStream(format, signal.getData(), pos, len);
        return new AudioInputStream(audioBytesPackerStream, format, len);
    }

    public AudioInputStream getAudioInputStream(String fileName) throws IOException, UnsupportedAudioFileException {
        return AudioSystem.getAudioInputStream(new File(fileName));
    }

    /**
     * Writes an audio rms into a WAV or FLAC file.
     *
     * @param fileName The entityName of the audio file.
     * @param extension Type of the audio file.
     * @param signal   The audio rms to be written into the WAV or FLAC file.
     * @param pos      Position of the first sample (frame) to be written.
     * @param len      Number of samples (frames) to be written.
     */
    public AudioFileWritingResult saveAudioFile(String fileName, String extension, AudioSignal signal, int pos, int len) {
        AudioInputStream audioInputStream = getAudioInputStream(signal, pos, len);
        Type fileType = AudioFormatsSupported.getFileType(extension);
        return writeAudioFile(audioInputStream, fileType, fileName + extension);
    }

    public AudioFileWritingResult saveAudioFile(String fileName, String extension, AudioSignal signal) {
        return saveAudioFile(fileName, extension, signal, 0, signal.getLength());
    }

    private AudioFileWritingResult writeAudioFile(AudioInputStream audioInputStream, Type fileType, String fileName) {
        try {
            AudioSystem.write(audioInputStream, fileType, new File(fileName));
            return AudioFileWritingResult.successful();
        } catch (Exception e) {
            return AudioFileWritingResult.unsuccessful(e);
        }
    }

    private static class AudioBytesPackerStream extends InputStream {
        AudioFormat format;
        float[][] inBufs;
        int inOffs;
        int inLen;
        int pos;

        public AudioBytesPackerStream(AudioFormat format, float[][] inBufs, int inOffs, int inLen) {
            this.format = format;
            this.inBufs = inBufs;
            this.inOffs = inOffs;
            this.inLen = inLen;
        }

        @Override
        public int read() throws IOException {
            throw new AssertionError("Not implemented.");
        }

        @Override
        public int read(byte[] outBuf, int outOffs, int outLen) throws IOException {
            int remFrames = inLen - pos;
            if (remFrames <= 0) {
                return -1;
            }
            int reqFrames = outLen / format.getFrameSize();
            int trFrames = Math.min(remFrames, reqFrames);
            packAudioStreamBytes(format, inBufs, inOffs + pos, outBuf, outOffs, trFrames);
            pos += trFrames;
            return trFrames * format.getFrameSize();
        }
    }

    public AudioSignal retrieveAudioSignalFromWavFile(String fileName) throws IOException, UnsupportedAudioFileException {
        try (AudioInputStream stream = getAudioInputStream(fileName)) {
            long totalFrames = stream.getFrameLength();
            final int blockFrames = 0x4000;
            return retrieveAudioSignal(stream, totalFrames, blockFrames);
        }
    }

    private AudioSignal retrieveAudioSignal(AudioInputStream stream, long totalFramesLong, int blockFrames) throws IOException {
        AudioFormat format = stream.getFormat();
        AudioSignal signal = new AudioSignal();
        signal.setSamplingRate(Math.round(format.getSampleRate()));
        int frameSize = format.getFrameSize();
        int channels = format.getChannels();
        if (totalFramesLong > Integer.MAX_VALUE) {
            throw new IOException("Sound file too long.");
        }
        int totalFrames = (int) totalFramesLong;
        signal.setData(new float[channels][]);
        for (int channel = 0; channel < channels; channel++) {
            signal.getData()[channel] = new float[totalFrames];
        }
        byte[] blockBuf = new byte[frameSize * blockFrames];
        int pos = 0;
        while (pos < totalFrames) {
            int reqFrames = Math.min(totalFrames - pos, blockFrames);
            int trBytes = stream.read(blockBuf, 0, reqFrames * frameSize);
            if (trBytes <= 0) {
                if (format.getEncoding() == Encoding.PCM_FLOAT && pos * frameSize == totalFrames) {
                    // Workaround for JDK bug JDK-8038139 / JI-9011075.
                    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8038139
                    // https://bugs.openjdk.java.net/browse/JDK-8038139
                    truncateSignal(signal, pos);
                    break;
                }
                throw new IOException("Unexpected EOF while reading WAV file. totalFrames=" + totalFrames + " pos=" + pos + " frameSize=" + frameSize + ".");
            }
            if (trBytes % frameSize != 0) {
                throw new IOException("Length of transmitted signal is not a multiple of frame size. reqFrames=" + reqFrames + " trBytes=" + trBytes + " frameSize=" + frameSize + ".");
            }
            int trFrames = trBytes / frameSize;
            unpackAudioStreamBytes(format, blockBuf, 0, signal.getData(), pos, trFrames);
            pos += trFrames;
        }
        return signal;
    }

    private static void truncateSignal(AudioSignal signal, int length) {
        for (int channel = 0; channel < signal.getChannels(); channel++) {
            signal.getData()[channel] = Arrays.copyOf(signal.getData()[channel], length);
        }
    }

    /**
     * A utility routine to unpack the signal of a Java Sound audio stream.
     */
    private static void unpackAudioStreamBytes(AudioFormat format, byte[] inBuf, int inPos, float[][] outBufs, int outPos, int frames) {
        Encoding encoding = format.getEncoding();
        if (encoding == Encoding.PCM_SIGNED) {
            unpackAudioStreamBytesPcmSigned(format, inBuf, inPos, outBufs, outPos, frames);
        } else if (encoding == Encoding.PCM_FLOAT) {
            unpackAudioStreamBytesPcmFloat(format, inBuf, inPos, outBufs, outPos, frames);
        } else {
            throw new UnsupportedOperationException("Audio stream format not supported (not signed PCM or Float).");
        }
    }

    private static void unpackAudioStreamBytesPcmSigned(AudioFormat format, byte[] inBuf, int inPos, float[][] outBufs, int outPos, int frames) {
        int channels = format.getChannels();
        boolean bigEndian = format.isBigEndian();
        int sampleBits = format.getSampleSizeInBits();
        int frameSize = format.getFrameSize();
        if (outBufs.length != channels) {
            throw new IllegalArgumentException("Number of channels not equal to number of buffers.");
        }
        if (sampleBits != 16 && sampleBits != 24 && sampleBits != 32) {
            throw new UnsupportedOperationException("Audio stream format not supported (" + sampleBits + " bits per sample for signed PCM).");
        }
        int sampleSize = (sampleBits + 7) / 8;
        if (sampleSize * channels != frameSize) {
            throw new AssertionError();
        }
        float maxValue = (float) ((1 << (sampleBits - 1)) - 1);
        for (int channel = 0; channel < channels; channel++) {
            float[] outBuf = outBufs[channel];
            int p0 = inPos + channel * sampleSize;
            for (int i = 0; i < frames; i++) {
                int v = unpackSignedInt(inBuf, p0 + i * frameSize, sampleBits, bigEndian);
                outBuf[outPos + i] = v / maxValue;
            }
        }
    }

    private static void unpackAudioStreamBytesPcmFloat(AudioFormat format, byte[] inBuf, int inPos, float[][] outBufs, int outPos, int frames) {
        int channels = format.getChannels();
        boolean bigEndian = format.isBigEndian();
        int sampleBits = format.getSampleSizeInBits();
        int frameSize = format.getFrameSize();
        if (outBufs.length != channels) {
            throw new IllegalArgumentException("Number of channels not equal to number of buffers.");
        }
        if (sampleBits != 32) {
            throw new UnsupportedOperationException("Audio stream format not supported (" + sampleBits + " bits per sample for floating-point PCM).");
        }
        int sampleSize = (sampleBits + 7) / 8;
        if (sampleSize * channels != frameSize) {
            throw new AssertionError();
        }
        for (int channel = 0; channel < channels; channel++) {
            float[] outBuf = outBufs[channel];
            int p0 = inPos + channel * sampleSize;
            for (int i = 0; i < frames; i++) {
                outBuf[outPos + i] = unpackFloat(inBuf, p0 + i * frameSize, bigEndian);
            }
        }
    }

    /**
     * A utility routine to pack the signal for a Java Sound audio stream.
     */
    private static void packAudioStreamBytes(AudioFormat format, float[][] inBufs, int inPos, byte[] outBuf, int outPos, int frames) {
        Encoding encoding = format.getEncoding();
        if (encoding == Encoding.PCM_SIGNED) {
            packAudioStreamBytesPcmSigned(format, inBufs, inPos, outBuf, outPos, frames);
        } else if (encoding == Encoding.PCM_FLOAT) {
            packAudioStreamBytesPcmFloat(format, inBufs, inPos, outBuf, outPos, frames);
        } else {
            throw new UnsupportedOperationException("Audio stream format not supported (not signed PCM or Float).");
        }
    }

    private static void packAudioStreamBytesPcmSigned(AudioFormat format, float[][] inBufs, int inPos, byte[] outBuf, int outPos, int frames) {
        int channels = format.getChannels();
        boolean bigEndian = format.isBigEndian();
        int sampleBits = format.getSampleSizeInBits();
        int frameSize = format.getFrameSize();
        if (inBufs.length != channels) {
            throw new IllegalArgumentException("Number of channels not equal to number of buffers.");
        }
        if (sampleBits != 16 && sampleBits != 24 && sampleBits != 32) {
            throw new UnsupportedOperationException("Audio stream format not supported (" + sampleBits + " bits per sample for signed PCM).");
        }
        int sampleSize = (sampleBits + 7) / 8;
        if (sampleSize * channels != frameSize) {
            throw new AssertionError();
        }
        int maxValue = (1 << (sampleBits - 1)) - 1;
        for (int channel = 0; channel < channels; channel++) {
            float[] inBuf = inBufs[channel];
            int p0 = outPos + channel * sampleSize;
            for (int i = 0; i < frames; i++) {
                float clipped = Math.max(-1, Math.min(1, inBuf[inPos + i]));
                int v = Math.round(clipped * maxValue);
                packSignedInt(v, outBuf, p0 + i * frameSize, sampleBits, bigEndian);
            }
        }
    }

    private static void packAudioStreamBytesPcmFloat(AudioFormat format, float[][] inBufs, int inPos, byte[] outBuf, int outPos, int frames) {
        int channels = format.getChannels();
        boolean bigEndian = format.isBigEndian();
        int sampleBits = format.getSampleSizeInBits();
        int frameSize = format.getFrameSize();
        if (inBufs.length != channels) {
            throw new IllegalArgumentException("Number of channels not equal to number of buffers.");
        }
        if (sampleBits != 32) {
            throw new UnsupportedOperationException("Audio stream format not supported (" + sampleBits + " bits per sample for floating-point PCM).");
        }
        int sampleSize = (sampleBits + 7) / 8;
        if (sampleSize * channels != frameSize) {
            throw new AssertionError();
        }
        for (int channel = 0; channel < channels; channel++) {
            float[] inBuf = inBufs[channel];
            int p0 = outPos + channel * sampleSize;
            for (int i = 0; i < frames; i++) {
                float clipped = Math.max(-1, Math.min(1, inBuf[inPos + i]));
                packFloat(clipped, outBuf, p0 + i * frameSize, bigEndian);
            }
        }
    }

    private static int unpackSignedInt(byte[] buf, int pos, int bits, boolean bigEndian) {
        switch (bits) {
            case 16:
                if (bigEndian) {
                    return (buf[pos] << 8) | (buf[pos + 1] & 0xFF);
                } else {
                    return (buf[pos + 1] << 8) | (buf[pos] & 0xFF);
                }
            case 24:
                if (bigEndian) {
                    return (buf[pos] << 16) | ((buf[pos + 1] & 0xFF) << 8) | (buf[pos + 2] & 0xFF);
                } else {
                    return (buf[pos + 2] << 16) | ((buf[pos + 1] & 0xFF) << 8) | (buf[pos] & 0xFF);
                }
            case 32:
                return unpackInt(buf, pos, bigEndian);
            default:
                throw new AssertionError();
        }
    }

    private static void packSignedInt(int i, byte[] buf, int pos, int bits, boolean bigEndian) {
        switch (bits) {
            case 16:
                if (bigEndian) {
                    buf[pos] = (byte) ((i >>> 8) & 0xFF);
                    buf[pos + 1] = (byte) (i & 0xFF);
                } else {
                    buf[pos] = (byte) (i & 0xFF);
                    buf[pos + 1] = (byte) ((i >>> 8) & 0xFF);
                }
                break;
            case 24:
                if (bigEndian) {
                    buf[pos] = (byte) ((i >>> 16) & 0xFF);
                    buf[pos + 1] = (byte) ((i >>> 8) & 0xFF);
                    buf[pos + 2] = (byte) (i & 0xFF);
                } else {
                    buf[pos] = (byte) (i & 0xFF);
                    buf[pos + 1] = (byte) ((i >>> 8) & 0xFF);
                    buf[pos + 2] = (byte) ((i >>> 16) & 0xFF);
                }
                break;
            case 32:
                packInt(i, buf, pos, bigEndian);
                break;
            default:
                throw new AssertionError();
        }
    }

    private static int unpackInt(byte[] buf, int pos, boolean bigEndian) {
        if (bigEndian) {
            return (buf[pos] << 24) | ((buf[pos + 1] & 0xFF) << 16) | ((buf[pos + 2] & 0xFF) << 8) | (buf[pos + 3] & 0xFF);
        } else {
            return (buf[pos + 3] << 24) | ((buf[pos + 2] & 0xFF) << 16) | ((buf[pos + 1] & 0xFF) << 8) | (buf[pos] & 0xFF);
        }
    }

    private static void packInt(int i, byte[] buf, int pos, boolean bigEndian) {
        if (bigEndian) {
            buf[pos] = (byte) ((i >>> 24) & 0xFF);
            buf[pos + 1] = (byte) ((i >>> 16) & 0xFF);
            buf[pos + 2] = (byte) ((i >>> 8) & 0xFF);
            buf[pos + 3] = (byte) (i & 0xFF);
        } else {
            buf[pos] = (byte) (i & 0xFF);
            buf[pos + 1] = (byte) ((i >>> 8) & 0xFF);
            buf[pos + 2] = (byte) ((i >>> 16) & 0xFF);
            buf[pos + 3] = (byte) ((i >>> 24) & 0xFF);
        }
    }

    private static float unpackFloat(byte[] buf, int pos, boolean bigEndian) {
        int i = unpackInt(buf, pos, bigEndian);
        return Float.intBitsToFloat(i);
    }

    private static void packFloat(float f, byte[] buf, int pos, boolean bigEndian) {
        int i = Float.floatToIntBits(f);
        packInt(i, buf, pos, bigEndian);
    }

}

