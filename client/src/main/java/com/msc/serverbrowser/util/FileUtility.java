package com.msc.serverbrowser.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Contains all kinds of utility methods that are somehow connected to file actions.
 *
 * @author Marcel
 */
public class FileUtility
{
	/**
	 * Downlaods a file and saves it to the given location.
	 *
	 * @param url
	 *            the url to download from
	 * @param outputPath
	 *            the path where to save the downloaded file
	 * @throws IOException
	 *             if an errors occurs while writing the file or opening the stream
	 */
	public static File downloadFile(final String url, final String outputPath) throws IOException
	{
		try (final ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());
				final FileOutputStream fileOutputStream = new FileOutputStream(outputPath);)
		{
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			return new File(outputPath);
		}
	}

	/**
	 * Unzips a file.
	 *
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 */
	public static void unzip(final String file, final String outputLocation) throws ZipException
	{
		final ZipFile zipFile = new ZipFile(file);
		zipFile.extractAll(outputLocation);
	}
}