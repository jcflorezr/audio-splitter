package net.jcflorezr.model.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;
import net.jcflorezr.model.persistence.AudioClipsPrimaryKey;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "audio_clip_result")
public class AudioClipsWritingResult {

    @PrimaryKey
    private AudioClipsPrimaryKey audioClipsPrimaryKey;
    @Column("success")
    private boolean success;
    @Column("exception")
    private String exception;

    public AudioClipsWritingResult(String audioFileName, AudioClipInfo audioClipInfo, AudioFileWritingResult audioFileWritingResult, String audioClipName) {
        audioClipsPrimaryKey = new AudioClipsPrimaryKey.AudioClipsPrimaryKeyBuilder()
                .audioFileName(audioFileName)
                .hours(audioClipInfo.getHours())
                .minutes(audioClipInfo.getMinutes())
                .seconds(audioClipInfo.getSeconds())
                .milliseconds(audioClipInfo.getMilliseconds())
                .audioClipName(audioClipName)
                .build();
        this.success = audioFileWritingResult.isSuccess();
        this.exception = audioFileWritingResult.getException() != null ? audioFileWritingResult.getException().toString() : null;
    }

    public AudioClipsPrimaryKey getAudioFileName() {
        return audioClipsPrimaryKey;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getException() {
        return exception;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioClipsWritingResult that = (AudioClipsWritingResult) o;

        if (success != that.success) return false;
        return audioClipsPrimaryKey != null ? audioClipsPrimaryKey.equals(that.audioClipsPrimaryKey) : that.audioClipsPrimaryKey == null;
    }

    @Override
    public int hashCode() {
        int result = audioClipsPrimaryKey != null ? audioClipsPrimaryKey.hashCode() : 0;
        result = 31 * result + (success ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AudioClipsWritingResult{" +
                "audioClipsPrimaryKey=" + audioClipsPrimaryKey +
                ", success=" + success +
                ", exception='" + exception + '\'' +
                '}';
    }
}
