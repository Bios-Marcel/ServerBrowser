package data.properties;

public enum PropertyIds
{
	LAST_VIEW(0, "1", Integer.class),
	ICONIFIED(1, "false", Boolean.class),
	MAXIMIZED(2, "false", Boolean.class),
	FULLSCREEN(3, "false", Boolean.class); // Not yet supported, but who knows.

	private int value;

	private String defaultValue;

	private Class<?> datatype;

	private <T extends Object> PropertyIds(final int value, final String defaultValue, final Class<T> datatype)
	{
		this.value = value;
		this.defaultValue = defaultValue;
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
