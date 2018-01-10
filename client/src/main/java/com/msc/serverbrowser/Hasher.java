package com.msc.serverbrowser;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.msc.serverbrowser.util.basic.FileUtility;
import com.msc.serverbrowser.util.basic.HashingUtility;

/**
 * Used to provide hashes for all samp versions.
 *
 * @author marcel
 * @since Jan 10, 2018
 */
public class Hasher {

	/**
	 * @param args
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static void main(final String[] args) throws IOException, NoSuchAlgorithmException {
		for (final File f : new File("/home/marcel/Downloads").listFiles()) {
			if (f.getName().contains("zip")) {
				FileUtility.unzip(f.getAbsoluteFile().toString(), "trash.zip");
				System.out.println(f.getName() + ": " + HashingUtility.verifyChecksum("samp.dll"));
				f.delete();
			}
		}
	}
}
