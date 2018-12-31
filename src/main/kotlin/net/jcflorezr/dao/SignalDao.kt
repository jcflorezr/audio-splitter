package net.jcflorezr.dao

import biz.source_code.dsp.model.AudioSignalKt
import com.datastax.driver.core.querybuilder.QueryBuilder
import net.jcflorezr.model.AudioPartEntity
import net.jcflorezr.model.AudioSignalRmsInfoKt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.selectOne
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.nio.ByteBuffer

interface AudioSignalDao {
    fun storeAudioSignal(audioSignal: AudioSignalKt) : Boolean
    fun storeAudioSignalPart(audioSignal: AudioSignalKt) : AudioPartEntity
    fun retrieveAudioSignalPart(audioFileName: String, index: Int) : AudioPartEntity?
    fun retrieveAudioSignalFromRange(key: String, min: Double, max: Double): MutableSet<AudioSignalKt>?
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
    ) = cassandraTemplate.insert(
            AudioPartEntity(
                audioFileName = audioSignal.audioFileName,
                index = audioSignal.index,
                channels = audioSignal.audioSourceInfo.channels,
                sampleRate = audioSignal.audioSourceInfo.sampleRate,
                sampleSizeInBits = audioSignal.audioSourceInfo.sampleSizeBits,
                sampleSize = audioSignal.audioSourceInfo.sampleSize,
                frameSize = audioSignal.audioSourceInfo.frameSize,
                bigEndian = audioSignal.audioSourceInfo.bigEndian,
                encoding = audioSignal.audioSourceInfo.encoding.name,
                content = ByteBuffer.wrap(audioSignal.dataInBytes)
            )
        )

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
    ): MutableSet<AudioSignalKt>? =
        audioSignalTemplate
            .boundZSetOps(key)
            .rangeByScore(min, max)

}

interface AudioSignalRmsDao {
    fun storeAudioSignalRms(audioSignalRms: AudioSignalRmsInfoKt) : Boolean
    fun retrieveAllAudioSignalsRms(key: String): MutableSet<AudioSignalRmsInfoKt>
    fun retrieveAudioSignalRmsFromRange(key: String, min: Double, max: Double): MutableSet<AudioSignalRmsInfoKt>?
}

@Repository
class AudioSignalRmsDaoImpl : AudioSignalRmsDao {

    @Autowired
    private lateinit var audioSignalRmsTemplate: RedisTemplate<String, AudioSignalRmsInfoKt>

    override fun storeAudioSignalRms(
        audioSignalRms: AudioSignalRmsInfoKt
    ) = audioSignalRmsTemplate
            .boundZSetOps(audioSignalRms.entityName + "_" + audioSignalRms.audioFileName)
            .add(audioSignalRms, audioSignalRms.index)!!

    override fun retrieveAllAudioSignalsRms(
            key: String
    ): MutableSet<AudioSignalRmsInfoKt> =
        audioSignalRmsTemplate
            .boundZSetOps(key)
            .range(0, -1)!!

    override fun retrieveAudioSignalRmsFromRange(
        key: String,
        min: Double,
        max: Double
    ): MutableSet<AudioSignalRmsInfoKt>? =
        audioSignalRmsTemplate
            .boundZSetOps(key)
            .rangeByScore(min, max)

}