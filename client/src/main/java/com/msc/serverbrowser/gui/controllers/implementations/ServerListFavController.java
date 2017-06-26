package com.msc.serverbrowser.gui.controllers.implementations;

import java.util.List;

import com.msc.sampbrowser.entities.SampServer;
import com.msc.serverbrowser.data.Favourites;

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

		addToFavouritesMenuItem.setVisible(false);
		removeFromFavouritesMenuItem.setVisible(true);
	}

	@Override
	public void onClose()
	{
		super.onClose();
	}
}
