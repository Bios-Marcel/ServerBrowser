package com.msc.serverbrowser.gui.controllers.implementations.serverlist;

import java.io.IOException;
import java.util.logging.Level;

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

		serverTable.setPlaceholder(new Label("Fetching servers, please wait a moment."));
		serverTable.setServerTableMode(SampServerTableMode.ALL);

		serverLookup = new Thread(() ->
		{
			try
			{
				serverTable.addAll(ServerUtility.fetchServersFromSouthclaws());
				Platform.runLater(() -> serverTable.refresh());
			}
			catch (final IOException exception)
			{
				Logging.log(Level.SEVERE, "Couldn't retrieve data from announce api.", exception);
				Platform.runLater(() -> serverTable.setPlaceholder(new Label("Couldn't fetch servers")));
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
