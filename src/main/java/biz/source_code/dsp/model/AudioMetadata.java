package biz.source_code.dsp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonGetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AudioMetadata {

    private static final String PREFIX = "xmpDM:";

    private String title;
    private String titleWithPrefix;
    private String album;
    private String artist;
    private String trackNumber;
    private String genre;

    @JsonGetter("title")
    public String getTitle() {
        return title;
    }

    @JsonGetter(PREFIX + "title")
    public String getTitleWithPrefix() {
        return titleWithPrefix;
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

}
