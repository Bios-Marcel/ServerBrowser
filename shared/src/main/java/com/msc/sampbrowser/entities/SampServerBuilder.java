package com.msc.sampbrowser.entities;

public class SampServerBuilder
{
	private final SampServer server;

	public SampServerBuilder(final String ipAddress, final Integer port)
	{
		server = new SampServer(ipAddress, port);
	}

	public SampServerBuilder setHostname(final String hostname)
	{
		server.setHostname(hostname);
		return this;
	}

	public SampServerBuilder setLagcomp(final String lagcomp)
	{
		server.setLagcomp(lagcomp);
		return this;
	}

	public SampServerBuilder setLanguage(final String language)
	{
		server.setLanguage(language);
		return this;
	}

	public SampServerBuilder setMaxPlayers(final Integer maxPlayers)
	{
		server.setMaxPlayers(maxPlayers);
		server.updatePlayersAndMaxPlayers();
		return this;
	}

	public SampServerBuilder setMode(final String mode)
	{
		server.setMode(mode);
		return this;
	}

	public SampServerBuilder setPlayers(final Integer players)
	{
		server.setPlayers(players);
		server.updatePlayersAndMaxPlayers();
		return this;
	}

	public SampServerBuilder setVersion(final String version)
	{
		server.setVersion(version);
		return this;
	}

	public SampServerBuilder setWebsite(final String website)
	{
		server.setWebsite(website);
		return this;
	}

	public SampServer build()
	{
		return server;
	}

}
