package com.msc.serverbrowser.util.basic

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.IOException
import java.math.BigInteger
import java.net.URISyntaxException
import java.nio.file.Files.*
import java.nio.file.Paths.get
import java.security.MessageDigest

/**
 * @author oliver
 * @since 02.07.2017
 */
internal class FileUtilityTest {
    private val PATH_TO_TEST_RESSOURCES = "/com/msc/serverbrowser/util/basic/"
    private val shaDigester: MessageDigest = MessageDigest.getInstance("SHA-512")

    @Test
    @DisplayName("Given a zipped file, when __unzip__ is called, then it should be correctly decompressed.")
    @Throws(IOException::class, URISyntaxException::class)
    fun testSimpleFileunzip() {
        testUnzipWithGivenFileWithSha("test_file.txt")
    }

    @Test
    @DisplayName("Given a zipped directory, when __unzip__ is called, then it should be correctly decompressed.")
    @Throws(URISyntaxException::class, IOException::class)
    fun testDirectoryUnzip() {
        testUnzipWithGivenFileWithSha("test_dir")
    }

    /**
     * Tests if a given ZIP archive has the correct content. The test data needs to have a
     * corresponding `*.sha512sum` in the same folder with gets used to validate the file
     * content survived the zipping without alteration.
     *
     * @param testDataName Full path to the ZIP under test.
     *
     * @throws URISyntaxException if the path is wrong
     * @throws IOException        if the ZIP archive or its sha512sum file could not be found
     */
    @Throws(URISyntaxException::class, IOException::class)
    private fun testUnzipWithGivenFileWithSha(testDataName: String) {
        val testDataZipUrl = javaClass.getResource("$PATH_TO_TEST_RESSOURCES$testDataName.zip")
        assertNotNull(testDataZipUrl, "path to test zip data not correct")
        val testDataZipPath = get(testDataZipUrl.toURI())
        assertTrue(exists(testDataZipPath), "The zipped file does not exist where expected: $testDataZipPath.")

        val testDataShaUrl = javaClass.getResource("$PATH_TO_TEST_RESSOURCES$testDataName.sha512sum")
        assertNotNull(testDataShaUrl, "path to test checksums not correct")
        val testDataShaPath = get(testDataShaUrl.toURI())
        assertTrue(exists(testDataShaPath), "The shasum file does not exist where expected: $testDataShaPath.")

        val tempDirectory = createTempDirectory(testDataName)

        FileUtility.unzip(testDataZipPath.toAbsolutePath().toString(), tempDirectory.toString())

        val checksums = readAllLines(testDataShaPath)
        for (line in checksums) {
            assertFalse(line.isEmpty(), "where is the content?")

            val split = line.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val sha512 = split[0]
            val name = split[1].substring(1)
            val pathToUnzippedFile = get(tempDirectory.toString(), name)

            assertTrue(exists(pathToUnzippedFile), "The unzipped file does not exist where expected: $pathToUnzippedFile.")

            val shaOfUnzipped = toHex(shaDigester.digest(readAllBytes(pathToUnzippedFile)))
            assertEquals(sha512, shaOfUnzipped)
        }
    }

    /**
     * Stolen from
     * https://stackoverflow.com/questions/332079/in-java-how-do-i-convert-a-byte-array-to-a-string-of-hex-digits-while-keeping-l#332433
     *
     * @param bytes some byte array
     *
     * @return lower case hex dump with leading zeros intact
     */
    private fun toHex(bytes: ByteArray): String {
        val bigInteger = BigInteger(1, bytes)
        return String.format("%0" + (bytes.size shl 1) + "x", bigInteger)
    }
}
