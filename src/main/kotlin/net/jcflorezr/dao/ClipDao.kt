package net.jcflorezr.dao

import com.datastax.driver.core.querybuilder.QueryBuilder
import mu.KotlinLogging
import net.jcflorezr.model.AudioClipInfoEntity
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.GroupedAudioClipInfoEntity
import net.jcflorezr.util.AudioUtils.tenthsSecondsFormat
import net.jcflorezr.util.PropsUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

interface AudioClipDao {
    suspend fun storeAudioClipInfo(audioClipInfo: AudioClipInfo)
    suspend fun persistAudioClipInfo(audioClipInfo: AudioClipInfo)
    suspend fun persistGroupedAudioClipInfo(firstAudioClipInfo: AudioClipInfo, lastAudioClipInfo: AudioClipInfo)
    fun retrieveAllAudioClipsInfo(key: String): List<AudioClipInfo>
    fun retrieveAllAudioClipsInfoPersisted(audioFileName: String): List<AudioClipInfoEntity>
    fun retrieveAllGroupedAudioClipsInfoPersisted(audioFileName: String): List<GroupedAudioClipInfoEntity>
    suspend fun removeAudioClipInfoFromRange(key: String, min: Double, max: Double): Long
}

@Repository
class AudioClipDaoImpl : AudioClipDao {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var audioClipTemplate: RedisTemplate<String, AudioClipInfo>
    @Autowired
    private lateinit var cassandraTemplate: CassandraOperations

    private val logger = KotlinLogging.logger { }

    override suspend fun storeAudioClipInfo(
        audioClipInfo: AudioClipInfo
    ) {
        audioClipTemplate
            .boundZSetOps("${audioClipInfo.entityName}_${audioClipInfo.audioFileName}")
            .add(audioClipInfo, audioClipInfo.index.toDouble())!!
        persistAudioClipInfo(audioClipInfo)
    }

    override suspend fun persistAudioClipInfo(audioClipInfo: AudioClipInfo) {
        val transactionId = propsUtils.getTransactionId(audioClipInfo.audioFileName)
        logger.info { "[$transactionId][5][clip-info] " +
            "Persisting Clip Info ${audioClipInfo.consecutive to audioClipInfo.audioClipName}. " +
            "Start: ${audioClipInfo.initialPositionInSeconds} - " +
            "end: ${audioClipInfo.endPositionInSeconds}" }
        cassandraTemplate.insert(AudioClipInfoEntity(audioClipInfo = audioClipInfo))
    }

    override suspend fun persistGroupedAudioClipInfo(
        firstAudioClipInfo: AudioClipInfo,
        lastAudioClipInfo: AudioClipInfo
    ) {
        val transactionId = propsUtils.getTransactionId(firstAudioClipInfo.audioFileName)
        logger.info { "[$transactionId][5][clip-info] " +
            "Persisting Grouped Clip Info ${firstAudioClipInfo.consecutive to firstAudioClipInfo.audioClipName}. " +
            "Start: ${firstAudioClipInfo.initialPositionInSeconds} - " +
            "end: ${lastAudioClipInfo.endPositionInSeconds}" }
        cassandraTemplate.insert(GroupedAudioClipInfoEntity(
            firstAudioClipInfo = firstAudioClipInfo,
            lastAudioClipInfo = lastAudioClipInfo
        ))
    }

    override fun retrieveAllAudioClipsInfo(
        key: String
    ) = audioClipTemplate
        .boundZSetOps(key)
        .range(0, -1)?.toList() ?: ArrayList()

    override fun retrieveAllAudioClipsInfoPersisted(
        audioFileName: String
    ): List<AudioClipInfoEntity> {
        val query = QueryBuilder
            .select()
            .from("audio_clip_info")
            .where(QueryBuilder.eq("audio_file_name", audioFileName))
        return cassandraTemplate.select(query, AudioClipInfoEntity::class.java)
    }

    override fun retrieveAllGroupedAudioClipsInfoPersisted(
        audioFileName: String
    ): List<GroupedAudioClipInfoEntity> {
        val query = QueryBuilder
            .select()
            .from("grouped_audio_clip_info")
            .where(QueryBuilder.eq("audio_file_name", audioFileName))
        return cassandraTemplate.select(query, GroupedAudioClipInfoEntity::class.java)
    }

    override suspend fun removeAudioClipInfoFromRange(
        key: String,
        min: Double,
        max: Double
    ): Long {
        val transactionId = propsUtils.getTransactionId(sourceAudioFileName = key.substringAfter("_"))
        logger.info { "[$transactionId][5][clip-info] Removing clip info previously cached. From: ${tenthsSecondsFormat(min)} - " +
            "to: ${tenthsSecondsFormat(max)}" }
        return audioClipTemplate
            .boundZSetOps(key)
            .removeRangeByScore(tenthsSecondsFormat(min), tenthsSecondsFormat(max)) ?: 0L
    }
}
