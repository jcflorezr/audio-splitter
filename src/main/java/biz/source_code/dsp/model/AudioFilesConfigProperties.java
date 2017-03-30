package biz.source_code.dsp.model;

import java.util.Properties;

public class AudioFilesConfigProperties {

    // TODO this class should be removed after implementing @Value annotation

    private String inputFilesDirectoryPath;
    private String outputFilesDirectoryPath;
    private String audioFileGroupSeparator2Channels44100;

    public AudioFilesConfigProperties(Properties properties) {
        this.inputFilesDirectoryPath = properties.getProperty("inputFilesDirectoryPath");
        this.outputFilesDirectoryPath = properties.getProperty("outputFilesDirectoryPath");
        this.audioFileGroupSeparator2Channels44100 = properties.getProperty("audioFileGroupSeparator2Channels44100");
    }

    public String getInputFilesDirectoryPath() {
        return inputFilesDirectoryPath;
    }

    public String getOutputFilesDirectoryPath() {
        return outputFilesDirectoryPath;
    }

    public String getAudioFileGroupSeparator2Channels44100() {
        return audioFileGroupSeparator2Channels44100;
    }
}
