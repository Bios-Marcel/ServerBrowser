package com.msc.serverbrowser.gui.controllers.implementations

import com.msc.serverbrowser.Client
import com.msc.serverbrowser.data.properties.ClientPropertiesController
import com.msc.serverbrowser.data.properties.LastViewProperty
import com.msc.serverbrowser.data.properties.SaveLastViewProperty
import com.msc.serverbrowser.data.properties.UseDarkThemeProperty
import com.msc.serverbrowser.gui.View
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController
import com.msc.serverbrowser.gui.views.FilesView
import com.msc.serverbrowser.gui.views.MainView
import com.msc.serverbrowser.gui.views.ServerView
import com.msc.serverbrowser.severe
import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import javafx.scene.text.Font
import java.io.IOException

/**
 * Main [ViewController] for this application.
 *
 * @author Marcel
 * @since 10.01.2018
 *
 * @property mainView the view to be used by this controller
 */
class MainController(val client: Client, val mainView: MainView) : ViewController {

    /**
     * @return the current view
     */
    var activeView: View
        private set

    private var activeSubViewController: ViewController? = null

    val titleProperty: StringProperty

    /**
     * Returns the current [activeSubViewController]
     *
     * @return [activeSubViewController]
     */
    val settingsController: SettingsController?
        get() = activeSubViewController as? SettingsController

    init {
        titleProperty = SimpleStringProperty((Client.APPLICATION_NAME))

        Font.loadFont(MainController::class.java.getResource("/com/msc/serverbrowser/fonts/FontAwesome.otf").toExternalForm(), 12.0)
        configureMenuItems()
        registerBottomBarHyperlinks()
        if (Client.isDevelopmentModeActivated) {
            registerDevShortcuts()
        }

        activeView = if (ClientPropertiesController.getProperty(SaveLastViewProperty)) {
            val view = View.valueOf(ClientPropertiesController.getProperty(LastViewProperty)).orElse(View.SERVERS)
            loadView(view)
        } else {
            loadView(View.valueOf(ClientPropertiesController.getDefaultProperty(LastViewProperty)).get())
        }
    }

    private fun registerDevShortcuts() {
        mainView.rootPane.addEventHandler(KeyEvent.KEY_RELEASED) { event ->
            if (event.isControlDown && event.code == KeyCode.D) {
                val currentValue = ClientPropertiesController.getProperty(UseDarkThemeProperty)
                ClientPropertiesController.setProperty(UseDarkThemeProperty, !currentValue)
                client.applyTheme(mainView.rootPane.scene)
                client.reloadViewIfLoaded(activeView)
            }
        }
    }

    private fun configureMenuItems() {
        mainView.setMenuItemAllAction(EventHandler { onServersMenuItemClicked() })
        mainView.setMenuItemUsernameAction(EventHandler { onUsernameMenuItemClicked() })
        mainView.setMenuItemVersionAction(EventHandler { onVersionMenuItemClicked() })
        mainView.setMenuItemFilesAction(EventHandler { onFilesMenuItemClicked() })
        mainView.setMenuItemSettingsAction(EventHandler { onSettingsMenuItemClicked() })
        mainView.setMenuItemKeyBinderAction(EventHandler { onKeyBinderMenuItemClicked() })
    }

    private fun registerBottomBarHyperlinks() {
        mainView.setGitHubHyperlink("https://github.com/Bios-Marcel/ServerBrowser")
        mainView.setHelpHyperlink("https://github.com/Bios-Marcel/ServerBrowser/wiki")
        mainView.setDonateHyperlink("https://github.com/Bios-Marcel/ServerBrowser#donate")
    }

    /**
     * Adds nodes to the Clients bottom bar.
     *
     * @param nodes the node that will be added
     */
    fun addItemsToBottomBar(vararg nodes: Node) {
        mainView.addToBottomBar(*nodes)
    }

    /**
     * @return the progress [DoubleProperty] of the [ProgressBar] which resides in the
     * [MainView]
     */
    fun progressProperty(): DoubleProperty {
        return mainView.globalProgressProperty()
    }

    /**
     * Sets the text in front of the global [ProgressBar] bar.
     *
     * @param text the text tht appears in front of the global [ProgressBar]
     */
    fun setGlobalProgressText(text: String) {
        mainView.setGlobalProgressBarText(text)
    }

    private fun onServersMenuItemClicked() {
        activeView = loadView(View.SERVERS)
    }

    private fun onUsernameMenuItemClicked() {
        activeView = loadView(View.USERNAME_CHANGER)
    }

    private fun onVersionMenuItemClicked() {
        activeView = loadView(View.VERSION_CHANGER)
    }

    private fun onFilesMenuItemClicked() {
        activeView = loadView(View.FILES)
    }

    private fun onSettingsMenuItemClicked() {
        activeView = loadView(View.SETTINGS)
    }

    private fun onKeyBinderMenuItemClicked() {
        activeView = loadView(View.KEY_BINDER)
    }

    /**
     * Loads a specific view.
     *
     * @param view the view to be loaded
     */
    fun loadView(view: View): View {
        if (activeSubViewController != null) {
            activeSubViewController!!.onClose()
        }

        mainView.removeNodesFromBottomBar()

        val loadedNode = when (view) {
            View.FILES -> loadFilesView()
            View.SERVERS -> loadServersView()
            View.SETTINGS -> loadSettingsView()
            View.USERNAME_CHANGER -> loadUsernameView()
            View.VERSION_CHANGER -> loadVersionChangerView()
            View.KEY_BINDER -> loadKeyBinderView()
        }

        initViewData(view, loadedNode)
        return view
    }

    private fun loadFilesView(): Parent {
        val filesView = FilesView()
        activeSubViewController = FilesController(filesView)
        return filesView.rootPane
    }

    private fun loadSettingsView() = loadFXML(SettingsController(client), View.SETTINGS)

    private fun loadServersView(): Parent {
        val serverView = ServerView(client)
        val serverListController = ServerListController(client, this, serverView)
        serverListController.initialize()
        activeSubViewController = serverListController
        return serverView.rootPane
    }

    private fun loadKeyBinderView(): Parent {
/*        val keyBinderView = KeyBinderView()
        val keyBinderController = KeyBinderController(keyBinderView)
        keyBinderController.initialize()
        activeSubViewController = keyBinderController
        return keyBinderController.rootPane*/
        return GridPane()

    }

    private fun loadUsernameView() = loadFXML(UsernameController(), View.USERNAME_CHANGER)

    private fun loadVersionChangerView() = loadFXML(VersionChangeController(client), View.VERSION_CHANGER)

    private fun loadFXML(controller: ViewController, view: View): Parent {
        try {
            val loader = FXMLLoader()
            loader.location = javaClass.getResource(view.fxmlPath)
            loader.resources = Client.languageResourceBundle

            // Creating a new instance of the specified controller, controllers never have
            // constructor arguments, therefore this is supposedly fine.
            activeSubViewController = controller
            loader.setController(controller)
            return loader.load()
        } catch (exception: IOException) {
            severe("Couldn't load view.", exception)
        } catch (exception: InstantiationException) {
            severe("Couldn't load view.", exception)
        } catch (exception: IllegalAccessException) {
            severe("Couldn't load view.", exception)
        }

        return Label("Error loading view.")
    }

    private fun initViewData(view: View, loadedNode: Parent) {
        loadedNode.stylesheets.setAll(view.stylesheetPath)
        mainView.selectMenuItemForView(view)
        mainView.setActiveViewNode(loadedNode)
        titleProperty.value = (Client.APPLICATION_NAME + " - " + view.title)
    }

    /**
     * Reloads the current view.
     */
    fun reloadView() {
        loadView(activeView)
    }

    override fun onClose() {
        ClientPropertiesController.setProperty(LastViewProperty, activeView.id)
        Platform.exit() // Make sure that the application doesn't stay open for some reason
    }
}