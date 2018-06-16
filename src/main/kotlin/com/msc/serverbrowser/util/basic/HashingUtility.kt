package com.msc.serverbrowser.util.basic

import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

/**
 * Contains a single method, that is used to take the SHA-256 of a file.
 *
 * @author Marcel
 */
object HashingUtility {

    private const val HEX_CHARS = "0123456789ABCDEF"

    /**
     * Gets a files SHA-256 Checksum.
     *
     * @param file and name of a file that is to be verified
     * @return true The SHA-256 checksum or an empty string.
     * @throws IOException if there was an error reading the file that is to be hashed
     * @throws FileNotFoundException if the file that is to be hashed couldn't be found
     * @throws NoSuchAlgorithmException if the used Hashing Algorithm couldn't be found
     */
    @Throws(IOException::class, NoSuchAlgorithmException::class)
    fun generateChecksum(file: String): String {
        val bytes = MessageDigest
                .getInstance("SHA-256")
                .digest(Files.readAllBytes(Paths.get(file)))
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }
}
