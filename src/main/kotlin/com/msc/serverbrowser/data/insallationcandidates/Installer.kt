package com.msc.serverbrowser.data.insallationcandidates

import com.msc.serverbrowser.constants.PathConstants
import com.msc.serverbrowser.data.InstallationCandidateCache
import com.msc.serverbrowser.data.properties.AllowCachingDownloadsProperty
import com.msc.serverbrowser.data.properties.ClientPropertiesController
import com.msc.serverbrowser.logging.Logging
import com.msc.serverbrowser.util.basic.FileUtility
import com.msc.serverbrowser.util.samp.GTAController
import com.msc.serverbrowser.util.windows.OSUtility

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Class for installing SA-MP Versions.
 *
 * @author Marcel
 * @since 22.01.2018
 */
object Installer {

    /**
     * Installs an [InstallationCandidate].
     *
     * @param candidate the candidate to be installed.
     */
    fun installViaInstallationCandidate(candidate: InstallationCandidate) {
        // RetrievePath (includes isDownload if necessary, otherwise cache path)
        try {
            Logging.info("Installing $candidate.")
            val installer = getInstallerPathAndDownloadIfNecessary(candidate)
            val gtaPath = GTAController.gtaPath!!

            // Check whether its an installer or a zip
            if (candidate.url.endsWith(".exe")) {
                runNullSoftInstaller(installer, gtaPath)
                Logging.info("Ran installer: $installer")
            } else {
                FileUtility.unzip(installer, gtaPath)
                Logging.info("Unzipped installation files: $installer")
            }

            // In case the cache wasn't use, we don't want to keep the temporary file.
            if (installer == PathConstants.TEMP_INSTALLER_EXE || installer == PathConstants.TEMP_INSTALLER_EXE) {
                Files.delete(Paths.get(installer))
                Logging.info("Deleted temporary installation files: $installer")
            }
        } catch (exception: IOException) {
            Logging.error("Error installing SA-MP.", exception)
        }

    }

    @Throws(IOException::class)
    private fun getInstallerPathAndDownloadIfNecessary(candidate: InstallationCandidate): String {

        if (InstallationCandidateCache.isVersionCached(candidate)) {
            val path = InstallationCandidateCache.getPathForCachedVersion(candidate)

            if (path.isPresent) {
                Logging.info("Using cached version for candidate '$candidate'.")
                return path.get()
            }
        }

        if (candidate.isDownload) {
            val isExecutable = candidate.url.endsWith(".exe")
            val outputPath = if (isExecutable) PathConstants.TEMP_INSTALLER_EXE else PathConstants.TEMP_INSTALLER_ZIP
            Logging.info("Downloading file for candidate '$candidate'.")
            FileUtility.downloadFile(candidate.url, outputPath)
            if (ClientPropertiesController.getProperty(AllowCachingDownloadsProperty)) {
                Logging.info("Adding file for candidate '$candidate' to cache.")
                InstallationCandidateCache.addCandidateToCache(candidate, outputPath)
            }
            return outputPath
        }

        return candidate.url
    }

    private fun runNullSoftInstaller(installerPath: String, gtaPath: String) {

        try {
            // cmd /c allows elevation of the command instead of retrieving an error
            // /S starts a silent installation
            // /D specifies the installation target folder
            var command = "cmd /c $installerPath /S /D=$gtaPath"
            if(OSUtility.isWindows.not()) {
                command = "wine $command"
            }
            val installerProcess = Runtime.getRuntime().exec(command)
            // Waiting until the installer has finished, in order to be able to give proper GUI
            // responses.
            installerProcess.waitFor()
        } catch (exception: IOException) {
            Logging.error("Error using installer: $installerPath", exception)
        } catch (exception: InterruptedException) {
            Logging.error("Error using installer: $installerPath", exception)
        }

    }
}
