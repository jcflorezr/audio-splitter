package net.jcflorezr.transcriber.core.config.util

import java.time.Duration
import org.awaitility.Awaitility
import org.awaitility.core.ConditionFactory

object TestUtils {

    fun waitAtMostTenSecondsByOneSecondIntervals(): ConditionFactory = Awaitility.await()
        .atMost(Duration.ofSeconds(10))
        .pollInterval(Duration.ofSeconds(1))
        .pollDelay(Duration.ofSeconds(1))
}
