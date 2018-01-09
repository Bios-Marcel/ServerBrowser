package com.msc.serverbrowser;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.msc.serverbrowser.util.basic.FileUtility;
import com.msc.serverbrowser.util.basic.HashingUtility;

public class Hasher {
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
