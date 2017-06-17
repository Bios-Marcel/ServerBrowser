package com.msc.serverbrowser.data.properties;

public enum PropertyIds
{
	LAST_VIEW(0, 1, Integer.class),
	MAXIMIZED(2, false, Boolean.class),
	FULLSCREEN(3, false, Boolean.class), // Not yet supported, but who knows.
	SHOW_CHANGELOG(4, true, Boolean.class);

	private int value;

	private String defaultValue;

	private Class<?> datatype;

	private <T extends Object> PropertyIds(final int value, final T defaultValue, final Class<T> datatype)
	{
		this.value = value;
		this.defaultValue = defaultValue.toString();
		this.datatype = datatype;
	}

	public Class<?> datatype()
	{
		return datatype;
	}

	public int value()
	{
		return value;
	}

	public String defaultValue()
	{
		return defaultValue;
	}
}
