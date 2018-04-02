package com.msc.serverbrowser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

import serverbrowser.util.Language;

/**
 * Checks all localization files for completion. There is a separate test for every language in
 * order to assure that every file gets tested.
 *
 * @author Marcel
 * @since 21.09.2017
 */
@SuppressWarnings("javadoc")
public class LocalizationTest {
	@Test
	public void testLanguageGermanForCompletion() {
		testLanguageForCompletion(Language.DE);
	}

	@Test
	public void testLanguageSpanishForCompletion() {
		testLanguageForCompletion(Language.ES);
	}

	@Test
	public void testLanguageGeorgianForCompletion() {
		testLanguageForCompletion(Language.GE);
	}

	@Test
	public void testLanguageGreeceForCompletion() {
		testLanguageForCompletion(Language.GR);
	}

	@Test
	public void testLanguageDutchForCompletion() {
		testLanguageForCompletion(Language.NL);
	}

	@Test
	public void testLanguagePolishForCompletion() {
		testLanguageForCompletion(Language.PL);
	}

	@Test
	public void testLanguageRussianForCompletion() {
		testLanguageForCompletion(Language.RU);
	}

	@Test
	public void testLanguageRomanianForCompletion() {
		testLanguageForCompletion(Language.RO);
	}

	@Test
	public void testLanguageTurkishForCompletion() {
		testLanguageForCompletion(Language.TR);
	}

	@Test
	public void testLanguageBosnianForCompletion() {
		testLanguageForCompletion(Language.BA);
	}

	private void testLanguageForCompletion(final Language lang) {
		final ResourceBundle englishLanguage = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", new Locale(Language.EN.getShortcut()));
		final ResourceBundle langProperties = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", new Locale(lang.getShortcut()));
		assertEquals(langProperties.keySet().size(), englishLanguage.keySet().size(), "Language " + lang + " doesn't inherit all necessary keys.");
	}

	@Test
	public void testLanguageEnglishForDuplicatedKeys() {
		testLanguageForDuplicatedKeys(Language.EN);
	}

	@Test
	public void testLanguageGermanForDuplicatedKeys() {
		testLanguageForDuplicatedKeys(Language.DE);
	}

	@Test
	public void testLanguageSpanishForDuplicatedKeys() {
		testLanguageForDuplicatedKeys(Language.ES);
	}

	@Test
	public void testLanguageGeorgianForDuplicatedKeys() {
		testLanguageForDuplicatedKeys(Language.GE);
	}

	@Test
	public void testLanguageGreeceForDuplicatedKeys() {
		testLanguageForDuplicatedKeys(Language.GR);
	}

	@Test
	public void testLanguageDutchForDuplicatedKeys() {
		testLanguageForDuplicatedKeys(Language.NL);
	}

	@Test
	public void testLanguagePolishForDuplicatedKeys() {
		testLanguageForDuplicatedKeys(Language.PL);
	}

	@Test
	public void testLanguageRussianForDuplicatedKeys() {
		testLanguageForDuplicatedKeys(Language.RU);
	}

	@Test
	public void testLanguageRomanianForDuplicatedKeys() {
		testLanguageForDuplicatedKeys(Language.RO);
	}

	@Test
	public void testLanguageTurkishForDuplicatedKeys() {
		testLanguageForDuplicatedKeys(Language.TR);
	}

	@Test
	public void testLanguageBosnianForDuplicatedKeys() {
		testLanguageForDuplicatedKeys(Language.BA);
	}

	private void testLanguageForDuplicatedKeys(final Language lang) {
		try {
			final List<String> lines = Files.readAllLines(Paths.get(this.getClass().getResource("/com/msc/serverbrowser/localization/Lang_" + lang.getShortcut() + ".properties").toURI()));
			final long numberOfKeysInFile = lines.stream().filter(line -> line.matches("\\w+=.+")).count();
			final ResourceBundle langProperties = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", new Locale(lang.getShortcut()));
			final long keysInResourceBundle = langProperties.keySet().size();
			assertEquals(numberOfKeysInFile, keysInResourceBundle, "The file contains " + numberOfKeysInFile + " keys, but the ResourceBundle contains only " + keysInResourceBundle + ".");
		}
		catch (final IOException | URISyntaxException exception) {
			fail(exception);
		}
	}
}