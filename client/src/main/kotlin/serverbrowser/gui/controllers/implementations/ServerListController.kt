package serverbrowser.gui.controllers.implementations

import com.github.plushaze.traynotification.animations.Animations
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder
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
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import javafx.util.Pair
import serverbrowser.Client
import serverbrowser.data.FavouritesController
import serverbrowser.data.ServerConfig
import serverbrowser.data.entites.Player
import serverbrowser.data.entites.SampServer
import serverbrowser.gui.components.SampServerTable
import serverbrowser.gui.components.SampServerTableMode
import serverbrowser.gui.controllers.interfaces.ViewController
import serverbrowser.logging.Logging
import serverbrowser.util.ServerUtility
import serverbrowser.util.basic.StringUtility
import serverbrowser.util.samp.GTAController
import serverbrowser.util.samp.SampQuery
import serverbrowser.util.windows.OSUtility
import java.io.IOException
import java.text.MessageFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Predicate
import java.util.regex.PatternSyntaxException

/**
 * Controller for the Server view.
 *
 * @author Marcel
 * @since 02.07.2017
 */
class ServerListController : ViewController {

    private val RETRIEVING = Client.getString("retrieving")

    private val TOO_MUCH_PLAYERS = Client.getString("tooMuchPlayers")
    private val SERVER_OFFLINE = Client.getString("serverOffline")
    private val SERVER_EMPTY = Client.getString("serverEmpty")

    @FXML
    private lateinit var tableTypeToggleGroup: ToggleGroup

    @FXML
    private lateinit var addressTextField: TextField

    private val userFilterProperty = SimpleObjectProperty<Predicate<SampServer>>(Predicate { true })
    private val dataFilterProperty = SimpleObjectProperty<Predicate<SampServer>>(Predicate { true })
    private val filterProperty = SimpleObjectProperty<Predicate<SampServer>>(Predicate { true })

    /**
     * This Table contains all available servers / favourite servers, depending on the active view.
     */
    @FXML
    private lateinit var serverTable: SampServerTable

    /**
     * Displays the number of active players on all Servers in [.serverTable].
     */
    private var playerCount: Label = Label()
    /**
     * Number of servers in [.serverTable].
     */
    private var serverCount: Label = Label()

    @FXML
    private lateinit var serverAddress: TextField
    @FXML
    private lateinit var serverLagcomp: Label
    @FXML
    private lateinit var serverPing: Label
    @FXML
    private lateinit var serverPassword: Label
    @FXML
    private lateinit var mapLabel: Label
    @FXML
    private lateinit var websiteLink: Hyperlink

    @FXML
    private lateinit var playerTable: TableView<Player>

    @FXML
    private lateinit var columnPlayers: TableColumn<SampServer, String>
    @FXML
    private lateinit var columnLastJoin: TableColumn<SampServer, Long>

    @FXML
    private lateinit var regexCheckBox: CheckBox
    @FXML
    private lateinit var nameFilter: TextField
    @FXML
    private lateinit var modeFilter: TextField
    @FXML
    private lateinit var languageFilter: TextField
    @FXML
    private lateinit var versionFilter: ComboBox<String>

    private var lookingUpForServer = Optional.empty<SampServer>()

    private val ipAndPort: Optional<Pair<String, String>>
        get() {
            val address = addressTextField.text

            if (Objects.nonNull(address) && !address.isEmpty()) {
                val ipAndPort = addressTextField.text.split("[:]".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
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

        Client.instance!!.addItemsToBottomBar(playerCount, serverCount)

        setupInfoLabel(playerCount)
        setupInfoLabel(serverCount)

        userFilterProperty.addListener { _ -> updateFilterProperty() }
        dataFilterProperty.addListener { _ -> updateFilterProperty() }

        serverTable.predicateProperty().bind(filterProperty)
        serverTable.sortedListComparatorProperty().bind(serverTable.comparatorProperty())
        addressTextField.textProperty().bindBidirectional(SERVER_ADDRESS_PROPERTY)

        setPlayerComparator()
        addServerUpdateListener()

        toggleFavouritesMode()

        columnLastJoin.setCellFactory {
            object : TableCell<SampServer, Long>() {
                override fun updateItem(item: Long?, empty: Boolean) {
                    if (!empty && Objects.nonNull(item)) {
                        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(item!!), ZoneId.systemDefault())
                        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
                        text = dateFormat.format(date)
                    }
                }
            }
        }

        /*
		 * Hack in order to remove the dot of the radiobuttons.
		 */
        tableTypeToggleGroup.toggles.forEach { toggle -> (toggle as Node).styleClass.remove("radio-button") }
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
        serverTable.setServerTableMode(mode)
        serverTable.selectionModel.clearSelection()
        displayNoServerInfo()
        serverTable.clear()

        when (mode) {
            SampServerTableMode.ALL -> {
                columnLastJoin.isVisible = false
                serverTable.placeholder = Label(Client.getString("fetchingServers"))
                fillTableWithOnlineServerList()
            }
            SampServerTableMode.FAVOURITES -> {
                columnLastJoin.isVisible = false
                serverTable.placeholder = Label(Client.getString("noFavouriteServers"))
                serverTable.addAll(FavouritesController.favourites)
                ServerConfig.initLastJoinData(serverTable.items)
            }
            SampServerTableMode.HISTORY -> {
                serverTable.placeholder = Label(Client.getString("noServerHistory"))
                columnLastJoin.isVisible = true
                val servers = ServerConfig.lastJoinedServers
                servers.forEach({ server -> updateServerInfo(server, false) })
                serverTable.addAll(servers)
            }
        }

        serverTable.refresh()
        updateGlobalInfo()
    }

    private fun fillTableWithOnlineServerList() {
        serverLookup = Thread {
            try {
                val serversToAdd = ServerUtility.fetchServersFromSouthclaws()
                ServerConfig.initLastJoinData(serversToAdd)
                if (Objects.nonNull(serverLookup) && !serverLookup!!.isInterrupted && serverTable.tableMode == SampServerTableMode.ALL) {
                    Platform.runLater {
                        serverTable.addAll(serversToAdd)
                        serverTable.refresh()
                    }
                }
            } catch (exception: IOException) {
                Logging.error("Couldn't retrieve data from announce api.", exception)
                Platform.runLater { serverTable.placeholder = Label(Client.getString("errorFetchingServers")) }
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
        columnPlayers.setComparator { stringOne, stringTwo ->
            val maxPlayersRemovalRegex = "/.*"
            val playersOne = Integer.parseInt(stringOne.replace(maxPlayersRemovalRegex.toRegex(), ""))
            val playersTwo = Integer.parseInt(stringTwo.replace(maxPlayersRemovalRegex.toRegex(), ""))

            Integer.compare(playersOne, playersTwo)
        }
    }

    private fun addServerUpdateListener() {
        serverTable.selectionModel.selectedIndices.addListener(InvalidationListener {
            killServerLookupThreads()

            if (serverTable.selectionModel.selectedIndices.size == 1) {
                val selectedServer = serverTable.selectionModel.selectedItem
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
        if (!serverTable.contains(newServer)) {
            serverTable.add(newServer)
        }
    }

    @FXML
    private fun onClickConnect() {

        val address = ipAndPort

        address.ifPresent { data ->
            if (ServerUtility.isPortValid(data.value)) {
                GTAController.tryToConnect(data.key, Integer.parseInt(data.value), "")
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

            if (!versionFilter.selectionModel.isEmpty) {
                val versionFilterSetting = versionFilter.selectionModel.selectedItem.toLowerCase()

                /*
				 * At this point and time i am assuring that the versio is not null, since in
				 * earlier versions of the backend i am using, the version wasn't part of the data
				 * one receives by default.
				 */
                val serverVersion = if (Objects.isNull(server.version)) "" else server.version
                versionFilterApplies = serverVersion.toLowerCase().contains(versionFilterSetting)
            }

            val nameFilterSetting = nameFilter.text.toLowerCase()
            val modeFilterSetting = modeFilter.text.toLowerCase()
            val languageFilterSetting = languageFilter.text.toLowerCase()

            val hostname = server.hostname.toLowerCase()
            val mode = server.mode.toLowerCase()
            val language = server.language.toLowerCase()

            if (regexCheckBox.isSelected) {
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
                        server.website = serverRules["weburl"]
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
        playerTable.items.clear()
        serverAddress.text = server.address + ":" + server.port
        websiteLink.isUnderline = false
        displayServerInfo(RETRIEVING, RETRIEVING, RETRIEVING, RETRIEVING, RETRIEVING, null, RETRIEVING)
    }

    private fun displayNoServerInfo() {
        playerTable.items.clear()
        serverAddress.text = ""
        displayServerInfo("", "", "", "", "", null, "")
    }

    private fun applyData(server: SampServer, playerList: ObservableList<Player>, ping: Long) {
        runIfLookupRunning(server, Runnable {
            Platform.runLater {
                serverPassword.text = if (server.isPassworded) Client.getString("yes") else Client.getString("no")
                serverPing.text = ping.toString()
                mapLabel.text = server.map
                websiteLink.text = server.website
                playerTable.items = playerList

                val websiteToLower = server.website!!.toLowerCase()
                val websiteFixed = StringUtility.fixUrlIfNecessary(websiteToLower)

                // drop validation since URL constructor does that anyways?
                if (StringUtility.isValidURL(websiteFixed)) {
                    websiteLink.isUnderline = true
                    websiteLink.setOnAction { OSUtility.browse(server.website!!) }
                }

                if (playerList.isEmpty()) {
                    if (server.players!! >= 100) {
                        val label = Label(TOO_MUCH_PLAYERS)
                        label.isWrapText = true
                        label.alignment = Pos.CENTER
                        playerTable.setPlaceholder(label)
                    } else {
                        playerTable.setPlaceholder(Label(SERVER_EMPTY))
                    }
                }

                serverLagcomp.text = server.lagcomp
                updateGlobalInfo()
            }
        })
    }

    private fun displayOfflineInformations() {
        displayServerInfo(SERVER_OFFLINE, "", "", "", "", null, SERVER_OFFLINE)
    }

    private fun displayServerInfo(ping: String, password: String, map: String, lagcomp: String, website: String,
                                  websiteClickHandler: EventHandler<ActionEvent>?, playerTablePlaceholder: String) {
        serverPing.text = ping
        serverPassword.text = password
        mapLabel.text = map
        serverLagcomp.text = lagcomp
        websiteLink.text = website
        // Not using setVisible because i don't want the items to resize or anything
        websiteLink.onAction = websiteClickHandler
        playerTable.placeholder = Label(playerTablePlaceholder)
    }

    /**
     * Updates the [Labels][Label] at the bottom of the Serverlist view.
     */
    private fun updateGlobalInfo() {
        var playersPlaying = 0

        for (server in serverTable.items) {
            playersPlaying += server.players!!
        }

        setServerCount(serverTable.items.size)
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
