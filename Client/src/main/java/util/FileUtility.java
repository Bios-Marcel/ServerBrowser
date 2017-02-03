package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class FileUtility
{
	public static void downloadUsingNIO(String urlStr, String outputPath) throws IOException
	{
		URL url = new URL(urlStr);
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		FileOutputStream fos = new FileOutputStream(outputPath);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		rbc.close();
	}

	/**
	 * Unzip it
	 * 
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 */
	public static void unZipIt(File file, String outputLocation)
	{
		try
		{
			ZipFile zipFile = new ZipFile(file.toString());
			zipFile.extractAll(outputLocation);
		}
		catch (ZipException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}