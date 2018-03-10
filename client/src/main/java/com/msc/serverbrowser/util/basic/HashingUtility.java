package com.msc.serverbrowser.util.basic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Contains a single method, that is used to take the SHA-256 of a file.
 *
 * @author Marcel
 */
public final class HashingUtility {
	private HashingUtility() {
		// Constructor to prevent instantiation
	}

	/**
	 * Gets a files SHA-256 Checksum.
	 *
	 * @param file and name of a file that is to be verified
	 * @return true The SHA-256 checksum or an empty string.
	 * @throws IOException if there was an error reading the file that is to be hashed
	 * @throws FileNotFoundException if the file that is to be hashed couldn't be found
	 * @throws NoSuchAlgorithmException if the used Hashing Algorithm couldn't be found
	 */
	public static String generateChecksum(final String file) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		try (InputStream inputStream = Files.newInputStream(Paths.get(file))) {
			final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			final byte[] data = new byte[1024];

			for (int read = 0; read != -1; read = inputStream.read(data)) {
				sha256.update(data, 0, read);
			}

			final byte[] hashBytes = sha256.digest();

			final StringBuilder buffer = new StringBuilder();
			for (final byte hashByte : hashBytes) {
				buffer.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
			}
			return buffer.toString();
		}
	}
}
