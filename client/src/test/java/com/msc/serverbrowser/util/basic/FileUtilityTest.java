package com.msc.serverbrowser.util.basic;

import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Paths.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import serverbrowser.util.basic.FileUtility;

/**
 * @author oliver
 * @since 02.07.2017
 */
class FileUtilityTest {
	private static final String        PATH_TO_TEST_RESSOURCES = "/com/msc/serverbrowser/util/basic/";
	private static       MessageDigest shaDigester;

	@BeforeAll
	public static void createShaEncoder() throws NoSuchAlgorithmException {
		shaDigester = MessageDigest.getInstance("SHA-512");
	}

	@Test
	@DisplayName("Given a zipped file, when __unzip__ is called, then it should be correctly decompressed.")
	public void testSimpleFileunzip() throws IOException, URISyntaxException {
		testUnzipWithGivenFileWithSha("test_file.txt");
	}

	@Test
	@DisplayName("Given a zipped directory, when __unzip__ is called, then it should be correctly decompressed.")
	public void testDirectoryUnzip() throws URISyntaxException, IOException {
		testUnzipWithGivenFileWithSha("test_dir");
	}

	/**
	 * Tests if a given ZIP archive has the correct content. The test data needs to have a
	 * corresponding <code>*.sha512sum</code> in the same folder with gets used to validate the file
	 * content survived the zipping without alteration.
	 *
	 * @param testDataName Full path to the ZIP under test.
	 *
	 * @throws URISyntaxException if the path is wrong
	 * @throws IOException        if the ZIP archive or its sha512sum file could not be found
	 */
	private void testUnzipWithGivenFileWithSha(final String testDataName) throws URISyntaxException, IOException {
		final URL testDataZipUrl = getClass().getResource(PATH_TO_TEST_RESSOURCES + testDataName + ".zip");
		assertNotNull(testDataZipUrl, "path to test zip data not correct");
		final Path testDataZipPath = get(testDataZipUrl.toURI());
		assertTrue(exists(testDataZipPath), "The zipped file does not exist where expected: " + testDataZipPath + ".");

		final URL testDataShaUrl = getClass().getResource(PATH_TO_TEST_RESSOURCES + testDataName + ".sha512sum");
		assertNotNull(testDataShaUrl, "path to test checksums not correct");
		final Path testDataShaPath = get(testDataShaUrl.toURI());
		assertTrue(exists(testDataShaPath), "The shasum file does not exist where expected: " + testDataShaPath + ".");

		final Path tempDirectory = createTempDirectory(testDataName);

		FileUtility.unzip(testDataZipPath.toAbsolutePath().toString(), tempDirectory.toString());

		final List<String> checksums = readAllLines(testDataShaPath);
		for (final String line : checksums) {
			assertFalse(line.isEmpty(), "where is the content?");

			final String[] split = line.split("\\s");
			final String sha512 = split[0];
			final String name = split[1].substring(1);
			final Path pathToUnzippedFile = get(tempDirectory.toString(), name);

			assertTrue(exists(pathToUnzippedFile), "The unzipped file does not exist where expected: " + pathToUnzippedFile + ".");

			final String shaOfUnzipped = toHex(shaDigester.digest(readAllBytes(pathToUnzippedFile)));
			assertEquals(sha512, shaOfUnzipped);
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
	private static String toHex(final byte[] bytes) {
		final BigInteger bigInteger = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "x", bigInteger);
	}
}
