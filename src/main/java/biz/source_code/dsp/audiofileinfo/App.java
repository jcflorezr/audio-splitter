package biz.source_code.dsp.audiofileinfo;

import biz.source_code.dsp.endpoint.FlacAudioSplitterByGroup;
import biz.source_code.dsp.model.AudioFileLocation;

public class App {

    public static void main(String[] args) throws Exception {
//        String audioFileName = "/Users/juancamiloroman/OneDrive/transcriber/2016031307.mp3";
        String audioFileName = "/Users/juancamiloroman/OneDrive/transcriber/4_Galatas 3(EB).mp3";
        String outputAudioClipsDirectory = "/Users/juancamiloroman/Downloads/voice/2017031307/20170401_103356/";
        AudioFileLocation audioFileLocation = new AudioFileLocation(audioFileName, outputAudioClipsDirectory);

        new FlacAudioSplitterByGroup().generateAudioMonoClips(audioFileLocation);
    }

}