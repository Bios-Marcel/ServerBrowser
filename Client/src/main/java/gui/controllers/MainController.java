package controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class MainController implements ViewController
{

	private static final String	VERSION_CHANGER_TITLE	= "Version Changer";

	private static final String	USERNAME_CHANGER_TITLE	= "Username Changer";

	private static final String	SA_MP_SERVERS_FAV_TITLE	= "Servers | Favourites";

	private static final String	SA_MP_SERVERS_ALL_TITLE	= "Servers | All";

	private static final int	VERSION_CHANGER			= 1;

	private static final int	USERNAME_CHANGER		= 2;

	private static final int	SETTINGS				= 3;

	private static final int	SERVERS_FAV				= 4;

	private static final int	SERVERS_ALL				= 5;

	@FXML
	private StackPane			menuItemFav;

	@FXML
	private StackPane			menuItemAll;

	@FXML
	private StackPane			menuItemUser;

	@FXML
	private StackPane			menuItemSettings;

	@FXML
	private StackPane			menuItemVersion;

	@FXML
	private StackPane			activeView;

	private int					activeViewId			= 0;

	@FXML
	private Label				headerTitle;

	@Override
	public void init()
	{
		loadView(SERVERS_FAV);
	}

	@FXML
	public void onMenuItemClicked(final MouseEvent event)
	{
		final StackPane clicked = (StackPane) event.getSource();

		if (clicked.equals(menuItemFav))
		{
			loadView(SERVERS_FAV);
		}
		else if (clicked.equals(menuItemAll))
		{
			loadView(SERVERS_ALL);
		}
		else if (clicked.equals(menuItemVersion))
		{
			loadView(VERSION_CHANGER);
		}
		else if (clicked.equals(menuItemUser))
		{
			loadView(USERNAME_CHANGER);
		}
	}

	private void loadView(final int viewId)
	{
		loadView(viewId, false);
	}

	private void loadView(final int viewId, final boolean refresh)
	{
		if (refresh || viewId != activeViewId)
		{
			menuItemFav.setStyle("-fx-background-color: #538ED7;");
			menuItemSettings.setStyle("-fx-background-color: #538ED7;");
			menuItemUser.setStyle("-fx-background-color: #538ED7;");
			menuItemAll.setStyle("-fx-background-color: #538ED7;");
			menuItemVersion.setStyle("-fx-background-color: #538ED7;");

			activeView.getChildren().clear();

			switch (viewId)
			{
				case VERSION_CHANGER:
				{
					loadVersionChanger();
					break;
				}
				case USERNAME_CHANGER:
				{
					loadUsernameChanger();
					break;
				}
				case SETTINGS:
				{
					break;
				}
				case SERVERS_FAV:
				{
					loadSeverListFavourite();
					break;
				}
				case SERVERS_ALL:
				{
					loadServerListAll();
					break;
				}
				default:
				{
					throw new IllegalArgumentException("Invalid View");
				}
			}

			activeViewId = viewId;
		}
	}

	private void loadSeverListFavourite()
	{
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/views/ServerList.fxml"));
		final ServerFavouriteListController controller = new ServerFavouriteListController();
		loader.setController(controller);
		try
		{
			activeView.getChildren().add(loader.load());
			menuItemFav.setStyle("-fx-background-color: #1F5FAE;");
			headerTitle.setText(SA_MP_SERVERS_FAV_TITLE);
			controller.init();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	private void loadVersionChanger()
	{
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/views/VersionUI.fxml"));
		final VersionChangeController controller = new VersionChangeController(this);
		loader.setController(controller);
		try
		{
			activeView.getChildren().add(loader.load());
			menuItemVersion.setStyle("-fx-background-color: #1F5FAE;");
			headerTitle.setText(VERSION_CHANGER_TITLE);
			controller.init();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	private void loadServerListAll()
	{
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/views/ServerList.fxml"));
		final ServerAllListController controller = new ServerAllListController();
		loader.setController(controller);
		try
		{
			activeView.getChildren().add(loader.load());
			menuItemAll.setStyle("-fx-background-color: #1F5FAE;");
			headerTitle.setText(SA_MP_SERVERS_ALL_TITLE);
			controller.init();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	private void loadUsernameChanger()
	{
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/views/Username.fxml"));
		final UsernameController controller = new UsernameController();
		loader.setController(controller);
		try
		{
			activeView.getChildren().add(loader.load());
			menuItemUser.setStyle("-fx-background-color: #1F5FAE;");
			headerTitle.setText(USERNAME_CHANGER_TITLE);
			controller.init();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	public void refreshVersionChangerViewIfDisplayed()
	{
		if (getActiveViewID() == VERSION_CHANGER)
		{
			loadView(VERSION_CHANGER, true);
		}
	}

	private int getActiveViewID()
	{
		return activeViewId;
	}
}
