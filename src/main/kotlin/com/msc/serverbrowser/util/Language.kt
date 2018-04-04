package com.msc.serverbrowser.util

import com.msc.serverbrowser.Client
import com.msc.serverbrowser.data.properties.ClientPropertiesController
import com.msc.serverbrowser.data.properties.LanguageProperty
import java.util.*

/**
 * An Enum containing all usable languages.
 *
 * @author marcel
 * @since Jan 10, 2018
 */
// Suppressing because i don't want to document the enum values itself
enum class Language(
        /**
         * The shortcut used to identify the language, for example
         * `de` would stand for `german`.
         *
         * @return the shortcut of this [Language]
        `` */
        val shortcut: String, private val defaultName: String) {

    EN("en", "English"),
    DE("de", "German"),
    GE("ge", "Georgian"),
    GR("gr", "Greek"),
    NL("nl", "Dutch"),
    RU("ru", "Russian"),
    PL("pl", "Polish"),
    RO("ro", "Romanian"),
    ES("es", "Spanish"),
    TR("tr", "Turkish"),
    BA("ba", "Bosnian");

    override fun toString(): String {
        if (Objects.nonNull(Client.languageResourceBundle)) {
            val languageName = Client.getString(shortcut)

            return if (ClientPropertiesController.getProperty(LanguageProperty).equals(EN.shortcut, ignoreCase = true)) {
                languageName
            } else "$languageName ($defaultName)"
        }

        return defaultName
    }

    companion object {

        /**
         * Returns a language by matching its shortcut, for example an input of `de` would
         * output the value [Language.DE].
         *
         * @param shortcut the shortcut to search for
         * @return An [Optional] containing the enum value or [Optional.empty]
         */
        fun getByShortcut(shortcut: String): Optional<Language> {
            for (lang in Language.values()) {
                if (lang.shortcut.equals(shortcut, ignoreCase = true)) {
                    return Optional.of(lang)
                }
            }

            return Optional.empty()
        }
    }
}