package com.msc.serverbrowser.util;

import java.util.Optional;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;

public enum Language
{
	EN("en", "English", 0),
	DE("de", "German", 1),
	GE("ge", "Georgian", 2),
	GR("gr", "Greek", 3),
	NL("nl", "Dutch", 4),
	RU("ru", "Russian", 5),
	PL("pl", "Polish", 6),
	RO("ro", "Romanian", 7);

	private String	shortcut;
	private String	defaultName;
	private int		index;

	Language(final String shortcut, final String defaultName, final int index)
	{
		this.shortcut = shortcut;
		this.defaultName = defaultName;
		this.index = index;
	}

	public String getShortcut()
	{
		return shortcut;
	}

	public int getIndex()
	{
		return index;
	}

	@Override
	public String toString()
	{
		final String languageName = Client.lang.getString(shortcut);

		if (ClientPropertiesController.getPropertyAsString(Property.LANGUAGE).equalsIgnoreCase(EN.shortcut))
		{
			return languageName;
		}
		return languageName + " (" + defaultName + ")";
	}

	public static Optional<Language> getByIndex(final int index)
	{
		for (final Language lang : Language.values())
		{
			if (lang.getIndex() == index)
			{
				return Optional.of(lang);
			}
		}

		return Optional.empty();
	}

	public static Optional<Language> getByShortcut(final String shortcut)
	{
		for (final Language lang : Language.values())
		{
			if (lang.getShortcut().equalsIgnoreCase(shortcut))
			{
				return Optional.of(lang);
			}
		}

		return Optional.empty();
	}
}