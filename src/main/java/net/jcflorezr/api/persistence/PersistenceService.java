package net.jcflorezr.api.persistence;

import net.jcflorezr.model.audioclips.AudioFileClip;
import net.jcflorezr.model.audioclips.AudioFileClipResult;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.audiocontent.AudioFileMetadata;
import net.jcflorezr.model.request.AudioFileBasicInfo;

import java.util.List;

public interface PersistenceService {

    void storeResults(AudioFileCompleteInfo audioFileCompleteInfo, List<AudioFileClipResult> audioFileClipResult);

    AudioFileBasicInfo retrieveAudioFileBasicInfo(String audioFileName);

    AudioFileMetadata retrieveAudioMetadata(String audioFileName);

    List<AudioFileClip> retrieveAudioFileClips(String audioFileName);

    List<AudioFileClipResult> retrieveAudioFileClipsResults(String audioFileName);
}
