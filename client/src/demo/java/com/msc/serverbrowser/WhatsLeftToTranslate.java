package com.msc.serverbrowser;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import com.msc.serverbrowser.util.Language;

public class WhatsLeftToTranslate {
	public static void main(final String[] args) {

		final ResourceBundle englishLanguage = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", new Locale(Language.EN.getShortcut()));

		Arrays.stream(Language.values())
				.filter(lang -> lang != Language.EN)
				.forEach(lang -> {

					System.out.println("##### " + englishLanguage.getString(lang.getShortcut()));
					System.out.println("```");
					final ResourceBundle langProperties = ResourceBundle.getBundle("com.msc.serverbrowser.localization.Lang", new Locale(lang.getShortcut()));

					for (final String key : englishLanguage.keySet()) {

						final String value = langProperties.getString(key);
						if (value.equals(englishLanguage.getString(key))) {
							System.out.println(key + "=" + value);
						}
					}
					System.out.println("```");
					System.out.println();
				});
	}
}
