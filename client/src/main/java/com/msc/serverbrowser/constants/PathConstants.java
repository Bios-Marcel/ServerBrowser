package com.msc.serverbrowser.constants;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

/**
 * Contains most Paths used by this application.
 *
 * @author Marcel
 */
@SuppressWarnings("javadoc")
public class PathConstants
{
	// Internal Paths
	public static final String	VIEW_PATH		= "/com/msc/serverbrowser/views/";
	public static final String	STYLESHEET_PATH	= VIEW_PATH + "stylesheets/";

	// Application specific Paths
	public static final String	USER_PATH	= System.getProperty("user.home") + File.separator;
	public static final String	SAMPEX_PATH	= USER_PATH + "sampex";

	// GTA / SAMP specific Paths
	public static final String	GTA_USER_FILES	= FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + File.separator + "GTA San Andreas User Files";
	public static final String	SAMP_PATH		= GTA_USER_FILES + File.separator + "SAMP";
	public static final String	SAMP_CFG		= SAMP_PATH + File.separator + "sa-mp.cfg";
	public static final String	SAMP_CHATLOG	= SAMP_PATH + "\\chatlog.txt";
	public static final String	SAMP_USERDATA	= SAMP_PATH + "\\USERDATA.DAT";
}
