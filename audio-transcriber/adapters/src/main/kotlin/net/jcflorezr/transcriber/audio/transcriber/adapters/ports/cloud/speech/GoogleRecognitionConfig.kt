package net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech

import com.google.cloud.speech.v1.RecognitionConfig
import net.jcflorezr.transcriber.core.util.Locales

sealed class GoogleRecognitionConfig(val config: RecognitionConfig) {

    class DefaultRecognitionConfig : GoogleRecognitionConfig(config = RecognitionConfig.newBuilder().build())

    class ColombianSpanishWithPunctuationAndWordTimeOffsetConfig :
        GoogleRecognitionConfig(
            config = RecognitionConfig.newBuilder()
                .setLanguageCode(Locales.COLOMBIAN_SPANISH.toString())
                .setEnableAutomaticPunctuation(true)
                .setEnableWordTimeOffsets(true)
                .build()
        )
}
