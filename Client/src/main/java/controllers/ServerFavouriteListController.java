package controllers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Set;

import data.Favourites;
import data.SampServer;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import query.SampQuery;
import util.GTA;

public class ServerFavouriteListController extends ServerListControllerMain
{

	@Override
	protected void displayMenu(SampServer server, double posX, double posY)
	{
		menu = new ContextMenu();
		MenuItem connectItem = new MenuItem("Connect to Server");
		MenuItem removeFromFavourites = new MenuItem("Remove from Favourites");
		MenuItem copyIpAddressAndPort = new MenuItem("Copy IP Address and Port");

		menu.getItems().add(connectItem);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(removeFromFavourites);
		menu.getItems().add(copyIpAddressAndPort);

		menu.setOnAction(action ->
		{
			MenuItem clickedItem = (MenuItem) action.getTarget();

			if (clickedItem == connectItem)
			{
				GTA.connectToServerPerShell(server.getAddress() + ":" + server.getPort());
			}
			else if (clickedItem == removeFromFavourites)
			{
				Favourites.removeServerFromFavourites(server);
				tableView.getItems().remove(server);
			}
			else if (clickedItem == copyIpAddressAndPort)
			{
				StringSelection stringSelection = new StringSelection(server.getAddress() + ":" + server.getPort());
				Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clpbrd.setContents(stringSelection, null);
			}
		});

		menu.show(tableView, posX, posY);
	}

	@Override
	public void init()
	{
		super.init();

		tableView.setOnKeyReleased(released ->
		{
			// if (released.getCode().equals(KeyCode.PASTE))
			// {
			// addServerToFavourites(ip, port);
			// }

			SampServer server = tableView.getSelectionModel().getSelectedItem();

			if (released.getCode().equals(KeyCode.DOWN) || released.getCode().equals(KeyCode.KP_DOWN) || released.getCode().equals(KeyCode.KP_UP) || released.getCode().equals(KeyCode.UP))
			{
				updateServerInfo(server);
			}
		});

		int playersPlaying = 0;
		int maxSlots = 0;

		Set<SampServer> favs = Favourites.getFavourites();
		for (SampServer server : favs)
		{
			SampQuery query = new SampQuery(server.getAddress(), Integer.parseInt(server.getPort()));

			if (query.connect())
			{
				String[] serverInfo = query.getBasicServerInfo();
				server.setPlayers(Integer.parseInt(serverInfo[1]));
				server.setMaxPlayers(Integer.parseInt(serverInfo[2]));

				playersPlaying += Integer.parseInt(serverInfo[1]);
				maxSlots += Integer.parseInt(serverInfo[2]);
				query.close();
			}

		}
		tableView.getItems().addAll(favs);

		int freeSlots = maxSlots - playersPlaying;

		serverCount.setText(tableView.getItems().size() + "");
		playerCount.setText(playersPlaying + "");
		slotCount.setText(freeSlots + "");
	}
}
