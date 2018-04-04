package com.msc.serverbrowser.gui

import com.msc.serverbrowser.constants.PathConstants
import com.msc.serverbrowser.gui.controllers.implementations.*
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds all the important information related to every single view.
 *
 * @author Marcel
 */
enum class View(
        /**
         * @return ID of the View
         */
        val id: Int,
        /**
         * @return The Views title that will be used in the titleBar of the application
         */
        val title: String,
        /**
         * @return the [ViewController] Type to be used for this View
         */
        val controllerType: Class<out ViewController>,
        /**
         * @return the path to the FXML file of this View
         */
        val fxmlPath: String,
        /**
         * @return the path to the css stylesheet of this View
         */
        val stylesheetPath: String) {
    SERVERS(1, "Servers", ServerListController::class.java, PathConstants.VIEW_PATH + "ServerList.fxml", PathConstants.STYLESHEET_PATH + "serverListStyle.css"),
    USERNAME_CHANGER(2, "Username Changer", UsernameController::class.java, PathConstants.VIEW_PATH + "Username.fxml", PathConstants.STYLESHEET_PATH + "usernameStyle.css"),
    VERSION_CHANGER(3, "Version Changer", VersionChangeController::class.java, PathConstants.VIEW_PATH + "Version.fxml", PathConstants.STYLESHEET_PATH + "versionStyle.css"),
    SETTINGS(4, "Settings", SettingsController::class.java, PathConstants.VIEW_PATH + "Settings.fxml", PathConstants.STYLESHEET_PATH + "settingsStyle.css"),
    FILES(5, "Files", FilesController::class.java, PathConstants.VIEW_PATH + "Files.fxml", PathConstants.STYLESHEET_PATH + "filesStyle.css");


    companion object {

        private val ID_MAPPING = ConcurrentHashMap<Int, View>()

        // Create Mapping in order to be able to find an enum value by simply providing
        // its id.
        init {
            for (view in View.values()) {
                View.ID_MAPPING[view.id] = view
            }
        }

        /**
         * Returns the Enum Value that has the given id.
         *
         * @param idToGet id to check against
         * @return the found Enum Value
         */
        fun valueOf(idToGet: Int): Optional<View> {
            return Optional.ofNullable(View.ID_MAPPING[idToGet])
        }
    }
}