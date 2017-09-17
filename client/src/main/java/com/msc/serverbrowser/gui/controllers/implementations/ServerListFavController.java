package com.msc.serverbrowser.gui.controllers.implementations;

import java.util.List;

import com.msc.serverbrowser.data.Favourites;
import com.msc.serverbrowser.data.SampServer;

/**
 * ViewController for the favourite servers list view.
 *
 * @author Marcel
 */
public class ServerListFavController extends ServerListControllerMain
{
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
