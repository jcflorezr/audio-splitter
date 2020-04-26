package net.jcflorezr.transcriber.core.util

import java.util.Locale

enum class Locales(private val locale: Locale) {
    COLOMBIAN_SPANISH(Locale("es", "CO")),
    US_ENGLISH(Locale.US);

    override fun toString() = "${locale.language}-${locale.country}"
}
