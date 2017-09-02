package net.jcflorezr.model.audiocontent;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.jcflorezr.model.persistence.AudioFileNamePrimaryKey;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Table(value = "audio_file_metadata")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AudioFileMetadata {

    private static final String PREFIX = "xmpDM:";

    @PrimaryKey
    private AudioFileNamePrimaryKey audioFileNamePrimaryKey;
    @Column("title")
    private String title;
    @Column("album")
    private String album;
    @Column("artist")
    private String artist;
    @Column("track_number")
    private String trackNumber;
    @Column("genre")
    private String genre;
    @Column("comments")
    private String comments;
    @Column("raw_metadata")
    private List<String> rawMetadata;

    public AudioFileMetadata() {
    }

    @JsonGetter("audioFileNamePrimaryKey")
    public AudioFileNamePrimaryKey getAudioFileNamePrimaryKey() {
        return audioFileNamePrimaryKey;
    }

    @JsonGetter("title")
    public String getTitle() {
        return title;
    }

    @JsonGetter(PREFIX + "album")
    public String getAlbum() {
        return album;
    }

    @JsonGetter(PREFIX + "artist")
    public String getArtist() {
        return artist;
    }

    @JsonGetter(PREFIX + "trackNumber")
    public String getTrackNumber() {
        return trackNumber;
    }

    @JsonGetter(PREFIX + "genre")
    public String getGenre() {
        return genre;
    }

    @JsonGetter(PREFIX + "logComment")
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        String[] arr = isNotBlank(comments) ? comments.split("\\n") : new String[]{};
        this.comments = arr.length > 1 ? arr[1] : comments;
    }

    public List<String> getRawMetadata() {
        return rawMetadata;
    }

    public void setRawMetadata(List<String> rawMetadata) {
        this.rawMetadata = rawMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioFileMetadata that = (AudioFileMetadata) o;

        if (audioFileNamePrimaryKey != null ? !audioFileNamePrimaryKey.equals(that.audioFileNamePrimaryKey) : that.audioFileNamePrimaryKey != null)
            return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (album != null ? !album.equals(that.album) : that.album != null) return false;
        return artist != null ? artist.equals(that.artist) : that.artist == null;
    }

    @Override
    public int hashCode() {
        int result = audioFileNamePrimaryKey != null ? audioFileNamePrimaryKey.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AudioFileMetadata{" +
                "audioFileNamePrimaryKey=" + audioFileNamePrimaryKey +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", trackNumber='" + trackNumber + '\'' +
                ", genre='" + genre + '\'' +
                ", comments='" + comments + '\'' +
                ", rawMetadata=" + rawMetadata +
                '}';
    }
}
