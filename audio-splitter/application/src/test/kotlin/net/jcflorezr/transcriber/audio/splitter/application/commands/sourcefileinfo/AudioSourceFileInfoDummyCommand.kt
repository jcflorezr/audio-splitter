package net.jcflorezr.transcriber.audio.splitter.application.commands.sourcefileinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.core.domain.Command
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

class AudioSourceFileInfoDummyCommand : Command<AudioSourceFileInfo> {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioSourceFileInfoDummyCommand> = this.javaClass
    private val sourceFilesPath: String

    init {
        sourceFilesPath = thisClass.getResource("/source-file-info").path
    }

    override suspend fun execute(aggregateRoot: AudioSourceFileInfo) {
        val audioFileExtension = File(aggregateRoot.originalAudioFile).extension
        val audioInfoPath = "$sourceFilesPath/audio-file-info-$audioFileExtension.json"
        val expectedAudioSourceFileInfo = MAPPER.readValue<AudioSourceFileInfo>(File(audioInfoPath))

        assertThat(aggregateRoot, Is(equalTo(expectedAudioSourceFileInfo)))
    }
}
