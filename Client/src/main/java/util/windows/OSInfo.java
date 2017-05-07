package util.windows;

public class OSInfo
{
	/**
	 * Preserved os name, since it won't change anyways and reading from a variable is faster.
	 */
	private final static String OS = System.getProperty("os.name").toLowerCase();

	/**
	 * @return true if the system is windows (most likely), otherwise false
	 */
	public static boolean isWindows()
	{
		return OSInfo.OS.startsWith("windows");
	}
}
