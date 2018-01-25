package com.msc.serverbrowser.data.insallationcandidates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import com.msc.serverbrowser.constants.PathConstants;
import com.msc.serverbrowser.data.InstallationCandidateCache;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.basic.FileUtility;
import com.msc.serverbrowser.util.samp.GTAController;

/**
 * Class for installing SA-MP Versions.
 *
 * @author Marcel
 * @since 22.01.2018
 */
public class Installer {

	/**
	 * Installs an {@link InstallationCandidate}.
	 *
	 * @param candidate the candidate to be installed.
	 */
	public static void installViaInstallationCandidate(final InstallationCandidate candidate) {
		// RetrievePath (includes download if necessary, otherwise cache path)
		try {
			final String installer = getInstallerPathAndDownloadIfNecessary(candidate);
			final String gtaPath = GTAController.getGtaPath().get();

			// Check wether its an installer or a zip
			if (candidate.getUrl().endsWith(".exe")) {
				runNullSoftInstaller(installer, gtaPath);
			}
			else {
				FileUtility.unzip(installer, gtaPath);
			}

			// In case the cache wasn't use, we don't want to keep the temporary file.
			if (installer.equals(PathConstants.TEMP_INSTALLER_EXE) || installer.equals(PathConstants.TEMP_INSTALLER_EXE)) {
				Files.delete(Paths.get(installer));
			}
		}
		catch (final IOException exception) {
			Logging.error("Error installing SA-MP.", exception);
		}
	}

	private static String getInstallerPathAndDownloadIfNecessary(final InstallationCandidate candidate) throws IOException {

		if (InstallationCandidateCache.isVersionCached(candidate)) {
			final Optional<String> path = InstallationCandidateCache.getPathForCachedVersion(candidate);

			if (path.isPresent()) {
				return path.get();
			}
		}

		if (candidate.isDownload()) {
			final boolean isExe = candidate.getUrl().endsWith(".exe");
			final String outputPath = isExe ? PathConstants.TEMP_INSTALLER_EXE : PathConstants.TEMP_INSTALLER_ZIP;
			FileUtility.downloadFile(candidate.getUrl(), outputPath);
			if (ClientPropertiesController.getPropertyAsBoolean(Property.ALLOW_CACHING_DOWNLOADS)) {
				InstallationCandidateCache.addCandidateToCache(candidate, outputPath);
			}
			return outputPath;
		}

		return candidate.getUrl();
	}

	private static void runNullSoftInstaller(final String installerPath, final String gtaPath) {

		try {
			// cmd /c allows elevation of the command instead of retrieving an error
			// /S starts a silent installation
			// /D specifies the installation target folder
			final Process installerProcess = Runtime.getRuntime().exec("cmd /c " + installerPath + " /S /D=" + gtaPath);
			// Waiting until the installer has finished, in order to be able to give proper GUI
			// responses.
			installerProcess.waitFor();
		}
		catch (final IOException | InterruptedException exception) {
			Logging.error("Error using installer: " + installerPath, exception);
		}
	}
}
