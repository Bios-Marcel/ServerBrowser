package com.msc.serverbrowser.gui.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.FavouritesController;
import com.msc.serverbrowser.data.entites.SampServer;
import com.msc.serverbrowser.data.properties.ClientPropertiesController;
import com.msc.serverbrowser.data.properties.Property;
import com.msc.serverbrowser.util.samp.GTAController;
import com.msc.serverbrowser.util.windows.OSUtility;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;

/**
 * {@link TableView} that was made for the ServerList View, contains a special TableRowFactory and
 * allows filtering and sorting.
 *
 * @author Marcel
 * @since 23.09.2017
 */
public class SampServerTable extends TableView<SampServer> {
	private SampServerTableMode tableMode = SampServerTableMode.FAVOURITES;

	private final MenuItem	addToFavouritesMenuItem			= new MenuItem(Client.getString("addToFavourites"));
	private final MenuItem	removeFromFavouritesMenuItem	= new MenuItem(Client.getString("removeFromFavourites"));
	private final MenuItem	visitWebsiteMenuItem			= new MenuItem(Client.getString("visitWebsite"));
	private final MenuItem	connectMenuItem					= new MenuItem(Client.getString("connectToServer"));
	private final MenuItem	copyIpAddressAndPortMenuItem	= new MenuItem(Client.getString("copyIpAddressAndPort"));

	private final ContextMenu contextMenu = new ContextMenu(connectMenuItem, new SeparatorMenuItem(), addToFavouritesMenuItem, removeFromFavouritesMenuItem, copyIpAddressAndPortMenuItem, visitWebsiteMenuItem);

	private final ObservableList<SampServer> servers = getItems();

	private final FilteredList<SampServer>	filteredServers					= new FilteredList<>(servers);
	private final SortedList<SampServer>	sortedServers					= new SortedList<>(filteredServers);
	private static final DataFormat			OLD_INDEXES_LIST_DATA_FORMAT	= new DataFormat("index");

	/**
	 * Contructor; sets the TableRowFactory, the ContextMenu Actions and table settings.
	 */
	public SampServerTable() {
		setItems(sortedServers);

		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		setKeyActions();
		initTableRowFactory();
		setMenuItemDefaultActions();
	}

	private Optional<SampServer> getFirstIfAnythingSelected() {
		final List<SampServer> selectedServers = getSelectionModel().getSelectedItems();

		if (selectedServers.isEmpty()) {
			return Optional.empty();
		}

		return Optional.ofNullable(selectedServers.get(0));
	}

	private void setKeyActions() {
		setOnKeyReleased(released -> {
			if (tableMode == SampServerTableMode.FAVOURITES && released.getCode() == KeyCode.DELETE) {
				deleteSelectedFavourites();
			}
		});

	}

	private void setMenuItemDefaultActions() {
		connectMenuItem.setOnAction(__ -> {
			getFirstIfAnythingSelected().ifPresent(server -> GTAController.tryToConnect(server.getAddress(), server.getPort()));
		});

		visitWebsiteMenuItem.setOnAction(__ -> getFirstIfAnythingSelected().ifPresent(server -> OSUtility.browse(server.getWebsite())));

		addToFavouritesMenuItem.setOnAction(__ -> {
			final List<SampServer> serverList = getSelectionModel().getSelectedItems();
			serverList.forEach(FavouritesController::addServerToFavourites);
		});

		removeFromFavouritesMenuItem.setOnAction(__ -> deleteSelectedFavourites());

		copyIpAddressAndPortMenuItem.setOnAction(__ -> {
			final Optional<SampServer> serverOptional = getFirstIfAnythingSelected();

			serverOptional.ifPresent(server -> {
				final ClipboardContent content = new ClipboardContent();
				content.putString(server.getAddress() + ":" + server.getPort());
				Clipboard.getSystemClipboard().setContent(content);
			});
		});
	}

	private void deleteSelectedFavourites() {
		final Alert alert = new Alert(AlertType.CONFIRMATION, Client.getString("sureYouWantToDeleteFavourites"), ButtonType.YES, ButtonType.NO);
		Client.insertAlertOwner(alert);
		alert.setTitle(Client.getString("deleteFavourites"));
		final Optional<ButtonType> result = alert.showAndWait();

		result.ifPresent(buttonType -> {
			if (buttonType == ButtonType.YES) {
				final List<SampServer> serverList = getSelectionModel().getSelectedItems();
				serverList.forEach(FavouritesController::removeServerFromFavourites);
				servers.removeAll(serverList);
			}
		});
	}

	private void initTableRowFactory() {

		setRowFactory(facotry -> {
			final TableRow<SampServer> row = new TableRow<>();

			row.setOnDragOver(event -> event.acceptTransferModes(TransferMode.MOVE));

			row.setOnDragEntered(event -> {
				row.getStyleClass().add("overline");
			});

			row.setOnDragExited(event -> {
				row.getStyleClass().remove("overline");
			});

			row.setOnDragDetected(event -> {
				final List<SampServer> selectedServers = getSelectionModel().getSelectedItems();
				final SampServer rowServer = row.getItem();
				if (servers.size() <= 1 || selectedServers.size() < 1 || !selectedServers.contains(rowServer)) {
					return;
				}

				final ClipboardContent clipboardContent = new ClipboardContent();

				final List<Integer> selectedServerIndices = getSelectionModel().getSelectedItems().stream()
						.map(servers::indexOf)
						.collect(Collectors.toList());
				clipboardContent.put(OLD_INDEXES_LIST_DATA_FORMAT, selectedServerIndices);

				final StringJoiner ghostString = new StringJoiner(System.lineSeparator());
				selectedServers.forEach(server -> ghostString.add(server.getHostname() + " - " + server.toString()));

				final Dragboard dragBoard = startDragAndDrop(TransferMode.MOVE);
				dragBoard.setDragView(new Text(ghostString.toString()).snapshot(null, null), event.getX(), event.getY());
				dragBoard.setContent(clipboardContent);
			});

			row.setOnDragDropped(event -> {
				final Dragboard dragBoard = event.getDragboard();
				final List<Integer> oldIndexes = (List<Integer>) dragBoard.getContent(OLD_INDEXES_LIST_DATA_FORMAT);
				final int newIndex = servers.indexOf(row.getItem());
				final int newIndexCorrect = newIndex == -1 ? servers.size() : newIndex;

				if (oldIndexes.contains(newIndexCorrect)) {
					return;
				}

				if (newIndexCorrect != oldIndexes.get(0)) {
					final List<SampServer> draggedServer = getSelectionModel().getSelectedItems().stream().collect(Collectors.toList());

					if (oldIndexes.get(0) < newIndexCorrect) {
						Collections.reverse(draggedServer);
						draggedServer.forEach(server -> servers.add(newIndexCorrect, server));

						Collections.sort(oldIndexes);
						Collections.reverse(oldIndexes);
						oldIndexes.forEach(index -> servers.remove(index.intValue()));
					}
					else {
						Collections.sort(oldIndexes);
						Collections.reverse(oldIndexes);
						oldIndexes.forEach(index -> servers.remove(index.intValue()));

						Collections.reverse(draggedServer);
						draggedServer.forEach(server -> servers.add(newIndexCorrect, server));
					}
				}

			});

			row.setOnMouseClicked(clicked -> {
				// A row has been clicked, so we want to hide the previous context menu
				contextMenu.hide();

				if (Objects.nonNull(row.getItem())) {
					// If there is an item in this row, we want to proceed further
					handleClick(row, clicked);
				}
				else {
					// Otherwise we clear the selection.
					getSelectionModel().clearSelection();
				}
			});

			return row;
		});

	}

	private void handleClick(final TableRow<SampServer> row, final MouseEvent clicked) {
		if (clicked.getButton() == MouseButton.PRIMARY) {
			handleLeftClick(row);
		}
		else if (clicked.getButton() == MouseButton.SECONDARY) {
			handleRightClick(row, clicked);
		}
	}

	private void handleRightClick(final TableRow<SampServer> row, final MouseEvent clicked) {
		final List<SampServer> selectedServers = getSelectionModel().getSelectedItems();

		if (getSelectionModel().getSelectedIndices().contains(row.getIndex())) {
			// In case the current selection model contains the clicked row, we want to open the
			// context menu on the current selection mode
			displayMenu(selectedServers, clicked.getScreenX(), clicked.getScreenY());
		}
		else {
			// Otherwise we will select the clicked item and open the context menu on it
			displayMenu(Arrays.asList(row.getItem()), clicked.getScreenX(), clicked.getScreenY());
		}
	}

	private void handleLeftClick(final TableRow<SampServer> row) {
		final Long lastLeftClickTime = (Long) row.getUserData();
		final boolean wasDoubleClick = Objects.nonNull(lastLeftClickTime) && System.currentTimeMillis() - lastLeftClickTime < 300;
		final boolean onlyOneSelectedItem = getSelectionModel().getSelectedItems().size() == 1;

		if (wasDoubleClick && onlyOneSelectedItem) {
			if (ClientPropertiesController.getPropertyAsBoolean(Property.CONNECT_ON_DOUBLECLICK)) {
				getFirstIfAnythingSelected().ifPresent(server -> GTAController.tryToConnect(server.getAddress(), server.getPort()));
			}
		}
		else {
			row.setUserData(Long.valueOf(System.currentTimeMillis()));
		}
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
	private void displayMenu(final List<SampServer> serverList, final double posX, final double posY) {
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
	public void setServerTableMode(final SampServerTableMode mode) {
		tableMode = mode;
	}

	/**
	 * @return {@link #tableMode}
	 */
	public SampServerTableMode getTableMode() {
		return tableMode;
	}

	/**
	 * @return the comparator property that is used to sort the items
	 */
	public ObjectProperty<Comparator<? super SampServer>> sortedListComparatorProperty() {
		return sortedServers.comparatorProperty();
	}

	/**
	 * @return the predicate property that is used to filter the data
	 */
	public ObjectProperty<Predicate<? super SampServer>> predicateProperty() {
		return filteredServers.predicateProperty();
	}

	/**
	 * @return the {@link ObservableList} list that contains all data and is mutable
	 */
	public ObservableList<SampServer> getDataList() {
		return servers;
	}

	/**
	 * Returns true if this list contains the specified element
	 *
	 * @param server
	 *            the server to search for
	 * @return true if the data contains the server
	 */
	public boolean contains(final SampServer server) {
		return servers.contains(server);
	}

	/**
	 * Deletes all currently contained servers.
	 */
	public void clear() {
		servers.clear();
	}

	/**
	 * Adds a new {@link SampServer} to the data.
	 *
	 * @param newServer
	 *            the server that will be added
	 */
	public void add(final SampServer newServer) {
		servers.add(newServer);
	}

	/**
	 * Adds a collection of new {@link SampServer} to the data.
	 *
	 * @param newServers
	 *            the servers that will be added
	 */
	public void addAll(final Collection<SampServer> newServers) {
		newServers.forEach(this::add);
	}
}
