package net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.Command
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.MatcherAssert.assertThat
import java.io.File

class AudioSourceFileInfoDummyCommand : Command {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioSourceFileInfoDummyCommand> = this.javaClass
    private val sourceFilesPath: String
    private val tempSourceFilesPath: String

    init {
        sourceFilesPath = thisClass.getResource("/source-file-info").path
        tempSourceFilesPath = thisClass.getResource("/temp-converted-files").path
    }

    override suspend fun execute(aggregateRoot: AggregateRoot) {
        val actualAudioSourceFileInfo = aggregateRoot as AudioSourceFileInfo
        val audioFileExtension = File(actualAudioSourceFileInfo.originalAudioFile).extension
        val audioInfoPath = "$sourceFilesPath/audio-file-info-$audioFileExtension.json"
        val expectedAudioSourceFileInfo = MAPPER.readValue<AudioSourceFileInfo>(File(audioInfoPath))

        assertThat(actualAudioSourceFileInfo, Is(equalTo(expectedAudioSourceFileInfo)))
    }
}