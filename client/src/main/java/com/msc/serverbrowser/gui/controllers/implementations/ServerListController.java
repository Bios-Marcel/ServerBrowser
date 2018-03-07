package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations;
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder;
import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.FavouritesController;
import com.msc.serverbrowser.data.ServerConfig;
import com.msc.serverbrowser.data.entites.Player;
import com.msc.serverbrowser.data.entites.SampServer;
import com.msc.serverbrowser.gui.components.SampServerTable;
import com.msc.serverbrowser.gui.components.SampServerTableMode;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.ServerUtility;
import com.msc.serverbrowser.util.basic.StringUtility;
import com.msc.serverbrowser.util.samp.GTAController;
import com.msc.serverbrowser.util.samp.SampQuery;
import com.msc.serverbrowser.util.windows.OSUtility;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

/**
 * Controller for the Server view.
 *
 * @author Marcel
 * @since 02.07.2017
 */
public class ServerListController implements ViewController {
	private static Thread serverLookup;

	private final String RETRIEVING = Client.getString("retrieving");

	private final String	TOO_MUCH_PLAYERS	= Client.getString("tooMuchPlayers");
	private final String	SERVER_OFFLINE		= Client.getString("serverOffline");
	private final String	SERVER_EMPTY		= Client.getString("serverEmpty");

	@FXML
	private ToggleGroup tableTypeToggleGroup;

	@FXML
	private TextField					addressTextField;
	private final static StringProperty	SERVER_ADDRESS_PROPERTY	= new SimpleStringProperty();

	private final ObjectProperty<Predicate<SampServer>>	userFilterProperty	= new SimpleObjectProperty<>(__ -> true);
	private final ObjectProperty<Predicate<SampServer>>	dataFilterProperty	= new SimpleObjectProperty<>(__ -> true);
	private final ObjectProperty<Predicate<SampServer>>	filterProperty		= new SimpleObjectProperty<>(__ -> true);

	/**
	 * This Table contains all available servers / favourite servers, depending on the active view.
	 */
	@FXML
	protected SampServerTable serverTable;

	/**
	 * Displays the number of active players on all Servers in {@link #serverTable}.
	 */
	private Label	playerCount;
	/**
	 * Number of servers in {@link #serverTable}.
	 */
	private Label	serverCount;

	@FXML
	private TextField	serverAddress;
	@FXML
	private Label		serverLagcomp;
	@FXML
	private Label		serverPing;
	@FXML
	private Label		serverPassword;
	@FXML
	private Label		mapLabel;
	@FXML
	private Hyperlink	websiteLink;

	@FXML
	private TableView<Player> playerTable;

	@FXML
	private TableColumn<SampServer, String>	columnPlayers;
	@FXML
	private TableColumn<SampServer, Long>	columnLastJoin;

	@FXML
	private CheckBox			regexCheckBox;
	@FXML
	private TextField			nameFilter;
	@FXML
	private TextField			modeFilter;
	@FXML
	private TextField			languageFilter;
	@FXML
	private ComboBox<String>	versionFilter;

	private Optional<SampServer> lookingUpForServer = Optional.empty();

	@Override
	public void initialize() {
		playerCount = new Label();
		serverCount = new Label();

		setPlayerCount(0);
		setServerCount(0);

		Client.getInstance().addItemsToBottomBar(playerCount, serverCount);

		setupInfoLabel(playerCount);
		setupInfoLabel(serverCount);

		userFilterProperty.addListener(__ -> updateFilterProperty());
		dataFilterProperty.addListener(__ -> updateFilterProperty());

		serverTable.predicateProperty().bind(filterProperty);
		serverTable.sortedListComparatorProperty().bind(serverTable.comparatorProperty());
		addressTextField.textProperty().bindBidirectional(SERVER_ADDRESS_PROPERTY);

		setPlayerComparator();
		addServerUpdateListener();

		toggleFavouritesMode();

		columnLastJoin.setCellFactory(factory -> {
			final TableCell<SampServer, Long> cell = new TableCell<SampServer, Long>() {
				@Override
				protected void updateItem(final Long item, final boolean empty) {
					if (!empty && Objects.nonNull(item)) {
						final LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(item), ZoneId.systemDefault());
						final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
						setText(dateFormat.format(date));
					}
				}
			};

			return cell;
		});

		/**
		 * Hack in order to remove the dot of the radiobuttons.
		 */
		tableTypeToggleGroup.getToggles().forEach(toggle -> {
			((Node) toggle).getStyleClass().remove("radio-button");
		});
	}

	private void updateFilterProperty() {
		filterProperty.set(userFilterProperty.get().and(dataFilterProperty.get()));
	}

	@FXML
	private void toggleFavouritesMode() {
		toggleMode(SampServerTableMode.FAVOURITES);
	}

	@FXML
	private void toggleAllMode() {
		toggleMode(SampServerTableMode.ALL);
	}

	@FXML
	private void toggleHistoryMode() {
		toggleMode(SampServerTableMode.HISTORY);
	}

	private void toggleMode(final SampServerTableMode mode) {
		killServerLookupThreads();
		serverTable.setServerTableMode(mode);
		serverTable.getSelectionModel().clearSelection();
		displayNoServerInfo();
		serverTable.clear();

		switch (mode) {
			case ALL:
				columnLastJoin.setVisible(false);
				serverTable.setPlaceholder(new Label(Client.getString("fetchingServers")));
				fillTableWithOnlineServerList();
				break;
			case FAVOURITES:
				columnLastJoin.setVisible(false);
				serverTable.setPlaceholder(new Label(Client.getString("noFavouriteServers")));
				serverTable.addAll(FavouritesController.getFavourites());
				ServerConfig.initLastJoinData(serverTable.getDataList());
				break;
			case HISTORY:
				serverTable.setPlaceholder(new Label(Client.getString("noServerHistory")));
				columnLastJoin.setVisible(true);
				final List<SampServer> servers = ServerConfig.getLastJoinedServers();
				servers.forEach(server -> updateServerInfo(server, false));
				serverTable.addAll(servers);
				break;
		}

		serverTable.refresh();
		updateGlobalInfo();
	}

	private void fillTableWithOnlineServerList() {
		serverLookup = new Thread(() -> {
			try {
				final List<SampServer> serversToAdd = ServerUtility.fetchServersFromSouthclaws();
				ServerConfig.initLastJoinData(serversToAdd);
				if (Objects.nonNull(serverLookup) && !serverLookup.isInterrupted() && serverTable.getTableMode() == SampServerTableMode.ALL) {
					Platform.runLater(() -> {
						serverTable.addAll(serversToAdd);
						serverTable.refresh();
					});
				}
			}
			catch (final IOException exception) {
				Logging.error("Couldn't retrieve data from announce api.", exception);
				Platform.runLater(() -> serverTable.setPlaceholder(new Label(Client.getString("errorFetchingServers"))));
			}

			Platform.runLater(() -> updateGlobalInfo());
		});
		serverLookup.start();
	}

	private static void setupInfoLabel(final Label label) {
		label.setMaxHeight(Double.MAX_VALUE);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setTextAlignment(TextAlignment.CENTER);

		HBox.setHgrow(label, Priority.ALWAYS);
	}

	/**
	 * Sets the text for the label that states how many active players there are.
	 *
	 * @param activePlayers the number of active players
	 */
	protected void setPlayerCount(final int activePlayers) {
		playerCount.setText(MessageFormat.format(Client.getString("activePlayers"), activePlayers));
	}

	/**
	 * Sets the text for the label that states how many active servers there are.
	 *
	 * @param activeServers the number of active servers
	 */
	private void setServerCount(final int activeServers) {
		serverCount.setText(MessageFormat.format(Client.getString("servers"), activeServers));
	}

	private void setPlayerComparator() {
		columnPlayers.setComparator((stringOne, stringTwo) -> {
			final String maxPlayersRemovalRegex = "/.*";
			final int playersOne = Integer.parseInt(stringOne.replaceAll(maxPlayersRemovalRegex, ""));
			final int playersTwo = Integer.parseInt(stringTwo.replaceAll(maxPlayersRemovalRegex, ""));

			return Integer.compare(playersOne, playersTwo);
		});
	}

	private void addServerUpdateListener() {
		serverTable.getSelectionModel().getSelectedIndices().addListener((InvalidationListener) changed -> {
			killServerLookupThreads();

			if (serverTable.getSelectionModel().getSelectedIndices().size() == 1) {
				final SampServer selectedServer = serverTable.getSelectionModel().getSelectedItem();
				if (Objects.nonNull(selectedServer)) {
					updateServerInfo(selectedServer);
				}
			}
			else {
				displayNoServerInfo();
			}
		});
	}

	private static void killServerLookupThreads() {
		if (Objects.nonNull(serverLookup)) {
			serverLookup.interrupt();
		}
	}

	@FXML
	private void onClickAddToFavourites() {

		final Optional<Pair<String, String>> address = getIpAndPort();

		address.ifPresent(data -> {
			if (ServerUtility.isPortValid(data.getValue())) {
				addServerToFavourites(data.getKey(), Integer.parseInt(data.getValue()));
			}
			else {
				new TrayNotificationBuilder()
						.type(NotificationTypeImplementations.ERROR)
						.title(Client.getString("addToFavourites"))
						.message(Client.getString("cantAddToFavouritesAddressInvalid"))
						.animation(Animations.POPUP)
						.build()
						.showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);
			}
		});
	}

	private void addServerToFavourites(final String ip, final int port) {
		final SampServer newServer = FavouritesController.addServerToFavourites(ip, port);
		if (!serverTable.contains(newServer)) {
			serverTable.add(newServer);
		}
	}

	@FXML
	private void onClickConnect() {

		final Optional<Pair<String, String>> address = getIpAndPort();

		address.ifPresent(data -> {
			if (ServerUtility.isPortValid(data.getValue())) {
				GTAController.tryToConnect(data.getKey(), Integer.parseInt(data.getValue()), "");
			}
			else {
				GTAController.showCantConnectToServerError();
			}
		});
	}

	private Optional<Pair<String, String>> getIpAndPort() {
		final String address = addressTextField.getText();

		if (Objects.nonNull(address) && !address.isEmpty()) {
			final String[] ipAndPort = addressTextField.getText().split("[:]");
			if (ipAndPort.length == 1) {
				return Optional.of(new Pair<>(ipAndPort[0], ServerUtility.DEFAULT_SAMP_PORT.toString()));
			}
			else if (ipAndPort.length == 2) {
				return Optional.of(new Pair<>(ipAndPort[0], ipAndPort[1]));
			}
		}

		return Optional.empty();
	}

	@FXML
	private void onFilterSettingsChange() {
		userFilterProperty.set(server -> {
			boolean nameFilterApplies = true;
			boolean modeFilterApplies = true;
			boolean languageFilterApplies = true;
			boolean versionFilterApplies = true;

			if (!versionFilter.getSelectionModel().isEmpty()) {
				final String versionFilterSetting = versionFilter.getSelectionModel().getSelectedItem().toString().toLowerCase();

				/*
				 * At this point and time i am assuring that the versio is not null, since in
				 * earlier versions of the backend i am using, the version wasn't part of the data
				 * one receives by default.
				 */
				final String serverVersion = Objects.isNull(server.getVersion()) ? "" : server.getVersion();
				versionFilterApplies = serverVersion.toLowerCase().contains(versionFilterSetting);
			}

			final String nameFilterSetting = nameFilter.getText().toLowerCase();
			final String modeFilterSetting = modeFilter.getText().toLowerCase();
			final String languageFilterSetting = languageFilter.getText().toLowerCase();

			final String hostname = server.getHostname().toLowerCase();
			final String mode = server.getMode().toLowerCase();
			final String language = server.getLanguage().toLowerCase();

			if (regexCheckBox.isSelected()) {
				nameFilterApplies = regexFilter(hostname, nameFilterSetting);
				modeFilterApplies = regexFilter(mode, modeFilterSetting);
				languageFilterApplies = regexFilter(language, languageFilterSetting);
			}
			else {
				nameFilterApplies = hostname.contains(nameFilterSetting);
				modeFilterApplies = mode.contains(modeFilterSetting);
				languageFilterApplies = language.contains(languageFilterSetting);
			}

			return nameFilterApplies && modeFilterApplies && versionFilterApplies && languageFilterApplies;
		});

		updateGlobalInfo();
	}

	private static boolean regexFilter(final String toFilter, final String filterSetting) {
		if (filterSetting.isEmpty()) {
			return true;
		}

		try {
			return toFilter.matches(filterSetting);
		}
		catch (@SuppressWarnings("unused") final PatternSyntaxException exception) {
			return false;
		}
	}

	/**
	 * Updates the data that the {@link SampServer} holds.
	 *
	 * @param server the {@link SampServer} object to update locally
	 */
	private void updateServerInfo(final SampServer server) {
		updateServerInfo(server, true);
	}

	/**
	 * Updates the data that the {@link SampServer} holds and optionally displays the correct values
	 * on the UI.
	 *
	 * @param server the {@link SampServer} object to update locally
	 * @param applyDataToUI if true, the data of the server will be shown in the ui
	 */
	private void updateServerInfo(final SampServer server, final boolean applyDataToUI) {
		killServerLookupThreads();

		synchronized (lookingUpForServer) {
			lookingUpForServer = Optional.of(server);
		}

		runIfLookupRunning(server, () -> {
			if (applyDataToUI) {
				setVisibleDetailsToRetrieving(server);
			}
		});

		final Thread serverInfoUpdateThread = new Thread(() -> {
			try (SampQuery query = new SampQuery(server.getAddress(), server.getPort())) {
				final Optional<String[]> infoOptional = query.getBasicServerInfo();
				final Optional<Map<String, String>> serverRulesOptional = query.getServersRules();

				if (infoOptional.isPresent() && serverRulesOptional.isPresent()) {
					final String[] info = infoOptional.get();
					final Map<String, String> serverRules = serverRulesOptional.get();

					final int activePlayers = Integer.parseInt(info[1]);
					final int maxPlayers = Integer.parseInt(info[2]);

					server.setPassworded(StringUtility.stringToBoolean(info[0]));
					server.setPlayers(activePlayers);
					server.setMaxPlayers(maxPlayers);
					server.setHostname(info[3]);
					server.setMode(info[4]);
					server.setLanguage(info[5]);
					server.setWebsite(serverRules.get("weburl"));
					server.setVersion(serverRules.get("version"));
					server.setLagcomp(serverRules.get("lagcomp"));
					server.setMap(serverRules.get("mapname"));

					final long ping = query.getPing();
					final ObservableList<Player> playerList = FXCollections.observableArrayList();

					if (activePlayers <= 100) {
						query.getBasicPlayerInfo().ifPresent(players -> playerList.addAll(players));
					}

					runIfLookupRunning(server, () -> {
						applyData(server, playerList, ping);
					});
					FavouritesController.updateServerData(server);

				}

				synchronized (lookingUpForServer) {
					lookingUpForServer = Optional.empty();
				}
			}
			catch (@SuppressWarnings("unused") final IOException exception) {
				runIfLookupRunning(server, () -> {
					Platform.runLater(() -> displayOfflineInformations());
					lookingUpForServer = Optional.empty();
				});
			}
		});

		serverInfoUpdateThread.start();
	}

	private void runIfLookupRunning(final SampServer server, final Runnable runnable) {
		synchronized (lookingUpForServer) {
			if (lookingUpForServer.isPresent() && lookingUpForServer.get().equals(server)) {
				runnable.run();
			}
		}
	}

	private void setVisibleDetailsToRetrieving(final SampServer server) {
		playerTable.getItems().clear();
		serverAddress.setText(server.getAddress() + ":" + server.getPort());
		websiteLink.setUnderline(false);
		displayServerInfo(RETRIEVING, RETRIEVING, RETRIEVING, RETRIEVING, RETRIEVING, null, RETRIEVING);
	}

	private void displayNoServerInfo() {
		playerTable.getItems().clear();
		serverAddress.setText("");
		displayServerInfo("", "", "", "", "", null, "");
	}

	private void applyData(final SampServer server, final ObservableList<Player> playerList, final long ping) {
		runIfLookupRunning(server, () -> {
			Platform.runLater(() -> {
				serverPassword.setText(server.isPassworded() ? Client.getString("yes") : Client.getString("no"));
				serverPing.setText(String.valueOf(ping));
				mapLabel.setText(server.getMap());
				websiteLink.setText(server.getWebsite());
				playerTable.setItems(playerList);

				final String websiteToLower = server.getWebsite().toLowerCase();
				final String websiteFixed = StringUtility.fixUrlIfNecessary(websiteToLower);

				if (StringUtility.isValidURL(websiteFixed)) {
					websiteLink.setUnderline(true);
					websiteLink.setOnAction(__ -> OSUtility.browse(server.getWebsite()));
				}

				final boolean noPlayers = playerList.isEmpty();
				if (noPlayers) {
					playerTable.setPlaceholder(new Label(SERVER_EMPTY));

					if (server.getPlayers() >= 100) {
						final Label label = new Label(TOO_MUCH_PLAYERS);
						label.setWrapText(true);
						label.setAlignment(Pos.CENTER);
						playerTable.setPlaceholder(label);
					}
				}

				serverLagcomp.setText(server.getLagcomp());
				updateGlobalInfo();
			});
		});
	}

	private void displayOfflineInformations() {
		displayServerInfo(SERVER_OFFLINE, "", "", "", "", null, SERVER_OFFLINE);
	}

	private void displayServerInfo(final String ping, final String password, final String map, final String lagcomp, final String website,
			final EventHandler<ActionEvent> websiteClickHandler, final String playerTablePlaceholder) {
		serverPing.setText(ping);
		serverPassword.setText(password);
		mapLabel.setText(map);
		serverLagcomp.setText(lagcomp);
		websiteLink.setText(website);
		// Not using setVisible because i dont want the items to resize or anything
		websiteLink.setOnAction(websiteClickHandler);
		playerTable.setPlaceholder(new Label(playerTablePlaceholder));
	}

	/**
	 * Updates the {@link Label Labels} at the bottom of the Serverlist view.
	 */
	protected void updateGlobalInfo() {
		int playersPlaying = 0;

		for (final SampServer server : serverTable.getItems()) {
			playersPlaying += server.getPlayers();
		}

		setServerCount(serverTable.getItems().size());
		setPlayerCount(playersPlaying);
	}

	@Override
	public void onClose() {
		killServerLookupThreads();
	}
}
