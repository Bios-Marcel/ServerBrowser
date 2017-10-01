package com.msc.serverbrowser.gui.controllers.implementations.serverlist;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.entites.SampServer;
import com.msc.serverbrowser.gui.components.SampServerTableMode;
import com.msc.serverbrowser.logging.Logging;
import com.msc.serverbrowser.util.ServerUtility;

import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * Controller, that controlls the "All Servers" view.
 *
 * @author Marcel
 */
public class ServerListAllController extends BasicServerListController
{
	private Thread serverLookup;

	@Override
	public void initialize()
	{
		super.initialize();

		serverTable.setPlaceholder(new Label(Client.lang.getString("fetchingServers")));
		serverTable.setServerTableMode(SampServerTableMode.ALL);

		serverLookup = new Thread(() ->
		{
			try
			{
				final List<SampServer> serversToAdd = ServerUtility.fetchServersFromSouthclaws();
				Platform.runLater(() ->
				{
					serverTable.addAll(serversToAdd);
					serverTable.refresh();
				});
			}
			catch (final IOException exception)
			{
				Logging.log(Level.SEVERE, "Couldn't retrieve data from announce api.", exception);
				Platform.runLater(() -> serverTable.setPlaceholder(new Label(Client.lang.getString("errorFetchingServers"))));
			}

			Platform.runLater(() -> updateGlobalInfo());
		});

		serverLookup.start();
	}

	@Override
	public void onClose()
	{
		super.onClose();
		serverLookup.interrupt();
	}
}
