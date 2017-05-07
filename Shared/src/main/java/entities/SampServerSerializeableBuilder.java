package entities;

import java.util.Arrays;

public class SampServerSerializeableBuilder
{
	private String hostname;

	private String address;

	private Integer port;

	private Integer players;

	private Integer maxPlayers;

	private String mode;

	private String language;

	private String lagcomp;

	private String website;

	private String version;

	public SampServerSerializeableBuilder setHostname(final String hostname)
	{
		this.hostname = hostname;
		return this;
	}

	public SampServerSerializeableBuilder setAddress(final String address)
	{
		this.address = address;
		return this;
	}

	public SampServerSerializeableBuilder setPort(final Integer port)
	{
		this.port = port;
		return this;
	}

	public SampServerSerializeableBuilder setPlayers(final Integer players)
	{
		this.players = players;
		return this;
	}

	public SampServerSerializeableBuilder setMaxPlayers(final Integer maxPlayers)
	{
		this.maxPlayers = maxPlayers;
		return this;
	}

	public SampServerSerializeableBuilder setMode(final String mode)
	{
		this.mode = mode;
		return this;
	}

	public SampServerSerializeableBuilder setLanguage(final String language)
	{
		this.language = language;
		return this;
	}

	public SampServerSerializeableBuilder setLagcomp(final String lagcomp)
	{
		this.lagcomp = lagcomp;
		return this;
	}

	public SampServerSerializeableBuilder setWebsite(final String website)
	{
		this.website = website;
		return this;
	}

	public SampServerSerializeableBuilder setVersion(final String version)
	{
		this.version = version;
		return this;
	}

	public SampServerSerializeable build()
	{
		if (Arrays.asList(hostname, address, port, players, maxPlayers, mode, language, version, website, lagcomp).contains(null))
		{
			throw new IllegalStateException("Can't build until all values have been set.");
		}

		return new SampServerSerializeable(hostname, address, port, players, maxPlayers, mode, language, lagcomp, website, version);
	}
}
