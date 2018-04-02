package serverbrowser.data.insallationcandidates

/**
 * Holds information about an installable SA-MP client.
 *
 * @author marcel
 * @since Jan 9, 2018
 *
 * @property sampDllChecksum Checksum used to find out which version is installed
 * @property name The name of the installation card (Shown in UI)
 * @property url The url from where the files are take
 * @property isCustom user-made or default
 * @property isDownload true if the resource lies in the internet
 * @property urlTargetChecksum Checksum of files behind url
 */
data class InstallationCandidate(val sampDllChecksum: String, val name: String, val url: String, private val isCustom: Boolean, val isDownload: Boolean, val urlTargetChecksum: String)
