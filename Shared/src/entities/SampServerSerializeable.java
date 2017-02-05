package entities;

import java.io.Serializable;

public class SampServerSerializeable implements Serializable
{
	private static final long	serialVersionUID	= 4910435015362133564L;

	private String				hostname;

	private String				address;

	private String				port;

	private Integer				players;

	private Integer				maxPlayers;

	private String				mode;

	private String				language;

	private String				lagcomp;

	private String				website;

	private String				version;

	public SampServerSerializeable(String hostname, String address, String port, int players, int maxPlayers, String mode, String language, String lagcomp, String website, String version)
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

	public String getPort()
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

	public void setAddress(String address)
	{
		this.address = address;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public void setPort(String port)
	{
		this.port = port;
	}

	public void setLagcomp(String lagcomp)
	{
		this.lagcomp = lagcomp;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	public void setMaxPlayers(Integer maxPlayers)
	{
		this.maxPlayers = maxPlayers;
	}

	public void setMode(String mode)
	{
		this.mode = mode;
	}

	public void setPlayers(Integer players)
	{
		this.players = players;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public void setWebsite(String website)
	{
		this.website = website;
	}
}
