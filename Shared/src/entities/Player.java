package entities;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player implements Serializable
{
	private static final long	serialVersionUID	= 2770927297609110070L;

	private final String		playerName;

	private final String		playerScore;

	public Player(final String playerName, final String playerScore)
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
