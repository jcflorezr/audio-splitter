package net.jcflorezr.api.endpoint;

import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.api.model.AudioSoundZoneInfo;
import net.jcflorezr.api.model.response.AudioSplitterResponse;
import net.jcflorezr.audioclips.AudioClipsGeneratorImpl;
import net.jcflorezr.audiofileinfo.AudioFileInfoService;
import net.jcflorezr.exceptions.AudioFileLocationException;
import net.jcflorezr.exceptions.BadRequestException;
import net.jcflorezr.exceptions.InternalServerErrorException;
import net.jcflorezr.model.AudioFileInfo;
import net.jcflorezr.model.AudioFileLocation;
import net.jcflorezr.model.OutputAudioClipsConfig;
import net.jcflorezr.model.response.ErrorResponse;
import net.jcflorezr.model.response.SuccessResponse;
import biz.source_code.dsp.util.AudioFormatsSupported;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AudioSplitter {

    protected AudioFileInfoService audioFileInfoService = new AudioFileInfoService();
    protected AudioClipsGenerator audioClipsGenerator = new AudioClipsGeneratorImpl();

    protected abstract AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation);

    protected abstract AudioSplitterResponse generateAudioMonoClips(AudioFileLocation audioFileLocation);
    
    public AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation, AudioFormatsSupported audioFormat, boolean asMono, boolean generateAudioClipsByGroup) {
        ErrorResponse errorResponse;
        try {
            validateAudioFileLocationInfo(audioFileLocation);
            AudioFileInfo audioFileInfo = audioFileInfoService.generateAudioFileInfo(audioFileLocation, generateAudioClipsByGroup);
            OutputAudioClipsConfig outputAudioClipsConfig = audioFileInfo.getOutputAudioClipsConfig(audioFormat, asMono);
            List<? extends AudioSoundZoneInfo> soundZonesInfo =
                    generateAudioClipsByGroup ? audioFileInfo.getGroupedAudioFileSoundZones() : audioFileInfo.getSingleAudioSoundZones();
            soundZonesInfo.stream().forEach(groupedSoundZone ->
                    groupedSoundZone.setAudioClipWritingResult(audioClipsGenerator.generateSoundZoneAudioFile(groupedSoundZone, outputAudioClipsConfig, generateAudioClipsByGroup)));
            Map<Boolean, Long> soundZonesGenerationResult = soundZonesInfo.stream()
                    .collect(Collectors.partitioningBy(soundInfo -> ((AudioSoundZoneInfo)soundInfo).getAudioClipWritingResult().isSuccess(), Collectors.counting()));
            return new SuccessResponse(soundZonesGenerationResult.get(true), soundZonesGenerationResult.get(false));
        } catch (IOException e) {
            errorResponse = new ErrorResponse(new BadRequestException(e.getMessage()));
        } catch (BadRequestException | InternalServerErrorException e) {
            errorResponse = new ErrorResponse(e);
        } catch (Exception e) {
            errorResponse = new ErrorResponse(new InternalServerErrorException(e));
        }
        return errorResponse;
    }

    private void validateAudioFileLocationInfo(AudioFileLocation audioFileLocation) {
        Path audioFileNamePath = Paths.get(audioFileLocation.getAudioFileName());
        Path outputAudioClipsPath = Paths.get(audioFileLocation.getOutputAudioClipsDirectoryPath());

        if (!Files.exists(audioFileNamePath)) throw AudioFileLocationException.audioFileDoesNotExist(audioFileNamePath);
        if (Files.isDirectory(audioFileNamePath)) throw AudioFileLocationException.audioFileShouldNotBeDirectory(audioFileNamePath);
        if (!Files.exists(outputAudioClipsPath)) throw AudioFileLocationException.outputDirectoryDoesNotExist(outputAudioClipsPath);

        Path audioFilePath = Paths.get(FilenameUtils.getPath(audioFileLocation.getAudioFileName()));
        if (audioFilePath.equals(outputAudioClipsPath)) throw AudioFileLocationException.sameAudioFileAndOutputDirectoryLocation();
    }

}
