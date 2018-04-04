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
        Files.newInputStream(Paths.get(file)).use { inputStream ->
            val sha256 = MessageDigest.getInstance("SHA-256")
            val data = ByteArray(1024)

            var read = 0
            while (read != -1) {
                sha256.update(data, 0, read)
                read = inputStream.read(data)
            }

            val hashBytes = sha256.digest()

            val buffer = StringBuilder()
            for (hashByte in hashBytes) {
                buffer.append(Integer.toString((hashByte and 0xff.toByte()) + 0x100, 16).substring(1))
            }
            return buffer.toString()
        }
    }
}// Constructor to prevent instantiation
