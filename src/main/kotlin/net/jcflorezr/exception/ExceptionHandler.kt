package net.jcflorezr.exception

import net.jcflorezr.util.PropsUtils
import org.apache.commons.io.FilenameUtils
import org.springframework.stereotype.Service

interface ExceptionHandler {
    suspend fun handle(exception: Throwable, sourceAudioFileName: String)
}

@Service
final class ExceptionHandlerImpl : ExceptionHandler {

    override suspend fun handle(exception: Throwable, sourceAudioFileName: String) {
        val sourceAudioFileBaseName = FilenameUtils.getBaseName(sourceAudioFileName)
        val transactionId = PropsUtils.getTransactionId(sourceAudioFileBaseName)
        println("AN ERROR OCCURRED. Transaction id: $transactionId")
        when (exception) {
            is AudioSplitterException -> exception
            is Error -> throw exception
            else -> InternalServerErrorException(errorCode = "outer_error", ex = (exception.cause ?: exception) as Exception)
        }.apply {
            println("MESSAGE: $message")
            if (this is InternalServerErrorException) {
                println("EXCEPTION TYPE: ${this.ex.javaClass}")
                getSimplifiedStackTrace().forEach {
                    println("------> ${it.className}, lines: ${it.lines}")
                }
            }
        }
    }
}