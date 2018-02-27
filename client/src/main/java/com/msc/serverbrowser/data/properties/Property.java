package com.msc.serverbrowser.data.properties;

/**
 * Holds all existent properties. Do not adjust any of the ids.
 * TODO Think about a way to do one time database migration and cleanup ids
 *
 * @author Marcel
 */
@SuppressWarnings("javadoc")
public enum Property {
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
	ALLOW_CACHING_DOWNLOADS(15, true, Boolean.class),
	AUTOMTAIC_UPDATES(16, true, Boolean.class),
	LANGUAGE(17, "en", String.class),
	CONNECT_ON_DOUBLECLICK(18, true, Boolean.class),
	DOWNLOAD_PRE_RELEASES(19, false, Boolean.class);

	private final int		id;
	private final String	defaultValue;
	private final Class<?>	datatype;

	private <T> Property(final int id, final T defaultValue, final Class<T> datatype) {
		this.id = id;
		this.defaultValue = defaultValue == null ? null : defaultValue.toString();
		this.datatype = datatype;
	}

	/**
	 * @return the datatype of this property
	 */
	public Class<?> getDatatype() {
		return datatype;
	}

	/**
	 * @return an integer that is used to identify the property
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the default value for this property
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
}
