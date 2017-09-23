package com.msc.serverbrowser.gui.components;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import com.msc.serverbrowser.data.FavouritesController;
import com.msc.serverbrowser.data.entites.SampServer;
import com.msc.serverbrowser.util.samp.GTAController;
import com.msc.serverbrowser.util.windows.OSUtility;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

/**
 * {@link TableView} that was made for the ServerList View, contains a special TableRowFactory and
 * allows filtering and sorting.
 *
 * @author Marcel
 * @since 23.09.2017
 */
public class SampServerTable extends TableView<SampServer>
{
	private SampServerTableMode tableMode = SampServerTableMode.ALL;

	private final MenuItem	addToFavouritesMenuItem			= new MenuItem("Add to Favourites");
	private final MenuItem	removeFromFavouritesMenuItem	= new MenuItem("Remove from Favourites");
	private final MenuItem	visitWebsiteMenuItem			= new MenuItem("Visit Website");
	private final MenuItem	connectMenuItem					= new MenuItem("Connect to Server");
	private final MenuItem	copyIpAddressAndPortMenuItem	= new MenuItem("Copy IP Address and Port");

	private final ContextMenu contextMenu = new ContextMenu(connectMenuItem, new SeparatorMenuItem(), addToFavouritesMenuItem, removeFromFavouritesMenuItem, copyIpAddressAndPortMenuItem, visitWebsiteMenuItem);

	private final ObservableList<SampServer> servers = getItems();

	final FilteredList<SampServer>	filteredServers	= new FilteredList<>(servers);
	final SortedList<SampServer>	sortedServers	= new SortedList<>(filteredServers);

	/**
	 * Contructor; sets the TableRowFactory, the ContextMenu Actions and table settings.
	 */
	public SampServerTable()
	{
		setItems(sortedServers);

		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		setTableRowFactory();
		setMenuItemDefaultActions();
	}

	private Optional<SampServer> getFirstIfAnythingSelected()
	{
		final List<SampServer> selectedServers = getSelectionModel().getSelectedItems();

		if (selectedServers.size() >= 1)
		{
			return Optional.ofNullable(selectedServers.get(0));
		}

		return Optional.empty();
	}

	private void setMenuItemDefaultActions()
	{
		connectMenuItem.setOnAction(__ ->
		{
			getFirstIfAnythingSelected().ifPresent(server -> GTAController.tryToConnect(server.getAddress(), server.getPort()));
		});

		visitWebsiteMenuItem.setOnAction(__ ->
		{
			getFirstIfAnythingSelected().ifPresent(server -> OSUtility.browse(server.getWebsite()));
		});

		addToFavouritesMenuItem.setOnAction(__ ->
		{
			final List<SampServer> serverList = getSelectionModel().getSelectedItems();
			serverList.forEach(FavouritesController::addServerToFavourites);
		});

		removeFromFavouritesMenuItem.setOnAction(__ ->
		{
			final List<SampServer> serverList = getSelectionModel().getSelectedItems();
			serverList.forEach(FavouritesController::removeServerFromFavourites);
			servers.removeAll(serverList);
		});

		copyIpAddressAndPortMenuItem.setOnAction(__ ->
		{
			final Optional<SampServer> serverOptional = getFirstIfAnythingSelected();

			serverOptional.ifPresent(server ->
			{
				final String addressAndPort = server.getAddress() + ":" + server.getPort();
				final StringSelection stringSelection = new StringSelection(addressAndPort);
				final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			});
		});
	}

	private void setTableRowFactory()
	{
		setRowFactory(facotry ->
		{
			final TableRow<SampServer> row = new TableRow<>();

			row.setOnMouseClicked(clicked ->
			{
				contextMenu.hide();
				final List<SampServer> serverList = getSelectionModel().getSelectedItems();
				final SampServer rowItem = row.getItem();

				if (getSelectionModel().getSelectedIndices().contains(row.getIndex()))
				{
					if (!serverList.isEmpty() && clicked.getButton().equals(MouseButton.SECONDARY))
					{
						displayMenu(serverList, clicked.getScreenX(), clicked.getScreenY());
					}
				}
				else
				{
					if (Objects.nonNull(rowItem))
					{
						getSelectionModel().select(rowItem);
						if (clicked.getButton().equals(MouseButton.SECONDARY))
						{
							displayMenu(Arrays.asList(rowItem), clicked.getScreenX(), clicked.getScreenY());
						}
					}
					else
					{
						getSelectionModel().clearSelection();
					}
				}

			});

			return row;
		});
	}

	/**
	 * Displays the context menu for server entries.
	 *
	 * @param serverList
	 *            The list of servers that the context menu actions will affect
	 * @param posX
	 *            X coordinate
	 * @param posY
	 *            Y coodrinate
	 */
	protected void displayMenu(final List<SampServer> serverList, final double posX, final double posY)
	{
		final boolean sizeEqualsOne = serverList.size() == 1;

		connectMenuItem.setVisible(sizeEqualsOne);
		contextMenu.getItems().get(1).setVisible(sizeEqualsOne); // Separator
		copyIpAddressAndPortMenuItem.setVisible(sizeEqualsOne);
		visitWebsiteMenuItem.setVisible(sizeEqualsOne);

		final boolean favouriteMode = tableMode == SampServerTableMode.FAVOURITES;

		addToFavouritesMenuItem.setVisible(!favouriteMode);
		removeFromFavouritesMenuItem.setVisible(favouriteMode);

		contextMenu.show(this, posX, posY);
	}

	/**
	 * Sets the mode, which decides how the table will behave.
	 *
	 * @param mode
	 *            the mode that the {@link SampServerTable} will be used for.
	 */
	public void setServerTableMode(final SampServerTableMode mode)
	{
		tableMode = mode;
	}

	/**
	 * @return the comparator property that is used to sort the items
	 */
	public ObjectProperty<Comparator<? super SampServer>> sortedListComparatorProperty()
	{
		return sortedServers.comparatorProperty();
	}

	/**
	 * @return the predicate property that is used to filter the data
	 */
	public ObjectProperty<Predicate<? super SampServer>> predicateProperty()
	{
		return filteredServers.predicateProperty();
	}

	/**
	 * @return the {@link ObservableList} list that contains all data and is mutable
	 */
	public ObservableList<SampServer> getDataList()
	{
		return servers;
	}

	/**
	 * Returns true if this list contains the specified element
	 *
	 * @param server
	 *            the server to search for
	 * @return true if the data contains the server
	 */
	public boolean contains(final SampServer server)
	{
		return servers.contains(server);
	}

	/**
	 * Adds a new {@link SampServer} to the data.
	 *
	 * @param newServer
	 *            the server that will be added
	 */
	public void add(final SampServer newServer)
	{
		servers.add(newServer);
	}

	/**
	 * Adds a collection of new {@link SampServer} to the data.
	 *
	 * @param newServers
	 *            the servers that will be added
	 */
	public void addAll(final Collection<SampServer> newServers)
	{
		newServers.forEach(this::add);
	}
}
