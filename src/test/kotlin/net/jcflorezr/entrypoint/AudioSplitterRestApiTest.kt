package net.jcflorezr.entrypoint

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.config.TestRestApiConfig
import net.jcflorezr.config.TestWebConfig
import net.jcflorezr.exception.BadRequestException
import net.jcflorezr.exception.SourceAudioFileValidationException
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.model.SuccessResponse
import net.jcflorezr.storage.BucketClient
import net.jcflorezr.util.JsonUtils
import net.jcflorezr.util.PropsUtils
import org.apache.commons.io.FilenameUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.io.File
import org.hamcrest.CoreMatchers.`is` as Is
import org.mockito.Mockito.`when` as When

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestWebConfig::class, TestRestApiConfig::class])
@WebAppConfiguration
class AudioSplitterRestApiTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext
    @Autowired
    private lateinit var bucketClient: BucketClient
    @Autowired
    private lateinit var propsUtils: PropsUtils

    private lateinit var mockMvc: MockMvc

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
        private const val API_ENDPOINT = "/v1/audio-splitter/split-audio-into-clips"
    }

    @Before
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    private val testResourcesPath: String
    private val tempConvertedFilesPath: String
    private val thisClass: Class<AudioSplitterRestApiTest> = this.javaClass

    init {
        testResourcesPath = thisClass.getResource("/entrypoint").path
        tempConvertedFilesPath = thisClass.getResource("/temp-converted-files").path
    }

    @Test
    fun generateAudioClipsFromFile() {
        val audioFileName = "test-audio-mono.mp3"
        val audioFileLocation = testResourcesPath
        val convertedAudioFileName = FilenameUtils.getBaseName(audioFileName) + ".wav"
        When(bucketClient.downloadSourceFileFromBucket("$audioFileLocation/$audioFileName"))
            .thenReturn(File("$audioFileLocation/$audioFileName"))
        try {
            val requestBody = InitialConfiguration(audioFileName = "$audioFileLocation/$audioFileName")
            mockMvc.perform(
                post(API_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(JsonUtils.convertObjectToJsonBytes(requestBody)
                )
            ).andReturn().response.let { response ->
                assertThat(response.status, Is(equalTo(HttpStatus.OK.value())))
                assertThat(response.contentType, Is(equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE)))

                val successResponse = MAPPER.readValue(response.contentAsString, SuccessResponse::class.java)
                val expectedTransactionId = propsUtils.getTransactionId(audioFileName)
                Assert.assertThat(successResponse.transactionId, Is(equalTo(expectedTransactionId)))
            }
        } finally {
            File("$tempConvertedFilesPath/$convertedAudioFileName").delete()
        }
    }

    @Test
    fun localConfiguration_shouldThrowMandatoryFieldsMissingException() {
        val requestBody = InitialConfiguration(audioFileName = "")
        mockMvc.perform(
            post(API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.convertObjectToJsonBytes(requestBody)
            )
        ).andReturn().response.let { validateBadRequestResponse(response = it, errorCode = "missing_mandatory_fields") }
    }

    @Test
    fun localConfiguration_shouldThrowAudioFileNotFoundException() {
        val audioFileName = "any-audio-file"
        When(bucketClient.downloadSourceFileFromBucket(audioFileName)).thenReturn(File(audioFileName))
        val requestBody = InitialConfiguration(audioFileName = audioFileName)
        mockMvc.perform(
            post(API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.convertObjectToJsonBytes(requestBody)
            )
        ).andReturn().response.let { validateBadRequestResponse(response = it, errorCode = "audio_file_not_found") }
    }

    @Test
    fun localConfiguration_shouldThrowAudioFileIsDirectoryException() {
        val audioFileLocation = testResourcesPath
        When(bucketClient.downloadSourceFileFromBucket("$audioFileLocation/"))
            .thenReturn(File("$audioFileLocation/"))
        val requestBody = InitialConfiguration(audioFileName = "$audioFileLocation/")
        mockMvc.perform(
            post(API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.convertObjectToJsonBytes(requestBody)
            )
        ).andReturn().response.let { validateBadRequestResponse(response = it, errorCode = "audio_file_path_is_a_directory_path") }
    }

    @Test
    fun localConfiguration_shouldThrowAudioFileNotFoundInBucketException() {
        val audioFileLocation = "any-file"
        When(bucketClient.downloadSourceFileFromBucket(audioFileLocation))
            .thenThrow(SourceAudioFileValidationException.audioFileDoesNotExistInBucket(audioFileLocation))
        val requestBody = InitialConfiguration(audioFileName = audioFileLocation)
        mockMvc.perform(
            post(API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JsonUtils.convertObjectToJsonBytes(requestBody)
            )
        ).andReturn().response.let { validateBadRequestResponse(response = it, errorCode = "audio_file_not_found_in_bucket") }
    }

    private fun validateBadRequestResponse(response: MockHttpServletResponse, errorCode: String) {
        assertThat(response.status, Is(equalTo(HttpStatus.BAD_REQUEST.value())))
        assertThat(response.contentType, Is(equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE)))

        val errorResponseType = object : TypeReference<HashMap<String, BadRequestException>>() {}
        val errorResponseMap = MAPPER.readValue<HashMap<String, BadRequestException>>(response.contentAsString, errorResponseType)
        assertTrue(errorResponseMap.contains("error"))
        val badRequestException = errorResponseMap["error"]!!
        assertThat(badRequestException.errorCode, Is(equalTo(errorCode)))
    }
}