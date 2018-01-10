package com.msc.serverbrowser.data.entites;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * <p>
 * Tablemodel for the Players of a server, only holds two properties:
 * </p>
 * <ul>
 * <li>Name</li>
 * <li>Score</li>
 * </ul>
 *
 * @author marcel
 * @since Jan 10, 2018
 */
public class Player {
	/** Property for holding the players name */
	private final StringProperty	playerName	= new SimpleStringProperty();
	/** Property for holding the players score */
	private final IntegerProperty	playerScore	= new SimpleIntegerProperty();
	
	/**
	 * @param playerName
	 *            Ingame name of the player
	 * @param playerScore
	 *            Ingame score of the player
	 */
	public Player(final String playerName, final Integer playerScore) {
		this.playerName.set(playerName);
		this.playerScore.set(playerScore);
	}
	
	/**
	 * @return {@link #playerName}
	 */
	public StringProperty playerNameProperty() {
		return playerName;
	}
	
	/**
	 * @return {@link #playerScore}
	 */
	public IntegerProperty playerScoreProperty() {
		return playerScore;
	}
}
