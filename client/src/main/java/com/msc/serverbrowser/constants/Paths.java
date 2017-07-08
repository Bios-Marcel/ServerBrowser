package com.msc.serverbrowser.constants;

import java.io.File;

/**
 * Contains most Paths used by this application.
 *
 * @author Marcel
 */
@SuppressWarnings("javadoc")
public class Paths
{
	public static final String	USER_PATH		= System.getProperty("user.home") + File.separator;
	public static final String	SAMPEX_PATH		= USER_PATH + "sampex";
	public static final String	VIEW_PATH		= "/com/msc/serverbrowser/views/";
	public static final String	STYLESHEET_PATH	= VIEW_PATH + "stylesheets/";
}
