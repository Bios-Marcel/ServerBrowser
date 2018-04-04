package com.msc.serverbrowser

import com.msc.serverbrowser.util.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail

import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Locale
import java.util.ResourceBundle

import org.junit.jupiter.api.Test

/**
 * Checks all localization files for completion. There is a separate test for every language in
 * order to assure that every file gets tested.
 *
 * @author Marcel
 * @since 21.09.2017
 */
class LocalizationTest {
    @Test
    fun testLanguageGermanForCompletion() {
        testLanguageForCompletion(Language.DE)
    }

    @Test
    fun testLanguageSpanishForCompletion() {
        testLanguageForCompletion(Language.ES)
    }

    @Test
    fun testLanguageGeorgianForCompletion() {
        testLanguageForCompletion(Language.GE)
    }

    @Test
    fun testLanguageGreeceForCompletion() {
        testLanguageForCompletion(Language.GR)
    }

    @Test
    fun testLanguageDutchForCompletion() {
        testLanguageForCompletion(Language.NL)
    }

    @Test
    fun testLanguagePolishForCompletion() {
        testLanguageForCompletion(Language.PL)
    }

    @Test
    fun testLanguageRussianForCompletion() {
        testLanguageForCompletion(Language.RU)
    }

    @Test
    fun testLanguageRomanianForCompletion() {
        testLanguageForCompletion(Language.RO)
    }

    @Test
    fun testLanguageTurkishForCompletion() {
        testLanguageForCompletion(Language.TR)
    }

    @Test
    fun testLanguageBosnianForCompletion() {
        testLanguageForCompletion(Language.BA)
    }

    private fun testLanguageForCompletion(lang: Language) {
        val englishLanguage = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", Locale(Language.EN.shortcut))
        val langProperties = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", Locale(lang.shortcut))
        assertEquals(langProperties.keySet().size, englishLanguage.keySet().size, "Language $lang doesn't inherit all necessary keys.")
    }

    @Test
    fun testLanguageEnglishForDuplicatedKeys() {
        testLanguageForDuplicatedKeys(Language.EN)
    }

    @Test
    fun testLanguageGermanForDuplicatedKeys() {
        testLanguageForDuplicatedKeys(Language.DE)
    }

    @Test
    fun testLanguageSpanishForDuplicatedKeys() {
        testLanguageForDuplicatedKeys(Language.ES)
    }

    @Test
    fun testLanguageGeorgianForDuplicatedKeys() {
        testLanguageForDuplicatedKeys(Language.GE)
    }

    @Test
    fun testLanguageGreeceForDuplicatedKeys() {
        testLanguageForDuplicatedKeys(Language.GR)
    }

    @Test
    fun testLanguageDutchForDuplicatedKeys() {
        testLanguageForDuplicatedKeys(Language.NL)
    }

    @Test
    fun testLanguagePolishForDuplicatedKeys() {
        testLanguageForDuplicatedKeys(Language.PL)
    }

    @Test
    fun testLanguageRussianForDuplicatedKeys() {
        testLanguageForDuplicatedKeys(Language.RU)
    }

    @Test
    fun testLanguageRomanianForDuplicatedKeys() {
        testLanguageForDuplicatedKeys(Language.RO)
    }

    @Test
    fun testLanguageTurkishForDuplicatedKeys() {
        testLanguageForDuplicatedKeys(Language.TR)
    }

    @Test
    fun testLanguageBosnianForDuplicatedKeys() {
        testLanguageForDuplicatedKeys(Language.BA)
    }

    private fun testLanguageForDuplicatedKeys(lang: Language) {
        try {
            val lines = Files.readAllLines(Paths.get(this.javaClass.getResource("/com/msc/serverbrowser/localization/Lang_" + lang.shortcut + ".properties").toURI()))
            val numberOfKeysInFile = lines.stream().filter { line -> line.matches("\\w+=.+".toRegex()) }.count()
            val langProperties = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", Locale(lang.shortcut))
            val keysInResourceBundle = langProperties.keySet().size.toLong()
            assertEquals(numberOfKeysInFile, keysInResourceBundle, "The file contains $numberOfKeysInFile keys, but the ResourceBundle contains only $keysInResourceBundle.")
        } catch (exception: IOException) {
            fail(exception)
        } catch (exception: URISyntaxException) {
            fail(exception)
        }

    }
}