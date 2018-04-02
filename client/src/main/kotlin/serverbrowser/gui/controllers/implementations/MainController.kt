package serverbrowser.gui.controllers.implementations

import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.text.Font
import serverbrowser.Client
import serverbrowser.data.properties.ClientPropertiesController
import serverbrowser.data.properties.LastViewProperty
import serverbrowser.data.properties.SaveLastViewProperty
import serverbrowser.data.properties.UseDarkThemeProperty
import serverbrowser.gui.View
import serverbrowser.gui.controllers.interfaces.ViewController
import serverbrowser.gui.views.FilesView
import serverbrowser.gui.views.MainView
import serverbrowser.logging.Logging
import java.io.IOException
import java.util.*

/**
 * Main [ViewController] for this application.
 *
 * @author Marcel
 * @since 10.01.2018
 */
class MainController
/**
 * @param mainView the view to be used by this controller
 */
(private val mainView: MainView) : ViewController {
    /**
     * @return the current view
     */
    var activeView: View? = null
        private set

    private var activeSubViewController: ViewController? = null

    /**
     * Returns an [Optional] of the current [ViewController] and tries casting it into
     * [SettingsController].
     *
     * @return [Optional] of [.activeSubViewController] or empty
     */
    val settingsController: SettingsController?
        get() = activeSubViewController as? SettingsController

    init {
        Font.loadFont(MainController::class.java.getResource("/com/msc/serverbrowser/fonts/FontAwesome.otf").toExternalForm(), 12.0)
        configureMenuItems()
        registerBottomBarHyperlinks()
        if (Client.isDevelopmentModeActivated) {
            registerDevShortcuts()
        }
    }

    private fun registerDevShortcuts() {
        mainView.rootPane.addEventHandler(KeyEvent.KEY_RELEASED) { event ->
            if (event.isControlDown && event.code == KeyCode.D) {
                val currentValue = ClientPropertiesController.getProperty(UseDarkThemeProperty)
                ClientPropertiesController.setProperty(UseDarkThemeProperty, !currentValue)
                Client.instance!!.applyTheme()
                Client.instance!!.reloadViewIfLoaded(activeView!!)
            }
        }
    }

    private fun configureMenuItems() {
        mainView.setMenuItemAllAction(EventHandler { onServersMenuItemClicked() })
        mainView.setMenuItemUsernameAction(EventHandler { onUsernameMenuItemClicked() })
        mainView.setMenuItemVersionAction(EventHandler { onVersionMenuItemClicked() })
        mainView.setMenuItemFilesAction(EventHandler { onFilesMenuItemClicked() })
        mainView.setMenuItemSettingsAction(EventHandler { onSettingsMenuItemClicked() })
    }

    override fun initialize() {
        if (ClientPropertiesController.getProperty(SaveLastViewProperty)) {
            val view = View.valueOf(ClientPropertiesController.getProperty(LastViewProperty)).orElse(View.SERVERS)
            loadView(view)
        } else {
            loadView(View.valueOf(ClientPropertiesController.getDefaultProperty(LastViewProperty)).get())
        }

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
        loadView(View.SERVERS)
    }

    private fun onUsernameMenuItemClicked() {
        loadView(View.USERNAME_CHANGER)
    }

    private fun onVersionMenuItemClicked() {
        loadView(View.VERSION_CHANGER)
    }

    private fun onFilesMenuItemClicked() {
        loadView(View.FILES)
    }

    private fun onSettingsMenuItemClicked() {
        loadView(View.SETTINGS)
    }

    /**
     * Loads a specific view.
     *
     * @param view the view to be loaded
     */
    fun loadView(view: View?) {

        if (Objects.nonNull(activeSubViewController)) {
            activeSubViewController!!.onClose()
        }

        mainView.removeNodesFromBottomBar()

        val loadedNode: Parent

        if (view == View.FILES) {
            loadedNode = loadFilesView()
        } else {
            loadedNode = loadFXML(view!!)
        }

        initViewData(view, loadedNode)
        activeView = view
    }

    private fun loadFilesView(): Parent {
        val filesView = FilesView()
        activeSubViewController = FilesController(filesView)
        return filesView.rootPane
    }

    private fun loadFXML(view: View): Parent {
        try {
            val loader = FXMLLoader()
            loader.location = javaClass.getResource(view.fxmlPath)
            loader.resources = Client.languageResourceBundle

            // Creating a new instance of the specified controller, controllers never have
            // constructor arguments, therefore this is supposedly fine.
            activeSubViewController = view.controllerType.newInstance()
            loader.setController(activeSubViewController)
            return loader.load()
        } catch (exception: IOException) {
            Logging.error("Couldn't load view.", exception)
        } catch (exception: InstantiationException) {
            Logging.error("Couldn't load view.", exception)
        } catch (exception: IllegalAccessException) {
            Logging.error("Couldn't load view.", exception)
        }

        return Label("Error loading view.")
    }

    private fun initViewData(view: View, loadedNode: Parent) {
        loadedNode.stylesheets.setAll(view.stylesheetPath)
        mainView.selectMenuItemForView(view)
        mainView.setActiveViewNode(loadedNode)
        Client.instance!!.setTitle(Client.APPLICATION_NAME + " - " + view.title)
    }

    /**
     * Reloads the current view.
     */
    fun reloadView() {
        loadView(activeView)
    }

    override fun onClose() {
        ClientPropertiesController.setProperty(LastViewProperty, activeView!!.id)
        Platform.exit() // Make sure that the application doesn't stay open for some reason
    }
}