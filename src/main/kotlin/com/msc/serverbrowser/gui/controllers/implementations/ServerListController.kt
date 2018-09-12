package com.msc.serverbrowser.gui.controllers.implementations

import com.github.plushaze.traynotification.animations.Animations
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder
import com.msc.serverbrowser.Client
import com.msc.serverbrowser.data.FavouritesController
import com.msc.serverbrowser.data.ServerConfig
import com.msc.serverbrowser.data.entites.Player
import com.msc.serverbrowser.data.entites.SampServer
import com.msc.serverbrowser.gui.components.SampServerTableMode
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController
import com.msc.serverbrowser.gui.views.ServerView
import com.msc.serverbrowser.severe
import com.msc.serverbrowser.util.ServerUtility
import com.msc.serverbrowser.util.basic.StringUtility
import com.msc.serverbrowser.util.samp.GTAController
import com.msc.serverbrowser.util.samp.SampQuery
import com.msc.serverbrowser.util.windows.OSUtility
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import javafx.util.Pair
import java.io.IOException
import java.text.MessageFormat
import java.util.*
import java.util.function.Predicate
import java.util.regex.PatternSyntaxException

/**
 * Controller for the Server view.
 *
 * @author Marcel
 * @since 02.07.2017
 */
class ServerListController(private val client: Client, private val mainController: MainController, private val view: ServerView) : ViewController {

    private val retrieving = Client.getString("retrieving")

    private val tooMuchPlayers = Client.getString("tooMuchPlayers")
    private val serverOffline = Client.getString("serverOffline")
    private val serverEmpty = Client.getString("serverEmpty")

    private val userFilterProperty = SimpleObjectProperty<Predicate<SampServer>>(Predicate { true })
    private val dataFilterProperty = SimpleObjectProperty<Predicate<SampServer>>(Predicate { true })
    private val filterProperty = SimpleObjectProperty<Predicate<SampServer>>(Predicate { true })

    /**
     * Displays the number of active players on all Servers in [.serverTable].
     */
    private var playerCount: Label = Label()
    /**
     * Number of servers in [.serverTable].
     */
    private var serverCount: Label = Label()

    private var lookingUpForServer = Optional.empty<SampServer>()

    private val ipAndPort: Optional<Pair<String, String>>
        get() {
            val address = view.addressTextField.text

            if (Objects.nonNull(address) && !address.isEmpty()) {
                val ipAndPort = view.addressTextField.text.split("[:]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (ipAndPort.size == 1) {
                    return Optional.of(Pair(ipAndPort[0], ServerUtility.DEFAULT_SAMP_PORT.toString()))
                } else if (ipAndPort.size == 2) {
                    return Optional.of(Pair(ipAndPort[0], ipAndPort[1]))
                }
            }

            return Optional.empty()
        }

    override fun initialize() {
        playerCount = Label()
        serverCount = Label()

        setPlayerCount(0)
        setServerCount(0)

        mainController.addItemsToBottomBar(playerCount, serverCount)

        setupInfoLabel(playerCount)
        setupInfoLabel(serverCount)

        userFilterProperty.addListener { _ -> updateFilterProperty() }
        dataFilterProperty.addListener { _ -> updateFilterProperty() }

        view.serverTable.predicateProperty().bind(filterProperty)
        view.serverTable.sortedListComparatorProperty().bind(view.serverTable.comparatorProperty())
        view.addressTextField.textProperty().bindBidirectional(SERVER_ADDRESS_PROPERTY)

        view.regexCheckBox.setOnAction { onFilterSettingsChange() }
        view.nameFilterTextField.setOnKeyReleased { onFilterSettingsChange() }
        view.gamemodeFilterTextField.setOnKeyReleased { onFilterSettingsChange() }
        view.languageFilterTextField.setOnKeyReleased { onFilterSettingsChange() }
        view.versionFilterComboBox.setOnAction { onFilterSettingsChange() }

        setPlayerComparator()
        addServerUpdateListener()

        toggleFavouritesMode()

        with(view) {
            addressTextField.setOnAction { onClickConnect() }
            connectButton.setOnAction { onClickConnect() }
            addToFavouritesButton.setOnAction { onClickAddToFavourites() }

            favouriteButton.setOnAction { toggleFavouritesMode() }
            allButton.setOnAction { toggleAllMode() }
            historyButton.setOnAction { toggleHistoryMode() }
        }

        /*
		 * Hack in order to remove the dot of the radiobuttons.
		 */
        view.tableTypeToggleGroup.toggles.forEach { toggle -> (toggle as Node).styleClass.remove("radio-button") }
    }

    private fun updateFilterProperty() {
        filterProperty.set(userFilterProperty.get().and(dataFilterProperty.get()))
    }

    @FXML
    private fun toggleFavouritesMode() {
        toggleMode(SampServerTableMode.FAVOURITES)
    }

    @FXML
    private fun toggleAllMode() {
        toggleMode(SampServerTableMode.ALL)
    }

    @FXML
    private fun toggleHistoryMode() {
        toggleMode(SampServerTableMode.HISTORY)
    }

    private fun toggleMode(mode: SampServerTableMode) {
        killServerLookupThreads()
        view.serverTable.setServerTableMode(mode)
        view.serverTable.selectionModel.clearSelection()
        displayNoServerInfo()
        view.serverTable.clear()

        when (mode) {
            SampServerTableMode.ALL -> {
                view.lastJoinTableColumn.isVisible = false
                view.serverTable.placeholder = Label(Client.getString("fetchingServers"))
                fillTableWithOnlineServerList()
            }
            SampServerTableMode.FAVOURITES -> {
                view.lastJoinTableColumn.isVisible = false
                view.serverTable.placeholder = Label(Client.getString("noFavouriteServers"))
                view.serverTable.addAll(FavouritesController.favourites)
                ServerConfig.initLastJoinData(view.serverTable.items)
            }
            SampServerTableMode.HISTORY -> {
                view.serverTable.placeholder = Label(Client.getString("noServerHistory"))
                view.lastJoinTableColumn.isVisible = true
                val servers = ServerConfig.lastJoinedServers
                servers.forEach { server -> updateServerInfo(server, false) }
                view.serverTable.addAll(servers)
            }
        }

        view.serverTable.refresh()
        updateGlobalInfo()
    }

    private fun fillTableWithOnlineServerList() {
        serverLookup = Thread {
            try {
                val serversToAdd = ServerUtility.fetchServersFromSouthclaws()
                ServerConfig.initLastJoinData(serversToAdd)
                if (serverLookup != null && !serverLookup!!.isInterrupted && view.serverTable.tableMode == SampServerTableMode.ALL) {
                    Platform.runLater {
                        view.serverTable.addAll(serversToAdd)
                        view.serverTable.refresh()
                    }
                }
            } catch (exception: IOException) {
                severe("Couldn't retrieve data from announce api.", exception)
                Platform.runLater { view.serverTable.placeholder = Label(Client.getString("errorFetchingServers")) }

                //The exception will be thrown in order to show an error dialog.
                throw exception
            }

            Platform.runLater { this.updateGlobalInfo() }
        }
        serverLookup!!.start()
    }

    /**
     * Sets the text for the label that states how many active players there are.
     *
     * @param activePlayers the number of active players
     */
    private fun setPlayerCount(activePlayers: Int) {
        playerCount.text = MessageFormat.format(Client.getString("activePlayers"), activePlayers)
    }

    /**
     * Sets the text for the label that states how many active servers there are.
     *
     * @param activeServers the number of active servers
     */
    private fun setServerCount(activeServers: Int) {
        serverCount.text = MessageFormat.format(Client.getString("servers"), activeServers)
    }

    private fun setPlayerComparator() {
        view.playersTableColumn.setComparator { stringOne, stringTwo ->
            val maxPlayersRemovalRegex = "/.*"
            val playersOne = stringOne.replace(maxPlayersRemovalRegex.toRegex(), "").toIntOrNull() ?: 0
            val playersTwo = stringTwo.replace(maxPlayersRemovalRegex.toRegex(), "").toIntOrNull() ?: 0

            Integer.compare(playersOne, playersTwo)
        }
    }

    private fun addServerUpdateListener() {
        view.serverTable.selectionModel.selectedIndices.addListener(InvalidationListener {
            killServerLookupThreads()

            if (view.serverTable.selectionModel.selectedIndices.size == 1) {
                val selectedServer = view.serverTable.selectionModel.selectedItem
                if (Objects.nonNull(selectedServer)) {
                    updateServerInfo(selectedServer)
                }
            } else {
                displayNoServerInfo()
            }
        })
    }

    @FXML
    private fun onClickAddToFavourites() {

        val address = ipAndPort

        address.ifPresent { data ->
            if (ServerUtility.isPortValid(data.value)) {
                addServerToFavourites(data.key, Integer.parseInt(data.value))
            } else {
                TrayNotificationBuilder()
                        .type(NotificationTypeImplementations.ERROR)
                        .title(Client.getString("addToFavourites"))
                        .message(Client.getString("cantAddToFavouritesAddressInvalid"))
                        .animation(Animations.POPUP)
                        .build()
                        .showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME)
            }
        }
    }

    private fun addServerToFavourites(ip: String, port: Int) {
        val newServer = FavouritesController.addServerToFavourites(ip, port)
        if (!view.serverTable.contains(newServer)) {
            view.serverTable.add(newServer)
        }
    }

    @FXML
    private fun onClickConnect() {
        val address = ipAndPort

        address.ifPresent { data ->
            if (ServerUtility.isPortValid(data.value)) {
                GTAController.tryToConnect(client, data.key, Integer.parseInt(data.value), "")
            } else {
                GTAController.showCantConnectToServerError()
            }
        }
    }

    @FXML
    private fun onFilterSettingsChange() {
        userFilterProperty.set(Predicate { server ->
            val nameFilterApplies: Boolean
            val modeFilterApplies: Boolean
            val languageFilterApplies: Boolean
            var versionFilterApplies = true

            if (!view.versionFilterComboBox.selectionModel.isEmpty) {
                val versionFilterSetting = view.versionFilterComboBox.selectionModel.selectedItem.toLowerCase()

                /*
				 * At this point and time i am assuring that the versio is not null, since in
				 * earlier versions of the backend i am using, the version wasn't part of the data
				 * one receives by default.
				 */
                val serverVersion = if (Objects.isNull(server.version)) "" else server.version
                versionFilterApplies = serverVersion.toLowerCase().contains(versionFilterSetting)
            }

            val nameFilterSetting = view.nameFilterTextField.text.toLowerCase()
            val modeFilterSetting = view.gamemodeFilterTextField.text.toLowerCase()
            val languageFilterSetting = view.languageFilterTextField.text.toLowerCase()

            val hostname = server.hostname.toLowerCase()
            val mode = server.mode.toLowerCase()
            val language = server.language.toLowerCase()

            if (view.regexCheckBox.isSelected) {
                nameFilterApplies = regexFilter(hostname, nameFilterSetting)
                modeFilterApplies = regexFilter(mode, modeFilterSetting)
                languageFilterApplies = regexFilter(language, languageFilterSetting)
            } else {
                nameFilterApplies = hostname.contains(nameFilterSetting)
                modeFilterApplies = mode.contains(modeFilterSetting)
                languageFilterApplies = language.contains(languageFilterSetting)
            }

            nameFilterApplies && modeFilterApplies && versionFilterApplies && languageFilterApplies
        })

        updateGlobalInfo()
    }

    /**
     * Updates the data that the [SampServer] holds and optionally displays the correct values
     * on the UI.
     *
     * @param server the [SampServer] object to update locally
     * @param applyDataToUI if true, the data of the server will be shown in the ui
     */
    private fun updateServerInfo(server: SampServer, applyDataToUI: Boolean = true) {
        killServerLookupThreads()

        synchronized(lookingUpForServer) {
            lookingUpForServer = Optional.of(server)
        }

        runIfLookupRunning(server, Runnable {
            if (applyDataToUI) {
                setVisibleDetailsToRetrieving(server)
            }
        })

        val serverInfoUpdateThread = Thread {
            try {
                SampQuery(server.address, server.port).use { query ->
                    val infoOptional = query.basicServerInfo
                    val serverRulesOptional = query.serversRules

                    if (infoOptional.isPresent && serverRulesOptional.isPresent) {
                        val info = infoOptional.get()
                        val serverRules = serverRulesOptional.get()

                        val activePlayers = Integer.parseInt(info[1])
                        val maxPlayers = Integer.parseInt(info[2])

                        server.isPassworded = StringUtility.stringToBoolean(info[0])
                        server.players = activePlayers
                        server.maxPlayers = maxPlayers
                        server.hostname = info[3]!!
                        server.mode = info[4]!!
                        server.language = info[5]!!
                        server.website = serverRules["weburl"]!!
                        server.version = serverRules["version"]!!
                        server.lagcomp = serverRules["lagcomp"]!!
                        server.map = serverRules["mapname"]!!

                        val ping = query.ping
                        val playerList = FXCollections.observableArrayList<Player>()

                        if (activePlayers <= 100) {
                            query.basicPlayerInfo.ifPresent { playerList.addAll(it) }
                        }

                        runIfLookupRunning(server, Runnable { applyData(server, playerList, ping) })
                        FavouritesController.updateServerData(server)

                    }

                    synchronized(lookingUpForServer) {
                        lookingUpForServer = Optional.empty()
                    }
                }
            } catch (exception: IOException) {
                runIfLookupRunning(server, Runnable {
                    Platform.runLater { this.displayOfflineInformations() }
                    lookingUpForServer = Optional.empty()
                })
            }
        }

        serverInfoUpdateThread.start()
    }

    private fun runIfLookupRunning(server: SampServer, runnable: Runnable) {
        synchronized(lookingUpForServer) {
            if (lookingUpForServer.isPresent && lookingUpForServer.get() == server) {
                runnable.run()
            }
        }
    }

    private fun setVisibleDetailsToRetrieving(server: SampServer) {
        view.playerTable.items.clear()
        view.serverAddressTextField.text = server.address + ":" + server.port
        view.serverWebsiteLink.isUnderline = false
        displayServerInfo(retrieving, retrieving, retrieving, retrieving, retrieving, null, retrieving)
    }

    private fun displayNoServerInfo() {
        view.playerTable.items.clear()
        view.serverAddressTextField.text = ""
        displayServerInfo("", "", "", "", "", null, "")
    }

    private fun applyData(server: SampServer, playerList: ObservableList<Player>, ping: Long) {
        runIfLookupRunning(server, Runnable {
            Platform.runLater {
                view.serverPasswordLabel.text = if (server.isPassworded) Client.getString("yes") else Client.getString("no")
                view.serverPingLabel.text = ping.toString()
                view.serverMapLabel.text = server.map
                view.serverWebsiteLink.text = server.website
                view.playerTable.items = playerList

                val websiteToLower = server.website.toLowerCase()
                val websiteFixed = StringUtility.fixUrlIfNecessary(websiteToLower)

                // drop validation since URL constructor does that anyways?
                if (StringUtility.isValidURL(websiteFixed)) {
                    view.serverWebsiteLink.isUnderline = true
                    view.serverWebsiteLink.setOnAction { OSUtility.browse(server.website) }
                }

                if (playerList.isEmpty()) {
                    if (server.players!! >= 100) {
                        val label = Label(tooMuchPlayers)
                        label.isWrapText = true
                        label.alignment = Pos.CENTER
                        view.playerTable.setPlaceholder(label)
                    } else {
                        view.playerTable.setPlaceholder(Label(serverEmpty))
                    }
                }

                view.serverLagcompLabel.text = server.lagcomp
                updateGlobalInfo()
            }
        })
    }

    private fun displayOfflineInformations() {
        displayServerInfo(serverOffline, "", "", "", "", null, serverOffline)
    }

    private fun displayServerInfo(ping: String, password: String, map: String, lagcomp: String, website: String,
                                  websiteClickHandler: EventHandler<ActionEvent>?, playerTablePlaceholder: String) {
        view.serverPingLabel.text = ping
        view.serverPasswordLabel.text = password
        view.serverMapLabel.text = map
        view.serverLagcompLabel.text = lagcomp
        view.serverWebsiteLink.text = website
        // Not using setVisible because i don't want the items to resize or anything
        view.serverWebsiteLink.onAction = websiteClickHandler
        view.playerTable.placeholder = Label(playerTablePlaceholder)
    }

    /**
     * Updates the [Labels][Label] at the bottom of the Serverlist view.
     */
    private fun updateGlobalInfo() {
        var playersPlaying = 0

        for (server in view.serverTable.items) {
            playersPlaying += server.players!!
        }

        setServerCount(view.serverTable.items.size)
        setPlayerCount(playersPlaying)
    }

    override fun onClose() {
        killServerLookupThreads()
    }

    companion object {
        private var serverLookup: Thread? = null
        private val SERVER_ADDRESS_PROPERTY = SimpleStringProperty()

        private fun setupInfoLabel(label: Label) {
            label.maxHeight = java.lang.Double.MAX_VALUE
            label.maxWidth = java.lang.Double.MAX_VALUE
            label.textAlignment = TextAlignment.CENTER

            HBox.setHgrow(label, Priority.ALWAYS)
        }

        private fun killServerLookupThreads() {
            if (Objects.nonNull(serverLookup)) {
                serverLookup!!.interrupt()
            }
        }

        private fun regexFilter(toFilter: String, filterSetting: String): Boolean {
            if (filterSetting.isEmpty()) {
                return true
            }

            try {
                return toFilter.matches(filterSetting.toRegex())
            } catch (exception: PatternSyntaxException) {
                return false
            }

        }
    }
}
