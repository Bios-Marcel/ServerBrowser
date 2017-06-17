package com.msc.serverbrowser.serviceImplementations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;

import com.msc.sampbrowser.interfaces.UpdateServiceInterface;
import com.msc.sampbrowser.util.Hashing;

public class UpdateServiceServerImplementation implements UpdateServiceInterface
{
	@Override
	public String getLatestVersionChecksum() throws FileNotFoundException, NoSuchAlgorithmException, IOException, RemoteException
	{
		return Hashing.verifyChecksum("/var/www/html/sampversion/launcher/launcher.jar");
	}

	@Override
	public String getLatestVersionURL() throws IOException, RemoteException
	{
		final GitHub gitHub = GitHubBuilder.fromEnvironment().withRateLimitHandler(RateLimitHandler.FAIL).build();
		final GHRepository repository = gitHub.getRepository("Bios-Marcel/ServerBrowser");
		final List<GHRelease> releases = repository.listReleases().asList();
		if (releases.size() >= 1)
		{
			return releases.get(0).getAssets().get(0).getBrowserDownloadUrl();
		}
		return null;
	}
}
