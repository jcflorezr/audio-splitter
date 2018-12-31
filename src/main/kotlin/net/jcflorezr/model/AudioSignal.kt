package net.jcflorezr.model

import net.jcflorezr.broker.Message

// TODO: rename it when its java equivalent is removed
data class AudioSignalRmsInfoKt(
    val entityName: String = "audioSignalRms",
    val audioFileName: String,
    val index: Double,
    val rms: Double,
    val samplingRate: Int,
    val audioLength: Int,
    val initialPosition: Int,
    val initialPositionInSeconds: Float,
    val segmentSize: Int,
    val segmentSizeInSeconds: Float,
    val silence: Boolean,
    val active: Boolean
) : Message