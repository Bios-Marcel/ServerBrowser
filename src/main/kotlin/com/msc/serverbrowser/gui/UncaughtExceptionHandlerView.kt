package com.msc.serverbrowser.gui

import com.msc.serverbrowser.Client
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Optional

/**
 * Simple view for showing runtime errors, it contains the severe message, the stacktrace and a
 * github issue creation hyperlink.
 *
 * @author Marcel
 * @since 06.03.2018
 */
class UncaughtExceptionHandlerView
/**
 * @param controller the view controller
 * @param cause the [Throwable] instance that requires a severe dialog
 */
(controller: UncaughtExceptionHandlerController, cause: Throwable) {
    private val root: Parent
    private var stage = Optional.empty<Stage>()

    init {
        val icon = ImageView(Image(this.javaClass.getResourceAsStream("/com/msc/serverbrowser/icons/error.png")))
        val title = Label(TITLE)
        title.font = Font.font(title.font.family, FontWeight.BOLD, 22.0)
        title.maxHeight = java.lang.Double.MAX_VALUE
        val header = HBox(15.0, icon, title)

        val messageHeader = Label("Error message:")
        messageHeader.font = Font.font(messageHeader.font.family, FontWeight.BOLD, 12.0)
        messageHeader.maxHeight = java.lang.Double.MAX_VALUE

        val message = Label(cause.message)

        val stackTraceHeader = Label("Full stacktrace:")
        stackTraceHeader.font = Font.font(stackTraceHeader.font.family, FontWeight.BOLD, 12.0)
        stackTraceHeader.maxHeight = java.lang.Double.MAX_VALUE

        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        cause.printStackTrace(printWriter)
        val stackTraceContent = stringWriter.toString()
        val stackTrace = TextArea(stackTraceContent)
        stackTrace.isEditable = false
        stackTrace.maxHeight = java.lang.Double.MAX_VALUE

        val whatDoHeader = Label("What can i do?")
        whatDoHeader.font = Font.font(whatDoHeader.font.family, FontWeight.BOLD, 12.0)
        whatDoHeader.maxHeight = java.lang.Double.MAX_VALUE

        val trimmedStackTrace = trimStackTrace(stackTrace.text)

        val whatDo = Hyperlink("Create an issue on Github.")
        whatDo.isUnderline = true
        whatDo.setOnAction { controller.onOpenGithubIssue(message.text ?: "Empty", trimmedStackTrace) }

        val information = VBox(5.0, messageHeader, message, whatDoHeader, whatDo, stackTraceHeader, stackTrace)
        information.padding = Insets(0.0, 30.0, 0.0, 35.0)
        VBox.setVgrow(stackTrace, Priority.ALWAYS)
        val internalContentInsets = Insets(0.0, 0.0, 0.0, 15.0)
        VBox.setMargin(stackTrace, internalContentInsets)
        VBox.setMargin(message, internalContentInsets)
        VBox.setMargin(whatDo, internalContentInsets)

        val content = VBox(10.0, header, information)
        content.padding = Insets(5.0)
        VBox.setVgrow(information, Priority.ALWAYS)

        val closeButton = Button("Close")
        closeButton.setOnAction { stage.ifPresent { it.close() } }
        val buttonBar = ButtonBar()
        buttonBar.padding = Insets(5.0)
        buttonBar.buttons.add(closeButton)

        root = VBox(5.0, content, buttonBar)
        VBox.setVgrow(content, Priority.ALWAYS)
    }

    /**
     * Reduces the stacktrace size, but keeps all application specific class paths.
     */
    private fun trimStackTrace(stackTrace: String): String {
        return stackTrace
                //Remove everything that isn't part of my classpath
                .replace("at (?!com\\.msc).*?([A-Z])".toRegex(), "$1")
                //There will obviously be no non-java / kotlin files
                .replace(".java", "")
                .replace(".kt", "")
    }

    /**
     * Shows the previously initialized View in a new [Stage].
     */
    fun show() {
        val dialogStage = Stage()
        stage = Optional.of(dialogStage)

        dialogStage.icons.add(Client.APPLICATION_ICON)
        dialogStage.title = Client.APPLICATION_NAME + " - " + TITLE
        dialogStage.initModality(Modality.APPLICATION_MODAL)
        val scene = Scene(root, 600.0, 400.0)
        dialogStage.scene = scene
        dialogStage.showAndWait()

        stage = Optional.empty()
    }

    companion object {
        private const val TITLE = "An severe occurred during program execution"
    }
}
