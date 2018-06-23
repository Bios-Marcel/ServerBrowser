package com.msc.serverbrowser.util.unix

import com.msc.serverbrowser.data.properties.ClientPropertiesController
import com.msc.serverbrowser.data.properties.WineBinaryProperty
import com.msc.serverbrowser.data.properties.WinePrefixProperty
import com.msc.serverbrowser.util.windows.Registry

object WineUtility {

    val documentsPath: String?
        get() {
            val pathAfterExpandedPath = Registry
                    .readString("HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\User Shell Folders", "Personal")

            pathAfterExpandedPath ?: return null

            val expandedPath = createWineRunner(mutableListOf("cmd", "/c", "echo", pathAfterExpandedPath))
                    .start()
                    .inputStream
                    .bufferedReader()
                    .readLine()
                    .replace("\"", "")

            return convertPath(expandedPath)
        }

    fun createWineRunner(commands: List<String>): ProcessBuilder {
        val processBuilder = ProcessBuilder(mutableListOf(getWineBinaryPath()))
        processBuilder.command().addAll(commands)

        val prefix = getCustomWinePrefix()
        if (prefix.isNullOrBlank().not()) {
            processBuilder.environment().put("WINEPREFIX", prefix)
        }

        return processBuilder
    }

    fun getWineBinaryPath(): String {
        val path = ClientPropertiesController.getProperty(WineBinaryProperty)
        return if (path.isNotBlank()) {
            path
        } else {
            "wine"
        }
    }

    fun getCustomWinePrefix(): String? {
        val path = ClientPropertiesController.getProperty(WinePrefixProperty)
        return if (path.isNotBlank()) {
            path
        } else {
            null
        }
    }

    fun convertPath(windowsPath: String): String = ProcessBuilder("winepath", windowsPath)
            .start()
            .inputStream
            .bufferedReader()
            .readLine()

}