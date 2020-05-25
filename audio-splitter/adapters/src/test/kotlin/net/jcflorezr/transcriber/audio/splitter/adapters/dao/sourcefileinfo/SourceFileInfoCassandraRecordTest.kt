package net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Test

internal class SourceFileInfoCassandraRecordTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<SourceFileInfoCassandraRecordTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/source-file-info").path

    @Test
    fun fromEntityToRecord_And_FromRecordToEntity() {
        val expectedAudioSourceFileInfo =
            MAPPER.readValue(File("$testResourcesPath/audio-file-info.json"), AudioSourceFileInfo::class.java)
        val actualAudioSourceFileInfo = SourceFileInfoCassandraRecord.fromEntity(expectedAudioSourceFileInfo).translate()
        assertThat(actualAudioSourceFileInfo, Is(equalTo(expectedAudioSourceFileInfo)))
    }
}
