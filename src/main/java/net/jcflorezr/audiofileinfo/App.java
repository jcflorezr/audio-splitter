package net.jcflorezr.audiofileinfo;

//import net.jcflorezr.model.AudioFileLocation;

import net.jcflorezr.endpoint.FlacAudioSplitterByGroup;
import net.jcflorezr.model.request.AudioFileLocation;

public class App {

    public static void main(String[] args) throws Exception {
//        String audioFileName = "/Users/juancamiloroman/OneDrive/transcriber/2016031307.mp3";
//        String audioFileName = "/Users/juancamiloroman/OneDrive/transcriber/4_Galatas 3(EB).mp3";
        String audioFileName = "/Users/juaflore/jcflorezr-net/audio-splitter/src/main/test/resources/test-audio-mono-22050.mp3";
        String outputAudioClipsDirectory = "/Users/juaflore/Downloads/voice/2017031307/20170401_103357/";
        AudioFileLocation audioFileLocation = new AudioFileLocation(audioFileName, outputAudioClipsDirectory);

        System.out.println(new FlacAudioSplitterByGroup().generateAudioMonoClips(audioFileLocation));
//        System.out.println(new FlacAudioSplitterByGroup().generateAudioClips(audioFileLocation));
//        System.out.println(new WavAudioSplitterByGroup().generateAudioMonoClips(audioFileLocation));
//        System.out.println(new WavAudioSplitterByGroup().generateAudioClips(audioFileLocation));

//        System.out.println(new WavAudioSplitterBySingleFiles().generateAudioClips(audioFileLocation));
//        System.out.println(new WavAudioSplitterBySingleFiles().generateAudioMonoClips(audioFileLocation));
//        System.out.println(new FlacAudioSplitterBySingleFiles().generateAudioClips(audioFileLocation));
//        System.out.println(new FlacAudioSplitterBySingleFiles().generateAudioMonoClips(audioFileLocation));
    }

}
