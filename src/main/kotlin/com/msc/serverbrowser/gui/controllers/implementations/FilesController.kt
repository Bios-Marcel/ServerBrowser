package com.msc.serverbrowser.gui.controllers.implementations

import com.github.plushaze.traynotification.animations.Animations
import com.github.plushaze.traynotification.notification.NotificationTypeImplementations
import com.github.plushaze.traynotification.notification.TrayNotificationBuilder
import com.msc.serverbrowser.Client
import com.msc.serverbrowser.constants.PathConstants
import com.msc.serverbrowser.data.properties.ClientPropertiesController
import com.msc.serverbrowser.data.properties.UseDarkThemeProperty
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController
import com.msc.serverbrowser.gui.views.FilesView
import com.msc.serverbrowser.info
import com.msc.serverbrowser.severe
import com.msc.serverbrowser.util.basic.FileUtility
import com.msc.serverbrowser.util.basic.StringUtility
import com.msc.serverbrowser.warn
import javafx.event.EventHandler
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Predicate
import java.util.regex.Pattern

/**
 * Controls the Files view which allows you to look at your taken screenshots, your chatlogs and
 * your saved positions.
 *
 * @author Marcel
 * @since 08.07.2017
 *
 * @property filesView the view to be used by this controller
 */
class FilesController(private val filesView: FilesView) : ViewController {

    init {
        filesView.setLoadChatLogsButtonAction(EventHandler { loadChatLog() })
        filesView.setClearChatLogsButtonAction(EventHandler { clearChatLog() })

        filesView.showColorsProperty.addListener { _ -> loadChatLog() }
        filesView.showColorsAsTextProperty.addListener { _ -> loadChatLog() }
        filesView.showTimesIfAvailableProperty.addListener { _ -> loadChatLog() }
        filesView.lineFilterProperty.addListener { _ -> loadChatLog() }

        loadChatLog()
    }

    private fun loadChatLog() {
        val darkThemeInUse = ClientPropertiesController.getProperty(UseDarkThemeProperty)
        val gray = "#333131"
        val white = "#FFFFFF"
        val newContent = StringBuilder("<html><body style='background-color: ")
                .append(if (darkThemeInUse) gray else white)
                .append("; color: ")
                .append(if (darkThemeInUse) white else gray)
                .append("';")
                .append(">")

        try {
            val path = Paths.get(PathConstants.SAMP_CHATLOG)
            val filterProperty = filesView.lineFilterProperty.valueSafe.toLowerCase()

            FileUtility.readAllLinesTryEncodings(path, StandardCharsets.ISO_8859_1, StandardCharsets.UTF_8, StandardCharsets.US_ASCII)
                    .stream()
                    .filter(Predicate<String> { it.isEmpty() }.negate())
                    .filter { line -> line.toLowerCase().contains(filterProperty) }
                    .map { StringUtility.escapeHTML(it) }
                    .map { this.processSampChatlogTimestamps(it) }
                    .map { this.processSampColorCodes(it) }
                    .map { line -> "$line<br/>" }
                    .forEach { newContent.append(it) }
        } catch (exception: FileNotFoundException) {
            info("Chatlog file doesn't exist.")
        } catch (exception: IOException) {
            severe("Error loading chatlog.", exception)
        }

        filesView.setChatLogTextAreaContent(newContent.toString())
    }

    private fun processSampChatlogTimestamps(line: String): String {
        if (filesView.showTimesIfAvailableProperty.get()) {
            return line
        }

        val timeRegex = "\\[(?:(?:([01]?\\d|2[0-3]):)?([0-5]?\\d):)?([0-5]?\\d)]"
        return if (line.length >= 10 && line.substring(0, 10).matches(timeRegex.toRegex())) {
            line.replaceFirst(timeRegex.toRegex(), "")
        } else line
    }

    private fun processSampColorCodes(line: String): String {
        val showColorsAsText = filesView.showColorsAsTextProperty.get()
        val showColors = filesView.showColorsProperty.get()
        if (showColorsAsText && !showColors) {
            return line
        }

        val colorRegex = "([{](.{6})[}])"

        if (showColors) {
            var fixedLine = "<span>" + line.replace("{000000}", "{FFFFFF}")
            val colorCodeMatcher = Pattern.compile(colorRegex).matcher(fixedLine)
            while (colorCodeMatcher.find()) {

                val replacementColorCode = "#" + colorCodeMatcher.group(2)
                val replacement = StringBuilder("</span><span style='color:")
                        .append(replacementColorCode)
                        .append(";'>")
                val color = colorCodeMatcher.group(1)
                if (showColorsAsText) {
                    replacement.append(color)
                }
                fixedLine = fixedLine.replace(color, replacement.toString())
            }

            return "$fixedLine</span>"
        }

        return line.replace(colorRegex.toRegex(), "")
    }

    private fun clearChatLog() {

        try {
            Files.deleteIfExists(Paths.get(PathConstants.SAMP_CHATLOG))
            loadChatLog()
        } catch (exception: IOException) {
            TrayNotificationBuilder()
                    .type(NotificationTypeImplementations.ERROR)
                    .animation(Animations.POPUP)
                    .title(Client.getString("couldntClearChatLog"))
                    .message(Client.getString("checkLogsForMoreInformation")).build().showAndDismiss(Client.DEFAULT_TRAY_DISMISS_TIME)

            warn("Couldn't clear chatlog", exception)
        }

    }
}
