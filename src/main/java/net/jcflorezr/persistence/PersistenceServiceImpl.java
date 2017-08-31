package net.jcflorezr.persistence;

import net.jcflorezr.api.persistence.PersistenceService;
import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.request.AudioFileLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersistenceServiceImpl implements PersistenceService {

    @Autowired
    private CassandraOperations cassandraTemplate;

    @Override
    public Object storeResults(AudioFileLocation audioFileLocation, AudioFileInfo audioFileInfo, List<AudioClipsWritingResult> audioClipsWritingResult) {




        return null;
    }

}
