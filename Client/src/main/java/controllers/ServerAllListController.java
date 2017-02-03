package controllers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;

import data.Favourites;
import data.SampServer;
import interfaces.DataServiceInterface;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import util.GTA;

public class ServerAllListController extends ServerListControllerMain
{
	public void init()
	{
		super.init();

		tableView.setOnKeyReleased(released ->
		{
			SampServer server = tableView.getSelectionModel().getSelectedItem();

			if (released.getCode().equals(KeyCode.DOWN) || released.getCode().equals(KeyCode.KP_DOWN) || released.getCode().equals(KeyCode.KP_UP) || released.getCode().equals(KeyCode.UP))
			{
				updateServerInfo(server);
			}
		});

		int playersPlaying = 0;
		int maxSlots = 0;

		try
		{
			String name = "DataServiceInterface";
			Registry registry = LocateRegistry.getRegistry("ts3.das-chat.xyz");
			DataServiceInterface comp = (DataServiceInterface) registry.lookup(name);

			Set<SampServer> servers = comp.getAllServers().stream().map(server -> new SampServer(server)).collect(Collectors.toSet());;
			for (SampServer server : servers)
			{
				playersPlaying += server.getPlayers();
				maxSlots += server.getMaxPlayers();
				server.setHostname(StringEscapeUtils.unescapeHtml4(server.getHostname()));
				server.setLanguage(StringEscapeUtils.unescapeHtml4(server.getLanguage()));
				server.setMode(StringEscapeUtils.unescapeHtml4(server.getMode()));
			}
			tableView.getItems().addAll(servers);
		}
		catch (RemoteException | NotBoundException e)
		{
			e.printStackTrace();
		}

		int freeSlots = maxSlots - playersPlaying;

		serverCount.setText(tableView.getItems().size() + "");
		playerCount.setText(playersPlaying + "");
		slotCount.setText(freeSlots + "");
	}

	@Override
	protected void displayMenu(SampServer server, double posX, double posY)
	{
		menu = new ContextMenu();
		MenuItem connectItem = new MenuItem("Connect to Server");
		MenuItem addToFavourites = new MenuItem("Add to Favourites");
		MenuItem copyIpAddressAndPort = new MenuItem("Copy IP Address and Port");

		menu.getItems().add(connectItem);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(addToFavourites);
		menu.getItems().add(copyIpAddressAndPort);

		menu.setOnAction(action ->
		{
			MenuItem clickedItem = (MenuItem) action.getTarget();

			if (clickedItem == connectItem)
			{
				GTA.connectToServerPerShell(server.getAddress() + ":" + server.getPort());
			}
			else if (clickedItem == addToFavourites)
			{
				Favourites.addServerToFavourites(server);
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
}
