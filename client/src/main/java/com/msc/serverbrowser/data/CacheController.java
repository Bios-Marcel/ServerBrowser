package com.msc.serverbrowser.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.gui.SAMPVersion;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.FileUtility;

/**
 * @author Marcel
 * @since 23.09.2017
 */
public final class CacheController
{
	private CacheController()
	{
		// Constructor to prevent instantiation
	}

	/**
	 * Checks if a version is cached.
	 *
	 * @param version
	 *            to check
	 * @return true wenn die Version gecached ist.
	 */
	public static boolean isVersionCached(final SAMPVersion version)
	{
		final File cachedVersion = new File(PathConstants.CLIENT_CACHE + File.separator + version.getVersionIdentifier() + ".zip");

		if (cachedVersion.exists())
		{
			if (FileUtility.validateFile(cachedVersion, version.getHashOfZip()))
			{// If its valid, we return true
				return true;
			}

			// Otherwise, we delete the invalid one and return false
			cachedVersion.delete();
			return false;
		}

		return false;
	}

	/**
	 * Caches a file for the specified {@link SAMPVersion}.
	 *
	 * @param version
	 *            Version to cache the file for
	 * @param toCache
	 *            file path that should be cached
	 * @return true if the version was cached, otherwise false
	 */
	public static boolean addVersionToCache(final SAMPVersion version, final String toCache)
	{
		try
		{
			final Path cachedVersion = Paths.get(PathConstants.CLIENT_CACHE + File.separator + version.getVersionIdentifier() + ".zip");
			Files.copy(Paths.get(toCache), cachedVersion, StandardCopyOption.REPLACE_EXISTING);
			return true;
		}
		catch (final IOException exception)
		{
			Logging.log(Level.WARNING, "Error caching version.", exception);
			return false;
		}
	}

	/**
	 * Clears the cache for downloaded SA-MP versions.
	 */
	public static void clearVersionCache()
	{
		final File clientCacheFolder = new File(PathConstants.CLIENT_CACHE);
		FileUtility.deleteRecursively(clientCacheFolder);
		clientCacheFolder.mkdirs();
	}
}
