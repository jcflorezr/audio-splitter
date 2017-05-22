package net.jcflorezr.audiofileinfo;

//import net.jcflorezr.model.AudioFileLocation;

import net.jcflorezr.endpoint.FlacAudioSplitterByGroup;
import net.jcflorezr.model.request.AudioFileLocation;

public class App {

    public static void main(String[] args) throws Exception {
//        String audioFileName = "/Users/juancamiloroman/OneDrive/transcriber/2016031307.mp3";
//        String audioFileName = "/Users/juancamiloroman/OneDrive/transcriber/4_Galatas 3(EB).mp3";

        //String audioFileName = "/Users/juancamiloroman/OneDrive/dsp-collection-maven copy/161-1_30.wav";
        String audioFileName = "/Users/juancamiloroman/OneDrive/dsp-collection-maven copy/strong-background-noise.wav";
        //String audioFileName = "/Users/juancamiloroman/OneDrive/dsp-collection-maven copy/135-1_30.wav";
        //String audioFileName = "/Users/juancamiloroman/Desktop/3818-2_00.wav";
//        String audioFileName = "/Users/juancamiloroman/jcflorezr-net/audio-splitter/src/main/test/resources/test-audio-mono-44100.mp3";
        String outputAudioClipsDirectory = "/Users/juancamiloroman/Downloads/voice/2017031307/20170401_103357/";
        AudioFileLocation audioFileLocation = new AudioFileLocation(audioFileName, outputAudioClipsDirectory, null);

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
