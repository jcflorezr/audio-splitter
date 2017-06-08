package net.jcflorezr.api.persistence;

import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audiocontent.AudioFileInfo;

import java.util.List;

public interface PersistenceService {

    Object storeResults(AudioFileInfo audioFileInfo, List<AudioClipsWritingResult> audioClipsWritingResult);

}
