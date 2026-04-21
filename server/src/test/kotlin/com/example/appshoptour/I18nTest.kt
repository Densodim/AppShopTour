package com.example.appshoptour

import com.example.appshoptour.auth.I18n
import com.example.appshoptour.auth.I18n.Lang
import com.example.appshoptour.auth.I18n.MessageKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class I18nTest {

    @Test fun `resolve RU for Accept-Language ru`() =
        assertEquals(Lang.RU, I18n.resolve("ru-RU,ru;q=0.9"))

    @Test fun `resolve ES for Accept-Language es`() =
        assertEquals(Lang.ES, I18n.resolve("es-ES,es;q=0.9"))

    @Test fun `fallback EN for unknown language`() =
        assertEquals(Lang.EN, I18n.resolve("fr-FR"))

    @Test fun `fallback EN for null`() =
        assertEquals(Lang.EN, I18n.resolve(null))

    @Test fun `jwtLang takes priority over Accept-Language`() =
        assertEquals(Lang.RU, I18n.resolve(acceptLanguage = "en", jwtLang = "ru"))

    @Test fun `all MessageKeys have non-blank translations for all languages`() {
        MessageKey.entries.forEach { key ->
            Lang.entries.forEach { lang ->
                assertTrue(I18n.message(key, lang).isNotBlank(), "Missing: $key/$lang")
            }
        }
    }
}
