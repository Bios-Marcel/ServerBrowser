package com.msc.serverbrowser.data.properties;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;

import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.logging.Logging;

/**
 * Contains convenient methods for interacting with the SA-MP legacy settings.
 *
 * @author Marcel
 */
@SuppressWarnings("javadoc")
public class LegacySAMPSettings
{
	public static final String	FPS_LIMIT			= "fpslimit";
	public static final String	PAGE_SIZE			= "pagesize";
	public static final String	MULTICORE			= "multicore";
	public static final String	TIMESTAMP			= "timestamp";
	public static final String	AUDIO_PROXY_OFF		= "audioproxyoff";
	public static final String	AUDIO_MESSAGE_OFF	= "audiomsgoff";

	/**
	 * @return {@link Properties} object containing the present legacy SA-MP Settings or an empty
	 *         {@link Optional}
	 */
	public static Optional<Properties> getLegacyProperties()
	{
		final Properties properties = new Properties();
		try (FileInputStream input = new FileInputStream(PathConstants.SAMP_CFG))
		{
			properties.load(input);
			return Optional.of(properties);
		}
		catch (final IOException exception)
		{
			Logging.logger().log(Level.SEVERE, "Error while loading SA_MP legacy properties.", exception);
			return Optional.empty();
		}
	}

	/**
	 * Override the SA-MP legacy settings using the passed {@link Properties} object.
	 *
	 * @param properties
	 *            the {@link Properties} object to overwrite the legacy properties with
	 */
	public static void save(final Properties properties)
	{
		try (FileOutputStream output = new FileOutputStream(PathConstants.SAMP_CFG))
		{
			properties.store(output, null);
		}
		catch (final IOException exception)
		{
			Logging.logger().log(Level.SEVERE, "Error while saving SA_MP legacy properties.", exception);
		}
	}
}
