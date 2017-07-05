package com.msc.serverbrowser.gui.controllers.implementations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import com.msc.sampbrowser.entities.SampServer;
import com.msc.sampbrowser.entities.SampServerSerializeable;
import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.logging.Logging;

import javafx.application.Platform;
import javafx.scene.control.Label;

@SuppressWarnings("null")
public class ServerListAllController extends ServerListControllerMain
{
	private Thread serverLookup;

	private static Object deserialzieAndDecompress(final byte[] data)
	{
		try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
				final GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
				final ObjectInputStream objectInputStream = new ObjectInputStream(gzipInputStream);)
		{
			final Object object = objectInputStream.readObject();
			return object;
		}
		catch (final IOException | ClassNotFoundException exception)
		{
			Logging.logger().log(Level.SEVERE, "Error deserializing and decompressing data", exception);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize()
	{
		super.initialize();

		serverTable.setPlaceholder(new Label("Loading server list, please wait a moment."));

		serverLookup = new Thread(() ->
		{

			if (Objects.nonNull(Client.remoteDataService))
			{
				try
				{
					final byte[] serializedData = Client.remoteDataService.getAllServers();
					final List<SampServerSerializeable> serializedServers = (List<SampServerSerializeable>) deserialzieAndDecompress(serializedData);

					servers.addAll(serializedServers.stream()
							.map(SampServer::new)
							.collect(Collectors.toSet()));
					Platform.runLater(() -> serverTable.refresh());
				}
				catch (final RemoteException exception)
				{
					Logging.logger().log(Level.SEVERE, "Couldn't retrieve data from server.", exception);
					Platform.runLater(() -> serverTable.setPlaceholder(new Label("Server connection couldn't be established.")));
				}
			}
			else
			{
				Platform.runLater(() -> serverTable.setPlaceholder(new Label("Server connection couldn't be established.")));
			}

			Platform.runLater(() -> updateGlobalInfo());
		});

		serverLookup.start();
	}

	@Override
	protected void displayMenu(final List<SampServer> selectedServers, final double posX, final double posY)
	{
		super.displayMenu(selectedServers, posX, posY);

		addToFavouritesMenuItem.setVisible(true);
		removeFromFavouritesMenuItem.setVisible(false);
	}

	@Override
	public void onClose()
	{
		super.onClose();
		serverLookup.interrupt();
	}
}
