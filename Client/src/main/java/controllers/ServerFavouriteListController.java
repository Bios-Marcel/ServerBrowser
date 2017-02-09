package controllers;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import data.Favourites;
import data.SampServer;
import query.SampQuery;

public class ServerFavouriteListController extends ServerListControllerMain
{

	@Override
	public void init()
	{
		super.init();

		Set<SampServer> favs = Favourites.getFavourites();
		for (SampServer server : favs)
		{
			SampQuery query = new SampQuery(server.getAddress(), Integer.parseInt(server.getPort()));

			if (query.isConnected())
			{
				String[] serverInfo = query.getBasicServerInfo();
				if (Objects.nonNull(serverInfo))
				{
					server.setPlayers(Integer.parseInt(serverInfo[1]));
					server.setMaxPlayers(Integer.parseInt(serverInfo[2]));

					playersPlaying += Integer.parseInt(serverInfo[1]);
					maxSlots += Integer.parseInt(serverInfo[2]);
				}
				query.close();
			}
		}

		servers.clear();
		servers.addAll(favs);

		sortedServers.clear();
		sortedServers.addAll(filteredServers);

		serverCount.setText(sortedServers.size() + "");
		playerCount.setText(playersPlaying + "");
		slotCount.setText(maxSlots - playersPlaying + "");

	}

	@Override
	protected void displayMenu(List<SampServer> servers, double posX, double posY)
	{
		super.displayMenu(servers, posX, posY);

		addToFavourites.setVisible(false);
		removeFromFavourites.setVisible(true);
	}
}
