package controllers;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
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

		try
		{
			servers.clear();
			servers.addAll(remoteDataService.getAllServers().stream().map(server ->
			{
				final SampServer newServer = new SampServer(server);
				playersPlaying += newServer.getPlayers();
				maxSlots += newServer.getMaxPlayers();
				newServer.setHostname(StringEscapeUtils.unescapeHtml4(newServer.getHostname()));
				newServer.setLanguage(StringEscapeUtils.unescapeHtml4(newServer.getLanguage()));
				newServer.setMode(StringEscapeUtils.unescapeHtml4(newServer.getMode()));
				return newServer;
			}).collect(Collectors.toSet()));

			sortedServers.clear();
			sortedServers.addAll(filteredServers);
		}
		catch (final RemoteException e)
		{
			e.printStackTrace();
		}

		serverCount.setText(sortedServers.size() + "");
		playerCount.setText(playersPlaying + "");
		slotCount.setText(maxSlots - playersPlaying + "");
	}

	@Override
	protected void displayMenu(final List<SampServer> servers, final double posX, final double posY)
	{
		super.displayMenu(servers, posX, posY);

		addToFavourites.setVisible(true);
		removeFromFavourites.setVisible(false);
	}
}
