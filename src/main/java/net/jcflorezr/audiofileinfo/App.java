package net.jcflorezr.audiofileinfo;

//import net.jcflorezr.model.AudioFileBasicInfo;

import net.jcflorezr.endpoint.FlacAudioSplitterByGroup;
import net.jcflorezr.model.request.AudioFileBasicInfo;

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
        AudioFileBasicInfo audioFileBasicInfo = new AudioFileBasicInfo(audioFileName, outputAudioClipsDirectory);

        System.out.println(new FlacAudioSplitterByGroup().generateAudioMonoClips(audioFileBasicInfo));
//        System.out.println(new FlacAudioSplitterByGroup().generateAudioClips(audioFileBasicInfo));
//        System.out.println(new WavAudioSplitterByGroup().generateAudioMonoClips(audioFileBasicInfo));
//        System.out.println(new WavAudioSplitterByGroup().generateAudioClips(audioFileBasicInfo));

//        System.out.println(new WavAudioSplitterBySingleFiles().generateAudioClips(audioFileBasicInfo));
//        System.out.println(new WavAudioSplitterBySingleFiles().generateAudioMonoClips(audioFileBasicInfo));
//        System.out.println(new FlacAudioSplitterBySingleFiles().generateAudioClips(audioFileBasicInfo));
//        System.out.println(new FlacAudioSplitterBySingleFiles().generateAudioMonoClips(audioFileBasicInfo));
    }

}
