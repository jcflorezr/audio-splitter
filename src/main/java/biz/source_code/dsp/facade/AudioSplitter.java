package biz.source_code.dsp.facade;

import biz.source_code.dsp.api.audiofilesgenerator.GroupSoundZonesGenerator;
import biz.source_code.dsp.audiofilesgenerator.GroupSoundZonesFlacGenerator;
import biz.source_code.dsp.audiolocation.AudioLocation;
import biz.source_code.dsp.model.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class AudioSplitter {

    private GroupSoundZonesGenerator groupSoundZonesGenerator;
    private AudioLocation audioLocation = new AudioLocation();


    public void init(boolean grouped) throws Exception {
        AudioFilesConfigProperties configProperties = audioLocation.getAudioFilesConfigProperties();
        String inputFilesDirectoryPath = configProperties.getInputFilesDirectoryPath();
        List<String> audioFilesToBeProcessed = audioLocation.getAudioFilesNamesInsideInputDirectory(inputFilesDirectoryPath);

        for(String audioFileName : audioFilesToBeProcessed) {
            AudioFileInfo audioFileInfo = audioLocation.generateAudioFileInfo(audioFileName, configProperties, grouped);

            audioFileInfo.getGroupedAudioFileSoundZones().forEach(System.out::println);
            System.out.println(audioFileInfo.getSingleAudioFileSoundZones().size());

            // TODO surround this loop with try catch
            writeAudioSoundZonesGroupFile(audioFileInfo.getGroupedAudioFileSoundZones(),
                    audioFileInfo.getAudioSignal(),
                    audioFileInfo.getSeparatorAudioSignal(),
                    audioFileInfo.getOutputFileDirectoryPathWithFileName());


        }
    }



    private List<AudioFileWritingResult> writeSingleAudioSoundZoneFile(List<SingleAudioSoundZoneInfo> soundZones, AudioSignal signal, String outputDirectoryPath) {
//        audioFileInfo.getSingleAudioSoundZonesInfo().stream()
//                .skip(1)
//                .forEach(audioFileSoundZone ->
//                        audioFileSoundZone.setSplittingResult(
//                                SingleSoundZoneFlacGenerator.generateAudioWavFile(audioFileSoundZone,
//                                        audioFileInfo.getAudioSignal(),
//                                        audioFileInfo.getOutputFileDirectoryPathWithFileName()))
//                    );
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private List<AudioFileWritingResult> writeAudioSoundZonesGroupFile(List<GroupAudioSoundZonesInfo> soundZones, AudioSignal audioSignal, AudioSignal groupSeparatorAudioSignal, String outputFileDirectoryPath) throws Exception {
        groupSoundZonesGenerator = new GroupSoundZonesFlacGenerator();
        return soundZones.stream()
                .map(audioFileSoundZone ->
                        groupSoundZonesGenerator.generateAudioMonoFile(audioFileSoundZone, outputFileDirectoryPath, audioSignal, groupSeparatorAudioSignal))
                .collect(toList());
    }



}
