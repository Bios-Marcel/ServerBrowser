package com.msc.serverbrowser.gui.views

import javafx.beans.property.DoubleProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import com.msc.serverbrowser.Client
import com.msc.serverbrowser.gui.View
import com.msc.serverbrowser.util.windows.OSUtility

/**
 * Class containing the component hierarchy for the main view.
 *
 *
 * It contains the menu bar, the active view and the bottom bar.
 *
 *
 * @author Marcel
 * @since 10.01.2018
 */
class MainView {
    val rootPane: HBox = HBox()

    private val menuItemServers: ToggleButton
    private val menuItemUser: ToggleButton
    private val menuItemVersion: ToggleButton
    private val menuItemFiles: ToggleButton
    private val menuItemSettings: ToggleButton

    private val contentScrollPane: ScrollPane

    private val githubLink: Hyperlink
    private val helpLink: Hyperlink
    private val donateLink: Hyperlink

    private val bottomBarCustom: HBox

    private val globalProgressLabel: Label
    private val globalProgressBar: ProgressBar

    /**
     * Initializes the whole view.
     */
    init {
        rootPane.setPrefSize(800.0, 500.0)
        rootPane.styleClass.add("root-pane")

        val menuContainer = VBox()
        menuContainer.styleClass.add("tabPane")

        val menuItemToggleGroup = ToggleGroup()
        val menuItemStyleClass = "MenuItem"

        menuItemServers = ToggleButton("\uf0c9")
        menuItemServers.styleClass.add(menuItemStyleClass)
        menuItemUser = ToggleButton("\uf007")
        menuItemUser.styleClass.add(menuItemStyleClass)
        menuItemVersion = ToggleButton("\uf0ed")
        menuItemVersion.styleClass.add(menuItemStyleClass)
        menuItemFiles = ToggleButton("\uf07b")
        menuItemFiles.styleClass.add(menuItemStyleClass)
        menuItemSettings = ToggleButton("\uf013")
        menuItemSettings.styleClass.add(menuItemStyleClass)

        menuItemToggleGroup.toggles.addAll(menuItemServers, menuItemUser, menuItemVersion, menuItemFiles, menuItemSettings)
        menuContainer.children.addAll(menuItemServers, menuItemUser, menuItemVersion, menuItemFiles, menuItemSettings)

        val menuScrollPane = ScrollPane(menuContainer)
        menuScrollPane.isFitToHeight = true
        menuScrollPane.isFitToWidth = true
        menuScrollPane.styleClass.add("tabScrollPane")

        val mainContentPane = VBox()
        HBox.setHgrow(mainContentPane, Priority.ALWAYS)
        contentScrollPane = ScrollPane()
        contentScrollPane.isFitToHeight = true
        contentScrollPane.isFitToWidth = true
        contentScrollPane.styleClass.add("viewContent")
        VBox.setVgrow(contentScrollPane, Priority.ALWAYS)

        val bottomBar = HBox()
        bottomBar.styleClass.add("bottom-bar")

        githubLink = Hyperlink("\uf09b")
        githubLink.styleClass.add("info-icon")
        githubLink.tooltip = Tooltip(Client.getString("openGithubTooltip"))
        githubLink.isFocusTraversable = false
        helpLink = Hyperlink("\uf059")
        helpLink.styleClass.add("info-icon")
        helpLink.tooltip = Tooltip(Client.getString("openGithubWikiTooltip"))
        helpLink.isFocusTraversable = false
        donateLink = Hyperlink(Client.getString("donate") + " \uf0d6")
        donateLink.styleClass.add("donate-button")
        donateLink.tooltip = Tooltip(Client.getString("openDonationPageTooltip"))
        donateLink.maxHeight = java.lang.Double.MAX_VALUE
        donateLink.isFocusTraversable = false

        bottomBarCustom = HBox()
        bottomBarCustom.styleClass.add("bottom-bar-isCustom")
        HBox.setHgrow(bottomBarCustom, Priority.ALWAYS)

        val progressBarContainer = HBox()
        progressBarContainer.styleClass.add("global-progress-bar-container")
        globalProgressLabel = Label()
        globalProgressBar = ProgressBar(0.0)
        progressBarContainer.children.addAll(globalProgressLabel, globalProgressBar)

        bottomBar.children.addAll(githubLink, helpLink, donateLink, bottomBarCustom, progressBarContainer)

        mainContentPane.children.add(contentScrollPane)
        mainContentPane.children.add(bottomBar)

        rootPane.children.add(menuScrollPane)
        rootPane.children.add(mainContentPane)
    }

    /**
     * Inserts the [Node] into the [.contentScrollPane].
     *
     * @param node the [Node] to be inserted into the [.contentScrollPane]
     */
    fun setActiveViewNode(node: Node) {
        contentScrollPane.content = node
    }

    /**
     * Adds [Node]s to the isCustom part of the BottomBar
     *
     * @param nodes the [Node]s to be added
     */
    fun addToBottomBar(vararg nodes: Node) {
        bottomBarCustom.children.addAll(*nodes)
    }

    /**
     * Removes all [Node]s that have been added to the [.bottomBarCustom].
     */
    fun removeNodesFromBottomBar() {
        bottomBarCustom.children.clear()
    }

    /**
     * Sets the text for the global [ProgressBar].
     *
     * @param text the text to be set
     */
    fun setGlobalProgressBarText(text: String) {
        globalProgressLabel.text = text
    }

    /**
     * @return the [ProgressProperty][DoubleProperty] for the [.globalProgressBar]
     */
    fun globalProgressProperty(): DoubleProperty {
        return globalProgressBar.progressProperty()
    }

    /**
     * Selects the proper menu item, depending on which [View] was given.
     *
     * @param view the [View] to select the menu item for
     */
    fun selectMenuItemForView(view: View) {
        when (view) {
            View.SERVERS -> menuItemServers.isSelected = true
            View.USERNAME_CHANGER -> menuItemUser.isSelected = true
            View.VERSION_CHANGER -> menuItemVersion.isSelected = true
            View.FILES -> menuItemFiles.isSelected = true
            View.SETTINGS -> menuItemSettings.isSelected = true
            else -> throw IllegalArgumentException("This View hasn't been implemented or is invalid: $view")
        }
    }

    /**
     * Sets the [EventHandler] to handle all [ActionEvent]s on the
     * [.menuItemServers].
     *
     * @param handler [EventHandler] to be set
     */
    fun setMenuItemAllAction(handler: EventHandler<ActionEvent>) {
        menuItemServers.onAction = handler
    }

    /**
     * Sets the [EventHandler] to handle all [ActionEvent]s on the
     * [.menuItemUser].
     *
     * @param handler [EventHandler] to be set
     */
    fun setMenuItemUsernameAction(handler: EventHandler<ActionEvent>) {
        menuItemUser.onAction = handler
    }

    /**
     * Sets the [EventHandler] to handle all [ActionEvent]s on the
     * [.menuItemVersion].
     *
     * @param handler [EventHandler] to be set
     */
    fun setMenuItemVersionAction(handler: EventHandler<ActionEvent>) {
        menuItemVersion.onAction = handler
    }

    /**
     * Sets the [EventHandler] to handle all [ActionEvent]s on the
     * [.menuItemFiles].
     *
     * @param handler [EventHandler] to be set
     */
    fun setMenuItemFilesAction(handler: EventHandler<ActionEvent>) {
        menuItemFiles.onAction = handler
    }

    /**
     * Sets the [EventHandler] to handle all [ActionEvent]s on the
     * [.menuItemSettings].
     *
     * @param handler [EventHandler] to be set
     */
    fun setMenuItemSettingsAction(handler: EventHandler<ActionEvent>) {
        menuItemSettings.onAction = handler
    }

    /**
     * Sets a browse action for the [.githubLink] using the given [String] as the URL.
     *
     * @param string the URL
     */
    fun setGitHubHyperlink(string: String) {
        githubLink.setOnAction { OSUtility.browse(string) }
    }

    /**
     * Sets a browse action for the [.helpLink] using the given [String] as the URL.
     *
     * @param string the URL
     */
    fun setHelpHyperlink(string: String) {
        helpLink.setOnAction { OSUtility.browse(string) }
    }

    /**
     * Sets a browse action for the [.donateLink] using the given [String] as the URL.
     *
     * @param string the URL
     */
    fun setDonateHyperlink(string: String) {
        donateLink.setOnAction { OSUtility.browse(string) }
    }
}
