package serverbrowser.data.entites

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

/**
 *
 *
 * Tablemodel for the Players of a server, only holds two properties:
 *
 *
 *  * Name
 *  * Score
 *
 *
 * @author marcel
 * @since Jan 10, 2018
 */
class Player
/**
 * @param playerName Ingame name of the player
 * @param playerScore Ingame score of the player
 */
(playerName: String, playerScore: Int?) {
    /** Property for holding the players name  */
    private val playerName = SimpleStringProperty()
    /** Property for holding the players score  */
    private val playerScore = SimpleIntegerProperty()

    init {
        this.playerName.set(playerName)
        this.playerScore.set(playerScore!!)
    }

    /**
     * @return [.playerName]
     */
    fun playerNameProperty(): StringProperty {
        return playerName
    }

    /**
     * @return [.playerScore]
     */
    fun playerScoreProperty(): IntegerProperty {
        return playerScore
    }
}
