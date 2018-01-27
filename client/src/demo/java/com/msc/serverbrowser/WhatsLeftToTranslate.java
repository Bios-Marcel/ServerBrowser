package com.msc.serverbrowser;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import com.msc.serverbrowser.util.Language;
import com.msc.serverbrowser.util.basic.ArrayUtility;

/**
 * Prints all keys that are awating translation
 *
 * @author Marcel
 * @since 21.01.2018
 */
public final class WhatsLeftToTranslate {

	private WhatsLeftToTranslate() {
		// Private constructor to prevent instanziation
	}

	/**
	 * @param args unused
	 */
	public static void main(final String[] args) {

		final boolean forSampForums = ArrayUtility.contains(args, "sampforums");
		final ResourceBundle englishLanguage = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", new Locale(Language.EN.getShortcut()));

		Arrays.stream(Language.values())
				.filter(lang -> lang != Language.EN)
				.forEach(lang -> {
					if (forSampForums) {
						System.out.println(englishLanguage.getString(lang.getShortcut()) + ":");
						System.out.println("[CODE]");
					}
					else {
						System.out.println("#### " + englishLanguage.getString(lang.getShortcut()));
						System.out.println("```");
					}
					final ResourceBundle langProperties = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", new Locale(lang.getShortcut()));

					for (final String key : englishLanguage.keySet()) {

						final String value = langProperties.getString(key);
						if (value.equals(englishLanguage.getString(key))) {
							System.out.println(key + "=" + value);
						}
					}
					if (forSampForums) {
						System.out.println("[/CODE]");
					}
					else {
						System.out.println("```");
					}
					System.out.println();
				});
	}
}
