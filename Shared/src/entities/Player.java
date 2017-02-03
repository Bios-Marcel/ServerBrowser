package entities;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player implements Serializable
{
	private static final long	serialVersionUID	= 2770927297609110070L;

	private String				playerName;

	private String				playerScore;

	public Player(String playerName, String playerScore)
	{
		this.playerName = playerName;
		this.playerScore = playerScore;
	}

	public StringProperty playerNameProperty()
	{
		return new SimpleStringProperty(playerName);
	}

	public StringProperty playerScoreProperty()
	{
		return new SimpleStringProperty(playerScore);
	}
}
