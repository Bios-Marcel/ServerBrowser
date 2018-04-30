package com.msc.serverbrowser.constants

import java.io.File
import javax.swing.filechooser.FileSystemView

/**
 * Contains most Paths used by this application.
 *
 * @author Marcel
 */
object PathConstants {
    // Internal Paths
    val OWN_JAR: File = File(System.getProperty("java.class.path")).absoluteFile
    private const val MAIN_PACKAGE = "/com/msc/serverbrowser/"
    const val APP_ICON_PATH = MAIN_PACKAGE + "icons/icon.png"
    const val VIEW_PATH = MAIN_PACKAGE + "views/"
    const val STYLESHEET_PATH = VIEW_PATH + "stylesheets/"

    // Application specific Paths
    private val USER_PATH = System.getProperty("user.home") + File.separator
    val SAMPEX_PATH = USER_PATH + "sampex"
    val SAMPEX_LOG = PathConstants.SAMPEX_PATH + File.separator + "Log.log"
    val SAMPEX_TEMP_JAR = PathConstants.SAMPEX_PATH + File.separator + "temp.jar"
    private val CACHE = SAMPEX_PATH + File.separator + "cache"
    val CLIENT_CACHE = CACHE + File.separator + "clientversions"
    val SAMP_CMD = SAMPEX_PATH + File.separator + "sampcmd.exe"
    val TEMP_INSTALLER_ZIP = SAMPEX_PATH + File.separator + "tempInstaller.zip"
    val TEMP_INSTALLER_EXE = SAMPEX_PATH + File.separator + "tempInstaller.exe"

    // GTA / SAMP specific Paths
    private val GTA_USER_FILES = (FileSystemView.getFileSystemView().defaultDirectory.path + File.separator
            + "GTA San Andreas User Files")
    val SAMP_PATH = GTA_USER_FILES + File.separator + "SAMP"
    val SAMP_SCREENS = SAMP_PATH + File.separator + "//screens"
    val SAMP_CFG = SAMP_PATH + File.separator + "sa-mp.cfg"
    val SAMP_CHATLOG = "$SAMP_PATH\\chatlog.txt"
    val SAMP_USERDATA = "$SAMP_PATH\\USERDATA.DAT"
}
