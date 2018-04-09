package com.msc.serverbrowser.util.basic

/**
 * @author Marcel
 * @since 19.09.2017
 */
object StringUtility {

    /**
     * Puts `http://` in front of the url if not it already has `http://` or
     * `https://` in front of it.
     *
     * @param url the url to fix
     * @return the fixed url or the original if there was no need to fix
     */
    @JvmStatic
    fun fixUrlIfNecessary(url: String?): String {
        if (url == null) {
            throw NullPointerException("Given url was null.")
        }
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            return "http://$url"
        }
        return url
    }

    /**
     * Converts a String to a boolean.
     *
     * @param toBeConverted the string that has to be converted
     * @return true if the string equals `true` (ignorecase) or `1`
     */
    @JvmStatic
    fun stringToBoolean(toBeConverted: String?): Boolean {
        return "true".equals(toBeConverted, ignoreCase = true) || "1" == toBeConverted
    }

    /**
     *
     *
     * Converts bytes into a human readable format the following way:
     *
     *
     * <pre>
     * 1024 byte = 1 KibiByte
     * 1024 KibiByte = 1 MebiByte
     * 1024 MebiByte = 1 GibiByte
     * 1024 GibiByte = 1 TebiByte
     * 1024 TebiByte = 1 PebiByte
     * 1024 PebiByte = 1 ExbiByte
    </pre> *
     *
     * @param bytes that will be converted
     * @return a human readable string following the example given in the method description
     */
    fun humanReadableByteCount(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) {
            // Needn't do any conversion at all, since it is still below 1 KiB
            return bytes.toString() + " B"
        }
        val expression = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val bytePrefix = "KMGTPE"[expression - 1] + "i"
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), expression.toDouble()), bytePrefix)
    }

    /**
     * Checks if a [String] conforms to the uri format.
     *
     * @param possibleUrl the [String] to check.
     * @return true if it was valid and false otherwise
     */
    fun isValidURL(possibleUrl: String?) = possibleUrl?.matches("^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!10(?:\\.\\d{1,3}){3})(?!127(?:\\.\\d{1,3}){3})(?!169\\.254(?:\\.\\d{1,3}){2})(?!192\\.168(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)*(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}]{2,})))(?::\\d{2,5})?(?:/[^\\s]*)?$".toRegex())
            ?: false

    /**
     * Escapes certain characters in given html:
     *
     * @param html html to be escaped
     * @return the escaped html
     */
    fun escapeHTML(html: String): String {
        return html.replace("\"", "&quot").replace("&", "&amp").replace("<", "&lt").replace("<", "&gt")
    }
}
