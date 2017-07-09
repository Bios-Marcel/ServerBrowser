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
	public static final String	USER_PATH		= System.getProperty("user.home") + File.separator;
	public static final String	SAMPEX_PATH		= USER_PATH + "sampex";
	public static final String	VIEW_PATH		= "/com/msc/serverbrowser/views/";
	public static final String	STYLESHEET_PATH	= VIEW_PATH + "stylesheets/";
	public static final String	SAMP_PATH		= FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "\\GTA San Andreas User Files\\SAMP";
	public static final String	SAMP_CFG		= SAMP_PATH + "\\sa-mp.cfg";
	public static final String	SAMP_CHATLOG	= SAMP_PATH + "\\chatlog.txt";
	public static final String	SAMP_USERDATA	= SAMP_PATH + "\\USERDATA.DAT";
}
