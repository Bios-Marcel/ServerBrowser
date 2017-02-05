package controllers;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;

import data.SampServer;
import interfaces.DataServiceInterface;
import logging.Logging;

public class ServerAllListController extends ServerListControllerMain
{
	private static Registry				registry;

	private static DataServiceInterface	remoteDataService;

	static
	{
		try
		{
			registry = LocateRegistry.getRegistry("ts3.das-chat.xyz");
			remoteDataService = (DataServiceInterface) registry.lookup("DataServiceInterface");
		}
		catch (RemoteException | NotBoundException e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't connect to RMI Server.", e);
		}
	}

	@Override
	public void init()
	{
		super.init();

		int playersPlaying = 0;
		int maxSlots = 0;

		try
		{

			servers.clear();
			servers.addAll(remoteDataService.getAllServers().stream().map(server -> new SampServer(server)).collect(Collectors.toSet()));

			for (SampServer server : servers)
			{
				playersPlaying += server.getPlayers();
				maxSlots += server.getMaxPlayers();
				server.setHostname(StringEscapeUtils.unescapeHtml4(server.getHostname()));
				server.setLanguage(StringEscapeUtils.unescapeHtml4(server.getLanguage()));
				server.setMode(StringEscapeUtils.unescapeHtml4(server.getMode()));
			}

			sortedServers.clear();
			sortedServers.addAll(filteredServers);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}

		serverCount.setText(sortedServers.size() + "");
		playerCount.setText(playersPlaying + "");
		slotCount.setText(maxSlots - playersPlaying + "");
	}

	@Override
	protected void displayMenu(SampServer server, double posX, double posY)
	{
		super.displayMenu(server, posX, posY);

		addToFavourites.setVisible(true);
		removeFromFavourites.setVisible(false);
	}
}
