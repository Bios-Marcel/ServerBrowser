package com.msc.serverbrowser.util.windows

import java.util.*

object Registry {
    fun readString(path: String, key: String): Optional<String> {
        val arguments = mutableListOf("reg", "query", path, "/v", key)

        if (OSUtility.isWindows.not()) {
            arguments.add(0, "wine")
        }

        val start: Process = ProcessBuilder(arguments).start()

        val name = start.inputStream
                .bufferedReader()
                .use { it.readLines() }
                .last { it.isNotBlank() }
                .substringAfter("REG_SZ")
                .trim()
        return Optional.of(name)
    }

    fun writeString(path: String, key: String, value: String) {
        val arguments = mutableListOf("reg", "add", path, "/v", key, "/d", value, "/f")

        if (OSUtility.isWindows.not()) {
            arguments.add(0, "wine")
        }

        ProcessBuilder(arguments).start()
    }
}