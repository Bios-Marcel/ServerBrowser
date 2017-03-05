package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing
{
	/**
	 * Gets a files SHA-256 Checksum.
	 * 
	 * @param Filepath
	 *            and name of a file that is to be verified
	 * 
	 * @return true The SHA-256 checksum or an empty string.
	 */
	public static String verifyChecksum(final String file)
	{
		try (final FileInputStream fis = new FileInputStream(file);)
		{
			final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			final byte[] data = new byte[1024];
			int read = 0;
			while ((read = fis.read(data)) != -1)
			{
				sha256.update(data, 0, read);
			}
			final byte[] hashBytes = sha256.digest();

			final StringBuffer sb = new StringBuffer();
			for (int i = 0; i < hashBytes.length; i++)
			{
				sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
			}

			final String checksum = sb.toString();

			return checksum;
		}
		catch (IOException | NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return "";
		}

	}
}
