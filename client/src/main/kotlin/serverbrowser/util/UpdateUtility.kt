package serverbrowser.util

import org.kohsuke.github.GHRelease
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.RateLimitHandler
import serverbrowser.data.properties.ClientPropertiesController
import serverbrowser.data.properties.DownloadPreReleasesProperty
import serverbrowser.util.basic.ArrayUtility
import java.io.IOException
import java.util.*
import java.util.stream.IntStream

/**
 * Contains to update the client to newer version.
 *
 * @author Marcel
 * @since 16.09.2017
 */
object UpdateUtility {
    /**
     *
     *
     * The current version of this application.
     *
     *
     *
     * For the record: the old version scheme was X.X.X, increasing the first only when gigantic
     * changes where made, the second when new features where added and such and the last one when
     * minor changes to the ui, language or whatever where made.
     *
     *
     *
     * In the new version scheme i will include the major version of the compatible jre version,
     * since in the feature i'll to know this as for having to update the jre as well.
     *
     */
    const val VERSION = "8.5.7"

    /**
     * Checks if the currently installed version is the latest.
     *
     * @return true if it is up to date, otherwise false
     * @throws IOException if the latest tag name couldn't be retrieved.
     */
    val isUpToDate: Boolean
        @Throws(IOException::class)
        get() {
            val latestRelease = release.get()
            return if (latestRelease.isPrerelease && !ClientPropertiesController.getProperty(DownloadPreReleasesProperty)) {
                true
            } else compareVersions(VERSION, latestRelease.tagName) != CompareResult.LESS

        }

    /**
     * @return a [GHRelease] for the latest ServerBrowser release
     * @throws IOException if there was an error querying GitHub
     */
    val release: Optional<GHRelease>
        @Throws(IOException::class)
        get() {
            val gitHub = GitHubBuilder.fromEnvironment().withRateLimitHandler(RateLimitHandler.FAIL).build()
            val repository = gitHub.getRepository("Bios-Marcel/ServerBrowser")
            val releases = repository.listReleases().asList()
            return if (!releases.isEmpty()) {
                Optional.ofNullable(releases[0])
            } else Optional.empty()
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
        val versionOneParts = trimmedOne.split("[.]".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val versionTwoParts = trimmedTwo.split("[.]".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

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
}// Constructor to prevent instantiation