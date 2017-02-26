package entities;

import java.io.Serializable;

public class SampServerSerializeable implements Serializable
{
	private static final long	serialVersionUID	= 4910435015362133564L;

	private String				hostname;

	private String				address;

	private Integer				port;

	private Integer				players;

	private Integer				maxPlayers;

	private String				mode;

	private String				language;

	private String				lagcomp;

	private String				website;

	private String				version;

	public SampServerSerializeable(final String hostname, final String address, final Integer port, final int players, final int maxPlayers, final String mode, final String language,
					final String lagcomp, final String website, final String version)
	{
		this.hostname = hostname;
		this.address = address;
		this.players = players;
		this.port = port;
		this.maxPlayers = maxPlayers;
		this.mode = mode;
		this.language = language;
		this.lagcomp = lagcomp;
		this.website = website;
		this.version = version;
	}

	public String getHostname()
	{
		return hostname;
	}

	public String getAddress()
	{
		return address;
	}

	public String getLagcomp()
	{
		return lagcomp;
	}

	public String getLanguage()
	{
		return language;
	}

	public Integer getMaxPlayers()
	{
		return maxPlayers;
	}

	public String getMode()
	{
		return mode;
	}

	public Integer getPort()
	{
		return port;
	}

	public Integer getPlayers()
	{
		return players;
	}

	public String getVersion()
	{
		return version;
	}

	public String getWebsite()
	{
		return website;
	}

	public void setAddress(final String address)
	{
		this.address = address;
	}

	public void setHostname(final String hostname)
	{
		this.hostname = hostname;
	}

	public void setPort(final Integer port)
	{
		this.port = port;
	}

	public void setLagcomp(final String lagcomp)
	{
		this.lagcomp = lagcomp;
	}

	public void setLanguage(final String language)
	{
		this.language = language;
	}

	public void setMaxPlayers(final Integer maxPlayers)
	{
		this.maxPlayers = maxPlayers;
	}

	public void setMode(final String mode)
	{
		this.mode = mode;
	}

	public void setPlayers(final Integer players)
	{
		this.players = players;
	}

	public void setVersion(final String version)
	{
		this.version = version;
	}

	public void setWebsite(final String website)
	{
		this.website = website;
	}

	@Override
	public boolean equals(final Object obj)
	{
		final SampServerSerializeable compare = (SampServerSerializeable) obj;
		return getAddress().equals(compare.getAddress()) && getPort().equals(compare.getPort());
	}

	@Override
	public int hashCode()
	{
		return (getAddress() + getPort()).hashCode();
	}

}
