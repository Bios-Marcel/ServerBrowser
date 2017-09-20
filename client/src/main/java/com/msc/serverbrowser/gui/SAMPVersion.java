package com.msc.serverbrowser.gui;

import java.util.Optional;

/**
 * Enum that contains all available SA-MP versions for version changing.
 *
 * @author Marcel
 * @since 18.09.2017
 */
@SuppressWarnings("javadoc")
public enum SAMPVersion
{
	ZeroThreeSeven("0.3.7", 2199552),
	ZeroThreeZ("0.3z", 1093632),
	ZeroThreeX("0.3x", 2084864),
	ZeroThreeE("0.3e", 1998848),
	ZeroThreeD("0.3d", 2015232),
	ZeroThreeC("0.3c", 1511424),
	ZeroThreeA("0.3a", 610304);

	private String		versionIdentifier;
	private final int	sizeOfDLL;

	SAMPVersion(final String versionIdentifier, final int sizeOfDLL)
	{
		this.versionIdentifier = versionIdentifier;
		this.sizeOfDLL = sizeOfDLL;
	}

	/**
	 * @return the identifier, for example <code>0.3.7</code>
	 */
	public String getVersionIdentifier()
	{
		return versionIdentifier;
	}

	public int getSizeOfDLL()
	{
		return sizeOfDLL;
	}

	public static Optional<SAMPVersion> findVersionByDLLSize(final int sizeOfDLLToFind)
	{
		for (final SAMPVersion version : SAMPVersion.values())
		{
			if (version.getSizeOfDLL() == sizeOfDLLToFind)
			{
				return Optional.of(version);
			}
		}

		return Optional.empty();
	}
}
