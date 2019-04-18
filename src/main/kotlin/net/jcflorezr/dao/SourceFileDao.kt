package net.jcflorezr.dao

import com.datastax.driver.core.querybuilder.QueryBuilder
import mu.KotlinLogging
import net.jcflorezr.model.AudioFileMetadataEntity
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.util.PropsUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.selectOne
import org.springframework.stereotype.Repository

interface SourceFileDao {
    fun persistAudioFileMetadata(initialConfiguration: InitialConfiguration): AudioFileMetadataEntity
    fun retrieveAudioFileMetadata(audioFileName: String): AudioFileMetadataEntity?
}

@Repository
class SourceFileDaoImpl : SourceFileDao {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var cassandraTemplate: CassandraOperations

    private val logger = KotlinLogging.logger { }

    override fun persistAudioFileMetadata(initialConfiguration: InitialConfiguration): AudioFileMetadataEntity {
        val audioFileMetadata = initialConfiguration.audioFileMetadata
        val audioFileName = audioFileMetadata!!.audioFileName
        val audioFileMetadataEntity = AudioFileMetadataEntity(
            audioFileName = audioFileName,
            title = audioFileMetadata.title,
            album = audioFileMetadata.album,
            artist = audioFileMetadata.artist,
            trackNumber = audioFileMetadata.trackNumber,
            genre = audioFileMetadata.genre,
            comments = audioFileMetadata.comments,
            sampleRate = audioFileMetadata.sampleRate,
            channels = audioFileMetadata.channels,
            duration = audioFileMetadata.duration
        )
        logger.info { "[${propsUtils.getTransactionId(audioFileMetadataEntity.audioFileName)}][1][entry-point] " +
            "Persisting audio metadata for ${audioFileMetadataEntity.audioFileName}." }
        return cassandraTemplate.insert(audioFileMetadataEntity)
    }

    override fun retrieveAudioFileMetadata(audioFileName: String): AudioFileMetadataEntity? {
        val query = QueryBuilder
            .select()
            .from("audio_file_metadata")
            .where(QueryBuilder.eq("audio_file_name", audioFileName))
        return cassandraTemplate.selectOne(query, AudioFileMetadataEntity::class)
    }
}
