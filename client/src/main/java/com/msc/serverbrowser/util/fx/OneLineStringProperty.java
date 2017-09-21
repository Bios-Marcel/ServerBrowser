package com.msc.serverbrowser.util.fx;

import java.util.Objects;

import javafx.beans.property.SimpleStringProperty;

/**
 * Special StringProperty Class, that automatically removes all LineBreaks of
 * the content and trims it. This Property doesn't support <code>null</code>
 * values.
 *
 * @author Marcel
 * @since 25.06.2017
 */
public class OneLineStringProperty extends SimpleStringProperty
{
	/**
	 * Default Constructor
	 */
	public OneLineStringProperty()
	{
		super("");
	}

	/**
	 * @param initialValue
	 *            the initial value for this property
	 */
	public OneLineStringProperty(final String initialValue)
	{
		super(replaceLineBreaks(initialValue));
	}

	@Override
	public void setValue(final String value)
	{
		final String toSet = replaceLineBreaks(value);
		super.setValue(toSet);
	}

	@Override
	public void set(final String value)
	{
		final String toSet = replaceLineBreaks(value);
		super.set(toSet);
	}

	private static String replaceLineBreaks(final String value)
	{
		/*
		 * Replacing \r\n first and afterwards leftover \n by doing it like that, i
		 * never replace a
		 * single line break by two spaces
		 */
		final String toSet = Objects.isNull(value) ? "" : value.replace("\r\n", " ").replace("\n", " ").trim();
		return toSet;
	}
}
