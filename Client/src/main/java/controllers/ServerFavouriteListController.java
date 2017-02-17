package controllers;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import data.Favourites;
import data.SampServer;
import javafx.application.Platform;
import logging.Logging;
import query.SampQuery;

public class ServerFavouriteListController extends ServerListControllerMain
{
	private static Thread queryThread;

	@Override
	public void init()
	{
		super.init();

		final Set<SampServer> favs = Favourites.getFavourites();
		servers.addAll(favs);

		// FIXME(MSC) Okay, that definitly needs improvement ;D

		if (Objects.nonNull(queryThread))
		{
			queryThread.interrupt();
		}

		queryThread = new Thread(() ->
		{
			for (final SampServer server : favs)
			{
				try (final SampQuery query = new SampQuery(server.getAddress(), Integer.parseInt(server.getPort())))
				{
					final String[] serverInfo = query.getBasicServerInfo();
					if (Objects.nonNull(serverInfo))
					{
						server.setPlayers(Integer.parseInt(serverInfo[1]));
						server.setMaxPlayers(Integer.parseInt(serverInfo[2]));

						playersPlaying += Integer.parseInt(serverInfo[1]);
						maxSlots += Integer.parseInt(serverInfo[2]);
					}
				}
				catch (final NumberFormatException e)
				{
					Logging.logger.log(Level.WARNING, "Server seems to contain invalid data.", e);
				}
				catch (final Exception e)
				{
					Logging.logger.log(Level.INFO, "Couldn't connect to Server: " + server.getAddress() + ":" + server.getPort() + ".");
				}
			}

			Platform.runLater(() ->
			{
				servers.setAll(favs);
				updateTable();

				serverCount.setText(sortedServers.size() + "");
				playerCount.setText(playersPlaying + "");
				slotCount.setText(maxSlots - playersPlaying + "");
			});
		});

		Platform.runLater(() ->
		{
			updateTable();
			queryThread.start();
		});
	}

	@Override
	protected void displayMenu(final List<SampServer> servers, final double posX, final double posY)
	{
		super.displayMenu(servers, posX, posY);

		addToFavourites.setVisible(false);
		removeFromFavourites.setVisible(true);
	}
}
