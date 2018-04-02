package serverbrowser.util.basic

import org.mozilla.universalchardet.UniversalDetector
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Contains utility methods for encoding and decoding strings.
 *
 * @author Marcel
 */
object Encoding {

    /**
     * Tries to decode a given byte array using the given charset. As a fallback
     * [StandardCharsets.UTF_8] will be used.
     *
     * @param toDecode the byte array to decode
     * @param charset the charset to be used for decoding
     * @return the decoded string
     */
    fun decodeUsingCharsetIfPossible(toDecode: ByteArray, charset: String): String {
        return try {
            String(toDecode, Charset.forName(charset))
        } catch (exception: UnsupportedEncodingException) {
            // In case that the given encoding is invalid, we use UTF-8 as fallback
            String(toDecode, StandardCharsets.UTF_8)
        }

    }

    /**
     * Returns an [Optional] of the charset that has been used or an [Optional.empty]
     * if no charset could be found.
     *
     * @param data the byte array that the charset should be found of
     * @return [Optional] of the charset or an [Optional.empty]
     */
    fun getEncoding(data: ByteArray): Optional<String> {
        val charsetDetector = UniversalDetector()
        charsetDetector.handleData(data)
        charsetDetector.dataEnd()
        return Optional.ofNullable(charsetDetector.detectedCharset)
    }
}// Constructor to prevent instantiation
