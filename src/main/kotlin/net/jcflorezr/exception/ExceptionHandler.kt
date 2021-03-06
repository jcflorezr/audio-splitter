package net.jcflorezr.exception

import mu.KotlinLogging
import net.jcflorezr.util.PropsUtils
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface ExceptionHandler {
    suspend fun handle(exception: Throwable, sourceAudioFileName: String)
}

@Service
final class ExceptionHandlerImpl : ExceptionHandler {

    @Autowired
    private lateinit var propsUtils: PropsUtils

    private val logger = KotlinLogging.logger { }

    override suspend fun handle(exception: Throwable, sourceAudioFileName: String) {
        val sourceAudioFileBaseName = FilenameUtils.getBaseName(sourceAudioFileName)
        val transactionId = propsUtils.getTransactionId(sourceAudioFileBaseName)
        logger.error { "AN ERROR OCCURRED. Transaction id: $transactionId" }
        when (exception) {
            is AudioSplitterException -> exception
            is Error -> throw exception
            else -> InternalServerErrorException(errorCode = "outer_error", ex = (exception.cause ?: exception) as Exception)
        }.apply {
            logger.error { "MESSAGE: $message" }
            if (this is InternalServerErrorException) {
                logger.error { "EXCEPTION TYPE: ${this.ex.javaClass}" }
                getSimplifiedStackTrace().forEach {
                    logger.error { "------> ${it.className}, lines: ${it.lines}" }
                }
            }
        }
    }
}