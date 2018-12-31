package net.jcflorezr.dao;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import net.jcflorezr.api.persistence.PersistenceService;
import net.jcflorezr.exceptions.InternalServerErrorException;
import net.jcflorezr.model.audioclips.AudioFileClipEntity;
import net.jcflorezr.model.audioclips.AudioFileClipResultEntity;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.audiocontent.AudioFileMetadataEntity;
import net.jcflorezr.model.endpoint.AudioFileBasicInfoEntity;
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
    public void storeResults(AudioFileCompleteInfo audioFileCompleteInfo, List<AudioFileClipResultEntity> audioFileClipsResults) {
        storeAudioFileBasicInfo(audioFileCompleteInfo);
        storeAudioFileMetadata(audioFileCompleteInfo.getAudioContent());
        storeAudioFileClips(audioFileCompleteInfo.getAudioClipsInfo());
        storeAudioClipsResults(audioFileClipsResults);
    }

    @Override
    public AudioFileBasicInfoEntity retrieveAudioFileBasicInfo(String audioFileName) {
        Select query = QueryBuilder.select()
                .from(AUDIO_FILE_INFO_TABLE)
                .where(QueryBuilder.eq(AUDIO_FILE_NAME_COLUMN, audioFileName)).limit(1);
        return cassandraTemplate.selectOne(query, AudioFileBasicInfoEntity.class);
    }

    @Override
    public AudioFileMetadataEntity retrieveAudioMetadata(String audioFileName) {
        Select query = QueryBuilder.select()
                .from(AUDIO_FILE_METADATA_TABLE)
                .where(QueryBuilder.eq(AUDIO_FILE_NAME_COLUMN, audioFileName)).limit(1);
        return cassandraTemplate.selectOne(query, AudioFileMetadataEntity.class);
    }

    @Override
    public List<AudioFileClipEntity> retrieveAudioFileClips(String audioFileName) {
        Select query = QueryBuilder.select()
                .from(AUDIO_CLIPS_TABLE)
                .where(QueryBuilder.eq(AUDIO_FILE_NAME_COLUMN, audioFileName))
                .orderBy(QueryBuilder.asc(HOURS_COLUMN));
        return cassandraTemplate.select(query, AudioFileClipEntity.class);
    }

    @Override
    public List<AudioFileClipResultEntity> retrieveAudioFileClipsResults(String audioFileName) {
        Select query = QueryBuilder.select()
                .from(AUDIO_CLIPS_RESULTS_TABLE)
                .where(QueryBuilder.eq(AUDIO_FILE_NAME_COLUMN, audioFileName))
                .orderBy(QueryBuilder.asc(HOURS_COLUMN));
        return cassandraTemplate.select(query, AudioFileClipResultEntity.class);
    }

    private void storeAudioFileBasicInfo(AudioFileCompleteInfo audioFileCompleteInfo) {
        AudioFileBasicInfoEntity audioFileBasicInfoEntity = audioFileCompleteInfo.getAudioFileBasicInfoEntity();
        if (audioFileBasicInfoEntity == null) {
            throw new InternalServerErrorException(new Exception("Could not store audio file info which is non-existent"));
        }
        cassandraTemplate.insert(audioFileBasicInfoEntity);
    }

    private void storeAudioFileMetadata(AudioContent audioContent) {
        AudioFileMetadataEntity audioFileMetadataEntity = audioContent.getAudioFileMetadataEntity();
        if (audioContent == null || audioFileMetadataEntity == null) {
            throw new InternalServerErrorException(new Exception("Could not store audio file metadata which is non-existent"));
        }
        cassandraTemplate.insert(audioFileMetadataEntity);
    }

    private void storeAudioFileClips(List<AudioFileClipEntity> audioFileClipEntities) {
        //TODO there is a manner to insert several rows as batch mode in a newer version
        // of spring data (1.5.6.RELEASE), this newer version introduces a new method
        // called CassandraOperations.batchOpt(), but unfortunately, this version seems
        // to be not compatible with the version 4 of the spring core.
        // WE ARE USING THE FOLLOWING LOOP WHILE THE INCOMPATIBILITY IS NOT FIXED
        audioFileClipEntities.forEach(audioClip -> cassandraTemplate.insert(audioClip));
    }

    private void storeAudioClipsResults(List<AudioFileClipResultEntity> audioFileClipsResults) {
        //TODO there is a manner to insert several rows as batch mode in a newer version
        // of spring data (1.5.6.RELEASE), this newer version introduces a new method
        // called CassandraOperations.batchOpt(), but unfortunately, this version seems
        // to be not compatible with the version 4 of the spring core.
        // WE ARE USING THE FOLLOWING LOOP WHILE THE INCOMPATIBILITY IS NOT FIXED
        audioFileClipsResults.forEach(audioClipResult -> cassandraTemplate.insert(audioClipResult));
    }

}
