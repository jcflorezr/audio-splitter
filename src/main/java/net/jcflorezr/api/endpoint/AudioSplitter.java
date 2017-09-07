package net.jcflorezr.api.endpoint;

import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.api.audiocontent.AudioFileInfoService;
import net.jcflorezr.exceptions.AudioFileLocationException;
import net.jcflorezr.exceptions.BadRequestException;
import net.jcflorezr.exceptions.InternalServerErrorException;
import net.jcflorezr.model.audioclips.AudioFileClipResultEntity;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.endpoint.AudioFileBasicInfoEntity;
import net.jcflorezr.model.endpoint.AudioSplitterResponse;
import net.jcflorezr.model.endpoint.SuccessResponse;
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

    protected abstract AudioSplitterResponse generateAudioClips(AudioFileBasicInfoEntity audioFileBasicInfoEntity);

    protected abstract AudioSplitterResponse generateAudioMonoClips(AudioFileBasicInfoEntity audioFileBasicInfoEntity);

    public AudioSplitterResponse generateAudioClips(AudioFileBasicInfoEntity audioFileBasicInfoEntity, AudioFormatsSupported audioFormat, boolean asMono) {
        return generateAudioClips(audioFileBasicInfoEntity, audioFormat, asMono, false, false);
    }

    public AudioSplitterResponse generateAudioClips(AudioFileBasicInfoEntity audioFileBasicInfoEntity, AudioFormatsSupported audioFormat, boolean asMono, boolean generateAudioClipsByGroup, boolean withSeparator) {
        try {
            validateAudioFileLocationInfo(audioFileBasicInfoEntity);
            AudioFileCompleteInfo audioFileCompleteInfo = audioFileInfoService.generateAudioFileInfo(audioFileBasicInfoEntity, generateAudioClipsByGroup);
            OutputAudioClipsConfig outputAudioClipsConfig = audioFileCompleteInfo.getOutputAudioClipsConfig(audioFormat, asMono, withSeparator);
            List<AudioFileClipResultEntity> audioFileClipResultEntity = audioClipsGenerator.generateAudioClip(audioFileBasicInfoEntity.getAudioFileName(), audioFileCompleteInfo, outputAudioClipsConfig, generateAudioClipsByGroup);

            // TODO this process should not be synchronous
            // perhaps we can make it asynchronous using reactive streams,
            // or apache kafka, or another tool...
            persistenceService.storeResults(audioFileCompleteInfo, audioFileClipResultEntity);

            Map<Boolean, Long> soundZonesGenerationResult = audioFileClipResultEntity.stream()
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

    private void validateAudioFileLocationInfo(AudioFileBasicInfoEntity audioFileBasicInfoEntity) {
        if (audioFileBasicInfoEntity == null) throw AudioFileLocationException.emptyAudioFileLocationObject();
        if (isBlank(audioFileBasicInfoEntity.getAudioFileName()) || isBlank(audioFileBasicInfoEntity.getOutputAudioClipsDirectoryPath())) {
            throw AudioFileLocationException.mandatoryFieldsException();
        }

        Path audioFileNamePath = Paths.get(new File(audioFileBasicInfoEntity.getAudioFileName()).getPath());
        Path outputAudioClipsPath = Paths.get(new File(audioFileBasicInfoEntity.getOutputAudioClipsDirectoryPath()).getPath());

        if (!Files.exists(audioFileNamePath)) throw AudioFileLocationException.audioFileDoesNotExist(audioFileNamePath);
        if (Files.isDirectory(audioFileNamePath)) throw AudioFileLocationException.audioFileShouldNotBeDirectory(audioFileNamePath);
        if (!Files.exists(outputAudioClipsPath)) throw AudioFileLocationException.outputDirectoryDoesNotExist(outputAudioClipsPath);

        Path audioFilePath = Paths.get(FilenameUtils.getFullPath(new File(audioFileBasicInfoEntity.getAudioFileName()).getPath()));
        if (audioFilePath.equals(outputAudioClipsPath)) throw AudioFileLocationException.sameAudioFileAndOutputDirectoryLocation();
    }

}
