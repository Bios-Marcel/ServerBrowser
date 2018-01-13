package com.msc.serverbrowser.gui.controllers.implementations;

import com.msc.serverbrowser.data.PastUsernames;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;
import com.msc.serverbrowser.util.samp.GTAController;

import javafx.collections.FXCollections;
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
	@FXML private TextField usernameTextField;

	@FXML private ListView<String> nameList;

	private final MenuItem	applyNameMenuItem	= new MenuItem("Apply Username");
	private final MenuItem	removeNameMenuItem	= new MenuItem("Remove username");

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

			final int numberOfSelectedItems = nameList.getSelectionModel().getSelectedItems().size();

			// If the selection is emoty, we don't really care
			if (numberOfSelectedItems == 0) {
				return;
			}

			/*
			 * Making an array copy of the list, because iterating over the ObservableList would
			 * lead to mistakes.
			 */
			final String[] usernames = nameList.getSelectionModel().getSelectedItems().toArray(new String[numberOfSelectedItems]);

			if (numberOfSelectedItems == 1) {
				showContextMenuForSingleItem(event.getScreenX(), event.getScreenY(), usernames[0]);
			}
			else if (numberOfSelectedItems > 1) {
				showContextMenuForMultipleItems(event.getScreenX(), event.getScreenY(), usernames);
			}
		}
	}

	private void showContextMenuForMultipleItems(final double showAtX, final double showAtY, final String[] usernames) {

		applyNameMenuItem.setVisible(false);
		removeNameMenuItem.setText("Remove Usernames");

		removeNameMenuItem.setOnAction(__ -> {
			for (final String name : usernames) {
				PastUsernames.removePastUsername(name);
				nameList.getItems().remove(name);
			}
		});

		menu.show(nameList, showAtX, showAtY);
	}

	private void showContextMenuForSingleItem(final double showAtX, final double showAtY, final String name) {

		applyNameMenuItem.setVisible(true);
		removeNameMenuItem.setText("Remove Username");

		applyNameMenuItem.setOnAction(__ -> {
			usernameTextField.setText(name);
			applyUsername();
		});

		removeNameMenuItem.setOnAction(__ -> {
			PastUsernames.removePastUsername(name);
			nameList.getItems().remove(name);
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
