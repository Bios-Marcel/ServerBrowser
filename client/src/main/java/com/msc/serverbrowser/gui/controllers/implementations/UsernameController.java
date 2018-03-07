package com.msc.serverbrowser.gui.controllers.implementations;

import com.msc.serverbrowser.Client;
import com.msc.serverbrowser.data.PastUsernames;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.util.samp.GTAController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * ViewController for the Username View.
 *
 * @author Marcel
 */
public class UsernameController implements ViewController {
	@FXML
	private TextField usernameTextField;

	@FXML
	private ListView<String> nameList;

	private final MenuItem	applyNameMenuItem	= new MenuItem(Client.getString("applyUsername"));
	private final MenuItem	removeNameMenuItem	= new MenuItem(Client.getString("removeUsernameSingular"));

	private final ContextMenu menu = new ContextMenu(applyNameMenuItem, removeNameMenuItem);

	@Override
	public void initialize() {
		usernameTextField.textProperty().bindBidirectional(GTAController.usernameProperty);

		nameList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		nameList.setItems(FXCollections.observableArrayList(PastUsernames.getPastUsernames()));
	}

	@FXML
	private void onUsernameClicked(final MouseEvent event) {

		menu.hide();

		if (event.getButton().equals(MouseButton.SECONDARY)) {

			final ObservableList<String> selectedItems = nameList.getSelectionModel().getSelectedItems();

			/*
			 * Making an array copy of the list, because iterating over the ObservableList would
			 * lead to mistakes.
			 */
			final String[] usernames = selectedItems.toArray(new String[0]);

			showContextMenuForMultipleItems(event.getScreenX(), event.getScreenY(), usernames);
		}
	}

	private void showContextMenuForMultipleItems(final double showAtX, final double showAtY, final String... names) {

		if (names == null || names.length == 0) {
			return;
		}

		final boolean singleUsername = names.length == 1;
		applyNameMenuItem.setVisible(singleUsername);
		applyNameMenuItem.setOnAction(__ -> {
			usernameTextField.setText(names[0]);
			applyUsername();
		});

		removeNameMenuItem.setText(singleUsername ? Client.getString("removeUsernameSingular") : Client.getString("removeUsernamePlural"));
		removeNameMenuItem.setOnAction(__ -> {
			for (final String name : names) {
				PastUsernames.removePastUsername(name);
				nameList.getItems().remove(name);
			}
		});

		menu.show(nameList, showAtX, showAtY);
	}

	@FXML
	private void applyUsername() {
		GTAController.applyUsername();
		nameList.setItems(FXCollections.observableArrayList(PastUsernames.getPastUsernames()));
	}

	@Override
	public void onClose() {
		// Do nothing
	}
}
