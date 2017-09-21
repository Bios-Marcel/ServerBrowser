package com.msc.serverbrowser.util;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;

import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.logging.Logging;

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
		final Optional<String> latestTagName = getLatestTagName();

		if (latestTagName.isPresent())
		{
			return lastTagName.equals(latestTagName.get());
		}
		return false;
	}

	public static Optional<String> getLatestTagName() throws IOException
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
}
