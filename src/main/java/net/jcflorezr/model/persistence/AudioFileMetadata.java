package net.jcflorezr.model.persistence;

import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.List;

@Table(value = "audio_file_metadata")
public class AudioFileMetadata {

    @PrimaryKey("audio_file_name")
    private String audioFileName;

    private String title;
    private String album;
    private String artist;
    private int trackNumber;
    private String genre;
    private List<String> rawMetadata;

    public String getAudioFileName() {
        return audioFileName;
    }

    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
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

        if (!audioFileName.equals(that.audioFileName)) return false;
        if (!title.equals(that.title)) return false;
        if (album != null ? !album.equals(that.album) : that.album != null) return false;
        return artist != null ? artist.equals(that.artist) : that.artist == null;
    }

    @Override
    public int hashCode() {
        int result = audioFileName.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }
}
