package controllers;

import java.util.Set;

import data.Favourites;
import data.SampServer;
import javafx.scene.input.KeyCode;
import query.SampQuery;

public class ServerFavouriteListController extends ServerListControllerMain
{

	@Override
	public void init()
	{
		super.init();

		tableView.setOnKeyReleased(released ->
		{
			// if (released.getCode().equals(KeyCode.PASTE))
			// {
			// addServerToFavourites(ip, port);
			// }

			SampServer server = tableView.getSelectionModel().getSelectedItem();

			if (released.getCode().equals(KeyCode.DOWN) || released.getCode().equals(KeyCode.KP_DOWN) || released.getCode().equals(KeyCode.KP_UP) || released.getCode().equals(KeyCode.UP))
			{
				updateServerInfo(server);
			}
		});

		int playersPlaying = 0;
		int maxSlots = 0;

		Set<SampServer> favs = Favourites.getFavourites();
		for (SampServer server : favs)
		{
			SampQuery query = new SampQuery(server.getAddress(), Integer.parseInt(server.getPort()));

			if (query.connect())
			{
				String[] serverInfo = query.getBasicServerInfo();
				server.setPlayers(Integer.parseInt(serverInfo[1]));
				server.setMaxPlayers(Integer.parseInt(serverInfo[2]));

				playersPlaying += Integer.parseInt(serverInfo[1]);
				maxSlots += Integer.parseInt(serverInfo[2]);
				query.close();
			}

		}

		servers.clear();
		servers.addAll(favs);

		int freeSlots = maxSlots - playersPlaying;

		serverCount.setText(tableView.getItems().size() + "");
		playerCount.setText(playersPlaying + "");
		slotCount.setText(freeSlots + "");
	}

	protected void displayMenu(SampServer server, double posX, double posY)
	{
		super.displayMenu(server, posX, posY);

		addToFavourites.setVisible(false);
		removeFromFavourites.setVisible(true);
	}
}
