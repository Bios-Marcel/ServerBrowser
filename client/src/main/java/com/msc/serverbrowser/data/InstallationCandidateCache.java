package com.msc.serverbrowser.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.insallationcandidates.InstallationCandidate;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.FileUtility;

/**
 * @author Marcel
 * @since 23.09.2017
 */
public final class InstallationCandidateCache {
	private InstallationCandidateCache() {
		// Constructor to prevent instantiation
	}

	/**
	 * Checks if a version is cached.
	 *
	 * @param version
	 *            to check
	 * @return true wenn die Version gecached ist.
	 */
	public static boolean isVersionCached(final InstallationCandidate version) {
		final Optional<String> path = getPathForCachedVersion(version);

		if (path.isPresent()) {
			final File cachedVersion = new File(path.get());

			if (cachedVersion.exists()) {
				if (FileUtility.validateFile(cachedVersion, version.getCheckSum())) {
					return true;
				}

				// Otherwise, we delete the invalid one and return false
				cachedVersion.delete();
			}
		}

		return false;
	}

	/**
	 * Returns the file path of the {@link InstallationCandidate} if it'd be cached.
	 *
	 * @param candidate the candidate to determine the path for.
	 * @return The correct path or an empty {@link Optional}
	 */
	public static Optional<String> getPathForCachedVersion(final InstallationCandidate candidate) {
		final String installerPrefix = PathConstants.CLIENT_CACHE + File.separator + candidate.getCheckSum();
		if (candidate.getUrl().endsWith(".zip")) {
			return Optional.of(installerPrefix + ".zip");
		}
		else if (candidate.getUrl().endsWith(".exe")) {
			return Optional.of(installerPrefix + ".exe");
		}
		else {
			return Optional.empty();
		}
	}

	/**
	 * Caches a file for the specified {@link SAMPVersion}.
	 *
	 * @param candidate
	 *            Version to cache the file for
	 * @param pathOfCandidate
	 *            file path that should be cached
	 * @return true if the version was cached, otherwise false
	 */
	public static boolean addCandidateToCache(final InstallationCandidate candidate, final String pathOfCandidate) {
		try {
			final Optional<String> pathForCachedVersion = getPathForCachedVersion(candidate);
			if (pathForCachedVersion.isPresent()) {
				Files.copy(Paths.get(pathOfCandidate), Paths.get(pathForCachedVersion.get()), StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
		}
		catch (final IOException exception) {
			Logging.warn("Error caching version.", exception);
		}
		return false;
	}

	/**
	 * @return true if the cache is empty, otherwise false
	 */
	public static boolean isEmpty() {
		return new File(PathConstants.CLIENT_CACHE).listFiles().length < 1;
	}

	/**
	 * Clears the cache for downloaded SA-MP versions.
	 *
	 * @return true if the cache has been cleared sucessfully
	 */
	public static boolean clearVersionCache() {
		final File clientCacheFolder = new File(PathConstants.CLIENT_CACHE);
		final boolean deletionSuccessful = FileUtility.deleteRecursively(clientCacheFolder);
		if (deletionSuccessful) {
			clientCacheFolder.mkdirs();
		}
		return deletionSuccessful;
	}
}
