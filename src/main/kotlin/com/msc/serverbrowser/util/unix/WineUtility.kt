package com.msc.serverbrowser.util.unix

import com.msc.serverbrowser.util.windows.Registry

object WineUtility {

    val documentsPath: String
        get() {
            val pathAfterExpandedPath = Registry
                    .readString("HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\User Shell Folders", "Personal")

            val expandedPath = ProcessBuilder(listOf("wine", "cmd", "/c", "echo", pathAfterExpandedPath))
                    .start()
                    .inputStream
                    .bufferedReader()
                    .readLine()
                    .replace("\"", "")

            return convertPath(expandedPath)
        }

    fun convertPath(windowsPath: String): String = ProcessBuilder("winepath", windowsPath)
            .start()
            .inputStream
            .bufferedReader()
            .readLine()

}