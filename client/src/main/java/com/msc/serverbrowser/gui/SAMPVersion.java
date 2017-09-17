package com.msc.serverbrowser.gui;

/**
 * Enum that contains all available SA-MP versions for version changing.
 *
 * @author Marcel
 * @since 18.09.2017
 */
@SuppressWarnings("javadoc")
public enum SAMPVersion
{
	ZeroThreeSeven("0.3.7"),
	ZeroThreeZ("0.3z"),
	ZeroThreeX("0.3x"),
	ZeroThreeE("0.3e"),
	ZeroThreeD("0.3d"),
	ZeroThreeC("0.3c"),
	ZeroThreeA("0.3a");

	private String versionIdentifier;

	SAMPVersion(final String versionIdentifier)
	{
		this.versionIdentifier = versionIdentifier;
	}

	/**
	 * @return the identifier, for example <code>0.3.7</code>
	 */
	public String getVersionIdentifier()
	{
		return versionIdentifier;
	}
}
