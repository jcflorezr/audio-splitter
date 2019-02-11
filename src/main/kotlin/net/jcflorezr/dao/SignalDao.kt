package net.jcflorezr.dao

import biz.source_code.dsp.model.AudioSignalKt
import com.datastax.driver.core.querybuilder.QueryBuilder
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.jcflorezr.model.AudioPartEntity
import net.jcflorezr.model.AudioSignalRmsEntity
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.util.AudioUtilsKt.tenthsSecondsFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.selectOne
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

interface AudioSignalDao {
    fun storeAudioSignal(audioSignal: AudioSignalKt) : Boolean
    fun storeAudioSignalPart(audioSignal: AudioSignalKt) : AudioPartEntity
    fun retrieveAudioSignalPart(audioFileName: String, index: Int) : AudioPartEntity?
    fun retrieveAudioSignalFromRange(key: String, min: Double, max: Double): List<AudioSignalKt>?
}

@Repository
class AudioSignalDaoImpl : AudioSignalDao {

    @Autowired
    private lateinit var audioSignalTemplate: RedisTemplate<String, AudioSignalKt>
    @Autowired
    private lateinit var cassandraTemplate: CassandraOperations

    override fun storeAudioSignal(
        audioSignal: AudioSignalKt
    ) = audioSignalTemplate
            .boundZSetOps(audioSignal.entityName + "_" + audioSignal.audioFileName)
            .add(audioSignal, audioSignal.index.toDouble())!!

    override fun storeAudioSignalPart(
        audioSignal: AudioSignalKt
    ) = cassandraTemplate.insert(AudioPartEntity(audioSignal = audioSignal))

    override fun retrieveAudioSignalPart(audioFileName: String, index: Int): AudioPartEntity? {
        val query = QueryBuilder
            .select()
            .from("audio_part")
            .where(QueryBuilder.eq("audio_file_name", audioFileName))
            .and(QueryBuilder.eq("ind", index))
        return cassandraTemplate.selectOne(query, AudioPartEntity::class)
    }

    override fun retrieveAudioSignalFromRange(
        key: String,
        min: Double,
        max: Double
    ) = audioSignalTemplate
            .boundZSetOps(key)
            .rangeByScore(min, max)?.toList()

}

interface AudioSignalRmsDao {
    suspend fun storeAudioSignalsRms(audioSignalsRms: List<AudioSignalRmsInfoKt>)
    fun retrieveAllAudioSignalsRms(key: String): List<AudioSignalRmsInfoKt>
    fun retrieveAudioSignalsRmsFromRange(key: String, min: Double, max: Double): List<AudioSignalRmsInfoKt>
    fun removeAudioSignalsRmsFromRange(key: String, min: Double, max: Double): Long
    fun retrieveAllAudioSignalsRmsPersisted(audioFileName: String): List<AudioSignalRmsEntity>
    fun persistAudioSignalsRms(audioSignalsRms: List<AudioSignalRmsInfoKt>)
}

@Repository
class AudioSignalRmsDaoImpl : AudioSignalRmsDao {

    @Autowired
    private lateinit var audioSignalRmsTemplate: RedisTemplate<String, AudioSignalRmsInfoKt>
    @Autowired
    private lateinit var cassandraTemplate: CassandraOperations

    override suspend fun storeAudioSignalsRms(
        audioSignalsRms: List<AudioSignalRmsInfoKt>
    ) = coroutineScope {
        launch { persistAudioSignalsRms(audioSignalsRms) }
        audioSignalsRms.forEach { storeAudioSignalRms(audioSignalRms = it) }
    }

    private suspend fun storeAudioSignalRms(
        audioSignalRms: AudioSignalRmsInfoKt
    ) = audioSignalRmsTemplate
        .boundZSetOps("${audioSignalRms.entityName}_${audioSignalRms.audioFileName}")
        .add(audioSignalRms, audioSignalRms.index)!!

    override fun persistAudioSignalsRms(
        audioSignalsRms: List<AudioSignalRmsInfoKt>
    ) = audioSignalsRms.forEach { persistAudioSignalRms(audioSignalRms = it) }

    private fun persistAudioSignalRms(audioSignalRms: AudioSignalRmsInfoKt) {
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
    ) = audioSignalRmsTemplate
        .boundZSetOps(key)
        .removeRangeByScore(tenthsSecondsFormat(min), tenthsSecondsFormat(max)) ?: 0L

}