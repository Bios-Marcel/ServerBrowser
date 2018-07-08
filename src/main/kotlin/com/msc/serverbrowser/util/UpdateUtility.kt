package com.msc.serverbrowser.util

import com.msc.serverbrowser.data.properties.ClientPropertiesController
import com.msc.serverbrowser.data.properties.DownloadPreReleasesProperty
import com.msc.serverbrowser.util.basic.ArrayUtility
import org.kohsuke.github.GHRelease
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.RateLimitHandler
import java.io.IOException
import java.util.Optional
import java.util.stream.IntStream

/**
 * Contains to update the client to newer version.
 *
 * @author Marcel
 * @since 16.09.2017
 */
object UpdateUtility {
    /**
     * The current version of this application.
     *
     * For the record: the old version scheme was X.X.X, increasing the first only when gigantic
     * changes where made, the second when new features where added and such and the last one when
     * minor changes to the ui, language or whatever where made.
     *
     * In the new version scheme i will include the major version of the compatible jre version,
     * since in the feature i'll to know this as for having to update the jre as well.
     */
    const val VERSION = "8.6.2"

    /** Username/Repository on GitHub. */
    private const val TARGET_REPOSITORY_FOR_UPDATES = "Bios-Marcel/ServerBrowser"

    /**
     * Retrieves the the latest applicable version from GitHub.
     *
     * Depending on the clients settings, the result might or might not be a prerelease.
     *
     * @throws IOException if the latest tag name couldn't be retrieved.
     */
    @Throws(IOException::class)
    private fun getLatestAvailableRelease(): GHRelease? {
        val usePreReleases = ClientPropertiesController.getProperty(DownloadPreReleasesProperty)
        val gitHub = GitHubBuilder.fromEnvironment().withRateLimitHandler(RateLimitHandler.FAIL).build()
        val repository = gitHub.getRepository(TARGET_REPOSITORY_FOR_UPDATES)
        return repository
                .listReleases()
                .asList()
                .stream()
                //Get all non preReleases and preReleases only if those are supposed to be used
                .filter { it.isPrerelease.not() || it.isPrerelease == usePreReleases }
                //We only want the latest one
                .findFirst()
                //If nothing could be found, there is no version available and therefore no update
                .orElse(null)
    }

    /**
     * Returns the latest availableUpdateRelease.
     *
     * @return a [GHRelease] for the latest ServerBrowser update or `null`
     * @throws IOException if there was an severe querying GitHub
     */
    val availableUpdateRelease: GHRelease?
        @Throws(IOException::class)
        get() {
            val latestAvailableRelease = getLatestAvailableRelease() ?: return null

            if (compareVersions(latestAvailableRelease.tagName, VERSION) == CompareResult.GREATER) {
                return latestAvailableRelease
            }

            return null
        }

    /**
     * Compares two version strings to each other.
     *
     * @param versionOne first version string
     * @param versionTwo second version string
     * @return [CompareResult.GREATER] if argument one is greater, [CompareResult.LESS]
     * if argument one is less and otherwise [CompareResult.EQUAL]
     * @throws NullPointerException if any of the parameters is null
     * @throws NumberFormatException if any of the parameters contains something besides spaces,
     * dots or integral numbers
     * @throws IllegalArgumentException if any of the arguments is empty
     */
    @JvmStatic
    @Throws(NullPointerException::class, IllegalArgumentException::class)
    fun compareVersions(versionOne: String?, versionTwo: String?): CompareResult {
        if (versionOne == null || versionTwo == null) {
            throw NullPointerException("One of the given version strings was null")
        }

        // Throw NullPointer if any is null and throw IllegalArgument if any is empty
        val trimmedOne = versionOne.trim { it <= ' ' }
        val trimmedTwo = versionTwo.trim { it <= ' ' }
        if (trimmedOne.isEmpty() || trimmedTwo.isEmpty()) {
            throw IllegalArgumentException("Empty versionstrings are invalid. (One: '$trimmedOne' Two: '$trimmedTwo')")
        }

        // Split Versions into their subversions;
        val versionOneParts = trimmedOne.split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val versionTwoParts = trimmedTwo.split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // Do first comparison
        val longest = Integer.max(versionOneParts.size, versionTwoParts.size)
        val length = Integer.min(versionOneParts.size, versionTwoParts.size)
        val result = compareVersionsUpToIndex(versionOneParts, versionTwoParts, length)

        // In case we don't have any result as of now, we check further
        val arrayNotEquallyLong = longest > length
        val noBiggerOneFoundYet = !result.isPresent
        if (noBiggerOneFoundYet && arrayNotEquallyLong) {
            // If one of the strings had more version parts, we will check those too.
            val bigger = ArrayUtility.getLongestArray(versionOneParts, versionTwoParts).get()
            val foundOneBiggerThanZero = IntStream.range(length, longest).filter { num -> Integer.parseInt(bigger[num]) != 0 }.findAny().isPresent

            if (foundOneBiggerThanZero) {
                return if (bigger.contentEquals(versionOneParts)) {
                    CompareResult.GREATER
                } else CompareResult.LESS
            }
        }

        return result.orElse(CompareResult.EQUAL)
    }

    private fun compareVersionsUpToIndex(versionOneParts: Array<String>, versionTwoParts: Array<String>, length: Int): Optional<CompareResult> {
        for (index in 0 until length) {
            val numberOne = Integer.parseInt(versionOneParts[index])
            val numberTwo = Integer.parseInt(versionTwoParts[index])

            if (numberOne < numberTwo) {
                return Optional.of(CompareResult.LESS)
            }
            if (numberTwo < numberOne) {
                return Optional.of(CompareResult.GREATER)
            }
        }

        return Optional.empty()
    }
}