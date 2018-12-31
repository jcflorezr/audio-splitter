package net.jcflorezr.dao

import com.datastax.driver.core.querybuilder.QueryBuilder
import net.jcflorezr.model.AudioFileMetadataEntity
import net.jcflorezr.model.InitialConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.selectOne
import org.springframework.stereotype.Repository

interface SourceFileDao {
    fun storeAudioFileMetadata(initialConfiguration: InitialConfiguration): AudioFileMetadataEntity
    fun retrieveAudioFileMetadata(audioFileName: String): AudioFileMetadataEntity?
}

@Repository
class SourceFileDaoImpl : SourceFileDao {

    @Autowired
    private lateinit var cassandraTemplate: CassandraOperations

    override fun storeAudioFileMetadata(initialConfiguration: InitialConfiguration): AudioFileMetadataEntity {
        val audioFileMetadata = initialConfiguration.audioFileMetadata
        val audioFileName = audioFileMetadata?.audioFileName?.takeIf { it.isNotBlank() }
            ?: initialConfiguration.audioFileLocation
        val audioFileMetadataEntity = AudioFileMetadataEntity(
            audioFileName = audioFileName,
            title = audioFileMetadata?.title,
            album = audioFileMetadata?.album,
            artist = audioFileMetadata?.artist,
            trackNumber = audioFileMetadata?.trackNumber,
            genre = audioFileMetadata?.genre,
            comments = audioFileMetadata?.comments,
            rawMetadata = audioFileMetadata?.rawMetadata,
            sampleRate = audioFileMetadata?.sampleRate,
            channels = audioFileMetadata?.channels,
            duration = audioFileMetadata?.duration,
            version = audioFileMetadata?.version,
            creator = audioFileMetadata?.creator,
            contentType = audioFileMetadata?.contentType
        )
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
