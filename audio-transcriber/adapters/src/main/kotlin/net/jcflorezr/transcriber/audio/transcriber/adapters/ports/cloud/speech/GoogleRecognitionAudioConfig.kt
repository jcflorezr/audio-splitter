package net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech

import com.google.cloud.speech.v1.RecognitionAudio
import com.google.protobuf.ByteString
import java.io.File

sealed class GoogleRecognitionAudioConfig {
    abstract fun getConfig(audioFilePath: String): RecognitionAudio

    object LocalStorageAudioConfig : GoogleRecognitionAudioConfig() {
        override fun getConfig(audioFilePath: String): RecognitionAudio {
            val audioBytes = ByteString.copyFrom(File(audioFilePath).readBytes())
            return RecognitionAudio.newBuilder().setContent(audioBytes).build()
        }
    }

    object GoogleCloudStorageAudioConfig : GoogleRecognitionAudioConfig() {
        override fun getConfig(audioFilePath: String): RecognitionAudio =
            RecognitionAudio.newBuilder().setUri(audioFilePath).build()
    }
}
