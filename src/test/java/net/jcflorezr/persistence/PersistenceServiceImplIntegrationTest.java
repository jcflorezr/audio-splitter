package net.jcflorezr.persistence;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.api.persistence.PersistenceService;
import net.jcflorezr.config.CassandraConfig;
import net.jcflorezr.config.RootConfig;
import net.jcflorezr.model.audioclips.AudioFileClipEntity;
import net.jcflorezr.model.audioclips.AudioFileClipResultEntity;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.audiocontent.AudioFileMetadataEntity;
import net.jcflorezr.model.endpoint.AudioFileBasicInfoEntity;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.cql.CqlIdentifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RootConfig.class, CassandraConfig.class})
public class PersistenceServiceImplIntegrationTest {

    private static final String CASSANDRA_CONTACT_POINTS = "127.0.0.1";
    private static final int CASSANDRA_PORT = 9142;
    private static final String KEYSPACE_CREATION_QUERY = "CREATE KEYSPACE IF NOT EXISTS AUDIO_TRANSCRIBER " + "WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': '3' };";
    private static final String KEYSPACE_ACTIVATE_QUERY = "USE AUDIO_TRANSCRIBER;";

    private static final String AUDIO_FILE_INFO_TABLE = "AUDIO_FILE_INFO";
    private static final String AUDIO_FILE_METADATA_TABLE = "AUDIO_FILE_METADATA";
    private static final String AUDIO_CLIPS_TABLE = "AUDIO_CLIPS";
    private static final String AUDIO_CLIPS_RESULTS_TABLE = "AUDIO_CLIPS_RESULTS";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String AUDIO_CLIPS_WRITING_RESULT = "/api/endpoint/audio-clips-writing-result.json";
    private static final String AUDIO_CLIPS_INFO = "/audioclips/audio-clips-info-min.json";
    private static final String MP3_AUDIO_METADATA_JSON_FILE = "/audiocontent/mp3-audio-metadata.json";

    private Class<? extends CassandraDaoIntegrationTest> thisClass;

    @Autowired
    private CassandraAdminOperations adminTemplate;
    @Autowired
    private PersistenceService persistenceService;

    @BeforeClass
    public static void startCassandraEmbedded() throws InterruptedException, TTransportException, ConfigurationException, IOException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        final Cluster cluster = Cluster.builder().addContactPoints(CASSANDRA_CONTACT_POINTS).withPort(CASSANDRA_PORT).build();
        final Session session = cluster.connect();
        session.execute(KEYSPACE_CREATION_QUERY);
        session.execute(KEYSPACE_ACTIVATE_QUERY);
        // Giving some time while the keyspace is created
        Thread.sleep(1000);
    }

    @Before
    public void setUp() throws Exception {
        //thisClass = this.getClass();
    }

    private void createTable(String tableName, Class aClass) {
        adminTemplate.createTable(true, CqlIdentifier.cqlId(tableName), aClass, new HashMap<>());
    }

    // TODO unsuccessful test cases are missing, but first it is needed to
    // implement a rollback process
    @Test
    public void storeResultsAndRetrieveThemAll() throws Exception {
        String audioFileName = "/path/to-find/audio-file";
        String outputAudioClipsDirectoryPath = "/any-path/to-store/the-audio-clips";

        AudioFileBasicInfoEntity dummyAudioFileBasicInfoEntity = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);
        List<AudioFileClipEntity> dummyAudioFileClipEntities = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_CLIPS_INFO), new TypeReference<List<AudioFileClipEntity>>(){});
        AudioFileMetadataEntity dummyAudioFileMetadataEntity = MAPPER.readValue(thisClass.getResourceAsStream(MP3_AUDIO_METADATA_JSON_FILE), AudioFileMetadataEntity.class);
        AudioContent dummyAudioContent = new AudioContent(null, dummyAudioFileMetadataEntity);

        AudioFileCompleteInfo dummyAudioFileCompleteInfo = new AudioFileCompleteInfo(dummyAudioFileBasicInfoEntity, dummyAudioFileClipEntities, dummyAudioContent);
        List<AudioFileClipResultEntity> dummyAudioFileClipsResults = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_CLIPS_WRITING_RESULT), new TypeReference<List<AudioFileClipResultEntity>>(){});

        createTable(AUDIO_FILE_INFO_TABLE, AudioFileBasicInfoEntity.class);
        createTable(AUDIO_FILE_METADATA_TABLE, AudioFileMetadataEntity.class);
        createTable(AUDIO_CLIPS_TABLE, AudioFileClipEntity.class);
        createTable(AUDIO_CLIPS_RESULTS_TABLE, AudioFileClipResultEntity.class);
        persistenceService.storeResults(dummyAudioFileCompleteInfo, dummyAudioFileClipsResults);

        AudioFileBasicInfoEntity actualAudioFileBasicInfoEntity = persistenceService.retrieveAudioFileBasicInfo(audioFileName);
        assertThat(actualAudioFileBasicInfoEntity, equalTo(dummyAudioFileBasicInfoEntity));

        AudioFileMetadataEntity actualAudioFileMetadataEntity = persistenceService.retrieveAudioMetadata(audioFileName);
        assertThat(actualAudioFileMetadataEntity, equalTo(dummyAudioFileMetadataEntity));

        List<AudioFileClipEntity> actualAudioFileClipEntities = persistenceService.retrieveAudioFileClips(audioFileName);
        assertThat(actualAudioFileClipEntities, equalTo(dummyAudioFileClipEntities));

        List<AudioFileClipResultEntity> actualAudioFileClipsResults = persistenceService.retrieveAudioFileClipsResults(audioFileName);
        assertThat(actualAudioFileClipsResults, equalTo(dummyAudioFileClipsResults));

        dropTable(AUDIO_FILE_INFO_TABLE);
    }

    private void dropTable(String tableName) {
        adminTemplate.dropTable(CqlIdentifier.cqlId(tableName));
    }

    @AfterClass
    public static void stopCassandraEmbedded() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    private AudioFileBasicInfoEntity createDummyAudioFileLocation (String audioFileName, String outputAudioClipsDirectoryPath) {
        return new AudioFileBasicInfoEntity(audioFileName, outputAudioClipsDirectoryPath);
    }

}