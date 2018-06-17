package com.msc.serverbrowser.util.windows

object Registry {
    fun readString(path: String, key: String): String? {
        val arguments = mutableListOf("reg", "query", path, "/v", key)

        if (OSUtility.isWindows.not()) {
            arguments.add(0, "wine")
        }

        val start: Process = ProcessBuilder(arguments).start()

        return start.inputStream
                .bufferedReader()
                .use { it.readLines() }
                .lastOrNull { it.isNotBlank() }
                ?.substringAfter("SZ")
                ?.trim()
    }

    fun writeString(path: String, key: String, value: String) {
        val arguments = mutableListOf("reg", "add", path, "/v", key, "/d", value, "/f")

        if (OSUtility.isWindows.not()) {
            arguments.add(0, "wine")
        }

        ProcessBuilder(arguments).start()
    }
}