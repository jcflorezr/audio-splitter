package net.jcflorezr.api.endpoint;

import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.api.audiofileinfo.AudioFileInfoService;
import net.jcflorezr.exceptions.AudioFileLocationException;
import net.jcflorezr.exceptions.BadRequestException;
import net.jcflorezr.exceptions.InternalServerErrorException;
import net.jcflorezr.model.audioclips.AudioFileClipResult;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.request.AudioFileBasicInfo;
import net.jcflorezr.model.response.AudioSplitterResponse;
import net.jcflorezr.model.response.SuccessResponse;
import net.jcflorezr.api.persistence.PersistenceService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
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

    @Autowired
    private PersistenceService persistenceService;

    protected abstract AudioSplitterResponse generateAudioClips(AudioFileBasicInfo audioFileBasicInfo);

    protected abstract AudioSplitterResponse generateAudioMonoClips(AudioFileBasicInfo audioFileBasicInfo);

    public AudioSplitterResponse generateAudioClips(AudioFileBasicInfo audioFileBasicInfo, AudioFormatsSupported audioFormat, boolean asMono) {
        return generateAudioClips(audioFileBasicInfo, audioFormat, asMono, false, false);
    }

    public AudioSplitterResponse generateAudioClips(AudioFileBasicInfo audioFileBasicInfo, AudioFormatsSupported audioFormat, boolean asMono, boolean generateAudioClipsByGroup, boolean withSeparator) {
        try {
            validateAudioFileLocationInfo(audioFileBasicInfo);
            AudioFileCompleteInfo audioFileCompleteInfo = audioFileInfoService.generateAudioFileInfo(audioFileBasicInfo, generateAudioClipsByGroup);
            OutputAudioClipsConfig outputAudioClipsConfig = audioFileCompleteInfo.getOutputAudioClipsConfig(audioFormat, asMono, withSeparator);
            List<AudioFileClipResult> audioFileClipResult = audioClipsGenerator.generateAudioClip(audioFileBasicInfo.getAudioFileName(), audioFileCompleteInfo, outputAudioClipsConfig, generateAudioClipsByGroup);

            // TODO this process should not be synchronous
            // perhaps we can make it asynchronous using reactive streams,
            // or apache kafka, or another tool...
            persistenceService.storeResults(audioFileCompleteInfo, audioFileClipResult);

            Map<Boolean, Long> soundZonesGenerationResult = audioFileClipResult.stream()
                    .collect(Collectors.partitioningBy(clipInfo -> clipInfo.isSuccess(), Collectors.counting()));
            return new SuccessResponse(soundZonesGenerationResult.get(true), soundZonesGenerationResult.get(false));
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage());
        } catch (BadRequestException | InternalServerErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    private void validateAudioFileLocationInfo(AudioFileBasicInfo audioFileBasicInfo) {
        if (audioFileBasicInfo == null) throw AudioFileLocationException.emptyAudioFileLocationObject();
        if (isBlank(audioFileBasicInfo.getAudioFileName()) || isBlank(audioFileBasicInfo.getOutputAudioClipsDirectoryPath())) {
            throw AudioFileLocationException.mandatoryFieldsException();
        }

        Path audioFileNamePath = Paths.get(new File(audioFileBasicInfo.getAudioFileName()).getPath());
        Path outputAudioClipsPath = Paths.get(new File(audioFileBasicInfo.getOutputAudioClipsDirectoryPath()).getPath());

        if (!Files.exists(audioFileNamePath)) throw AudioFileLocationException.audioFileDoesNotExist(audioFileNamePath);
        if (Files.isDirectory(audioFileNamePath)) throw AudioFileLocationException.audioFileShouldNotBeDirectory(audioFileNamePath);
        if (!Files.exists(outputAudioClipsPath)) throw AudioFileLocationException.outputDirectoryDoesNotExist(outputAudioClipsPath);

        Path audioFilePath = Paths.get(FilenameUtils.getFullPath(new File(audioFileBasicInfo.getAudioFileName()).getPath()));
        if (audioFilePath.equals(outputAudioClipsPath)) throw AudioFileLocationException.sameAudioFileAndOutputDirectoryLocation();
    }

}
