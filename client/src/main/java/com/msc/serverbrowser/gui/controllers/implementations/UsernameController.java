package com.msc.serverbrowser.gui.controllers.implementations;

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
	private void onUsernameClicked(final MouseEvent e) {
		final ObservableList<String> usernames = nameList.getSelectionModel().getSelectedItems();
		
		menu.hide();
		
		if (e.getButton().equals(MouseButton.SECONDARY)) {
			if (usernames.size() == 1) {// If only one item is selected
				final String name = usernames.get(0);
				
				applyNameMenuItem.setVisible(true);
				removeNameMenuItem.setText("Remove Username");
				menu.setOnAction(click -> {
					final MenuItem clickedItem = (MenuItem) click.getTarget();
					
					if (clickedItem.equals(applyNameMenuItem)) {
						usernameTextField.setText(name);
						applyUsername();
					} else {
						PastUsernames.removePastUsername(name);
						nameList.getItems().remove(name);
					}
				});
				
				menu.show(nameList, e.getScreenX(), e.getScreenY());
			} else if (usernames.size() > 1) {// if more than one item is selected
				applyNameMenuItem.setVisible(false);
				removeNameMenuItem.setText("Remove Usernames");
				menu.setOnAction(click -> {
					final MenuItem clickedItem = (MenuItem) click.getTarget();
					
					if (clickedItem.equals(removeNameMenuItem)) {
						for (final String name : usernames) {
							PastUsernames.removePastUsername(name);
							nameList.getItems().remove(name);
						}
					}
				});
				
				menu.show(nameList, e.getScreenX(), e.getScreenY());
			}
		}
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
