package net.jcflorezr.model

data class AudioClipInfo(
    val audioFileName: String,
    val entityName: String = "audioClip",
    val index: Float,
    val startPosition: Int,
    val startPositionInSeconds: Float,
    val endPosition: Int,
    val endPositionInSeconds: Float,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int,
    val audioClipName: String
)