package com.msc.serverbrowser.data

import com.msc.serverbrowser.constants.PathConstants
import com.msc.serverbrowser.data.insallationcandidates.InstallationCandidate
import com.msc.serverbrowser.logging.Logging
import com.msc.serverbrowser.util.basic.FileUtility
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.Optional

/**
 * @author Marcel
 * @since 23.09.2017
 */
object InstallationCandidateCache {

    /**
     * Checks if a version is cached.
     *
     * @param version to check
     * @return true if the version is cached, otherwise false
     */
    fun isVersionCached(version: InstallationCandidate): Boolean {
        val cachedVersionPath = getPathForCachedVersion(version)

        if (!cachedVersionPath.isPresent) {
            return false
        }

        val cachedVersion = File(cachedVersionPath.get())

        if (cachedVersion.exists()) {
            if (FileUtility.validateFile(cachedVersion, version.urlTargetChecksum)) {
                return true
            }

            // Otherwise, we delete the invalid one and return false
            cachedVersion.delete()
        }

        return false
    }

    /**
     * Returns the file path of the [InstallationCandidate] if it'd be cached.
     *
     * @param candidate the candidate to determine the path for.
     * @return The correct path or an empty [Optional]
     */
    fun getPathForCachedVersion(candidate: InstallationCandidate): Optional<String> {
        val installerPrefix = PathConstants.CLIENT_CACHE + File.separator + candidate.urlTargetChecksum
        return when {
            candidate.url.endsWith(".zip") -> Optional.of("$installerPrefix.zip")
            candidate.url.endsWith(".exe") -> Optional.of("$installerPrefix.exe")
            else -> Optional.empty()
        }
    }

    /**
     * Caches a file for the specified [InstallationCandidate].
     *
     * @param candidate Version to cache the file for
     * @param pathOfCandidate file path that should be cached
     * @return true if the version was cached, otherwise false
     */
    fun addCandidateToCache(candidate: InstallationCandidate, pathOfCandidate: String): Boolean {
        try {
            val pathForCachedVersion = getPathForCachedVersion(candidate)
            if (pathForCachedVersion.isPresent) {
                Files.copy(Paths.get(pathOfCandidate), Paths.get(pathForCachedVersion.get()), StandardCopyOption.REPLACE_EXISTING)
                return true
            }
        } catch (exception: IOException) {
            Logging.warn("Error caching version.", exception)
        }

        return false
    }

    /**
     * Clears the cache for downloaded SA-MP versions.
     *
     * @return true if the cache has been cleared successfully
     */
    fun clearVersionCache(): Boolean {
        val clientCacheFolder = File(PathConstants.CLIENT_CACHE)
        val deletionSuccessful = FileUtility.deleteRecursively(clientCacheFolder)
        if (deletionSuccessful) {
            clientCacheFolder.mkdirs()
        }
        return deletionSuccessful
    }
}// Constructor to prevent instantiation
