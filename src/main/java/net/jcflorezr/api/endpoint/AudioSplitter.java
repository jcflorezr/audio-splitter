package net.jcflorezr.api.endpoint;

import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.api.audiofileinfo.AudioFileInfoService;
import net.jcflorezr.model.audioclips.AudioClipInfo;
import net.jcflorezr.model.response.AudioSplitterResponse;
import net.jcflorezr.audioclips.AudioClipsGeneratorImpl;
import net.jcflorezr.audiofileinfo.AudioFileInfoServiceImpl;
import net.jcflorezr.exceptions.AudioFileLocationException;
import net.jcflorezr.exceptions.BadRequestException;
import net.jcflorezr.exceptions.InternalServerErrorException;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.request.AudioFileLocation;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.response.ErrorResponse;
import net.jcflorezr.model.response.SuccessResponse;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AudioSplitter {

    protected AudioFileInfoService audioFileInfoService = new AudioFileInfoServiceImpl();
    protected AudioClipsGenerator audioClipsGenerator = new AudioClipsGeneratorImpl();

    protected abstract AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation);

    protected abstract AudioSplitterResponse generateAudioMonoClips(AudioFileLocation audioFileLocation);
    
    public AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation, AudioFormatsSupported audioFormat, boolean asMono, boolean generateAudioClipsByGroup) {
        ErrorResponse errorResponse;
        try {
            validateAudioFileLocationInfo(audioFileLocation);
            AudioFileInfo audioFileInfo = audioFileInfoService.generateAudioFileInfo(audioFileLocation, generateAudioClipsByGroup);
            OutputAudioClipsConfig outputAudioClipsConfig = audioFileInfo.getOutputAudioClipsConfig(audioFormat, asMono);
            List<? extends AudioClipInfo> audioClipsInfo =
                    generateAudioClipsByGroup ? audioFileInfo.getGroupedAudioFileSoundZones() : audioFileInfo.getSingleAudioSoundZones();
            audioClipsInfo.stream().forEach(groupedSoundZone ->
                    groupedSoundZone.setAudioClipWritingResult(audioClipsGenerator.generateAudioClip(groupedSoundZone, outputAudioClipsConfig, generateAudioClipsByGroup)));
            Map<Boolean, Long> soundZonesGenerationResult = audioClipsInfo.stream()
                    .collect(Collectors.partitioningBy(clipInfo -> ((AudioClipInfo)clipInfo).getAudioClipWritingResult().isSuccess(), Collectors.counting()));
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
