package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import util.GTA;

public class UsernameController
{
	@FXML
	private TextField usernameTextField;

	public void init()
	{
		usernameTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue,
					Boolean newPropertyValue)
			{
				if (!newPropertyValue)
				{
					GTA.getUsername().set(usernameTextField.getText());
				}
			}
		});

		usernameTextField.setText(GTA.getUsername().get());
	}
}
