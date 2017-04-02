package biz.source_code.dsp.api.endpoint;

import biz.source_code.dsp.api.audioclips.AudioClipsGenerator;
import biz.source_code.dsp.api.model.response.AudioSplitterResponse;
import biz.source_code.dsp.audioclips.AudioClipsGeneratorImpl;
import biz.source_code.dsp.audiofileinfo.AudioFileInfoService;
import biz.source_code.dsp.exceptions.AudioFileLocationException;
import biz.source_code.dsp.exceptions.BadRequestException;
import biz.source_code.dsp.exceptions.InternalServerErrorException;
import biz.source_code.dsp.model.AudioFileInfo;
import biz.source_code.dsp.model.AudioFileLocation;
import biz.source_code.dsp.model.GroupAudioSoundZonesInfo;
import biz.source_code.dsp.model.OutputAudioClipsConfig;
import biz.source_code.dsp.model.SingleAudioSoundZoneInfo;
import biz.source_code.dsp.util.AudioFormatsSupported;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public abstract class AudioSplitter {

    protected AudioFileInfoService audioFileInfoService = new AudioFileInfoService();
    protected AudioClipsGenerator audioClipsGenerator = new AudioClipsGeneratorImpl();

    protected abstract AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation);

    protected abstract AudioSplitterResponse generateAudioMonoClips(AudioFileLocation audioFileLocation);

    protected AudioSplitterResponse generateAudioClipsByGroup(AudioFileLocation audioFileLocation, AudioFormatsSupported audioFormat, boolean asMono) {
        boolean generateAudioClipsByGroup = true;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup);
    }
    
    protected AudioSplitterResponse generateSingleAudioClips(AudioFileLocation audioFileLocation, AudioFormatsSupported audioFormat, boolean asMono) {
        boolean generateAudioClipsByGroup = false;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup);
    }
    
    private AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation, AudioFormatsSupported audioFormat, boolean asMono, boolean generateAudioClipsByGroup) {
        try {
            validateAudioFileLocationInfo(audioFileLocation);
            AudioFileInfo audioFileInfo = audioFileInfoService.generateAudioFileInfo(audioFileLocation, generateAudioClipsByGroup);
            OutputAudioClipsConfig outputAudioClipsConfig = audioFileInfo.getOutputAudioClipsConfig(audioFormat, asMono);
            if (generateAudioClipsByGroup) {
                Stream<GroupAudioSoundZonesInfo> groupedSoundZonesStream = audioFileInfo.getGroupedAudioFileSoundZones().stream();
                groupedSoundZonesStream.forEach(groupedSoundZone ->
                        groupedSoundZone.setAudioClipWritingResult(audioClipsGenerator.generateGroupSoundZonesAudioFile(groupedSoundZone, outputAudioClipsConfig)));
            } else {
                Stream<SingleAudioSoundZoneInfo> groupedSoundZonesStream = audioFileInfo.getSingleAudioSoundZones().stream();
                groupedSoundZonesStream.forEach(soundZone ->
                        soundZone.setAudioClipWritingResult(audioClipsGenerator.generateSingleSoundZoneAudioFile(soundZone, outputAudioClipsConfig)));
            }
        } catch (IOException | BadRequestException e) {
            System.out.println(new BadRequestException(e.getMessage()));
        } catch (Exception e) {
            System.out.println(new InternalServerErrorException(e));
        }
        // TODO get the consolidated result from AudioFileInfo through streams
        return null;
    }

    protected void validateAudioFileLocationInfo(AudioFileLocation audioFileLocation) {
        Path audioFileNamePath = Paths.get(audioFileLocation.getAudioFileName());
        Path outputAudioClipsPath = Paths.get(audioFileLocation.getOutputAudioClipsDirectoryPath());

        if (!Files.exists(audioFileNamePath)) throw AudioFileLocationException.audioFileDoesNotExist(audioFileNamePath);
        if (Files.isDirectory(audioFileNamePath)) throw AudioFileLocationException.audioFileShouldNotBeDirectory(audioFileNamePath);
        if (!Files.exists(outputAudioClipsPath)) throw AudioFileLocationException.outputDirectoryDoesNotExist(outputAudioClipsPath);

        Path audioFilePath = Paths.get(FilenameUtils.getPath(audioFileLocation.getAudioFileName()));
        if (!audioFilePath.equals(outputAudioClipsPath)) throw AudioFileLocationException.sameAudioFileAndOutputDirectoryLocation();
    }

}
