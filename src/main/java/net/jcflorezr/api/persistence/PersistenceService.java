package net.jcflorezr.api.persistence;

import net.jcflorezr.model.audioclips.AudioFileClipEntity;
import net.jcflorezr.model.audioclips.AudioFileClipResultEntity;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.audiocontent.AudioFileMetadataEntity;
import net.jcflorezr.model.endpoint.AudioFileBasicInfoEntity;

import java.util.List;

public interface PersistenceService {

    void storeResults(AudioFileCompleteInfo audioFileCompleteInfo, List<AudioFileClipResultEntity> audioFileClipResultEntity);

    AudioFileBasicInfoEntity retrieveAudioFileBasicInfo(String audioFileName);

    AudioFileMetadataEntity retrieveAudioMetadata(String audioFileName);

    List<AudioFileClipEntity> retrieveAudioFileClips(String audioFileName);

    List<AudioFileClipResultEntity> retrieveAudioFileClipsResults(String audioFileName);
}
