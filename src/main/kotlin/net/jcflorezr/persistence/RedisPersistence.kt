package net.jcflorezr.persistence

import biz.source_code.dsp.model.AudioSignalKt
import net.jcflorezr.model.AudioSignalRmsInfoKt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

interface RedisDao

interface AudioSignalDao : RedisDao {
    fun storeAudioSignal(audioSignal: AudioSignalKt) : Boolean
    fun retrieveAudioSignal(key: String, min: Double, max: Double): MutableSet<AudioSignalKt>?
}

interface AudioSignalRmsDao : RedisDao {
    fun storeAudioSignalRms(audioSignalRms: AudioSignalRmsInfoKt) : Boolean
    fun retrieveAudioSignalRms(key: String, min: Double, max: Double): MutableSet<AudioSignalRmsInfoKt>?
}

@Repository
class AudioSignalDaoImpl : AudioSignalDao {

    @Autowired
    private lateinit var audioSignalTemplate: RedisTemplate<String, AudioSignalKt>

    override fun storeAudioSignal(
        audioSignal: AudioSignalKt
    ) = audioSignalTemplate
            .boundZSetOps(audioSignal.entityName + "_" + audioSignal.audioFileName)
            .add(audioSignal, audioSignal.index.toDouble())!!

    override fun retrieveAudioSignal(
        key: String,
        min: Double,
        max: Double
    ): MutableSet<AudioSignalKt>? =
        audioSignalTemplate
            .boundZSetOps(key)
            .rangeByScore(min, max)

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

    override fun retrieveAudioSignalRms(
        key: String,
        min: Double,
        max: Double
    ): MutableSet<AudioSignalRmsInfoKt>? =
        audioSignalRmsTemplate
            .boundZSetOps(key)
            .rangeByScore(min, max)

}