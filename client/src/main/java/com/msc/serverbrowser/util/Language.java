package com.msc.serverbrowser.util;

import java.util.Optional;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;

/**
 * An Enum containing all useable languages.
 *
 * @author marcel
 * @since Jan 10, 2018
 */
@SuppressWarnings("javadoc") // Supressing because i don't want to document the enum values itself
public enum Language {

	EN("en", "English"),
	DE("de", "German"),
	GE("ge", "Georgian"),
	GR("gr", "Greek"),
	NL("nl", "Dutch"),
	RU("ru", "Russian"),
	PL("pl", "Polish"),
	RO("ro", "Romanian"),
	ES("es", "Spanish");

	private final String	shortcut;
	private final String	defaultName;

	Language(final String shortcut, final String defaultName) {
		this.shortcut = shortcut;
		this.defaultName = defaultName;
	}

	/**
	 * The shortcut used to identify the language, for example
	 * <code>de<code> would stand for <code>german</code>.
	 *
	 * @return the shortcut of this {@link Language}
	 */
	public String getShortcut() {
		return shortcut;
	}

	@Override
	public String toString() {
		final String languageName = Client.lang.getString(shortcut);

		if (ClientPropertiesController.getPropertyAsString(Property.LANGUAGE).equalsIgnoreCase(EN.shortcut)) {
			return languageName;
		}
		return languageName + " (" + defaultName + ")";
	}

	/**
	 * Returns a language by matching its shortcut, for example an input of <code>de</code> would
	 * output the value {@link Language#DE}.
	 *
	 * @param shortcut
	 *            the shortcut to searh for
	 * @return An {@link Optional} containg the enum value or {@link Optional#empty()}
	 */
	public static Optional<Language> getByShortcut(final String shortcut) {
		for (final Language lang : Language.values()) {
			if (lang.getShortcut().equalsIgnoreCase(shortcut)) {
				return Optional.of(lang);
			}
		}

		return Optional.empty();
	}
}