package com.msc.serverbrowser.util.unix

object WineUtility {
    fun convertPath(windowsPath: String) = ProcessBuilder("winepath", windowsPath)
            .start()
            .inputStream
            .bufferedReader()
            .readLine()

}