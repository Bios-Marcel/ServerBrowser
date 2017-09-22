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
	ZeroThreeSeven("0.3.7", 2199552, "4CBFD7E3FB3CD4934A94A8F9B387DDD75581A4E97CBCA10AA568341DE5273630"),
	ZeroThreeZ("0.3z", 1093632, "9ECD672DC16C24EF445AA1B411CB737832362B2632ACDA60BCC66358D4D85AD3"),
	ZeroThreeX("0.3x", 2084864, "B0D3FE71D9F7FF39D18468F6FCD506B8D1B28267EC81D7616E886B9A238400EC"),
	ZeroThreeE("0.3e", 1998848, "13E2F31718C24ADE07E3E8E79D644957589C1584022FA2F87895A1B7298F1C25"),
	ZeroThreeD("0.3d", 2015232, "356E78D14221D74793349A9C306720CDF9D1B2EC94172A27D85163818CBDE63C"),
	ZeroThreeC("0.3c", 1511424, "F5C1A0EDF562F188365038D97A28F950AFF8CA56C7362F9DC813FDC2BDE3B8F6"),
	ZeroThreeA("0.3a", 610304, "C860D1032BBD9DCC9DF9E0E4E89611D5F12C967E29BE138CCBCC3ECB3303C2BF");

	private String versionIdentifier;
	// TODO(MSC) Replace with a hash aswell
	private final int		sizeOfDLL;
	private final String	hashOfZip;

	SAMPVersion(final String versionIdentifier, final int sizeOfDLL, final String hashOfZip)
	{
		this.versionIdentifier = versionIdentifier;
		this.sizeOfDLL = sizeOfDLL;
		this.hashOfZip = hashOfZip;
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

	public String getHashOfZip()
	{
		return hashOfZip;
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
