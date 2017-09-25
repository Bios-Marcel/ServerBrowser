package com.msc.serverbrowser.constants;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

/**
 * Contains most Paths used by this application.
 *
 * @author Marcel
 */
@SuppressWarnings("javadoc")
public final class PathConstants
{
	// Internal Paths
	public static final String	VIEW_PATH		= "/com/msc/serverbrowser/views/";
	public static final String	STYLESHEET_PATH	= VIEW_PATH + "stylesheets/";
	public static final String	APP_ICON_PATH	= "/com/msc/serverbrowser/icons/icon.png";

	// Application specific Paths
	public static final String	USER_PATH		= System.getProperty("user.home") + File.separator;
	public static final String	SAMPEX_PATH		= USER_PATH + "sampex";
	public static final String	SAMPEX_LOG		= PathConstants.SAMPEX_PATH + File.separator + "Log.log";
	public static final String	SAMPEX_TEMP_JAR	= PathConstants.SAMPEX_PATH + File.separator + "temp.jar";
	public static final String	CACHE			= SAMPEX_PATH + File.separator + "cache";
	public static final String	CLIENT_CACHE	= CACHE + File.separator + "clientversions";
	public static final String	SAMP_CMD		= SAMPEX_PATH + File.separator + "sampcmd.exe";

	// GTA / SAMP specific Paths
	public static final String	GTA_USER_FILES			= FileSystemView.getFileSystemView().getDefaultDirectory()
			.getPath() + File.separator + "GTA San Andreas User Files";
	public static final String	SAMP_PATH				= GTA_USER_FILES + File.separator + "SAMP";
	public static final String	SAMP_SCREENS			= SAMP_PATH + File.separator + "//screens";
	public static final String	SAMP_CFG				= SAMP_PATH + File.separator + "sa-mp.cfg";
	public static final String	SAMP_CHATLOG			= SAMP_PATH + "\\chatlog.txt";
	public static final String	SAMP_USERDATA			= SAMP_PATH + "\\USERDATA.DAT";
	public static final String	SAMP__DOWNLOAD_LOCATION	= "https://raw.githubusercontent.com/Bios-Marcel/SA-MP-Versions/master/";
	public static final String	OUTPUT_ZIP				= SAMPEX_PATH + File.separator + "temp.zip";

	private PathConstants()
	{
		// Constructor to prevent instantiation
	}
}
