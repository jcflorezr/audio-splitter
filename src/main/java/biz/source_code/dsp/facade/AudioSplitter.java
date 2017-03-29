package biz.source_code.dsp.facade;

import biz.source_code.dsp.api.audiofilesgenerator.GroupSoundZonesGenerator;
import biz.source_code.dsp.audiolocation.AudioLocation;
import biz.source_code.dsp.audiofilesgenerator.GroupSoundZonesFlacGenerator;
import biz.source_code.dsp.model.AudioFileInfo;
import biz.source_code.dsp.model.AudioFilesConfigProperties;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.model.GroupAudioSoundZonesInfo;
import biz.source_code.dsp.model.SingleAudioSoundZoneInfo;
import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.sound.AudioIo;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class AudioSplitter {

    private GroupSoundZonesGenerator groupSoundZonesGenerator;


    public void init(boolean grouped) throws Exception {
        AudioFilesConfigProperties configProperties = AudioLocation.getAudioFilesConfigProperties();
        String inputFilesDirectoryPath = configProperties.getInputFilesDirectoryPath();
        List<String> audioFilesToBeProcessed = AudioLocation.getAudioFilesNamesInsideInputDirectory(inputFilesDirectoryPath);

        for(String audioFileName : audioFilesToBeProcessed) {
            AudioFileInfo audioFileInfo = AudioLocation.generateAudioFileInfo(audioFileName, configProperties, grouped);

            audioFileInfo.getGroupedAudioFileSoundZones().forEach(System.out::println);
            System.out.println(audioFileInfo.getSingleAudioFileSoundZones().size());

            // TODO surround this loop with try catch
            writeAudioSoundZonesGroupFile(audioFileInfo.getGroupedAudioFileSoundZones(),
                    audioFileInfo.getAudioSignal(),
                    audioFileInfo.getOutputFileDirectoryPathWithFileName(),
                    audioFileInfo.getAudioFilesConfigProperties());

            if (audioFileInfo.audioFileWasConverted()) {
                Path fileConvertedPath = Paths.get(audioFileInfo.getConvertedAudioFileName());
                Files.delete(fileConvertedPath);
            }
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

    private List<AudioFileWritingResult> writeAudioSoundZonesGroupFile(List<GroupAudioSoundZonesInfo> soundZones, AudioSignal audioSignal, String outputFileDirectoryPath, AudioFilesConfigProperties audioFilesConfigProperties) throws Exception {
        groupSoundZonesGenerator = new GroupSoundZonesFlacGenerator();
        AudioSignal groupSeparatorAudioSignal = getSeparatorAudioSignal(audioFilesConfigProperties, audioSignal);
        return soundZones.stream()
                .map(audioFileSoundZone ->
                        groupSoundZonesGenerator.generateAudioMonoFile(audioFileSoundZone, outputFileDirectoryPath, audioSignal, groupSeparatorAudioSignal))
                .collect(toList());
    }

    private AudioSignal getSeparatorAudioSignal(AudioFilesConfigProperties audioFilesConfigProperties, AudioSignal audioSignal) throws Exception {
        int samplingRate = audioSignal.getSamplingRate();
        int channels = audioSignal.getChannels();
        String groupSeparatorAudioFileName = null;
        if (samplingRate == 44100 && channels == 2) {
            groupSeparatorAudioFileName = audioFilesConfigProperties.getAudioFileGroupSeparator2Channels44100();
        } // To add more group separator audio files cases
        if (groupSeparatorAudioFileName != null) {
            return AudioIo.loadWavFile(groupSeparatorAudioFileName);
        }
        throw new UnsupportedAudioFileException("Could not found a group separator audio file with '" + channels + "' channels and " +
                "sampling rate of '" + samplingRate + "'.");
    }

}
