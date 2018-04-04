package com.msc.serverbrowser

import com.msc.serverbrowser.util.Language
import com.msc.serverbrowser.util.basic.ArrayUtility
import java.util.*

/**
 * Prints all keys that are awaiting translation.
 *
 * @author Marcel
 * @since 21.01.2018
 */
object WhatsLeftToTranslate {

    /**
     * @param args unused
     */
    @JvmStatic
    fun main(args: Array<String>) {

        val forSampForums = ArrayUtility.contains(args, "sampforums")
        val englishLanguage = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", Locale(Language.EN.shortcut))

        Arrays.stream(Language.values())
                .filter { lang -> lang !== Language.EN }
                .forEach { lang ->
                    if (forSampForums) {
                        println(englishLanguage.getString(lang.shortcut) + ":")
                        println("[CODE]")
                    } else {
                        println("#### " + englishLanguage.getString(lang.shortcut))
                        println("```")
                    }
                    val langProperties = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", Locale(lang.shortcut))

                    for (key in englishLanguage.keySet()) {

                        val value = langProperties.getString(key)
                        if (value == englishLanguage.getString(key)) {
                            println("$key=$value")
                        }
                    }

                    if (forSampForums) {
                        println("[/CODE]")
                    } else {
                        println("```")
                    }
                    println()
                }
    }
}
