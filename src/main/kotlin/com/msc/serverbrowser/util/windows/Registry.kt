package com.msc.serverbrowser.util.windows

import com.msc.serverbrowser.util.unix.WineUtility

object Registry {
    fun readString(path: String, key: String): String? {
        val arguments = mutableListOf("reg", "query", path, "/v", key)

        val process = if (OSUtility.isWindows) {
            ProcessBuilder(arguments).start()
        } else {
            WineUtility.createWineRunner(arguments).start()
        }

        return process.inputStream
                .bufferedReader()
                .use { it.readLines() }
                .lastOrNull { it.isNotBlank() }
                ?.substringAfter("SZ")
                ?.trim()
    }

    fun writeString(path: String, key: String, value: String) {
        val arguments = listOf("reg", "add", path, "/v", key, "/d", value, "/f")

        if (OSUtility.isWindows) {
            ProcessBuilder(arguments).start()
        } else {
            WineUtility.createWineRunner(arguments).start()
        }

    }
}