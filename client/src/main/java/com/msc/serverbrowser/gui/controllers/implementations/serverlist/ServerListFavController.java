package com.msc.serverbrowser.gui.controllers.implementations.serverlist;

import com.msc.serverbrowser.data.FavouritesController;
import com.msc.serverbrowser.gui.components.SampServerTableMode;

/**
 * ViewController for the favourite servers list view.
 *
 * @author Marcel
 */
public class ServerListFavController extends BasicServerListController {
	@Override
	public void initialize() {
		super.initialize();
		
		serverTable.setServerTableMode(SampServerTableMode.FAVOURITES);
		
		serverTable.addAll(FavouritesController.getFavourites());
	}
}
