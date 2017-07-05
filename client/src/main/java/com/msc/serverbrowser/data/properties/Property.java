package com.msc.serverbrowser.data.properties;

import com.msc.serverbrowser.util.GTA;

public enum Property
{
	LAST_VIEW(0, 1, Integer.class),
	MAXIMIZED(2, false, Boolean.class),
	FULLSCREEN(3, false, Boolean.class), // Not yet supported, but who knows.
	SHOW_CHANGELOG(4, true, Boolean.class),
	NOTIFY_SERVER_ON_STARTUP(5, true, Boolean.class),
	REMEMBER_LAST_VIEW(6, true, Boolean.class),
	ASK_FOR_NAME_ON_CONNECT(7, false, Boolean.class),
	SAMP_PATH(8, GTA.getGtaPathUnsafe(), String.class),
	USE_DARK_THEME(9, false, Boolean.class),
	ALLOW_CLOSE_GTA(10, false, Boolean.class),
	ALLOW_CLOSE_SAMP(11, false, Boolean.class);

	private int id;

	private String defaultValue;

	private Class<?> datatype;

	private <T extends Object> Property(final int value, final T defaultValue, final Class<T> datatype)
	{
		id = value;
		this.defaultValue = defaultValue == null ? null : defaultValue.toString();
		this.datatype = datatype;
	}

	public Class<?> datatype()
	{
		return datatype;
	}

	public int id()
	{
		return id;
	}

	public String defaultValue()
	{
		return defaultValue;
	}
}
