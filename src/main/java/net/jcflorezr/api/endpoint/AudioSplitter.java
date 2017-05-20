package net.jcflorezr.api.endpoint;

import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.api.audiofileinfo.AudioFileInfoService;
import net.jcflorezr.exceptions.AudioFileLocationException;
import net.jcflorezr.exceptions.BadRequestException;
import net.jcflorezr.exceptions.InternalServerErrorException;
import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.request.AudioFileLocation;
import net.jcflorezr.model.response.AudioSplitterResponse;
import net.jcflorezr.model.response.SuccessResponse;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public abstract class AudioSplitter {

    @Autowired
    private AudioFileInfoService audioFileInfoService;

    @Autowired
    private AudioClipsGenerator audioClipsGenerator;

    protected abstract AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation);

    protected abstract AudioSplitterResponse generateAudioMonoClips(AudioFileLocation audioFileLocation);

    public AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation, AudioFormatsSupported audioFormat, boolean asMono) {
        return generateAudioClips(audioFileLocation, audioFormat, asMono, false, false);
    }

    public AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation, AudioFormatsSupported audioFormat, boolean asMono, boolean generateAudioClipsByGroup, boolean withSeparator) {
        try {
            validateAudioFileLocationInfo(audioFileLocation);
            AudioFileInfo audioFileInfo = audioFileInfoService.generateAudioFileInfo(audioFileLocation, generateAudioClipsByGroup);
            OutputAudioClipsConfig outputAudioClipsConfig = audioFileInfo.getOutputAudioClipsConfig(audioFormat, asMono, withSeparator);
            List<AudioClipsWritingResult> audioClipsWritingResult = audioClipsGenerator.generateAudioClip(audioFileInfo, outputAudioClipsConfig, generateAudioClipsByGroup);

            // TODO Do I really need to store this object inside AudioFileInfo?
            //audioFileInfo.setAudioClipsWritingResult(audioClipsWritingResult);

            Map<Boolean, Long> soundZonesGenerationResult = audioClipsWritingResult.stream()
                    .collect(Collectors.partitioningBy(clipInfo -> clipInfo.getAudioClipWritingResult().isSuccess(), Collectors.counting()));
            return new SuccessResponse(soundZonesGenerationResult.get(true), soundZonesGenerationResult.get(false));
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage());
        } catch (BadRequestException | InternalServerErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    private void validateAudioFileLocationInfo(AudioFileLocation audioFileLocation) {
        if (audioFileLocation == null) throw AudioFileLocationException.emptyAudioFileLocationObject();
        if (isBlank(audioFileLocation.getAudioFileName()) || isBlank(audioFileLocation.getOutputAudioClipsDirectoryPath())) {
            throw AudioFileLocationException.mandatoryFieldsException();
        }

        Path audioFileNamePath = Paths.get(audioFileLocation.getAudioFileName());
        Path outputAudioClipsPath = Paths.get(audioFileLocation.getOutputAudioClipsDirectoryPath());

        if (!Files.exists(audioFileNamePath)) throw AudioFileLocationException.audioFileDoesNotExist(audioFileNamePath);
        if (Files.isDirectory(audioFileNamePath)) throw AudioFileLocationException.audioFileShouldNotBeDirectory(audioFileNamePath);
        if (!Files.exists(outputAudioClipsPath)) throw AudioFileLocationException.outputDirectoryDoesNotExist(outputAudioClipsPath);

        Path audioFilePath = Paths.get(FilenameUtils.getFullPath(audioFileLocation.getAudioFileName()));
        if (audioFilePath.equals(outputAudioClipsPath)) throw AudioFileLocationException.sameAudioFileAndOutputDirectoryLocation();
    }

}
