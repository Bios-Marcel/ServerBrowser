package com.msc.serverbrowser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

import com.msc.serverbrowser.util.Language;

/**
 * Checks all localization files for completion. There is a seperate test for every language in
 * order to assure that every file gets tested.
 *
 * @author Marcel
 * @since 21.09.2017
 */
@SuppressWarnings("javadoc")
public class LocalizationTest {
	@Test
	public void testLanguageGerman() {
		testLanguage(Language.DE);
	}

	@Test
	public void testLanguageSpanish() {
		testLanguage(Language.ES);
	}

	@Test
	public void testLanguageGeorgian() {
		testLanguage(Language.GE);
	}

	@Test
	public void testLanguageGreece() {
		testLanguage(Language.GR);
	}

	@Test
	public void testLanguageDutch() {
		testLanguage(Language.NL);
	}

	@Test
	public void testLanguagePolish() {
		testLanguage(Language.PL);
	}

	@Test
	public void testLanguageRussian() {
		testLanguage(Language.RU);
	}

	@Test
	public void testLanguageRomanian() {
		testLanguage(Language.RO);
	}

	@Test
	public void testLanguageTurkish() {
		testLanguage(Language.TR);
	}

	@Test
	public void testLanguageBosnian() {
		testLanguage(Language.BA);
	}

	private void testLanguage(final Language lang) {
		final ResourceBundle englishLanguage = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", new Locale(Language.EN.getShortcut()));
		final ResourceBundle langProperties = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", new Locale(lang.getShortcut()));
		assertEquals(langProperties.keySet().size(), englishLanguage.keySet().size(), "Language " + lang + " doesn't inherit all necessary keys.");
	}
}
