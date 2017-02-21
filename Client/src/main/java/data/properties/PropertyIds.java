package data.properties;

public enum PropertyIds
{
	LAST_VIEW(0, "1");

	private int		value;

	private String	defaultValue;

	private PropertyIds(final int value, final String defaultValue)
	{
		this.value = value;
		this.defaultValue = defaultValue;
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
