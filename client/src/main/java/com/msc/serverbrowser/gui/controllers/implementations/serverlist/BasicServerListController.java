package com.msc.serverbrowser.gui.controllers.implementations.serverlist;

import java.io.IOException;
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
import com.msc.serverbrowser.data.entites.Player;
import com.msc.serverbrowser.data.entites.SampServer;
import com.msc.serverbrowser.gui.components.SampServerTable;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
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
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * @since 02.07.2017
 */
public class BasicServerListController implements ViewController
{
	private static final String									RETRIEVING				= "Retrieving...";

	private final ObjectProperty<Predicate<? super SampServer>>	filterProperty			= new SimpleObjectProperty<>();

	@FXML
	private TextField											addressTextField;

	private final static StringProperty							SERVER_ADDRESS_PROPERTY	= new SimpleStringProperty();

	/**
	 * This Table contains all available servers / favourite servers, depending on
	 * the active view.
	 */
	@FXML
	protected SampServerTable									serverTable;

	/**
	 * Displays the number of active players on all Servers in {@link #serverTable}.
	 */
	private Label												playerCount;
	/**
	 * Displays the amount of all slots on all Servers in {@link #serverTable}.
	 */
	private Label												slotCount;
	/**
	 * Number of servers in {@link #serverTable}.
	 */
	private Label												serverCount;

	@FXML
	private TextField											serverAddress;
	@FXML
	private Label												serverLagcomp;
	@FXML
	private Label												serverPing;
	@FXML
	private Label												serverPassword;
	@FXML
	private Label												mapLabel;
	@FXML
	private Hyperlink											websiteLink;

	@FXML
	private TableView<Player>									playerTable;
	@FXML
	private TableColumn<SampServer, String>						columnPlayers;

	@FXML
	private CheckBox											regexCheckBox;
	@FXML
	private TextField											nameFilter;
	@FXML
	private TextField											modeFilter;
	@FXML
	private TextField											languageFilter;
	@FXML
	private ComboBox<String>									versionFilter;

	private static Thread										serverInfoUpdateThread;

	/**
	 * Empty Constructor.
	 */
	protected BasicServerListController()
	{
		// Prevent instantiation from outside.
	}

	@Override
	public void initialize()
	{
		playerCount = new Label("Active players: 0");
		serverCount = new Label("Servers: 0");
		slotCount = new Label("Free Slots: 0");

		Client.getInstance().addItemsToBottomBar(slotCount, playerCount, serverCount);

		serverTable.predicateProperty().bind(filterProperty);
		serverTable.sortedListComparatorProperty().bind(serverTable.comparatorProperty());
		addressTextField.textProperty().bindBidirectional(SERVER_ADDRESS_PROPERTY);

		setPlayerComparator();
		addServerUpdateListener();
	}

	protected void setPlayerCount(final int activePlayers)
	{
		playerCount.setText("Active players: " + activePlayers);
	}

	protected void setServerCount(final int activeServers)
	{
		serverCount.setText("Severs: " + activeServers);
	}

	protected void setFreeSlotCount(final int freeSlots)
	{
		slotCount.setText("Free slots: " + freeSlots);
	}

	private void setPlayerComparator()
	{
		columnPlayers.setComparator((o1, o2) ->
		{
			final int p1 = Integer.parseInt(o1.replaceAll("[/](.*)", ""));
			final int p2 = Integer.parseInt(o2.replaceAll("[/](.*)", ""));

			if (p1 < p2)
			{
				return -1;
			}
			else if (p1 == p2)
			{
				return 0;
			}
			else
			{
				return 1;
			}
		});
	}

	private void addServerUpdateListener()
	{
		serverTable.getSelectionModel().getSelectedCells().addListener((InvalidationListener) changed ->
		{
			if (serverTable.getSelectionModel().getSelectedIndices().size() == 1)
			{
				final SampServer selectedServer = serverTable.getSelectionModel().getSelectedItem();
				if (Objects.nonNull(selectedServer))
				{
					updateServerInfo(selectedServer);
				}
			}
			else
			{
				playerTable.getItems().clear();
				playerTable.setPlaceholder(new Label(""));
				serverAddress.setText("");
				serverLagcomp.setText("");
				serverPing.setText("");
				serverPassword.setText("");

				killServerLookupThread();
			}
		});
	}

	private static void killServerLookupThread()
	{
		if (Objects.nonNull(serverInfoUpdateThread))
		{
			serverInfoUpdateThread.interrupt();
		}
	}

	@FXML
	private void onClickAddToFavourites()
	{
		final String address = addressTextField.getText();
		if (Objects.nonNull(address) && !address.isEmpty())
		{
			final String[] ipAndPort = addressTextField.getText().split("[:]");
			if (ipAndPort.length == 1)
			{
				addServerToFavourites(ipAndPort[0], ServerUtility.DEFAULT_SAMP_PORT);
			}
			else if (ipAndPort.length == 2 && ServerUtility.isPortValid(ipAndPort[1]))
			{
				addServerToFavourites(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
			}
			else
			{
				new TrayNotificationBuilder()
						.type(NotificationTypeImplementations.ERROR)
						.title("Add to favourites")
						.message("Can't add to favourites, server address is invalid.")
						.animation(Animations.POPUP)
						.build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME);
			}
		}
	}

	private void addServerToFavourites(final String ip, final int port)
	{
		final SampServer newServer = FavouritesController.addServerToFavourites(ip, port);
		if (!serverTable.contains(newServer))
		{
			serverTable.add(newServer);
		}
	}

	@FXML
	private void onClickConnect()
	{
		final String[] ipAndPort = Optional.ofNullable(addressTextField.getText()).orElse("").split("[:]");
		if (ipAndPort.length == 1)
		{
			GTAController.tryToConnect(ipAndPort[0], ServerUtility.DEFAULT_SAMP_PORT);
		}
		else if (ipAndPort.length == 2 && ServerUtility.isPortValid(ipAndPort[1]))
		{
			GTAController.tryToConnect(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
		}
		else
		{
			GTAController.showCantConnectToServerError();
		}
	}

	@FXML
	private void onFilterSettingsChange()
	{
		filterProperty.set(server ->
		{
			boolean nameFilterApplies = true;
			boolean modeFilterApplies = true;
			boolean languageFilterApplies = true;
			boolean versionFilterApplies = true;

			if (!versionFilter.getSelectionModel().isEmpty())
			{
				final String versionFilterSetting = versionFilter.getSelectionModel().getSelectedItem().toString()
						.toLowerCase();

				// TODO(MSC) Only necessary because i don't retrieve the version when querying
				// southclaws api. I should request a change in the api.
				final String serverVersion = Objects.isNull(server.getVersion()) ? "" : server.getVersion();
				versionFilterApplies = serverVersion.toLowerCase().contains(versionFilterSetting);
			}

			final String nameFilterSetting = nameFilter.getText().toLowerCase();
			final String modeFilterSetting = modeFilter.getText().toLowerCase();
			final String languageFilterSetting = languageFilter.getText().toLowerCase();

			final String hostname = server.getHostname().toLowerCase();
			final String mode = server.getMode().toLowerCase();
			final String language = server.getLanguage().toLowerCase();

			if (regexCheckBox.isSelected())
			{
				nameFilterApplies = regexFilter(hostname, nameFilterSetting);
				modeFilterApplies = regexFilter(mode, modeFilterSetting);
				languageFilterApplies = regexFilter(language, languageFilterSetting);
			}
			else
			{
				nameFilterApplies = hostname.contains(nameFilterSetting);
				modeFilterApplies = mode.contains(modeFilterSetting);
				languageFilterApplies = language.contains(languageFilterSetting);
			}

			return nameFilterApplies && modeFilterApplies && versionFilterApplies && languageFilterApplies;
		});

		updateGlobalInfo();
	}

	private static boolean regexFilter(final String toFilter, final String filterSetting)
	{
		if (filterSetting.isEmpty())
		{
			return true;
		}

		try
		{
			return toFilter.matches(filterSetting);
		}
		catch (@SuppressWarnings("unused") final PatternSyntaxException exception)
		{
			return false;
		}
	}

	// TODO(MSC) Burn it before it lays eggs. Hans, get the Flammenwerfer.
	/**
	 * Updates the data that the {@link SampServer} holds and displays the correct
	 * values on the UI.
	 *
	 * @param server
	 *            the {@link SampServer} object to update locally
	 */
	private void updateServerInfo(final SampServer server)
	{
		setVisibleDetailsToRetrieving(server);
		killServerLookupThread();

		serverInfoUpdateThread = new Thread(() ->
		{
			try (final SampQuery query = new SampQuery(server.getAddress(), server.getPort()))
			{
				final Optional<String[]> infoOptional = query.getBasicServerInfo();
				final Optional<Map<String, String>> serverRulesOptional = query.getServersRules();

				if (infoOptional.isPresent() && serverRulesOptional.isPresent())
				{
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

					final ObservableList<Player> playerList = FXCollections.observableArrayList();
					query.getBasicPlayerInfo().ifPresent(players -> playerList.addAll(players));
					final long ping = query.getPing();

					applyData(server, playerList, ping);
					FavouritesController.updateServerData(server);
				}
			}
			catch (@SuppressWarnings("unused") final IOException exception)
			{
				if (!serverInfoUpdateThread.isInterrupted())
				{
					Platform.runLater(() -> displayOfflineInformation());
				}
			}
		});

		serverInfoUpdateThread.start();
	}

	private void setVisibleDetailsToRetrieving(final SampServer server)
	{
		playerTable.getItems().clear();
		playerTable.setPlaceholder(new Label(RETRIEVING));
		serverAddress.setText(server.getAddress() + ":" + server.getPort());
		serverLagcomp.setText(RETRIEVING);
		serverPing.setText(RETRIEVING);
		serverPassword.setText(RETRIEVING);
		mapLabel.setText(RETRIEVING);
		websiteLink.setText(RETRIEVING);
		websiteLink.setUnderline(false);
		websiteLink.setOnAction(null);
	}

	private void applyData(final SampServer server, final ObservableList<Player> playerList,
			final long ping)
	{
		if (!serverInfoUpdateThread.isInterrupted())
		{
			Platform.runLater(() ->
			{
				serverPassword.setText(server.isPassworded() ? "Yes" : "No");
				serverPing.setText(String.valueOf(ping));
				mapLabel.setText(server.getMap());
				websiteLink.setText(server.getWebsite());
				playerTable.setItems(playerList);

				final String websiteToLower = server.getWebsite().toLowerCase();
				final String websiteFixed = StringUtility.fixUrlIfNecessary(websiteToLower);

				if (StringUtility.isValidURL(websiteFixed))
				{
					websiteLink.setUnderline(true);
					websiteLink.setOnAction(__ -> OSUtility.browse(server.getWebsite()));
				}

				final boolean noPlayers = playerList.isEmpty();
				if (noPlayers)
				{
					playerTable.setPlaceholder(new Label("Server is empty"));

					if (server.getPlayers() >= 100)
					{
						final String TOO_MUCH_PLAYERS = "Can't retrieve players, if there are more than 100.";
						final Label label = new Label(TOO_MUCH_PLAYERS);
						label.setWrapText(true);
						label.setAlignment(Pos.CENTER);
						playerTable.setPlaceholder(label);
					}
				}

				serverLagcomp.setText(server.getLagcomp());
				updateGlobalInfo();
			});
		}
	}

	private void displayOfflineInformation()
	{
		serverPing.setText("Server Offline");
		serverPassword.setText("");
		mapLabel.setText("");
		serverLagcomp.setText("");
		websiteLink.setText("");
		// Not using setVisible because i dont want the items to resize or anything
		websiteLink.setOnAction(null);
		playerTable.setPlaceholder(new Label("Server is offline."));
	}

	/**
	 * Updates the {@link Label Labels} at the bottom of the Serverlist view.
	 */
	protected void updateGlobalInfo()
	{
		int playersPlaying = 0;
		int maxSlots = 0;

		for (final SampServer server : serverTable.getItems())
		{
			playersPlaying += server.getPlayers();
			maxSlots += server.getMaxPlayers();
		}

		setServerCount(serverTable.getItems().size());
		setPlayerCount(playersPlaying);
		setFreeSlotCount(maxSlots - playersPlaying);
	}

	@Override
	public void onClose()
	{
		killServerLookupThread();
	}
}
