package net.jcflorezr.model.audiocontent;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AudioMetadata {

    private static final String PREFIX = "xmpDM:";

    private String title;
    private String album;
    private String artist;
    private String trackNumber;
    private String genre;
    private String comments;
    private List<String> rawMetadata;

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

        AudioMetadata that = (AudioMetadata) o;

        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (album != null ? !album.equals(that.album) : that.album != null) return false;
        if (artist != null ? !artist.equals(that.artist) : that.artist != null) return false;
        if (trackNumber != null ? !trackNumber.equals(that.trackNumber) : that.trackNumber != null) return false;
        return genre != null ? genre.equals(that.genre) : that.genre == null;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (trackNumber != null ? trackNumber.hashCode() : 0);
        result = 31 * result + (genre != null ? genre.hashCode() : 0);
        return result;
    }
}
