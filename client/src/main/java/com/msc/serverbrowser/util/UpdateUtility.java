package com.msc.serverbrowser.util;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.IntStream;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;

import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.ArrayUtility;

/**
 * Contains to update the client to newer version.
 *
 * @author Marcel
 * @since 16.09.2017
 */
public final class UpdateUtility
{
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
		final String lastTagName = ClientPropertiesController.getPropertyAsString(Property.LAST_TAG_NAME);
		final Optional<String> latestTag = getLatestTag();

		if (latestTag.isPresent())
		{
			return lastTagName.equals(latestTag.get());
		}
		return false;
	}

	/**
	 * @return the tag of the latest github release
	 * @throws IOException
	 *             if there was an errors while retrieving data
	 */
	public static Optional<String> getLatestTag() throws IOException
	{
		try
		{
			final GitHub gitHub = GitHubBuilder.fromEnvironment().withRateLimitHandler(RateLimitHandler.FAIL).build();
			final GHRepository repository = gitHub.getRepository("Bios-Marcel/ServerBrowser");
			final List<GHRelease> releases = repository.listReleases().asList();
			if (!releases.isEmpty())
			{
				final GHRelease release = releases.get(0);
				return Optional.ofNullable(release.getTagName());
			}
		}
		catch (final IOException exception)
		{
			Logging.log(Level.SEVERE, "Couldn't retrieve latest version information.", exception);
			throw exception;
		}

		return Optional.empty();
	}

	/**
	 * Retrieves the URL to download the latest version.
	 *
	 * @return An {@link Optional} of the latest version or {@link Optional#empty()}
	 * @throws IOException
	 *             if there was an error querying github
	 */
	public static String getLatestVersionURL() throws IOException
	{
		final GitHub gitHub = GitHubBuilder.fromEnvironment().withRateLimitHandler(RateLimitHandler.FAIL).build();
		final GHRepository repository = gitHub.getRepository("Bios-Marcel/ServerBrowser");
		final List<GHRelease> releases = repository.listReleases().asList();
		if (!releases.isEmpty())
		{
			final GHRelease release = releases.get(0);
			return release.getAssets().get(0).getBrowserDownloadUrl();
		}
		return null;
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
		final String trimmedOne = versionOne.trim();
		final String trimmedTwo = versionTwo.trim();

		if (trimmedOne.isEmpty() || trimmedTwo.isEmpty())
		{
			throw new IllegalArgumentException("Empty versionstrings are invalid. (One: '" + trimmedOne + "' Two: '" + trimmedTwo + "')");
		}

		final String[] versionOneParts = trimmedOne.split("[.]");
		final String[] versionTwoParts = trimmedTwo.split("[.]");

		final int longest = Integer.max(versionOneParts.length, versionTwoParts.length);

		if (longest > 3)
		{
			throw new IllegalArgumentException("The semantic version should only have up to 3 parts, eg: '1.23.2312'.");
		}

		final int length = Integer.min(versionOneParts.length, versionTwoParts.length);

		for (int index = 0; index < length; index++)
		{
			final Integer numberOne = Integer.parseInt(versionOneParts[index]);
			final Integer numberTwo = Integer.parseInt(versionTwoParts[index]);

			if (numberOne < numberTwo)
			{
				return CompareResult.LESS;
			}
			if (numberTwo < numberOne)
			{
				return CompareResult.GREATER;
			}
		}

		if (longest > length)
		{// If one of the strings had more version parts, we will check those too.
			final String[] bigger = ArrayUtility.getLonger(versionOneParts, versionTwoParts);
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

		return CompareResult.EQUAL;
	}
}
