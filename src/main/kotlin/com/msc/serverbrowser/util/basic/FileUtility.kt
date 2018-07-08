package com.msc.serverbrowser.util.basic

import com.msc.serverbrowser.warn
import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import java.io.File
import java.io.File.separator
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.channels.Channels
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.NoSuchAlgorithmException
import java.util.zip.ZipFile

/**
 * Util methods for dealing with downloading and unzipping files.
 *
 * @author oliver
 * @author Marcel
 * @since 01.07.2017
 */
object FileUtility {

    /**
     * Downloads a file and saves it to the given location.
     *
     * @param url the url to isDownload from
     * @param outputPath the path where to save the downloaded file
     * @return the downloaded file
     * @throws IOException if an errors occurs while writing the file or opening the stream
     */
    @Throws(IOException::class)
    fun downloadFile(url: String, outputPath: String): File {
        Channels.newChannel(URL(url).openStream()).use { readableByteChannel ->
            FileOutputStream(outputPath).use { fileOutputStream ->
                fileOutputStream.channel.transferFrom(readableByteChannel, 0, java.lang.Long.MAX_VALUE)
                return File(outputPath)
            }
        }
    }

    /**
     * Copies a file overwriting the target if existent
     *
     * @param source source file
     * @param target target file/location
     * @throws IOException if there was an severe during the copy action
     */
    @Throws(IOException::class)
    fun copyOverwrite(source: String, target: String) {
        Files.newInputStream(Paths.get(source)).use { fileInputStream -> Channels.newChannel(fileInputStream).use({ readableByteChannel -> FileOutputStream(target).use { fileOutputStream -> fileOutputStream.channel.transferFrom(readableByteChannel, 0, java.lang.Long.MAX_VALUE) } }) }
    }

    /**
     * Downloads a file and saves it at the given location.
     *
     * @param url the url to isDownload from
     * @param outputPath the path where to save the downloaded file
     * @param progressProperty a property that will contain the current isDownload process from 0.0 to
     * 1.0
     * @param fileLength length of the file
     * @return the downloaded file
     * @throws IOException if an errors occurs while writing the file or opening the stream
     */
    @Throws(IOException::class)
    fun downloadFile(url: URL, outputPath: String, progressProperty: DoubleProperty, fileLength: Double): File {
        url.openStream().use { input ->
            Files.newOutputStream(Paths.get(outputPath)).use { fileOutputStream ->
                val currentProgress = progressProperty.get().toInt().toDouble()
                val buffer = ByteArray(10000)
                while (true) {
                    val length = input.read(buffer).toDouble()

                    if (length <= 0) {
                        break
                    }

                    /*
				 * Setting the progress property inside of a run later in order to avoid a crash,
				 * since this function is usually used inside of a different thread than the ui
				 * thread.
				 */
                    Platform.runLater {
                        val additional = length / fileLength * (1.0 - currentProgress)
                        progressProperty.set(progressProperty.get() + additional)
                    }

                    fileOutputStream.write(buffer, 0, length.toInt())
                }

                return File(outputPath)
            }
        }
    }

    /**
     * Retrieving the size of a file that lies somewhere on the web. The file size is retrieved via
     * the http header. It shall be noted, that this method won't work in all cases.
     *
     * @param url the files [URL]
     * @return the retrieved filesize
     * @throws IOException if there was an severe during the web request
     */
    @Throws(IOException::class)
    fun getOnlineFileSize(url: URL): Int {
        var connection: HttpURLConnection? = null
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.inputStream
            return connection.contentLength
        } finally {
            if (connection != null) {
                connection.disconnect()
            }
        }
    }

    /**
     * Unzips a file, placing its contents in the given output location.
     *
     * @param zipFilePath input zip file
     * @param outputLocation zip file output folder
     * @throws IOException if there was an severe reading the zip file or writing the unzipped data
     */
    @JvmStatic
    @Throws(IOException::class)
    fun unzip(zipFilePath: String, outputLocation: String) {
        // Open the zip file
        ZipFile(zipFilePath).use { zipFile ->
            val enu = zipFile.entries()
            while (enu.hasMoreElements()) {

                val zipEntry = enu.nextElement()
                val name = zipEntry.name
                val outputFile = File(outputLocation + separator + name)

                if (name[name.length - 1] == '/') {
                    outputFile.mkdirs()
                    continue
                }

                val parent = outputFile.parentFile
                parent?.mkdirs()

                // Extract the file
                zipFile.getInputStream(zipEntry).use({ inputStream ->
                    Files.newOutputStream(Paths.get(outputFile.toURI())).use { outputStream ->
                        /*
					 * The buffer is the max amount of bytes kept in RAM during any given time while
					 * unzipping. Since most windows disks are aligned to 4096 or 8192, we use a
					 * multiple of those values for best performance.
					 */
                        val bytes = ByteArray(8192)
                        while (inputStream.available() > 0) {
                            val length = inputStream.read(bytes)
                            outputStream.write(bytes, 0, length)
                        }
                    }
                })
            }
        }
    }

    /**
     * Validates a [File] against a SHA-256 checksum.
     *
     * @param file the file that has to be validated
     * @param sha256Checksum the checksum to validate against
     * @return true if the file was valid, otherwise false
     */
    fun validateFile(file: File, sha256Checksum: String): Boolean {
        try {
            return HashingUtility.generateChecksum(file.absolutePath).equals(sha256Checksum, ignoreCase = true)
        } catch (exception: NoSuchAlgorithmException) {
            warn("File invalid: " + file.absolutePath, exception)
            return false
        } catch (exception: IOException) {
            warn("File invalid: " + file.absolutePath, exception)
            return false
        }

    }

    /**
     * Deletes a given [File]. In case the file is a directory, it will recursively delete all
     * its containments. If at any step during the deletion of files an exception is throwing, there
     * won't be any rollback, therefore all deleted files will be gone.
     *
     * @param file the file that is to be deleted
     * @return true if successful, otherwise false
     */
    fun deleteRecursively(file: File): Boolean {
        if (file.isDirectory) {
            val files = file.listFiles() ?: return false

            for (fileOrFolder in files) {
                if (!deleteRecursively(fileOrFolder)) {
                    return false
                }
            }
        }

        return file.delete()
    }

    /**
     * Tries reading a file with all given charsets until it works.
     *
     * @param path the [Path] to read from
     * @param charsets the [Charset]s to try when reading
     * @return A [List] of all lines within the file
     * @throws IOException if none of the read-attempts was successful
     */
    @Throws(IOException::class)
    fun readAllLinesTryEncodings(path: Path, vararg charsets: Charset): List<String> {
        if(!Files.exists(path)) {
            throw FileNotFoundException("The file at $path doesn't exist.")
        }

        for (charset in charsets) {
            try {
                return Files.readAllLines(path, charset)
            } catch (exception: IOException) {
                warn("Error loading $path with encoding $charset")
            }

        }

        throw IOException("Couldn't load file $path using any of the given encodings.")
    }
}
