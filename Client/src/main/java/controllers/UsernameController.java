package controllers;

import java.util.Objects;

import data.PastUsernames;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import util.GTA;

public class UsernameController
{
	@FXML
	private TextField			usernameTextField;

	@FXML
	private ListView<String>	nameList;

	private ContextMenu			menu;

	private MenuItem			setName;

	private MenuItem			removeName;

	public void init()
	{
		usernameTextField.focusedProperty().addListener((ChangeListener<Boolean>) (arg0, oldPropertyValue, newPropertyValue) ->
		{
			if (!newPropertyValue)
			{
				GTA.getUsername().set(usernameTextField.getText());
			}
		});

		usernameTextField.setText(GTA.getUsername().get());

		nameList.setItems(FXCollections.observableArrayList(PastUsernames.getPastUsernames()));
	}

	@FXML
	public void onUsernameClicked(final MouseEvent e)
	{
		final String username = nameList.getSelectionModel().getSelectedItem();

		if (Objects.isNull(menu))
		{
			menu = new ContextMenu();
			setName = new MenuItem("Use Username");
			removeName = new MenuItem("Remove username");
			menu.getItems().add(setName);
			menu.getItems().add(removeName);
		}

		menu.hide();

		if (Objects.nonNull(username))
		{
			if (e.getButton().equals(MouseButton.PRIMARY) || e.getButton().equals(MouseButton.SECONDARY))
			{

				menu.setOnAction(click ->
				{
					final MenuItem clickedItem = (MenuItem) click.getTarget();

					if (clickedItem.equals(setName))
					{
						usernameTextField.setText(username);
					}
					else
					{
						PastUsernames.removePastUsername(username);
					}
				});

				menu.show(nameList, e.getScreenX(), e.getScreenY());

				nameList.getSelectionModel().clearSelection();
			}
		}
	}

}
