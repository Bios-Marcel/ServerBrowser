package controllers;

import java.util.Objects;

import data.SampServer;
import entities.Player;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import query.SampQuery;

public abstract class ServerListControllerMain implements ViewController
{
	@FXML
	protected TableView<SampServer>				tableView;

	@FXML
	private TableColumn<SampServer, String>		columnPassword;

	@FXML
	private TableColumn<SampServer, String>		columnHostname;

	@FXML
	private TableColumn<SampServer, Integer>	columnPlayers;

	@FXML
	private TableColumn<SampServer, String>		columnPing;

	@FXML
	private TableColumn<SampServer, String>		columnMode;

	@FXML
	private TableColumn<SampServer, String>		columnLanguage;

	@FXML
	private TableView<Player>					playerTable;

	@FXML
	private TableColumn<Player, String>			playerName;

	@FXML
	private TableColumn<Player, String>			playerScore;

	@FXML
	protected Label								playerCount;

	@FXML
	protected Label								slotCount;

	@FXML
	protected Label								serverCount;

	@FXML
	private Label								serverAddress;

	@FXML
	private Label								serverLagcomp;

	@FXML
	private Label								serverPing;

	@FXML
	private Label								serverPassword;

	protected static ContextMenu				menu;

	private static Thread						threadGetPlayers;

	public void init()
	{
		tableView.setOnMouseReleased(clicked ->
		{
			if (Objects.nonNull(menu))
			{
				menu.hide();
			}

			SampServer server = tableView.getSelectionModel().getSelectedItem();

			if (Objects.nonNull(server))
			{
				if (clicked.getButton().equals(MouseButton.PRIMARY))
				{
					updateServerInfo(server);
				}
				else if (clicked.getButton().equals(MouseButton.SECONDARY))
				{
					displayMenu(server, clicked.getScreenX(), clicked.getScreenY());
				}
			}
		});
	}

	protected abstract void displayMenu(SampServer server, double d, double e);

	protected void updateServerInfo(SampServer server)
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
			SampQuery query = new SampQuery(server.getAddress(), Integer.parseInt(server.getPort()));

			if (!query.connect())
			{
				Platform.runLater(() ->
				{
					serverPing.setText("Server Offline");
					serverPassword.setText("");
					playerTable.setPlaceholder(new Label("Couldn't retrieve players, server is offline."));
				});
			}
			else
			{
				String[] serverInfo = query.getBasicServerInfo();

				if (Objects.nonNull(serverInfo))
				{
					server.setPlayers(Integer.parseInt(serverInfo[1]));
					server.setMaxPlayers(Integer.parseInt(serverInfo[2]));
				}

				ObservableList<Player> players = FXCollections.observableArrayList();

				String[][] basicPlayers = query.getBasicPlayerInfo();

				if (Objects.nonNull(basicPlayers))
				{
					for (int i = 0; i < basicPlayers.length; i++)
					{
						players.add(new Player(basicPlayers[i][0], basicPlayers[i][1]));
					}
				}

				String passworded = serverInfo[0].equals("0") ? "No" : "Yes";

				long ping = query.getPing();

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
						Label label = new Label("Sorry, since this server has more than 100 active players, we are not able to retrieve any player related information.");
						label.setWrapText(true);
						label.setAlignment(Pos.CENTER);
						playerTable.setPlaceholder(label);
					}
				});

				String[][] rules = query.getServersRules();

				if (Objects.nonNull(rules))
				{
					for (int i = 0; rules.length > i; i++)
					{
						if (rules[i][0].equals("lagcomp"))
						{
							String temp = rules[i][1];
							if (Objects.nonNull(temp) && !temp.isEmpty())
							{
								Platform.runLater(() ->
								{
									serverLagcomp.setText(temp);
								});
							}
							break;
						}
					}
				}
				query.close();

				Platform.runLater(() ->
				{
					updateGlobalInfo();
				});
			}
		});

		threadGetPlayers.start();
	}

	private void updateGlobalInfo()
	{
		int playersPlaying = 0;
		int maxSlots = 0;

		for (SampServer server : tableView.getItems())
		{
			playersPlaying += server.getPlayers();
			maxSlots += server.getMaxPlayers();
		}

		int freeSlots = maxSlots - playersPlaying;

		serverCount.setText(tableView.getItems().size() + "");
		playerCount.setText(playersPlaying + "");
		slotCount.setText(freeSlots + "");
	}

	// private void addData(SampServer server)
	// {
	// tableView.getItems().add(server);
	// }

}
