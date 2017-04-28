package gui.controllers.implementations;

import java.util.List;

import data.Favourites;
import entities.SampServer;

public class ServerListFavController extends ServerListControllerMain
{
	// private static Thread queryThread;

	@Override
	public void initialize()
	{
		super.initialize();

		servers.addAll(Favourites.getFavourites());
	}

	@Override
	protected void displayMenu(final List<SampServer> selectedServers, final double posX, final double posY)
	{
		super.displayMenu(selectedServers, posX, posY);

		addToFavourites.setVisible(false);
		removeFromFavourites.setVisible(true);
	}
}
