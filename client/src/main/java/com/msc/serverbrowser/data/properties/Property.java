package com.msc.serverbrowser.data.properties;

/**
 * Holds all existent properties.
 *
 * @author Marcel
 */
@SuppressWarnings("javadoc")
public enum Property
{
	LAST_VIEW(0, 1, Integer.class),
	MAXIMIZED(2, false, Boolean.class),
	FULLSCREEN(3, false, Boolean.class), // Not yet supported, but who knows.
	SHOW_CHANGELOG(4, false, Boolean.class),
	SAVE_LAST_VIEW(6, true, Boolean.class),
	ASK_FOR_NAME_ON_CONNECT(7, false, Boolean.class),
	SAMP_PATH(8, "", String.class),
	USE_DARK_THEME(9, false, Boolean.class),
	ALLOW_CLOSE_GTA(10, false, Boolean.class),
	ALLOW_CLOSE_SAMP(11, false, Boolean.class),
	CHANGELOG_ENABLED(12, true, Boolean.class),
	DEVELOPMENT(13, false, Boolean.class),
	ALLOW_CACHING_DOWNLOADS(15, true, Boolean.class),
	AUTOMTAIC_UPDATES(16, true, Boolean.class);

	private int id;

	private String defaultValue;

	private Class<?> datatype;

	private <T extends Object> Property(final int value, final T defaultValue, final Class<T> datatype)
	{
		id = value;
		this.defaultValue = defaultValue == null ? null : defaultValue.toString();
		this.datatype = datatype;
	}

	/**
	 * @return the datatype of this property
	 */
	public Class<?> datatype()
	{
		return datatype;
	}

	/**
	 * @return an integer that is used to identify the property
	 */
	public int id()
	{
		return id;
	}

	/**
	 * @return the default value for this property
	 */
	public String defaultValue()
	{
		return defaultValue;
	}
}
