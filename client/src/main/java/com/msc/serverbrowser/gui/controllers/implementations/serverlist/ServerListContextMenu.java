// package com.msc.serverbrowser.gui.controllers.implementations.serverlist;
//
// import com.msc.serverbrowser.data.entites.SampServer;
//
// import javafx.scene.control.ContextMenu;
// import javafx.scene.control.MenuItem;
// import javafx.scene.control.SeparatorMenuItem;
// import javafx.scene.control.TableSelectionModel;
//
// public class ServerListContextMenu extends ContextMenu
// {
// /**
// * When clicked, all selected servers will be added to favourites.
// */
// protected MenuItem addToFavouritesMenuItem = new MenuItem(
// "Add to Favourites");
// /**
// * When clicked, all selected servers will be removed from favourites.
// */
// protected MenuItem removeFromFavouritesMenuItem = new MenuItem("Remove from
// Favourites");
// private final MenuItem visitWebsiteMenuItem = new MenuItem("Visit Website");
// private final MenuItem connectMenuItem = new MenuItem("Connect to Server");
// private final MenuItem copyIpAddressAndPortMenuItem = new MenuItem("Copy IP
// Address and Port");
//
// public ServerListContextMenu(final TableSelectionModel<SampServer>
// selectionModel)
// {
//
// getItems().addAll(connectMenuItem,
// new SeparatorMenuItem(),
// addToFavouritesMenuItem,
// removeFromFavouritesMenuItem,
// copyIpAddressAndPortMenuItem,
// visitWebsiteMenuItem);
// }
//
// public void show(final boolean forFavourites)
// {
// final boolean notForFavourites = !forFavourites;
//
// addToFavouritesMenuItem.setVisible(notForFavourites);
// removeFromFavouritesMenuItem.setVisible(forFavourites);
//
// }
// }
