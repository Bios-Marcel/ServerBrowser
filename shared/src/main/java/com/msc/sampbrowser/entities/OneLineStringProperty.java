package com.msc.sampbrowser.entities;

import java.util.Objects;

import javafx.beans.property.SimpleStringProperty;

public class OneLineStringProperty extends SimpleStringProperty
{
	@Override
	public void setValue(final String v)
	{
		final String toSet = replaceLineBreaks(v);
		super.setValue(toSet);
	}

	@Override
	public void set(final String newValue)
	{
		final String toSet = replaceLineBreaks(newValue);
		super.set(toSet);
	}

	private String replaceLineBreaks(final String v)
	{
		final String toSet = Objects.isNull(v) ? null : v.replaceAll(System.lineSeparator(), " ");
		return toSet;
	}
}
