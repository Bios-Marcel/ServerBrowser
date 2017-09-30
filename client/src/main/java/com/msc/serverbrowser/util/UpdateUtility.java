package com.msc.serverbrowser.util;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;

import com.msc.serverbrowser.util.basic.ArrayUtility;

/**
 * Contains to update the client to newer version.
 *
 * @author Marcel
 * @since 16.09.2017
 */
public final class UpdateUtility
{
	/**
	 * The current version of this application
	 */
	public static final String VERSION = "1.4.2";

	private UpdateUtility()
	{
		// Constructor to prevent instantiation
	}

	/**
	 * Checks if the currently installed version is the latest.
	 *
	 * @return true if it is up to date, otherwise false
	 * @throws IOException
	 *             if the latest tag name couldn't be retrieved.
	 */
	public static Boolean isUpToDate() throws IOException
	{
		final String latestVersion = getRelease().get().getTagName();

		final CompareResult result = compareVersions(VERSION, latestVersion);
		return result != CompareResult.LESS;
	}

	/**
	 * @return a {@link GHRelease} for the latest ServerBrowser release
	 * @throws IOException
	 *             if there was an error querying github
	 */
	public static Optional<GHRelease> getRelease() throws IOException
	{
		final GitHub gitHub = GitHubBuilder.fromEnvironment().withRateLimitHandler(RateLimitHandler.FAIL).build();
		final GHRepository repository = gitHub.getRepository("Bios-Marcel/ServerBrowser");
		final List<GHRelease> releases = repository.listReleases().asList();
		if (!releases.isEmpty())
		{
			return Optional.ofNullable(releases.get(0));
		}
		return Optional.empty();
	}

	/**
	 * Compares two version strings to each other.
	 *
	 * @param versionOne
	 *            first version string
	 * @param versionTwo
	 *            second version string
	 * @return {@link CompareResult#GREATER} if argument one is greater, {@link CompareResult#LESS}
	 *         if argument one is less and otherwise {@link CompareResult#EQUAL}
	 * @throws NullPointerException
	 *             if any of the parameters is null
	 * @throws NumberFormatException
	 *             if any of the parameters contains something besides spaces, dots or integral
	 *             numbers
	 * @throws IllegalArgumentException
	 *             if any of the arguments is empty
	 */
	public static CompareResult compareVersions(final String versionOne, final String versionTwo) throws NullPointerException, NumberFormatException, IllegalArgumentException
	{
		// Throw NullPointer if any is null and throw IllegalArgument if any is empty
		final String trimmedOne = versionOne.trim();
		final String trimmedTwo = versionTwo.trim();
		if (trimmedOne.isEmpty() || trimmedTwo.isEmpty())
		{
			throw new IllegalArgumentException("Empty versionstrings are invalid. (One: '" + trimmedOne + "' Two: '" + trimmedTwo + "')");
		}

		// Split Versions into their subversions;
		final String[] versionOneParts = trimmedOne.split("[.]");
		final String[] versionTwoParts = trimmedTwo.split("[.]");

		// Check if the Version matches the maximum number of subversions
		final int longest = Integer.max(versionOneParts.length, versionTwoParts.length);
		if (longest > 3)
		{
			throw new IllegalArgumentException("The semantic version should only have up to 3 parts, eg: '1.23.2312'.");
		}

		// Do first comparison
		final int length = Integer.min(versionOneParts.length, versionTwoParts.length);
		final Optional<CompareResult> result = compareVersionsUpToIndex(versionOneParts, versionTwoParts, length);

		// Incase we don't have any result as of now, we check furhter
		final boolean arrayNotEquallyLong = longest > length;
		final boolean noBiggerOneFoundYet = !result.isPresent();
		if (noBiggerOneFoundYet && arrayNotEquallyLong)
		{// If one of the strings had more version parts, we will check those too.
			final String[] bigger = ArrayUtility.getLonger(versionOneParts, versionTwoParts).get();
			final boolean foundOneBiggerThanZero = IntStream.range(length, longest)
					.filter(num -> Integer.parseInt(bigger[num]) != 0)
					.findAny().isPresent();

			if (foundOneBiggerThanZero)
			{
				if (bigger == versionOneParts)
				{
					return CompareResult.GREATER;
				}
				return CompareResult.LESS;
			}
		}

		return result.orElse(CompareResult.EQUAL);
	}

	private static Optional<CompareResult> compareVersionsUpToIndex(final String[] versionOneParts, final String[] versionTwoParts, final int length)
	{
		for (int index = 0; index < length; index++)
		{
			final Integer numberOne = Integer.parseInt(versionOneParts[index]);
			final Integer numberTwo = Integer.parseInt(versionTwoParts[index]);

			if (numberOne < numberTwo)
			{
				return Optional.of(CompareResult.LESS);
			}
			if (numberTwo < numberOne)
			{
				return Optional.of(CompareResult.GREATER);
			}
		}

		return Optional.empty();
	}
}