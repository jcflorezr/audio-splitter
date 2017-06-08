package net.jcflorezr.persistence;

import net.jcflorezr.api.persistence.PersistenceService;
import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersistenceServiceImpl implements PersistenceService {

    @Override
    public Object storeResults(AudioFileInfo audioFileInfo, List<AudioClipsWritingResult> audioClipsWritingResult) {
        return null;
    }

}
