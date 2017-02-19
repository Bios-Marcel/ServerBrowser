package entities;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player
{
	private final SimpleStringProperty	playerName	= new SimpleStringProperty();

	private final SimpleStringProperty	playerScore	= new SimpleStringProperty();

	public Player(final String playerName, final String playerScore)
	{
		this.playerName.set(playerName);
		this.playerScore.set(playerScore);
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
