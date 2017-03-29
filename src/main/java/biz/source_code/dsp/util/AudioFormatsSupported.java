package biz.source_code.dsp.util;

import net.sourceforge.javaflacencoder.FLACFileWriter;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.UnsupportedAudioFileException;

public enum AudioFormatsSupported {

    WAV("audio/x-wav", ".wav", Type.WAVE),
    FLAC("audio/x-flac", ".flac", FLACFileWriter.FLAC),
    MP3("audio/mpeg", ".mp3", null),
    MP3_1("audio/x-mpeg-3", ".mp3", null);

    private String mimeType;
    private String extension;
    private Type fileType;

    AudioFormatsSupported(String mimeType, String extension, Type fileType) {
        this.mimeType = mimeType;
        this.extension = extension;
        this.fileType = fileType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return this.extension;
    }

    public Type getFileType() {
        return fileType;
    }

    public static String getExtension(String mimeType) {
        for(AudioFormatsSupported supportedAudioFormat : AudioFormatsSupported.values()) {
            if (supportedAudioFormat.mimeType.equals(mimeType)) {
                return supportedAudioFormat.extension;
            }
        }
        throw new UnsupportedOperationException("The MIME type '" + mimeType + "' is not supported.");
    }

    public static boolean isAudioFormatSupported(String mimeType) {
        for(AudioFormatsSupported supportedAudioFormat : AudioFormatsSupported.values()) {
            if (supportedAudioFormat.mimeType.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }

    public static Type getFileType(String extension) {
        for(AudioFormatsSupported supportedAudioFormat : AudioFormatsSupported.values()) {
            if (supportedAudioFormat.extension.equals(extension)) {
                return supportedAudioFormat.fileType;
            }
        }
        throw new UnsupportedOperationException("The file extension '" + extension + "' is not supported.");
    }

}
