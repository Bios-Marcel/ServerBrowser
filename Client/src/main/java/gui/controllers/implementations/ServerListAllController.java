package gui.controllers.implementations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import application.Client;
import data.SampServer;
import entities.SampServerSerializeable;
import logging.Logging;

public class ServerListAllController extends ServerListControllerMain
{
	private static Object deserialzieAndDecompress(final byte[] data)
	{
		try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data); final GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
						final ObjectInputStream objectInputStream = new ObjectInputStream(gzipInputStream);)
		{
			final Object object = objectInputStream.readObject();
			return object;
		}
		catch (final IOException | ClassNotFoundException e)
		{
			Logging.logger.log(Level.SEVERE, "Error deserializing and decompressing data", e);
			return null;
		}
	}

	@Override
	public void init()
	{
		super.init();

		try
		{
			servers.clear();

			final List<SampServerSerializeable> serializedServers = (List<SampServerSerializeable>) deserialzieAndDecompress(Client.remoteDataService.getAllServers());
			servers.addAll(serializedServers.stream().map(server ->
			{
				final SampServer newServer = new SampServer(server);
				playersPlaying += newServer.getPlayers();
				maxSlots += newServer.getMaxPlayers();
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
		slotCount.setText((maxSlots - playersPlaying) + "");
	}

	@Override
	protected void displayMenu(final List<SampServer> selectedServers, final double posX, final double posY)
	{
		super.displayMenu(selectedServers, posX, posY);

		addToFavourites.setVisible(true);
		removeFromFavourites.setVisible(false);
	}
}
