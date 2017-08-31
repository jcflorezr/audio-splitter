package net.jcflorezr.api.persistence;

import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.request.AudioFileLocation;

import java.util.List;

public interface PersistenceService {

    Object storeResults(AudioFileLocation audioFileLocation, AudioFileInfo audioFileInfo, List<AudioClipsWritingResult> audioClipsWritingResult);

}
