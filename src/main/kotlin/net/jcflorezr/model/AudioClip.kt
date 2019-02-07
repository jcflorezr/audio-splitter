package net.jcflorezr.model

import net.jcflorezr.broker.Message

data class AudioClipInfo(
    val audioFileName: String,
    val entityName: String = "audioClip",
    val index: Float,
    val initialPosition: Int,
    val initialPositionInSeconds: Float,
    val endPosition: Int,
    val endPositionInSeconds: Float,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int,
    val audioClipName: String
) : Message