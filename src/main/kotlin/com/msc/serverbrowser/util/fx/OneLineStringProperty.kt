package com.msc.serverbrowser.util.fx

import javafx.beans.property.SimpleStringProperty

/**
 * Special StringProperty Class, that automatically removes all LineBreaks of the content and trims
 * it. This Property doesn't support `null` values.
 *
 * @author Marcel
 * @since 25.06.2017
 */
class OneLineStringProperty : SimpleStringProperty("") {

    override fun setValue(value: String?) {
        val toSet = replaceLineBreaks(value ?: "")
        super.setValue(toSet)
    }

    override fun set(value: String?) {
        val toSet = replaceLineBreaks(value ?: "")
        super.set(toSet)
    }

}

private fun replaceLineBreaks(value: String): String {
    /*
     * Replacing \r\n first and afterwards leftover \n by doing it like that, i never replace a
     * single line break by two spaces
     */
    return value.replace("\r\n", " ").replace("\n", " ").trim({ it <= ' ' })
}
