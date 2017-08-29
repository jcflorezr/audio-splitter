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
        AUDIO_FILE_NAME VARCHAR,
        AUDIO_CLIPS_OUTPUT_DIRECTORY VARCHAR,
        PRIMARY KEY (AUDIO_FILE_NAME)
    );

    CREATE TABLE AUDIO_FILE_METADATA (
        AUDIO_FILE_NAME VARCHAR,
        TITLE VARCHAR,
        ALBUM VARCHAR,
        ARTIST VARCHAR,
        TRACK_NUMBER INT,
        GENRE VARCHAR,
        RAW_METADATA LIST<VARCHAR>,
        PRIMARY KEY (AUDIO_FILE_NAME)
    );

    CREATE TABLE AUDIO_CLIPS (
        AUDIO_FILE_NAME VARCHAR,
        HOURS INT,
        MINUTES INT,
        SECONDS INT,
        MILLISECONDS INT,
        AUDIO_CLIP_NAME VARCHAR,
        GROUP_NUMBER INT,
        START_POSITION INT,
        START_POSITION_IN_SECONDS FLOAT,
        END_POSITION INT,
        END_POSITION_IN_SECONDS FLOAT,
        DURATION_IN_SECONDS FLOAT,
        PRIMARY KEY (AUDIO_FILE_NAME, HOURS, MINUTES, SECONDS, MILLISECONDS)
    );

    CREATE TABLE AUDIO_CLIP_RESULT (
        AUDIO_FILE_NAME VARCHAR,
        HOURS INT,
        MINUTES INT,
        SECONDS INT,
        MILLISECONDS INT,
        SUCCESS BOOLEAN,
        ERROR_MESSAGE TEXT,
        PRIMARY KEY (AUDIO_FILE_NAME, HOURS, MINUTES, SECONDS, MILLISECONDS)
    );

     */








    @Override
    public Object storeResults(AudioFileInfo audioFileInfo, List<AudioClipsWritingResult> audioClipsWritingResult) {
        return null;
    }

}
