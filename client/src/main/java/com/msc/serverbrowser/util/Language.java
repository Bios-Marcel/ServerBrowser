package com.msc.serverbrowser.util;

import java.util.Optional;

import com.msc.serverbrowser.Client;

public enum Language
{
	EN("en", 0),
	DE("de", 1);

	private String	shortcut;
	private int		index;

	Language(final String shortcut, final int index)
	{
		this.shortcut = shortcut;
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
		return Client.lang.getString(shortcut);
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