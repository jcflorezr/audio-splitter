package net.jcflorezr.dao

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.jcflorezr.model.AudioClipInfoEntity
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.GroupedAudioClipInfoEntity
import net.jcflorezr.util.AudioUtilsKt.tenthsSecondsFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

interface AudioClipDao {
    suspend fun storeAudioClipInfo(audioClipInfo: AudioClipInfo): Boolean
    fun persistAudioClipInfo(audioClipInfo: AudioClipInfo)
    fun persistGroupedAudioClipInfo(groupNumber: Int, firstAudioClipInfo: AudioClipInfo, lastAudioClipInfo: AudioClipInfo)
    fun retrieveAllAudioClipsInfo(key: String): List<AudioClipInfo>
    fun removeAudioClipInfoFromRange(key: String, min: Double, max: Double): Long
}

@Repository
class AudioClipDaoImpl : AudioClipDao {

    @Autowired
    private lateinit var audioClipTemplate: RedisTemplate<String, AudioClipInfo>
    @Autowired
    private lateinit var cassandraTemplate: CassandraOperations

    override suspend fun storeAudioClipInfo(
        audioClipInfo: AudioClipInfo
    ) = coroutineScope {
        launch { persistAudioClipInfo(audioClipInfo) }
        audioClipTemplate
            .boundZSetOps("${audioClipInfo.entityName}_${audioClipInfo.audioFileName}")
            .add(audioClipInfo, audioClipInfo.index.toDouble())!!
    }

    override fun persistAudioClipInfo(audioClipInfo: AudioClipInfo) {
        cassandraTemplate.insert(AudioClipInfoEntity(audioClipInfo = audioClipInfo))
    }

    override fun persistGroupedAudioClipInfo(
        groupNumber: Int,
        firstAudioClipInfo: AudioClipInfo,
        lastAudioClipInfo: AudioClipInfo
    ) {
        cassandraTemplate.insert(GroupedAudioClipInfoEntity(
            groupNumber = groupNumber,
            firstAudioClipInfo = firstAudioClipInfo,
            lastAudioClipInfo = lastAudioClipInfo
        ))
    }

    override fun retrieveAllAudioClipsInfo(
        key: String
    ) = audioClipTemplate
        .boundZSetOps(key)
        .range(0, -1)?.toList() ?: ArrayList()

    override fun removeAudioClipInfoFromRange(
        key: String,
        min: Double,
        max: Double
    ) = audioClipTemplate
        .boundZSetOps(key)
        .removeRangeByScore(tenthsSecondsFormat(min), tenthsSecondsFormat(max)) ?: 0L

}
