package com.msc.serverbrowser.data.insallationcandidates;

import java.util.Objects;

/**
 * Holds information about an installable SA-MP client.
 *
 * @author marcel
 * @since Jan 9, 2018
 */
public class InstallationCandidate {
	/**
	 * Another checksum which is used to identfy a specific samp.dll, which then
	 * tells us which installation candidate it belongs to.
	 */
	private final String sampDllChecksum;

	/**
	 * The Name of the release candidate, for example 'SA-MP Client 5.0'.
	 */
	private final String name;

	/**
	 * This is where the installation candidate lies, might be inside of jar, the
	 * filesystem or even somewhere on the internet.
	 */
	private final String url;

	/**
	 * Used to verify that the version of SA-MP that is to be installed hasn't been
	 * altered after creation of this {@link InstallationCandidate}.
	 */
	private final String checksum;

	/**
	 * <p>
	 * Determines wether the source for this {@link InstallationCandidate} is on the
	 * internet, the filesystem or inside of the jar.
	 * </p>
	 * <p>
	 * The reason why i chose to manually give this, is because i don't want to
	 * parse the url in order to understand what it stands for.
	 * </p>
	 */
	private final boolean download;

	/**
	 * Determines wether this installation is usermade or one of the default
	 * installation candidates.
	 */
	private final boolean custom;

	/**
	 * @param sampDllChecksum
	 *            Checksum used to find out wich version is installe
	 * @param name
	 *            The name of the intallation card (Shown in UI)
	 * @param url
	 *            The url from where the files are take
	 * @param custom
	 *            usermade or default
	 * @param download
	 *            true if the ressource lies in the internet
	 * @param urlTargetChecksum
	 *            Checksum of files behind url
	 */
	public InstallationCandidate(final String sampDllChecksum, final String name, final String url, final boolean custom, final boolean download,
			final String urlTargetChecksum) {
		this.sampDllChecksum = Objects.requireNonNull(sampDllChecksum);
		this.name = name;
		this.url = url;
		this.custom = custom;
		this.download = download;
		this.checksum = urlTargetChecksum;
	}

	/**
	 * @return {@link #sampDllChecksum}
	 */
	public String getSampDLLChecksum() {
		return sampDllChecksum;
	}

	/**
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return {@link #url}
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return {@link #custom}
	 */
	public boolean isCustom() {
		return custom;
	}

	/**
	 * @return {@link #download}
	 */
	public boolean isDownload() {
		return download;
	}

	/**
	 * @return {@link #checksum}
	 */
	public String getCheckSum() {
		return checksum;
	}
}
