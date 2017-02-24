package gui.controllers.implementations;

import data.PastUsernames;
import gui.controllers.interfaces.ViewController;
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
import util.GTA;

public class UsernameController implements ViewController
{
	@FXML
	private TextField			usernameTextField;

	@FXML
	private ListView<String>	nameList;

	private final MenuItem		setName		= new MenuItem("Use Username");

	private final MenuItem		removeName	= new MenuItem("Remove username");

	private final ContextMenu	menu		= new ContextMenu(setName, removeName);

	@Override
	public void init()
	{
		usernameTextField.textProperty().bindBidirectional(GTA.usernameProperty());

		nameList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		nameList.setItems(FXCollections.observableArrayList(PastUsernames.getPastUsernames()));
	}

	@FXML
	public void onUsernameClicked(final MouseEvent e)
	{
		final ObservableList<String> usernames = nameList.getSelectionModel().getSelectedItems();

		menu.hide();

		if (e.getButton().equals(MouseButton.SECONDARY))
		{
			if (usernames.size() == 1)
			{
				final String name = usernames.get(0);

				setName.setVisible(true);
				menu.setOnAction(click ->
				{
					final MenuItem clickedItem = (MenuItem) click.getTarget();

					if (clickedItem.equals(setName))
					{
						usernameTextField.setText(name);
					}
					else
					{
						PastUsernames.removePastUsername(name);
						nameList.getItems().remove(name);
					}
				});

				menu.show(nameList, e.getScreenX(), e.getScreenY());
			}
			else if (usernames.size() > 1)
			{
				setName.setVisible(false);
				menu.setOnAction(click ->
				{
					final MenuItem clickedItem = (MenuItem) click.getTarget();

					if (clickedItem.equals(removeName))
					{
						for (final String name : usernames)
						{
							PastUsernames.removePastUsername(name);
							nameList.getItems().remove(name);
						}
					}
				});

				menu.show(nameList, e.getScreenX(), e.getScreenY());
			}
		}
	}

}
