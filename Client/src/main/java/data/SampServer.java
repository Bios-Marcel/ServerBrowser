package data;

import entities.SampServerSerializeable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SampServer
{
	private final StringProperty	hostnameProperty		= new SimpleStringProperty();

	private final StringProperty	addressProperty			= new SimpleStringProperty();

	private final StringProperty	portProperty			= new SimpleStringProperty();

	private final IntegerProperty	playersProperty			= new SimpleIntegerProperty();

	private final IntegerProperty	maxPlayersProperty		= new SimpleIntegerProperty();

	private final StringProperty	actualPlayersProperty	= new SimpleStringProperty();

	private final StringProperty	modeProperty			= new SimpleStringProperty();

	private final StringProperty	languageProperty		= new SimpleStringProperty();

	private final StringProperty	lagcompProperty			= new SimpleStringProperty();

	private final StringProperty	websiteProperty			= new SimpleStringProperty();

	private final StringProperty	versionProperty			= new SimpleStringProperty();

	public SampServer(final String address, final String port)
	{
		addressProperty.set(address);
		portProperty.set(port);
	}

	public SampServer(final SampServerSerializeable server)
	{
		hostnameProperty.set(server.getHostname());
		addressProperty.set(server.getAddress());
		portProperty.set(server.getPort());
		playersProperty.set(server.getPlayers());
		maxPlayersProperty.set(server.getMaxPlayers());
		modeProperty.set(server.getMode());
		languageProperty.set(server.getLanguage());
		lagcompProperty.set(server.getLagcomp());
		versionProperty.set(server.getVersion());
		websiteProperty.set(server.getWebsite());
		updatePlayersAndMaxPlayers();
	}

	public boolean equals(final SampServer compare)
	{
		return getAddress().equals(compare.getAddress()) && getPort().equals(compare.getPort());
	}

	public StringProperty hostnameProperty()
	{
		return hostnameProperty;
	}

	public StringProperty portProperty()
	{
		return portProperty;
	}

	public StringProperty addressProperty()
	{
		return addressProperty;
	}

	public StringProperty lagcompProperty()
	{
		return lagcompProperty;
	}

	public StringProperty languageProperty()
	{
		return languageProperty;
	}

	public IntegerProperty maxPlayersProperty()
	{
		return maxPlayersProperty;
	}

	public StringProperty modeProperty()
	{
		return modeProperty;
	}

	public StringProperty playersAndMaxPlayersProperty()
	{
		return actualPlayersProperty;
	}

	public void updatePlayersAndMaxPlayers()
	{
		actualPlayersProperty.set(playersProperty.get() + "/" + maxPlayersProperty.get());
	}

	public IntegerProperty playersProperty()
	{
		return playersProperty;
	}

	public StringProperty versionProperty()
	{
		return versionProperty;
	}

	public StringProperty websiteProperty()
	{
		return websiteProperty;
	}

	public String getHostname()
	{
		return hostnameProperty.get();
	}

	public String getAddress()
	{
		return addressProperty.get();
	}

	public String getLagcomp()
	{
		return lagcompProperty.get();
	}

	public String getLanguage()
	{
		return languageProperty.get();
	}

	public Integer getMaxPlayers()
	{
		return maxPlayersProperty.get();
	}

	public String getMode()
	{
		return modeProperty.get();
	}

	public String getPort()
	{
		return portProperty.get();
	}

	public Integer getPlayers()
	{
		return playersProperty.get();
	}

	public String getVersion()
	{
		return versionProperty.get();
	}

	public String getWebsite()
	{
		return websiteProperty.get();
	}

	public void setAddress(final String address)
	{
		addressProperty.set(address);
	}

	public void setHostname(final String hostname)
	{
		hostnameProperty.set(hostname);
	}

	public void setPort(final String port)
	{
		portProperty.set(port);
	}

	public void setLagcomp(final String lagcomp)
	{
		lagcompProperty.set(lagcomp);
	}

	public void setLanguage(final String language)
	{
		languageProperty.set(language);
	}

	public void setMaxPlayers(final Integer maxPlayers)
	{
		maxPlayersProperty.set(maxPlayers);
		updatePlayersAndMaxPlayers();
	}

	public void setMode(final String mode)
	{
		modeProperty.set(mode);
	}

	public void setPlayers(final Integer players)
	{
		playersProperty.set(players);
		updatePlayersAndMaxPlayers();
	}

	public void setVersion(final String version)
	{
		versionProperty.set(version);
	}

	public void setWebsite(final String website)
	{
		websiteProperty.set(website);
	}
}
