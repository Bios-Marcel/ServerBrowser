package com.msc.serverbrowser.util.basic;

import static java.io.File.separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.msc.serverbrowser.logging.Logging;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;

/**
 * Util methods for dealing with downloading and unzipping files.
 *
 * @author oliver
 * @author Marcel
 * @since 01.07.2017
 */
public final class FileUtility {
	private FileUtility() {
		// Constructor to prevent instantiation
	}

	/**
	 * Downloads a file and saves it to the given location.
	 *
	 * @param url the url to download from
	 * @param outputPath the path where to save the downloaded file
	 * @return the downloaded file
	 * @throws IOException if an errors occurs while writing the file or opening the stream
	 */
	public static File downloadFile(final String url, final String outputPath) throws IOException {
		try (final ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());
				final FileOutputStream fileOutputStream = new FileOutputStream(outputPath);) {
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			return new File(outputPath);
		}
	}

	/**
	 * Copies a file overwriting the target if existent
	 *
	 * @param source source file
	 * @param target target file/location
	 * @throws IOException if there was an error during the copy action
	 */
	public static void copyOverwrite(final String source, final String target) throws IOException {
		try (FileInputStream fileInputStream = new FileInputStream(source);
				final ReadableByteChannel readableByteChannel = Channels.newChannel(fileInputStream);
				final FileOutputStream fileOutputStream = new FileOutputStream(target);) {
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
		}
	}

	/**
	 * Downloads a file and saves it at the given location.
	 *
	 * @param url the url to download from
	 * @param outputPath the path where to save the downloaded file
	 * @param progressProperty a property that will contain the current download process from 0.0 to
	 *            1.0
	 * @param fileLength length of the file
	 * @return the downloaded file
	 * @throws IOException if an errors occurs while writing the file or opening the stream
	 */
	public static File downloadFile(final URL url, final String outputPath, final DoubleProperty progressProperty, final double fileLength) throws IOException {
		try (final InputStream input = url.openStream(); final FileOutputStream fileOutputStream = new FileOutputStream(outputPath);) {
			final double currentProgress = (int) progressProperty.get();
			final byte[] buffer = new byte[10000];
			while (true) {
				final double length = input.read(buffer);

				if (length <= 0) {
					break;
				}

				/*
				 * Setting the progress property inside of a run later in order to avoid a crash,
				 * since this function is usually used inside of a different thread than the ui
				 * thread.
				 */
				Platform.runLater(() -> {
					final double additional = length / fileLength * (1.0 - currentProgress);
					progressProperty.set(progressProperty.get() + additional);
				});

				fileOutputStream.write(buffer, 0, (int) length);
			}

			return new File(outputPath);
		}
	}

	/**
	 * Retrieving the size of a file that lies somewhere on the web. The file size is retrieved via
	 * the http header. It shall be noted, that this method won't work in all cases.
	 *
	 * @param url the files {@link URL}
	 * @return the retrieved filesize
	 * @throws IOException if there was an error during the web request
	 */
	public static int getOnlineFileSize(final URL url) throws IOException {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			connection.getInputStream();
			return connection.getContentLength();
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * Unzips a file, placing its contents in the given output location.
	 *
	 * @param zipFilePath input zip file
	 * @param outputLocation zip file output folder
	 * @throws IOException if there was an error reading the zip file or writing the unzipped data
	 */
	public static void unzip(final String zipFilePath, final String outputLocation) throws IOException {
		// Open the zip file
		try (final ZipFile zipFile = new ZipFile(zipFilePath)) {
			final Enumeration<? extends ZipEntry> enu = zipFile.entries();
			while (enu.hasMoreElements()) {

				final ZipEntry zipEntry = enu.nextElement();
				final String name = zipEntry.getName();
				final File outputFile = new File(outputLocation + separator + name);

				if (name.endsWith("/")) {
					outputFile.mkdirs();
					continue;
				}

				final File parent = outputFile.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}

				// Extract the file
				try (final InputStream inputStream = zipFile.getInputStream(zipEntry);
						final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
					/*
					 * The buffer is the max amount of bytes kept in RAM during any given time while
					 * unzipping. Since most windows disks are aligned to 4096 or 8192, we use a
					 * multiple of those values for best performance.
					 */
					final byte[] bytes = new byte[8192];
					while (inputStream.available() > 0) {
						final int length = inputStream.read(bytes);
						outputStream.write(bytes, 0, length);
					}
				}
			}
		}
	}

	/**
	 * Validates a {@link File} against a SHA-256 checksum.
	 *
	 * @param file the file that has to be validated
	 * @param sha256Checksum the checksum to validate against
	 * @return true if the file was valid, otherwise false
	 */
	public static boolean validateFile(final File file, final String sha256Checksum) {
		try {
			return HashingUtility.generateChecksum(file.getAbsolutePath()).equalsIgnoreCase(sha256Checksum);
		}
		catch (NoSuchAlgorithmException | IOException exception) {
			Logging.warn("File invalid: " + file.getAbsolutePath(), exception);
			return false;
		}
	}

	/**
	 * Deletes a given {@link File}. In case the file is a directory, it will recursively delete all
	 * its containments. If at any step during the deletion of files an exception is throwing, there
	 * won't be any rollback, therefore all deleted files will be gone.
	 *
	 * @param file the file that is to be deleted
	 * @return true if successful, otherwise false
	 */
	public static boolean deleteRecursively(final File file) {
		if (file.isDirectory()) {
			for (final File fileOrFolder : file.listFiles()) {
				if (!deleteRecursively(fileOrFolder)) {
					return false;
				}
			}
		}

		return file.delete();
	}

	/**
	 * Tries reading a file with all given charsets until it works.
	 *
	 * @param path the {@link Path} to read from
	 * @param charsets the {@link Charset}s to try when reading
	 * @return A {@link List} of all lines within the file
	 * @throws IOException if none of the read-attempts was sucessful
	 */
	public static List<String> readAllLinesTryEncodings(final Path path, final Charset... charsets) throws IOException {
		for (final Charset charset : charsets) {
			try {
				return Files.readAllLines(path, charset);
			}
			catch (@SuppressWarnings("unused") final IOException exception) {
				Logging.warn("Error loading " + path + " with encoding " + charset);
			}
		}

		throw new IOException("Couldn't load file " + path + " using any of the given encodings.");
	}
}
