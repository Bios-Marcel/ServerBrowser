package controllers;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;

import data.SampServer;
import interfaces.DataServiceInterface;
import javafx.scene.input.KeyCode;

public class ServerAllListController extends ServerListControllerMain
{
	@Override
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

			servers.clear();
			servers.addAll(comp.getAllServers().stream().map(server -> new SampServer(server)).collect(Collectors.toSet()));

			for (SampServer server : servers)
			{
				playersPlaying += server.getPlayers();
				maxSlots += server.getMaxPlayers();
				server.setHostname(StringEscapeUtils.unescapeHtml4(server.getHostname()));
				server.setLanguage(StringEscapeUtils.unescapeHtml4(server.getLanguage()));
				server.setMode(StringEscapeUtils.unescapeHtml4(server.getMode()));
			}
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

	protected void displayMenu(SampServer server, double posX, double posY)
	{
		super.displayMenu(server, posX, posY);

		addToFavourites.setVisible(true);
		removeFromFavourites.setVisible(false);
	}
}
