package com.msc.serverbrowser.data.entites;

import java.util.Objects;

import com.msc.serverbrowser.util.fx.OneLineStringProperty;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;

@SuppressWarnings("javadoc")
public class SampServer {
	private final BooleanProperty passwordedProperty = new SimpleBooleanProperty();

	private final StringProperty	hostnameProperty		= new OneLineStringProperty();
	private final StringProperty	addressProperty			= new OneLineStringProperty();
	private final StringProperty	actualPlayersProperty	= new OneLineStringProperty();
	private final StringProperty	modeProperty			= new OneLineStringProperty();
	private final StringProperty	languageProperty		= new OneLineStringProperty();
	private final StringProperty	lagcompProperty			= new OneLineStringProperty();
	private final StringProperty	websiteProperty			= new OneLineStringProperty();
	private final StringProperty	versionProperty			= new OneLineStringProperty();
	private final StringProperty	mapProperty				= new OneLineStringProperty();

	private final IntegerProperty	portProperty		= new SimpleIntegerProperty();
	private final IntegerProperty	playersProperty		= new SimpleIntegerProperty();
	private final IntegerProperty	maxPlayersProperty	= new SimpleIntegerProperty();

	public SampServer(final String address, final Integer port) {
		setAddress(address);
		setPort(port);
	}

	public BooleanProperty passwordedProperty() {
		return passwordedProperty;
	}

	public StringProperty hostnameProperty() {
		return hostnameProperty;
	}

	public IntegerProperty portProperty() {
		return portProperty;
	}

	public StringProperty addressProperty() {
		return addressProperty;
	}

	public StringProperty lagcompProperty() {
		return lagcompProperty;
	}

	public StringProperty languageProperty() {
		return languageProperty;
	}

	public IntegerProperty maxPlayersProperty() {
		return maxPlayersProperty;
	}

	public StringProperty modeProperty() {
		return modeProperty;
	}

	public StringProperty playersAndMaxPlayersProperty() {
		return actualPlayersProperty;
	}

	public IntegerProperty playersProperty() {
		return playersProperty;
	}

	public StringProperty versionProperty() {
		return versionProperty;
	}

	public StringProperty websiteProperty() {
		return websiteProperty;
	}

	public StringProperty mapProperty() {
		return mapProperty;
	}

	public boolean isPassworded() {
		return passwordedProperty.get();
	}

	public String getHostname() {
		return hostnameProperty.get();
	}

	public String getAddress() {
		return addressProperty.get();
	}

	public String getLagcomp() {
		return lagcompProperty.get();
	}

	public String getLanguage() {
		return languageProperty.get();
	}

	public Integer getMaxPlayers() {
		return maxPlayersProperty.get();
	}

	public String getMode() {
		return modeProperty.get();
	}

	public Integer getPort() {
		return portProperty.get();
	}

	public Integer getPlayers() {
		return playersProperty.get();
	}

	public String getVersion() {
		return versionProperty.get();
	}

	public String getWebsite() {
		return websiteProperty.get();
	}

	public String getMap() {
		return mapProperty.get();
	}

	public void setPassworded(final boolean passworded) {
		passwordedProperty.set(passworded);
	}

	public void setAddress(final String address) {
		addressProperty.set(address);
	}

	public void setHostname(final String hostname) {
		hostnameProperty.set(hostname);
	}

	public void setPort(final Integer port) {
		portProperty.set(port);
	}

	public void setLagcomp(final String lagcomp) {
		lagcompProperty.set(lagcomp);
	}

	public void setLanguage(final String language) {
		languageProperty.set(language);
	}

	public void setMaxPlayers(final Integer maxPlayers) {
		maxPlayersProperty.set(maxPlayers);
		updatePlayersAndMaxPlayers();
	}

	public void setMode(final String mode) {
		modeProperty.set(mode);
	}

	public void setPlayers(final Integer players) {
		playersProperty.set(players);
		updatePlayersAndMaxPlayers();
	}

	public void setVersion(final String version) {
		versionProperty.set(version);
	}

	public void setWebsite(final String website) {
		websiteProperty.set(website);
	}

	public void setMap(final String map) {
		mapProperty.set(map);
	}

	private void updatePlayersAndMaxPlayers() {
		actualPlayersProperty.set(playersProperty.get() + "/" + maxPlayersProperty.get());
	}

	@Override
	public String toString() {
		return getAddress() + ":" + getPort();
	}

	@Override
	public boolean equals(final Object object) {
		if (Objects.isNull(object) || !object.getClass().equals(SampServer.class)) {
			return false;
		}

		final SampServer compare = (SampServer) object;
		return compare == this || getAddress().equals(compare.getAddress()) && getPort().equals(compare.getPort());
	}

	@Override
	public int hashCode() {
		return (getAddress() + getPort()).hashCode();
	}

}
