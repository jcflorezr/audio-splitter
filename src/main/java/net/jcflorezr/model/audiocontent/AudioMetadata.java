package net.jcflorezr.model.audiocontent;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;

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
        String[] arr = comments.split("\\n");
        this.comments = arr.length > 0 ? arr[1] : comments;
    }

    public List<String> getRawMetadata() {
        return rawMetadata;
    }

    public void setRawMetadata(List<String> rawMetadata) {
        this.rawMetadata = rawMetadata;
    }

}
