package gui.controllers.implementations;

import java.io.IOException;
import java.util.logging.Level;

import data.properties.ClientProperties;
import data.properties.PropertyIds;
import gui.Views;
import gui.controllers.interfaces.ViewController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import logging.Logging;

public class MainController implements ViewController
{
	@FXML
	private StackPane	menuItemFav;

	@FXML
	private StackPane	menuItemAll;

	@FXML
	private StackPane	menuItemUser;

	@FXML
	private StackPane	menuItemSettings;

	@FXML
	private StackPane	menuItemVersion;

	@FXML
	private StackPane	activeViewContainer;

	private Views		activeView;

	@FXML
	private Label		headerTitle;

	@Override
	public void init()
	{
		loadView(Views.valueOf(ClientProperties.getPropertyAsInt(PropertyIds.LAST_VIEW)));
	}

	@FXML
	public void onMenuItemClicked(final MouseEvent event)
	{
		final StackPane clicked = (StackPane) event.getSource();

		if (clicked.equals(menuItemFav))
		{
			loadView(Views.SERVERS_FAV);
		}
		else if (clicked.equals(menuItemAll))
		{
			loadView(Views.SERVERS_ALL);
		}
		else if (clicked.equals(menuItemUser))
		{
			loadView(Views.USERNAME_CHANGER);
		}
		else if (clicked.equals(menuItemVersion))
		{
			loadView(Views.VERSION_CHANGER);
		}
	}

	private void loadView(final Views view)
	{
		loadView(view, false);
	}

	private void loadView(final Views view, final boolean refresh)
	{
		if (refresh || view != activeView)
		{
			menuItemFav.setStyle("-fx-background-color: #538ED7;");
			menuItemSettings.setStyle("-fx-background-color: #538ED7;");
			menuItemUser.setStyle("-fx-background-color: #538ED7;");
			menuItemAll.setStyle("-fx-background-color: #538ED7;");
			menuItemVersion.setStyle("-fx-background-color: #538ED7;");

			switch (view)
			{
				case VERSION_CHANGER:
				{
					loadFXML(view);
					menuItemVersion.setStyle("-fx-background-color: #1F5FAE;");
					break;
				}
				case USERNAME_CHANGER:
				{
					loadFXML(view);
					menuItemUser.setStyle("-fx-background-color: #1F5FAE;");
					break;
				}
				case SETTINGS:
				{
					break;
				}
				case SERVERS_FAV:
				{
					loadFXML(view);
					menuItemFav.setStyle("-fx-background-color: #1F5FAE;");
					break;
				}
				case SERVERS_ALL:
				{
					loadFXML(view);
					menuItemAll.setStyle("-fx-background-color: #1F5FAE;");
					break;
				}
				default:
				{
					throw new IllegalArgumentException("Invalid View");
				}
			}

			activeView = view;
		}
	}

	private void loadFXML(final Views view)
	{
		try
		{
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(view.getFXMLPath()));
			final ViewController controller = view.getControllerType().newInstance();
			loader.setController(controller);
			activeViewContainer.getChildren().clear();
			activeViewContainer.getChildren().add(loader.load());
			activeViewContainer.getStylesheets().setAll(view.getStylesheetPath());
			headerTitle.setText(view.getTitle());
			controller.init();
		}
		catch (final IOException | InstantiationException | IllegalAccessException e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't load view.", e.getStackTrace());
		}
	}

	public void refreshViewIfDisplayed(final Views viewToRefresh)
	{
		if (getActiveViewID() == viewToRefresh)
		{
			loadView(viewToRefresh, true);
		}
	}

	private Views getActiveViewID()
	{
		return activeView;
	}

	public void onClose()
	{
		ClientProperties.setProperty(PropertyIds.LAST_VIEW, activeView.getId());
	}
}
