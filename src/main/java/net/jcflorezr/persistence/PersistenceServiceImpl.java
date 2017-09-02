package net.jcflorezr.persistence;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import net.jcflorezr.api.persistence.PersistenceService;
import net.jcflorezr.exceptions.InternalServerErrorException;
import net.jcflorezr.model.audioclips.AudioFileClip;
import net.jcflorezr.model.audioclips.AudioFileClipResult;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.audiocontent.AudioFileMetadata;
import net.jcflorezr.model.request.AudioFileBasicInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PersistenceServiceImpl implements PersistenceService {

    private static final String AUDIO_FILE_INFO_TABLE = "audio_file_info";
    private static final String AUDIO_FILE_METADATA_TABLE = "audio_file_metadata";
    private static final String AUDIO_CLIPS_TABLE = "audio_clips";
    private static final String AUDIO_CLIPS_RESULTS_TABLE = "audio_clips_results";
    private static final String AUDIO_FILE_NAME_COLUMN = "audio_file_name";
    private static final String HOURS_COLUMN = "hours";

    @Autowired
    private CassandraOperations cassandraTemplate;

    // TODO we need to implement a rollback process if one of this insertions is not successful
    @Override
    public void storeResults(AudioFileCompleteInfo audioFileCompleteInfo, List<AudioFileClipResult> audioFileClipsResults) {
        storeAudioFileBasicInfo(audioFileCompleteInfo);
        storeAudioFileMetadata(audioFileCompleteInfo.getAudioContent());
        storeAudioFileClips(audioFileCompleteInfo.getAudioClipsInfo());
        storeAudioClipsResults(audioFileClipsResults);
    }

    @Override
    public AudioFileBasicInfo retrieveAudioFileBasicInfo(String audioFileName) {
        Select query = QueryBuilder.select()
                .from(AUDIO_FILE_INFO_TABLE)
                .where(QueryBuilder.eq(AUDIO_FILE_NAME_COLUMN, audioFileName)).limit(1);
        return cassandraTemplate.selectOne(query, AudioFileBasicInfo.class);
    }

    @Override
    public AudioFileMetadata retrieveAudioMetadata(String audioFileName) {
        Select query = QueryBuilder.select()
                .from(AUDIO_FILE_METADATA_TABLE)
                .where(QueryBuilder.eq(AUDIO_FILE_NAME_COLUMN, audioFileName)).limit(1);
        return cassandraTemplate.selectOne(query, AudioFileMetadata.class);
    }

    @Override
    public List<AudioFileClip> retrieveAudioFileClips(String audioFileName) {
        Select query = QueryBuilder.select()
                .from(AUDIO_CLIPS_TABLE)
                .where(QueryBuilder.eq(AUDIO_FILE_NAME_COLUMN, audioFileName))
                .orderBy(QueryBuilder.asc(HOURS_COLUMN));
        return cassandraTemplate.select(query, AudioFileClip.class);
    }

    @Override
    public List<AudioFileClipResult> retrieveAudioFileClipsResults(String audioFileName) {
        Select query = QueryBuilder.select()
                .from(AUDIO_CLIPS_RESULTS_TABLE)
                .where(QueryBuilder.eq(AUDIO_FILE_NAME_COLUMN, audioFileName))
                .orderBy(QueryBuilder.asc(HOURS_COLUMN));
        return cassandraTemplate.select(query, AudioFileClipResult.class);
    }

    private void storeAudioFileBasicInfo(AudioFileCompleteInfo audioFileCompleteInfo) {
        AudioFileBasicInfo audioFileBasicInfo = audioFileCompleteInfo.getAudioFileBasicInfo();
        if (audioFileBasicInfo == null) {
            throw new InternalServerErrorException(new Exception("Could not store audio file info which is non-existent"));
        }
        cassandraTemplate.insert(audioFileBasicInfo);
    }

    private void storeAudioFileMetadata(AudioContent audioContent) {
        AudioFileMetadata audioFileMetadata = audioContent.getAudioFileMetadata();
        if (audioContent == null || audioFileMetadata == null) {
            throw new InternalServerErrorException(new Exception("Could not store audio file metadata which is non-existent"));
        }
        cassandraTemplate.insert(audioFileMetadata);
    }

    private void storeAudioFileClips(List<AudioFileClip> audioFileClips) {
        //TODO there is a manner to insert several rows as batch mode in a newer version
        // of spring data (1.5.6.RELEASE), this newer version introduces a new method
        // called CassandraOperations.batchOpt(), but unfortunately, this version seems
        // to be not compatible with the version 4 of the spring core.
        // WE ARE USING THE FOLLOWING LOOP WHILE THE INCOMPATIBILITY IS NOT FIXED
        audioFileClips.forEach(audioClip -> cassandraTemplate.insert(audioClip));
    }

    private void storeAudioClipsResults(List<AudioFileClipResult> audioFileClipsResults) {
        //TODO there is a manner to insert several rows as batch mode in a newer version
        // of spring data (1.5.6.RELEASE), this newer version introduces a new method
        // called CassandraOperations.batchOpt(), but unfortunately, this version seems
        // to be not compatible with the version 4 of the spring core.
        // WE ARE USING THE FOLLOWING LOOP WHILE THE INCOMPATIBILITY IS NOT FIXED
        audioFileClipsResults.forEach(audioClipResult -> cassandraTemplate.insert(audioClipResult));
    }

}
