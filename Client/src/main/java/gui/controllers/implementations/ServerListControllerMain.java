package gui.controllers.implementations;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.PatternSyntaxException;

import data.Favourites;
import data.SampServer;
import entities.Player;
import gui.controllers.interfaces.ViewController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import query.SampQuery;
import util.GTA;

public abstract class ServerListControllerMain implements ViewController
{

	@FXML
	private TextField						addressTextField;

	private static StringProperty			serverAddressProperty	= new SimpleStringProperty();

	@FXML
	protected TableView<SampServer>			tableView;

	@FXML
	private TableColumn<SampServer, String>	columnPlayers;

	@FXML
	private TableView<Player>				playerTable;

	@FXML
	protected Label							playerCount;

	@FXML
	protected Label							slotCount;

	@FXML
	protected Label							serverCount;

	@FXML
	private TextField						serverAddress;

	@FXML
	private Label							serverLagcomp;

	@FXML
	private Label							serverPing;

	@FXML
	private Label							serverPassword;

	private static Thread					threadGetPlayers;

	protected MenuItem						addToFavourites			= new MenuItem("Add to Favourites");

	protected MenuItem						removeFromFavourites	= new MenuItem("Remove from Favourites");

	private final MenuItem					connectItem				= new MenuItem("Connect to Server");

	private final MenuItem					copyIpAddressAndPort	= new MenuItem("Copy IP Address and Port");

	/**
	 * The menu that will be dsiplayed, when a user selects 1 .. n servers and right clicks the table
	 */
	protected ContextMenu					menu					= new ContextMenu(connectItem, new SeparatorMenuItem(), addToFavourites, removeFromFavourites, copyIpAddressAndPort);

	@FXML
	private CheckBox						regexCheckBox;

	@FXML
	private TextField						nameFilter;

	@FXML
	private TextField						modeFilter;

	@FXML
	private TextField						languageFilter;

	@FXML
	private ComboBox<String>				versionFilter;

	protected int							playersPlaying			= 0;

	protected int							maxSlots				= 0;

	/* HACK(MSC) This is a little hacky, because it needs 3 lists in order to
	 * keep all data, make sorting possible and make filtering possible. */
	protected ObservableList<SampServer>	servers					= FXCollections.observableArrayList();

	protected FilteredList<SampServer>		filteredServers			= new FilteredList<>(servers);

	protected ObservableList<SampServer>	sortedServers			= FXCollections.observableArrayList();

	@Override
	public void init()
	{
		addressTextField.textProperty().bindBidirectional(serverAddressProperty);

		// TODO(MSC) Improve by including MaxPlayers as Secondary sorting
		// condition
		columnPlayers.setComparator((o1, o2) ->
		{
			final int p1 = Integer.parseInt(o1.replaceAll("[/](.*)", ""));
			final int p2 = Integer.parseInt(o2.replaceAll("[/](.*)", ""));
			return p1 < p2 ? -1 : p1 == p2 ? 0 : 1;
		});

		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		tableView.setItems(sortedServers);
	}

	@FXML
	private void onClickAddToFavourites()
	{
		final String[] ipAndPort = addressTextField.getText().split("[:]");
		if (ipAndPort.length == 2 && validateServerAddress(ipAndPort[0]) && validatePort(ipAndPort[1]))
		{
			servers.add(Favourites.addServerToFavourites(ipAndPort[0], Integer.parseInt(ipAndPort[1])));
			updateTable();
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

	@FXML
	public void onClickConnect()
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

	private boolean validatePort(final String port)
	{
		try
		{
			final int portNumber = Integer.parseInt(port);

			return !(portNumber < 0 || portNumber > 65535);
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

		final List<SampServer> serverList = tableView.getSelectionModel().getSelectedItems();

		if (!serverList.isEmpty())
		{
			if (clicked.getButton().equals(MouseButton.PRIMARY))
			{
				updateServerInfo(serverList.get(0));
			}
			else if (clicked.getButton().equals(MouseButton.SECONDARY))
			{
				displayMenu(serverList, clicked.getScreenX(), clicked.getScreenY());
			}
		}
	}

	@FXML
	protected void onTableViewKeyReleased(final KeyEvent released)
	{
		final SampServer server = tableView.getSelectionModel().getSelectedItem();

		final KeyCode usedKey = released.getCode();

		if (usedKey.equals(KeyCode.DOWN) || usedKey.equals(KeyCode.KP_DOWN) || usedKey.equals(KeyCode.KP_UP) || usedKey.equals(KeyCode.UP))
		{
			updateServerInfo(server);
		}
	}

	@FXML
	public void onFilterSettingsChange()
	{
		filteredServers.setPredicate(server ->
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
				doesModeFilterApply = hostname.contains(modeFilterSetting);
				doesLanguageFilterApply = hostname.contains(languageFilterSetting);
			}

			return doesNameFilterApply && doesModeFilterApply && doesVersionFilterApply && doesLanguageFilterApply;
		});

		updateTable();
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
	 * Updates and resorts the TableView.
	 * 
	 * Since i am using 3 diffrent Collections to keep hold of the data, filter it and sort it. i have to update the table view a little
	 * tricky.
	 */
	public void updateTable()
	{
		sortedServers.clear();
		sortedServers.addAll(filteredServers);
		tableView.sort();
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
		final boolean sizeEqualsOne = (serverList.size() == 1);

		connectItem.setVisible(sizeEqualsOne);
		copyIpAddressAndPort.setVisible(sizeEqualsOne);
		menu.getItems().get(1).setVisible(sizeEqualsOne);

		menu.setOnAction(action ->
		{
			final MenuItem clickedItem = (MenuItem) action.getTarget();

			if (clickedItem == connectItem)
			{
				final SampServer server = serverList.get(0);
				tryToConnect(server.getAddress(), server.getPort());
			}
			else if (clickedItem == addToFavourites)
			{
				for (final SampServer serverItem : serverList)
				{
					Favourites.addServerToFavourites(serverItem);
				}
			}
			else if (clickedItem == removeFromFavourites)
			{
				for (final SampServer serverItem : serverList)
				{
					Favourites.removeServerFromFavourites(serverItem);
				}
				servers.removeAll(serverList);
				updateTable();
			}
			else if (clickedItem == copyIpAddressAndPort)
			{
				final SampServer server = serverList.get(0);
				final StringSelection stringSelection = new StringSelection(server.getAddress() + ":" + server.getPort());
				final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			}
		});

		menu.show(tableView, posX, posY);
	}

	/**
	 * Connects to a server, depending on if it is passworded, the user will be asked to enter a password. If the server is not reachable
	 * the user can not connect.
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
				dialog.setHeaderText("Enter the servers password.");

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

		if (Objects.nonNull(threadGetPlayers))
		{
			threadGetPlayers.interrupt();
		}

		threadGetPlayers = new Thread(() ->
		{
			try (final SampQuery query = new SampQuery(server.getAddress(), server.getPort()))

			{
				final Optional<String[]> serverInfoOptional = query.getBasicServerInfo();

				final String passworded;

				if (serverInfoOptional.isPresent())
				{
					final String[] serverInfo = serverInfoOptional.get();

					server.setPlayers(Integer.parseInt(serverInfo[1]));
					server.setMaxPlayers(Integer.parseInt(serverInfo[2]));
					passworded = serverInfo[0].equals("0") ? "No" : "Yes";
				}
				else
				{
					passworded = "";
				}

				final ObservableList<Player> players = FXCollections.observableArrayList();

				query.getBasicPlayerInfo().ifPresent(basicPlayers ->
				{
					for (int i = 0; i < basicPlayers.length; i++)
					{
						players.add(new Player(basicPlayers[i][0], basicPlayers[i][1]));
					}
				});

				final long ping = query.getPing();

				Platform.runLater(() ->
				{
					serverPassword.setText(passworded);
					serverPing.setText("" + ping);

					if (!players.isEmpty())
					{
						playerTable.setItems(players);
					}
					else
					{
						playerTable.setPlaceholder(new Label("Currently, noone is playing on this server."));
					}

					if (playerTable.getItems().isEmpty() && server.getPlayers() >= 100)
					{
						final Label label = new Label("Sorry, since this server has more than 100 active players, we are not able to retrieve any player related information.");
						label.setWrapText(true);
						label.setAlignment(Pos.CENTER);
						playerTable.setPlaceholder(label);
					}
				});

				query.getServersRules().ifPresent(rules ->
				{
					for (int i = 0; rules.length > i; i++)
					{
						if (rules[i][0].equals("lagcomp"))
						{
							final String temp = rules[i][1];
							Platform.runLater(() ->
							{
								serverLagcomp.setText(temp);
							});
							break;
						}
					}
				});

				Platform.runLater(() ->
				{
					updateGlobalInfo();
				});
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

		threadGetPlayers.start();
	}

	private void updateGlobalInfo()
	{
		playersPlaying = 0;
		maxSlots = 0;

		for (final SampServer server : sortedServers)
		{
			playersPlaying += server.getPlayers();
			maxSlots += server.getMaxPlayers();
		}

		final int freeSlots = maxSlots - playersPlaying;

		serverCount.setText(sortedServers.size() + "");
		playerCount.setText(playersPlaying + "");
		slotCount.setText(freeSlots + "");
	}
}
