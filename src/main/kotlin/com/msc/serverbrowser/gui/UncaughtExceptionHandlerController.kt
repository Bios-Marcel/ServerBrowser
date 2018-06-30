package com.msc.serverbrowser.gui

import com.msc.serverbrowser.logging.Logging
import com.msc.serverbrowser.util.windows.OSUtility

import java.net.URI
import java.net.URISyntaxException

/**
 * Viewcontroller for an error report dialog.
 *
 * @author Marcel
 * @since 06.03.2018
 */
class UncaughtExceptionHandlerController {

    /**
     * Opens the webbrowser with the new issue Github-page and an already given issue title and
     * issue body, which already contains information about the exception, th os and the jvm.
     *
     * @param errorMessage the error message which is also used as issue title
     * @param stackTrace the stacktrace of the [Exception] which the issue is related to
     */
    fun onOpenGithubIssue(errorMessage: String, stackTrace: String) {
        val message = StringBuilder(700)
        val stacktraceMarkdown = "```\n$stackTrace```"

        message
                .append("<!--")
                .append(LINE_SEPARATOR)
                .append("This is an autogenerated issue template which already contains some information and you are supposed to fill in the rest.")
                .append(LINE_SEPARATOR)
                .append(LINE_SEPARATOR)
                .append("Please attach your latest logfile, which is located at `%userprofile%/sampex/Log.log`, to this issue.")
                .append(LINE_SEPARATOR)
                .append("-->")
                .append(LINE_SEPARATOR)
                .append(LINE_SEPARATOR)
                .append("# Description of the Problem")
                .append(LINE_SEPARATOR)
                .append("PLEASE FILL IN - What have you done to cause the error?")
                .append(LINE_SEPARATOR)
                .append("## Error message")
                .append(LINE_SEPARATOR)
                .append(errorMessage)
                .append(LINE_SEPARATOR)
                .append("## Full stacktrace")
                .append(LINE_SEPARATOR)
                .append(stacktraceMarkdown)
                .append(LINE_SEPARATOR)
                .append("## Additional information")
                .append(LINE_SEPARATOR)
                .append("Operating system: ")
                .append(System.getProperty("os.name"))
                .append(", ")
                .append(System.getProperty("os.version"))
                .append(", ")
                .append(System.getProperty("os.arch"))
                .append(LINE_SEPARATOR)
                .append("Java: ")
                .append(System.getProperty("java.vm.name"))
                .append(" (")
                .append(System.getProperty("java.runtime.version"))
                .append(") by ")
                .append(System.getProperty("java.vm.vendor"))
                .append(LINE_SEPARATOR)

        try {
            val fragment = "title=" + errorMessage + "&body=" + message.toString()
            val uri = URI("https", "github.com", "/Bios-Marcel/ServerBrowser/issues/new", fragment, "")
            OSUtility.browse(uri)
        } catch (exception: URISyntaxException) {
            Logging.error("Error reporting error", exception)
        }

    }

    companion object {
        private const val LINE_SEPARATOR = "\n"
    }
}
