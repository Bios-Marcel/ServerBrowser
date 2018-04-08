package com.msc.serverbrowser.gui.views

import com.msc.serverbrowser.Client
import com.msc.serverbrowser.data.entites.Player
import com.msc.serverbrowser.data.entites.SampServer
import com.msc.serverbrowser.gui.components.SampServerTable
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.RadioButton
import javafx.scene.control.Separator
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.control.ToggleGroup
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.RowConstraints
import javafx.scene.layout.VBox
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ServerView(client: Client) {
    /**
     * Root-container of this view.
     */
    val rootPane: VBox

    val addressTextField: TextField
    val connectButton: Button
    val addToFavouritesButton: Button


    val tableTypeToggleGroup: ToggleGroup
    val favouriteButton: RadioButton
    val historyButton: RadioButton
    val allButton: RadioButton

    val serverTable: SampServerTable
    val lastJoinTableColumn: TableColumn<SampServer, Long>
    val playersTableColumn: TableColumn<SampServer, String>

    val playerTable: TableView<Player>

    val regexCheckBox: CheckBox
    val nameFilterTextField: TextField
    val gamemodeFilterTextField: TextField
    val languageFilterTextField: TextField
    val versionFilterComboBox: ComboBox<String>

    val serverAddressTextField: TextField
    val serverPingLabel: Label
    val serverLagcompLabel: Label
    val serverPasswordLabel: Label
    val serverMapLabel: Label
    val serverWebsiteLink: Hyperlink

    init {
        /**
         * Addressfield and Buttons at the top
         */
        addressTextField = TextField()
        addressTextField.promptText = Client.getString("promptEnterValidAddress")
        connectButton = Button(Client.getString("connect"))
        addToFavouritesButton = Button(Client.getString("addToFavourites"))
        val addressFieldContainer = HBox(5.0, addressTextField, connectButton, addToFavouritesButton)
        HBox.setHgrow(addressTextField, Priority.ALWAYS)

        /**
         * Buttons for switching the table between favourites-, all- and historymode
         */

        favouriteButton = RadioButton(Client.getString("favourites"))
        allButton = RadioButton(Client.getString("all"))
        historyButton = RadioButton(Client.getString("history"))

        favouriteButton.isSelected = true

        tableTypeToggleGroup = ToggleGroup()
        tableTypeToggleGroup.toggles.addAll(favouriteButton, allButton, historyButton)
        val tableTypeButtonContainer = HBox(5.0, favouriteButton, allButton, historyButton)

        HBox.setHgrow(favouriteButton, Priority.ALWAYS)
        HBox.setHgrow(allButton, Priority.ALWAYS)
        HBox.setHgrow(historyButton, Priority.ALWAYS)

        val tableButtonTypeWidthBinding = tableTypeButtonContainer.widthProperty().divide(0.33)
        favouriteButton.prefWidthProperty().bind(tableButtonTypeWidthBinding)
        allButton.prefWidthProperty().bind(tableButtonTypeWidthBinding)
        historyButton.prefWidthProperty().bind(tableButtonTypeWidthBinding)

        favouriteButton.styleClass.add("server-type-switch-button")
        allButton.styleClass.add("server-type-switch-button")
        historyButton.styleClass.add("server-type-switch-button")

        /**
         * Servertable
         */

        serverTable = SampServerTable(client)
        serverTable.maxHeight = Double.MAX_VALUE

        val hostnameTableColumn = TableColumn<SampServer, String>(Client.getString("hostnameTableHeader"))
        playersTableColumn = TableColumn(Client.getString("playersTableHeader"))
        val gamemodeTableColumn = TableColumn<SampServer, String>(Client.getString("gamemodeTableHeader"))
        val languageTableColumn = TableColumn<SampServer, String>(Client.getString("languageTableHeader"))
        val versionTableColum = TableColumn<SampServer, String>(Client.getString("versionTableHeader"))
        lastJoinTableColumn = TableColumn(Client.getString("columnLastJoin"))

        hostnameTableColumn.cellValueFactory = PropertyValueFactory<SampServer, String>("hostname")
        playersTableColumn.cellValueFactory = PropertyValueFactory<SampServer, String>("playersAndMaxPlayers")
        gamemodeTableColumn.cellValueFactory = PropertyValueFactory<SampServer, String>("mode")
        languageTableColumn.cellValueFactory = PropertyValueFactory<SampServer, String>("language")
        versionTableColum.cellValueFactory = PropertyValueFactory<SampServer, String>("version")
        lastJoinTableColumn.cellValueFactory = PropertyValueFactory<SampServer, Long>("lastJoin")
        lastJoinTableColumn.setCellFactory {
            object : TableCell<SampServer, Long>() {
                override fun updateItem(item: Long?, empty: Boolean) {
                    if (empty.not() && item != null) {
                        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(item), ZoneId.systemDefault())
                        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
                        text = dateFormat.format(date)
                    }
                }
            }
        }

        serverTable.columns.addAll(hostnameTableColumn, playersTableColumn, gamemodeTableColumn, languageTableColumn, versionTableColum, lastJoinTableColumn)
        serverTable.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

        /**
         * Playertable
         */

        playerTable = TableView()

        val playerNameTableColumn = TableColumn<Player, String>(Client.getString("playerTableHeader"))
        val playerScoreTableColumn = TableColumn<Player, Int>(Client.getString("scoreTableHeader"))

        playerNameTableColumn.cellValueFactory = PropertyValueFactory<Player, String>("playerName")
        playerScoreTableColumn.cellValueFactory = PropertyValueFactory<Player, Int>("playerScore")

        val playerScoreTableColumnWidth = 85.0
        playerScoreTableColumn.maxWidth = playerScoreTableColumnWidth
        playerScoreTableColumn.minWidth = playerScoreTableColumnWidth

        playerTable.prefHeight = 200.0
        playerTable.columns.addAll(playerNameTableColumn, playerScoreTableColumn)
        playerTable.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

        /**
         * Filterarea
         */

        regexCheckBox = CheckBox()
        nameFilterTextField = TextField()
        gamemodeFilterTextField = TextField()
        languageFilterTextField = TextField()
        versionFilterComboBox = ComboBox()

        val filterPanelContent = GridPane()
        with(filterPanelContent) {
            vgap = 5.0
            hgap = 5.0

            val constraintsColumnTitle = ColumnConstraints()
            constraintsColumnTitle.hgrow = Priority.NEVER
            constraintsColumnTitle.minWidth = Region.USE_PREF_SIZE

            val constraintsColumnValue = ColumnConstraints()
            constraintsColumnValue.hgrow = Priority.ALWAYS

            columnConstraints.addAll(constraintsColumnTitle, constraintsColumnValue)

            rowConstraints.add(RowConstraints())
            rowConstraints.add(RowConstraints())

            for (i in 0 until 4) {
                val rowConstraint = RowConstraints()
                rowConstraint.vgrow = Priority.ALWAYS
                rowConstraints.add(rowConstraint)
            }

            add(Label(Client.getString("useRegex")), 0, 0)
            add(regexCheckBox, 1, 0)

            add(Separator(Orientation.HORIZONTAL), 0, 1, 2, 1)

            add(Label(Client.getString("hostnameTableHeader")), 0, 2)
            add(nameFilterTextField, 1, 2)

            add(Label(Client.getString("gamemodeTableHeader")), 0, 3)
            add(gamemodeFilterTextField, 1, 3)

            add(Label(Client.getString("languageTableHeader")), 0, 4)
            add(languageFilterTextField, 1, 4)

            add(Label(Client.getString("versionTableHeader")), 0, 5)
            add(versionFilterComboBox, 1, 5)
            versionFilterComboBox.maxWidth = Double.MAX_VALUE
        }

        val filterPanel = TitledPane(Client.getString("filterSettings"), filterPanelContent)
        filterPanel.isCollapsible = false
        filterPanel.maxHeight = Double.MAX_VALUE

        /**
         * Serverinfo area
         */

        serverAddressTextField = TextField()
        serverAddressTextField.styleClass.add("copyableLabel")
        serverAddressTextField.isEditable = false
        serverPingLabel = Label()
        serverLagcompLabel = Label()
        serverPasswordLabel = Label()
        serverMapLabel = Label()
        serverWebsiteLink = Hyperlink()
        serverWebsiteLink.isUnderline = true

        val serverInfoPanelContent = GridPane()
        with(serverInfoPanelContent) {
            vgap = 5.0
            hgap = 10.0

            val constraintsColumnTitle = ColumnConstraints()
            constraintsColumnTitle.hgrow = Priority.NEVER
            constraintsColumnTitle.minWidth = Region.USE_PREF_SIZE

            val constraintsColumnValue = ColumnConstraints()
            constraintsColumnValue.hgrow = Priority.NEVER

            columnConstraints.addAll(constraintsColumnTitle, constraintsColumnValue)

            for (i in 0 until 6) {
                val rowConstraint = RowConstraints()
                rowConstraint.vgrow = Priority.ALWAYS
                rowConstraints.add(rowConstraint)
            }

            add(Label(Client.getString("address")), 0, 0)
            add(serverAddressTextField, 1, 0)

            add(Label(Client.getString("ping")), 0, 1)
            add(serverPingLabel, 1, 1)

            add(Label(Client.getString("lagcomp")), 0, 2)
            add(serverLagcompLabel, 1, 2)

            add(Label(Client.getString("password")), 0, 3)
            add(serverPasswordLabel, 1, 3)

            add(Label(Client.getString("map")), 0, 4)
            add(serverMapLabel, 1, 4)

            add(Label(Client.getString("website")), 0, 5)
            add(serverWebsiteLink, 1, 5)
        }

        val serverInfoPanel = TitledPane(Client.getString("serverInfo"), serverInfoPanelContent)
        serverInfoPanel.isCollapsible = false
        serverInfoPanel.maxHeight = Double.MAX_VALUE

        /**
         * Bottom area
         */

        val bottomHBox = HBox(5.0, playerTable, filterPanel, serverInfoPanel)
        HBox.setHgrow(filterPanel, Priority.ALWAYS)

        /**
         * Root
         */

        rootPane = VBox(5.0, addressFieldContainer, tableTypeButtonContainer, serverTable, bottomHBox)
        VBox.setVgrow(serverTable, Priority.ALWAYS)
    }
}