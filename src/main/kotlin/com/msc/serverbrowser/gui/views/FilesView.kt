package com.msc.serverbrowser.gui.views

import com.msc.serverbrowser.Client
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.CheckBox
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TabPane.TabClosingPolicy
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.web.WebView

/**
 * View for interacting with SA-MP files.
 *
 *
 * Contains:
 *
 *  * chatlog viewer
 *
 *
 *
 * @author Marcel
 * @since 14.01.2018
 */
class FilesView{
    /**
     * Root-container of this view.
     */
    val rootPane: TabPane

    private val chatLogTextArea: WebView = WebView()

    private val clearLogsButton: Button
    private val loadLogsButton: Button

    /**
     * @return [.showTimesIfAvailableProperty]
     */
    val showTimesIfAvailableProperty: BooleanProperty = SimpleBooleanProperty(false)
    /**
     * @return [.showColorsProperty]
     */
    val showColorsProperty: BooleanProperty = SimpleBooleanProperty(false)
    /**
     * @return [.showColorsAsTextProperty]
     */
    val showColorsAsTextProperty: BooleanProperty = SimpleBooleanProperty(false)
    /**
     * @return [.lineFilterProperty]
     */
    val lineFilterProperty: StringProperty = SimpleStringProperty("")

    /**
     * Initializes the whole view.
     */
    init {

        clearLogsButton = Button(Client.getString("clear"))
        loadLogsButton = Button(Client.getString("reload"))

        val buttonBar = ButtonBar()
        buttonBar.buttons.addAll(loadLogsButton, clearLogsButton)

        val showTimesCheckBox = CheckBox(Client.getString("showTimestamps"))
        showTimesIfAvailableProperty.bind(showTimesCheckBox.selectedProperty())
        setupCheckBox(showTimesCheckBox)

        val showColorsCheckBox = CheckBox(Client.getString("showChatlogColors"))
        showColorsProperty.bind(showColorsCheckBox.selectedProperty())
        setupCheckBox(showColorsCheckBox)

        val showColorsAsTextCheckBox = CheckBox(Client.getString("showChatlogColorsAsText"))
        showColorsAsTextProperty.bind(showColorsAsTextCheckBox.selectedProperty())
        setupCheckBox(showColorsAsTextCheckBox)

        val filterTextField = TextField()
        filterTextField.promptText = Client.getString("enterFilterValue")
        lineFilterProperty.bind(filterTextField.textProperty())

        val optionCheckBoxes = HBox(5.0, showColorsCheckBox, showTimesCheckBox, showColorsAsTextCheckBox, filterTextField)

        val chatLogsTabContent = VBox(5.0, chatLogTextArea, optionCheckBoxes, buttonBar)
        VBox.setVgrow(chatLogTextArea, Priority.ALWAYS)

        val chatLogsTab = Tab(Client.getString("chatlogs"), chatLogsTabContent)

        rootPane = TabPane(chatLogsTab)
        rootPane.tabClosingPolicy = TabClosingPolicy.UNAVAILABLE
    }

    /**
     * Adjusts the layout properties for a [CheckBox].
     *
     * @param showColorsAsTextCheckBox [CheckBox] to adjust the properties for
     */
    private fun setupCheckBox(showColorsAsTextCheckBox: CheckBox) {
        showColorsAsTextCheckBox.alignment = Pos.CENTER
        showColorsAsTextCheckBox.maxHeight = java.lang.Double.MAX_VALUE
    }

    /**
     * @param eventHandler the [ActionEvent] handler to be set
     */
    fun setClearChatLogsButtonAction(eventHandler: EventHandler<ActionEvent>) {
        clearLogsButton.onAction = eventHandler
    }

    /**
     * @param eventHandler the [ActionEvent] handler to be set
     */
    fun setLoadChatLogsButtonAction(eventHandler: EventHandler<ActionEvent>) {
        loadLogsButton.onAction = eventHandler
    }

    /**
     * Sets the text inside of the [.chatLogTextArea].
     *
     * @param content the content to be set
     */
    fun setChatLogTextAreaContent(content: String) {
        chatLogTextArea.engine.loadContent(content, "text/html")
    }
}
