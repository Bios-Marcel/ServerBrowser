package com.msc.sampbrowser.util;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

import org.mozilla.universalchardet.UniversalDetector;

public class Encoding
{
	private static UniversalDetector detector = new UniversalDetector(null);

	public static String encodeUsingCharsetIfPossible(final byte[] toEncode, final String charset)
	{
		if (Objects.nonNull(charset))
		{
			try
			{
				return new String(toEncode, charset);
			}
			catch (final UnsupportedEncodingException e)
			{
				// DO NTHN
			}
		}
		return new String(toEncode);
	}

	public static String getEncoding(final byte[] data)
	{
		detector.handleData(data, 0, data.length - 1);
		detector.dataEnd();

		final String charset = detector.getDetectedCharset();
		detector.reset();
		if (charset != null)
		{
			return charset;
		}

		return null;
	}
}
