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
 * <p>
 * <a href="http://wiki.sa-mp.com/wiki/Sa-mp.cfg.html">SA-MP.cfg Wiki</a>
 * </p>
 *
 * @author Marcel
 */
@SuppressWarnings("javadoc")
public final class LegacySettingsController
{
	private static final String	FALSE_AS_INT				= "0";

	public static final String	FPS_LIMIT					= "fpslimit";
	public static final String	PAGE_SIZE					= "pagesize";
	public static final String	MULTICORE					= "multicore";
	public static final String	TIMESTAMP					= "timestamp";
	public static final String	AUDIO_PROXY_OFF				= "audioproxyoff";
	public static final String	AUDIO_MESSAGE_OFF			= "audiomsgoff";
	public static final String	DISABLE_HEAD_MOVE			= "disableheadmove";
	public static final String	IME							= "ime";
	public static final String	DIRECT_MODE					= "directmode";
	public static final String	NO_NAME_TAG_STATUS			= "nonametagstatus";

	public static final String	FPS_LIMIT_DEFAULT			= "50";
	public static final String	PAGE_SIZE_DEFAULT			= "10";
	public static final String	MULTICORE_DEFAULT			= "1";
	public static final String	TIMESTAMP_DEFAULT			= FALSE_AS_INT;
	public static final String	AUDIO_PROXY_OFF_DEFAULT		= FALSE_AS_INT;
	public static final String	AUDIO_MESSAGE_OFF_DEFAULT	= FALSE_AS_INT;
	public static final String	DISABLE_HEAD_MOVE_DEFAULT	= FALSE_AS_INT;
	public static final String	IME_DEFAULT					= FALSE_AS_INT;
	public static final String	DIRECT_MODE_DEFAULT			= FALSE_AS_INT;
	public static final String	NO_NAME_TAG_STATUS_DEFAULT	= FALSE_AS_INT;

	private LegacySettingsController()
	{
		// Constructor to prevent instantiation
	}

	/**
	 * @return {@link Properties} object containing the present legacy SA-MP
	 *         Settings or an empty
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
			Logging.log(Level.SEVERE, "Error while loading SA_MP legacy properties.", exception);
			return Optional.empty();
		}
	}

	/**
	 * Override the SA-MP legacy settings using the passed {@link Properties}
	 * object.
	 *
	 * @param properties
	 *            the {@link Properties} object to overwrite the legacy properties
	 *            with
	 */
	public static void save(final Properties properties)
	{
		try (FileOutputStream output = new FileOutputStream(PathConstants.SAMP_CFG))
		{
			properties.store(output, null);
		}
		catch (final IOException exception)
		{
			Logging.log(Level.SEVERE, "Error while saving SA_MP legacy properties.", exception);
		}
	}
}
