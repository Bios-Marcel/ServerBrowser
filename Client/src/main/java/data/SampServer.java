package data;

import entities.SampServerSerializeable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SampServer
{
	private StringProperty	hostnameProperty		= new SimpleStringProperty();

	private StringProperty	addressProperty			= new SimpleStringProperty();

	private StringProperty	portProperty			= new SimpleStringProperty();

	private IntegerProperty	playersProperty			= new SimpleIntegerProperty();

	private IntegerProperty	maxPlayersProperty		= new SimpleIntegerProperty();

	private StringProperty	actualPlayersProperty	= new SimpleStringProperty();

	private StringProperty	modeProperty			= new SimpleStringProperty();

	private StringProperty	languageProperty		= new SimpleStringProperty();

	private StringProperty	lagcompProperty			= new SimpleStringProperty();

	private StringProperty	websiteProperty			= new SimpleStringProperty();

	private StringProperty	versionProperty			= new SimpleStringProperty();

	public SampServer(SampServerSerializeable server)
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

	public void setAddress(String address)
	{
		addressProperty.set(address);
	}

	public void setHostname(String hostname)
	{
		hostnameProperty.set(hostname);
	}

	public void setPort(String port)
	{
		portProperty.set(port);
	}

	public void setLagcomp(String lagcomp)
	{
		lagcompProperty.set(lagcomp);
	}

	public void setLanguage(String language)
	{
		languageProperty.set(language);
	}

	public void setMaxPlayers(Integer maxPlayers)
	{
		maxPlayersProperty.set(maxPlayers);
		updatePlayersAndMaxPlayers();
	}

	public void setMode(String mode)
	{
		modeProperty.set(mode);
	}

	public void setPlayers(Integer players)
	{
		playersProperty.set(players);
		updatePlayersAndMaxPlayers();
	}

	public void setVersion(String version)
	{
		versionProperty.set(version);
	}

	public void setWebsite(String website)
	{
		websiteProperty.set(website);
	}
}
