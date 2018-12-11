package net.jcflorezr.model.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;
import net.jcflorezr.model.persistence.AudioFileClipsPrimaryKey;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table(value = "audio_clips_results")
public class AudioFileClipResultEntity {

    @PrimaryKey
    private AudioFileClipsPrimaryKey audioFileClipsPrimaryKey;
    @Column("success")
    private boolean success;
    @Column("exception")
    private String exception;

    public AudioFileClipResultEntity() {
    }

    public AudioFileClipResultEntity(String audioFileName, AudioFileClipEntity audioFileClipEntity, AudioFileWritingResult audioFileWritingResult, String audioClipName) {
        audioFileClipsPrimaryKey = new AudioFileClipsPrimaryKey.AudioClipsPrimaryKeyBuilder()
                .audioFileName(audioFileName)
                .hours(audioFileClipEntity.getHours())
                .minutes(audioFileClipEntity.getMinutes())
                .seconds(audioFileClipEntity.getSeconds())
                .milliseconds(audioFileClipEntity.getMilliseconds())
                .audioClipName(audioClipName)
                .build();
        this.success = audioFileWritingResult.isSuccess();
        this.exception = audioFileWritingResult.getException() != null ? audioFileWritingResult.getException().toString() : null;
    }

    public AudioFileClipsPrimaryKey getAudioFileClipsPrimaryKey() {
        return audioFileClipsPrimaryKey;
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

        AudioFileClipResultEntity that = (AudioFileClipResultEntity) o;

        if (success != that.success) return false;
        return audioFileClipsPrimaryKey != null ? audioFileClipsPrimaryKey.equals(that.audioFileClipsPrimaryKey) : that.audioFileClipsPrimaryKey == null;
    }

    @Override
    public int hashCode() {
        int result = audioFileClipsPrimaryKey != null ? audioFileClipsPrimaryKey.hashCode() : 0;
        result = 31 * result + (success ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AudioFileClipResultEntity{" +
                "audioFileClipsPrimaryKey=" + audioFileClipsPrimaryKey +
                ", success=" + success +
                ", exception='" + exception + '\'' +
                '}';
    }
}
