package com.msc.serverbrowser.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player
{
	private final StringProperty	playerName	= new SimpleStringProperty();
	private final StringProperty	playerScore	= new SimpleStringProperty();

	public Player(final String playerName, final String playerScore)
	{
		this.playerName.set(playerName);
		this.playerScore.set(playerScore);
	}

	public Player(final String playerName, final Integer playerScore)
	{
		this(playerName, playerScore.toString());
	}

	public StringProperty playerNameProperty()
	{
		return playerName;
	}

	public StringProperty playerScoreProperty()
	{
		return playerScore;
	}
}
