package net.jcflorezr.dao

import com.datastax.driver.core.querybuilder.QueryBuilder
import mu.KotlinLogging
import net.jcflorezr.model.AudioPartEntity
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.model.AudioSignalRmsEntity
import net.jcflorezr.model.AudioSignalRmsInfo
import net.jcflorezr.util.AudioUtils.tenthsSecondsFormat
import net.jcflorezr.util.PropsUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.selectOne
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
interface AudioSignalDao {
    fun storeAudioSignal(audioSignal: AudioSignal): Boolean
    fun persistAudioSignalPart(audioSignal: AudioSignal): AudioPartEntity
    fun retrieveAudioSignalPart(audioFileName: String, index: Float): AudioPartEntity?
    fun retrieveAudioSignalsFromRange(key: String, min: Double, max: Double): List<AudioSignal>
    fun retrieveAllAudioSignals(key: String): List<AudioSignal>
    suspend fun removeAudioSignalsFromRange(key: String, min: Double, max: Double): Long
}

class AudioSignalDaoImpl : AudioSignalDao {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var audioSignalTemplate: RedisTemplate<String, AudioSignal>
    @Autowired
    private lateinit var cassandraTemplate: CassandraOperations

    private val logger = KotlinLogging.logger { }

    override fun storeAudioSignal(
        audioSignal: AudioSignal
    ): Boolean {
        logger.info { "[${propsUtils.getTransactionId(audioSignal.audioFileName)}][2][audio-signal] " +
            "Storing audio signal with index: ${audioSignal.index} for caching." }
        return audioSignalTemplate
            .boundZSetOps("${audioSignal.entityName}_${audioSignal.audioFileName}")
            .add(audioSignal, audioSignal.index.toDouble())!!
    }

    override fun persistAudioSignalPart(audioSignal: AudioSignal): AudioPartEntity {
        logger.info { "[${propsUtils.getTransactionId(audioSignal.audioFileName)}][2][audio-signal] " +
            "Persisting audio signal part with index from ${audioSignal.initialPositionInSeconds} - to: ${audioSignal.endPositionInSeconds}." }
        return cassandraTemplate.insert(AudioPartEntity(audioSignal = audioSignal))
    }

    override fun retrieveAudioSignalPart(audioFileName: String, index: Float): AudioPartEntity? {
        val query = QueryBuilder
            .select()
            .from("audio_part")
            .where(QueryBuilder.eq("audio_file_name", audioFileName))
            .and(QueryBuilder.eq("ind", index))
        return cassandraTemplate.selectOne(query, AudioPartEntity::class)
    }

    override fun retrieveAudioSignalsFromRange(
        key: String,
        min: Double,
        max: Double
    ) = audioSignalTemplate
        .boundZSetOps(key)
        .rangeByScore(min, max)?.toList() ?: ArrayList()

    override fun retrieveAllAudioSignals(
        key: String
    ) = audioSignalTemplate
        .boundZSetOps(key)
        .range(0, -1)?.toList() ?: ArrayList()

    override suspend fun removeAudioSignalsFromRange(
        key: String,
        min: Double,
        max: Double
    ): Long {
        val transactionId = propsUtils.getTransactionId(sourceAudioFileName = key.substringAfter("_"))
        logger.info { "[$transactionId][5][clip-info] Removing audio signals previously cached. From: $min - to: $max" }
        return audioSignalTemplate
            .boundZSetOps(key)
            .removeRangeByScore(tenthsSecondsFormat(min), tenthsSecondsFormat(max)) ?: 0L
    }
}

interface AudioSignalRmsDao {
    suspend fun storeAudioSignalsRms(audioSignalsRms: List<AudioSignalRmsInfo>)
    fun retrieveAllAudioSignalsRms(key: String): List<AudioSignalRmsInfo>
    fun retrieveAudioSignalsRmsFromRange(key: String, min: Double, max: Double): List<AudioSignalRmsInfo>
    fun removeAudioSignalsRmsFromRange(key: String, min: Double, max: Double): Long
    fun retrieveAllAudioSignalsRmsPersisted(audioFileName: String): List<AudioSignalRmsEntity>
    suspend fun persistAudioSignalsRms(audioSignalsRms: List<AudioSignalRmsInfo>)
}

@Repository
class AudioSignalRmsDaoImpl : AudioSignalRmsDao {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var audioSignalRmsTemplate: RedisTemplate<String, AudioSignalRmsInfo>
    @Autowired
    private lateinit var cassandraTemplate: CassandraOperations

    private val logger = KotlinLogging.logger { }

    override suspend fun storeAudioSignalsRms(audioSignalsRms: List<AudioSignalRmsInfo>) {
        val transactionId = propsUtils.getTransactionId(audioSignalsRms.first().audioFileName)
        audioSignalsRms.forEach {
            logger.info { "[$transactionId][3][RMS] " +
                "Storing Root Mean Square (RMS) of audio signal with index: ${it.index} for caching." }
            storeAudioSignalRms(audioSignalRms = it)
        }
        persistAudioSignalsRms(audioSignalsRms)
    }

    private suspend fun storeAudioSignalRms(
        audioSignalRms: AudioSignalRmsInfo
    ) = audioSignalRmsTemplate
        .boundZSetOps("${audioSignalRms.entityName}_${audioSignalRms.audioFileName}")
        .add(audioSignalRms, audioSignalRms.index)!!

    override suspend fun persistAudioSignalsRms(audioSignalsRms: List<AudioSignalRmsInfo>) {
        val transactionId = propsUtils.getTransactionId(audioSignalsRms.first().audioFileName)
        audioSignalsRms.forEach {
            logger.info { "[$transactionId][3][RMS] Persisting Root Mean Square (RMS) of audio signal with index: ${it.index}." }
            persistAudioSignalRms(audioSignalRms = it)
        }
    }

    private fun persistAudioSignalRms(audioSignalRms: AudioSignalRmsInfo) {
        cassandraTemplate.insert(AudioSignalRmsEntity(audioSignalRms = audioSignalRms))
    }

    override fun retrieveAllAudioSignalsRms(
        key: String
    ) = audioSignalRmsTemplate
        .boundZSetOps(key)
        .range(0, -1)?.toList() ?: ArrayList()

    override fun retrieveAudioSignalsRmsFromRange(
        key: String,
        min: Double,
        max: Double
    ) = audioSignalRmsTemplate
        .boundZSetOps(key)
        .rangeByScore(tenthsSecondsFormat(min), tenthsSecondsFormat(max))
        ?.toList() ?: ArrayList()

    override fun retrieveAllAudioSignalsRmsPersisted(
        audioFileName: String
    ): List<AudioSignalRmsEntity> {
        val query = QueryBuilder
            .select()
            .from("audio_signal_rms")
            .where(QueryBuilder.eq("audio_file_name", audioFileName))
        return cassandraTemplate.select(query, AudioSignalRmsEntity::class.java)
    }

    override fun removeAudioSignalsRmsFromRange(
        key: String,
        min: Double,
        max: Double
    ): Long {
        val transactionId = propsUtils.getTransactionId(sourceAudioFileName = key.substringAfter("_"))
        logger.info { "[$transactionId][4][sound-zones] Remove set of Root Mean Squares (RMS) from: $min - to: $max." }
        return audioSignalRmsTemplate
            .boundZSetOps(key)
            .removeRangeByScore(tenthsSecondsFormat(min), tenthsSecondsFormat(max)) ?: 0L
    }
}