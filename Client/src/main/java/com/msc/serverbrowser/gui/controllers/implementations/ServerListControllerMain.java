package com.msc.serverbrowser.gui.controllers.implementations;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;

import com.msc.sampbrowser.entities.Player;
import com.msc.sampbrowser.entities.SampServer;
import com.msc.sampbrowser.query.SampQuery;
import com.msc.serverbrowser.data.Favourites;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.util.GTA;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public abstract class ServerListControllerMain implements ViewController
{
	private final ObjectProperty<Predicate<? super SampServer>> filterProperty = new SimpleObjectProperty<>();

	@FXML
	private TextField addressTextField;

	private final static StringProperty serverAddressProperty = new SimpleStringProperty();

	@FXML
	protected TableView<SampServer>	serverTable;
	@FXML
	protected Label					playerCount;
	@FXML
	protected Label					slotCount;
	@FXML
	protected Label					serverCount;
	@FXML
	private TextField				serverAddress;
	@FXML
	private Label					serverLagcomp;
	@FXML
	private Label					serverPing;
	@FXML
	private Label					serverPassword;

	@FXML
	private TableView<Player>				playerTable;
	@FXML
	private TableColumn<SampServer, String>	columnPlayers;

	/**
	 * The menu that will be dsiplayed, when a user selects 1 .. n servers and right clicks the
	 * table
	 */
	protected MenuItem		addToFavouritesMenuItem			= new MenuItem("Add to Favourites");
	protected MenuItem		removeFromFavouritesMenuItem	= new MenuItem("Remove from Favourites");
	private final MenuItem	connectMenuItem					= new MenuItem("Connect to Server");
	private final MenuItem	copyIpAddressAndPortMenuItem	= new MenuItem("Copy IP Address and Port");
	protected ContextMenu	menu							= new ContextMenu(connectMenuItem, new SeparatorMenuItem(), addToFavouritesMenuItem, removeFromFavouritesMenuItem, copyIpAddressAndPortMenuItem);

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

	protected int	playersPlaying	= 0;
	protected int	maxSlots		= 0;

	protected ObservableList<SampServer> servers = FXCollections.observableArrayList();

	private static Thread serverInfoUpdateThread;

	@Override
	public void initialize()
	{
		final FilteredList<SampServer> filteredServers = new FilteredList<>(servers);
		filteredServers.predicateProperty().bind(filterProperty);

		final SortedList<SampServer> sortedServers = new SortedList<>(filteredServers);
		serverTable.comparatorProperty().addListener(changed ->
		{
			sortedServers.setComparator(serverTable.comparatorProperty().get());
			serverTable.refresh();
		});

		addressTextField.textProperty().bindBidirectional(serverAddressProperty);

		columnPlayers.setComparator((o1, o2) ->
		{
			final int p1 = Integer.parseInt(o1.replaceAll("[/](.*)", ""));
			final int p2 = Integer.parseInt(o2.replaceAll("[/](.*)", ""));
			return p1 < p2 ? -1 : p1 == p2 ? 0 : 1;
		});

		serverTable.setRowFactory(facotry ->
		{
			final TableRow<SampServer> row = new TableRow<>();

			row.setOnMouseClicked(clicked ->
			{
				menu.hide();
				final List<SampServer> serverList = serverTable.getSelectionModel().getSelectedItems();
				final SampServer rowItem = row.getItem();

				if (!serverTable.getSelectionModel().getSelectedIndices().contains(row.getIndex()))
				{
					if (rowItem != null)
					{
						serverTable.getSelectionModel().select(rowItem);
						if (clicked.getButton().equals(MouseButton.SECONDARY))
						{
							displayMenu(Arrays.asList(rowItem), clicked.getScreenX(), clicked.getScreenY());
						}
					}
					else
					{
						serverTable.getSelectionModel().clearSelection();
					}
				}
				else
				{
					if (!serverList.isEmpty() && clicked.getButton().equals(MouseButton.SECONDARY))
					{
						displayMenu(serverList, clicked.getScreenX(), clicked.getScreenY());
					}
				}

			});

			return row;
		});

		serverTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		serverTable.setItems(sortedServers);
	}

	@FXML
	private void onClickAddToFavourites()
	{
		if (Objects.nonNull(addressTextField.getText()))
		{
			final String[] ipAndPort = addressTextField.getText().split("[:]");
			if (ipAndPort.length == 2 && validateServerAddress(ipAndPort[0]) && validatePort(ipAndPort[1]))
			{
				final SampServer newServer = Favourites.addServerToFavourites(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
				if (!servers.contains(newServer))
				{
					servers.add(newServer);
				}
			}
			else
			{
				final Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Add to Favourites");
				alert.setHeaderText("Couldn't add server to favourites.");
				alert.setContentText("The address that you have entered, doesn't seem to be valid.");

				alert.showAndWait();
			}
		}
	}

	@FXML
	private void onClickConnect()
	{
		final String[] ipAndPort = addressTextField.getText().split("[:]");
		if (ipAndPort.length == 2 && validateServerAddress(ipAndPort[0]) && validatePort(ipAndPort[1]))
		{
			tryToConnect(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
		}
		else
		{
			final Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Connect to Server");
			alert.setHeaderText("Can't connect to Server");
			alert.setContentText("The address that you have entered, doesn't seem to be valid.");

			alert.showAndWait();
		}
	}

	private boolean validateServerAddress(final String address)
	{
		try
		{
			InetAddress.getByName(address);
			return true;
		}
		catch (final UnknownHostException e)
		{
			return false;
		}
	}

	/**
	 * Validates the given port.
	 *
	 * @param port
	 *            the ort to be validated
	 * @return true if it is an int and between 0 and 65535
	 */
	private boolean validatePort(final String port)
	{
		try
		{
			final int portNumber = Integer.parseInt(port);
			return portNumber >= 0 && portNumber <= 65535;
		}
		catch (final NumberFormatException e)
		{
			return false;
		}
	}

	@FXML
	protected void onTableViewMouseReleased(final MouseEvent clicked)
	{
		menu.hide();

		final List<SampServer> serverList = serverTable.getSelectionModel().getSelectedItems();

		if (!serverList.isEmpty())
		{
			if (clicked.getButton().equals(MouseButton.PRIMARY))
			{
				updateServerInfo(serverList.get(0));
			}
		}
	}

	@FXML
	protected void onTableViewKeyReleased(final KeyEvent released)
	{
		final SampServer server = serverTable.getSelectionModel().getSelectedItem();
		final KeyCode usedKey = released.getCode();

		if (usedKey.equals(KeyCode.DOWN) || usedKey.equals(KeyCode.KP_DOWN) || usedKey.equals(KeyCode.KP_UP) || usedKey.equals(KeyCode.UP))
		{
			updateServerInfo(server);
		}
	}

	@FXML
	private void onFilterSettingsChange()
	{
		filterProperty.set(server ->
		{
			boolean doesNameFilterApply = true;
			boolean doesModeFilterApply = true;
			boolean doesLanguageFilterApply = true;
			boolean doesVersionFilterApply = true;

			if (!versionFilter.getSelectionModel().isEmpty())
			{
				final String versionFilterSetting = versionFilter.getSelectionModel().getSelectedItem().toString();
				doesVersionFilterApply = server.getVersion().toLowerCase().contains(versionFilterSetting.toLowerCase());
			}

			final String nameFilterSetting = nameFilter.getText().toLowerCase();
			final String modeFilterSetting = modeFilter.getText().toLowerCase();
			final String languageFilterSetting = languageFilter.getText().toLowerCase();

			final String hostname = server.getHostname().toLowerCase();
			final String mode = server.getMode().toLowerCase();
			final String language = server.getLanguage().toLowerCase();

			if (regexCheckBox.isSelected())
			{
				doesNameFilterApply = regexFilter(hostname, nameFilterSetting);
				doesModeFilterApply = regexFilter(mode, modeFilterSetting);
				doesLanguageFilterApply = regexFilter(language, languageFilterSetting);
			}
			else
			{
				doesNameFilterApply = hostname.contains(nameFilterSetting);
				doesModeFilterApply = mode.contains(modeFilterSetting);
				doesLanguageFilterApply = language.contains(languageFilterSetting);
			}

			return doesNameFilterApply && doesModeFilterApply && doesVersionFilterApply && doesLanguageFilterApply;
		});

		updateGlobalInfo();
	}

	private boolean regexFilter(final String toFilter, final String filterSetting)
	{
		if (!filterSetting.isEmpty())
		{
			try
			{
				if (!toFilter.matches(filterSetting))
				{
					return false;
				}
			}
			catch (final PatternSyntaxException e)
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Displays the context menu for server entries.
	 *
	 * @param serverList
	 *            The list of servers that the context menu actions will affect
	 * @param posX
	 *            X coordinate
	 * @param posY
	 *            Y coodrinate
	 */
	protected void displayMenu(final List<SampServer> serverList, final double posX, final double posY)
	{
		final boolean sizeEqualsOne = serverList.size() == 1;

		connectMenuItem.setVisible(sizeEqualsOne);
		copyIpAddressAndPortMenuItem.setVisible(sizeEqualsOne);
		menu.getItems().get(1).setVisible(sizeEqualsOne);

		menu.setOnAction(action ->
		{
			final MenuItem clickedItem = (MenuItem) action.getTarget();

			if (clickedItem == connectMenuItem)
			{
				final SampServer server = serverList.get(0);
				tryToConnect(server.getAddress(), server.getPort());
			}
			else if (clickedItem == addToFavouritesMenuItem)
			{
				serverList.forEach(Favourites::addServerToFavourites);
			}
			else if (clickedItem == removeFromFavouritesMenuItem)
			{
				serverList.forEach(Favourites::removeServerFromFavourites);
				servers.removeAll(serverList);
			}
			else if (clickedItem == copyIpAddressAndPortMenuItem)
			{
				final SampServer server = serverList.get(0);
				final StringSelection stringSelection = new StringSelection(server.getAddress() + ":" + server.getPort());
				final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			}
		});

		menu.show(serverTable, posX, posY);
	}

	/**
	 * Connects to a server, depending on if it is passworded, the user will be asked to enter a
	 * password. If the server is not reachable the user can not connect.
	 *
	 * @param address
	 *            server ip
	 * @param port
	 *            server port
	 */
	private void tryToConnect(final String address, final Integer port)
	{
		try (final SampQuery query = new SampQuery(address, port))
		{
			final Optional<String[]> serverInfo = query.getBasicServerInfo();

			if (serverInfo.isPresent() && !serverInfo.get()[0].equals("0"))
			{
				final TextInputDialog dialog = new TextInputDialog();
				dialog.setTitle("Connect to Server");
				dialog.setHeaderText("Enter the servers password (Leave empty if u think there is none).");

				final Optional<String> result = dialog.showAndWait();
				result.ifPresent(password ->
				{
					GTA.connectToServer(address + ":" + port, password);
				});
			}
			else
			{
				GTA.connectToServer(address + ":" + port);
			}
		}
		catch (final Exception e)
		{
			final Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Connect to Server");
			alert.setHeaderText("Can't connect to Server");
			alert.setContentText("Server seems to be offline, try again.");

			alert.showAndWait();
		}
	}

	protected void updateServerInfo(final SampServer server)
	{
		playerTable.getItems().clear();

		playerTable.setPlaceholder(new Label("Retrieving..."));

		serverAddress.setText(server.getAddress() + ":" + server.getPort());
		serverLagcomp.setText(server.getLagcomp());

		serverPing.setText("Retrieving ...");
		serverPassword.setText("Retrieving ...");

		if (Objects.nonNull(serverInfoUpdateThread))
		{
			serverInfoUpdateThread.interrupt();
		}

		serverInfoUpdateThread = new Thread(() ->
		{
			try (final SampQuery query = new SampQuery(server.getAddress(), server.getPort()))
			{

				final Optional<String[]> infoOptional = query.getBasicServerInfo();

				final Optional<String[][]> infoMoreOptional = query.getServersRules();

				if (infoOptional.isPresent() && infoMoreOptional.isPresent())
				{
					final String[] info = infoOptional.get();
					final String[][] infoMore = infoMoreOptional.get();

					final int players = Integer.parseInt(info[1]);
					final int maxPlayers = Integer.parseInt(info[2]);

					String weburl = null;
					String lagcomp = null;
					String version = null;

					for (final String[] element : infoMore)
					{
						if (element[0].equals("lagcomp"))
						{
							lagcomp = element[1];
						}
						else if (element[0].equals("weburl"))
						{
							weburl = element[1];
						}
						else if (element[0].equals("version"))
						{
							version = element[1];
						}
					}

					server.setPlayers(players);
					server.setMaxPlayers(maxPlayers);
					server.setHostname(info[3]);
					server.setMode(info[4]);
					server.setLanguage(info[5]);
					server.setWebsite(weburl);
					server.setVersion(version);
					server.setLagcomp(lagcomp);

					final ObservableList<Player> playerList = FXCollections.observableArrayList();

					query.getBasicPlayerInfo().ifPresent(basicPlayers ->
					{
						for (final String[] basicPlayer : basicPlayers)
						{
							playerList.add(new Player(basicPlayer[0], basicPlayer[1]));
						}
					});

					final long ping = query.getPing();

					Platform.runLater(() ->
					{
						serverPassword.setText(info[0].equals("0") ? "No" : "Yes");
						serverPing.setText("" + ping);

						if (playerList.isEmpty())
						{
							playerTable.setPlaceholder(new Label("Currently, noone is playing on this server."));
						}
						else
						{
							playerTable.setItems(playerList);
						}

						if (playerTable.getItems().isEmpty() && server.getPlayers() >= 100)
						{
							final Label label = new Label("Sorry, since this server has more than 100 active players, we are not able to retrieve any player related information.");
							label.setWrapText(true);
							label.setAlignment(Pos.CENTER);
							playerTable.setPlaceholder(label);
						}

						serverLagcomp.setText(server.getLagcomp());
					});

					Favourites.updateServerData(server);
				}
			}
			catch (final Exception e)
			{
				Platform.runLater(() ->
				{
					serverPing.setText("Server Offline");
					serverPassword.setText("");
					playerTable.setPlaceholder(new Label("Couldn't retrieve players, server is offline."));
				});
			}
		});

		serverInfoUpdateThread.start();
	}

	void updateGlobalInfo()
	{
		playersPlaying = 0;
		maxSlots = 0;

		for (final SampServer server : servers)
		{
			playersPlaying += server.getPlayers();
			maxSlots += server.getMaxPlayers();
		}

		serverCount.setText(serverTable.getItems().size() + "");
		playerCount.setText(playersPlaying + "");
		slotCount.setText(maxSlots - playersPlaying + "");
	}
}
