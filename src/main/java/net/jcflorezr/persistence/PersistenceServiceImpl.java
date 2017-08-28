package net.jcflorezr.persistence;

import net.jcflorezr.api.persistence.PersistenceService;
import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersistenceServiceImpl implements PersistenceService {

    /*

    DROP KEYSPACE IF EXISTS AUDIOTRANSCRIBER;

    CREATE KEYSPACE IF NOT EXISTS AUDIOTRANSCRIBER WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 2};

    USE AUDIOTRANSCRIBER;

    CREATE TABLE AUDIO_FILE_INFO (
        TITLE VARCHAR,
        ALBUM VARCHAR,
        ARTIST VARCHAR,
        TRACK_NUMBER INT,
        GENRE VARCHAR(50),
        RAW_METADATA LIST<VARCHAR>,
        AUDIO_CLIPS_OUTPUT_DIRECTORY VARCHAR,
        PRIMARY KEY (TITLE)
    );

    CREATE TABLE AUDIO_CLIPS (
        HOURS INT,
        MINUTES INT,
        SECONDS INT,
        MILLISECONDS INT,
        AUDIO_CLIP_NAME VARCHAR,
        PRIMARY KEY (HOURS, MINUTES, SECONDS, MILLISECONDS)
    );

    CREATE TABLE AUDIO_CLIP_RESULT (
        HOURS INT,
        MINUTES INT,
        SECONDS INT,
        MILLISECONDS INT,
        SUCCESS BOOLEAN,
        ERROR_MESSAGE TEXT,
        PRIMARY KEY (HOURS, MINUTES, SECONDS, MILLISECONDS)
    );

     */








    @Override
    public Object storeResults(AudioFileInfo audioFileInfo, List<AudioClipsWritingResult> audioClipsWritingResult) {
        return null;
    }

}
