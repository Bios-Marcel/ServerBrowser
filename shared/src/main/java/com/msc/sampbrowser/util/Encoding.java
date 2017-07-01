package com.msc.sampbrowser.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.mozilla.universalchardet.UniversalDetector;

public class Encoding
{
	private static UniversalDetector detector = new UniversalDetector(null);

	public static String encodeUsingCharsetIfPossible(final byte[] toEncode, final String charset)
	{
		try
		{
			return new String(toEncode, charset);
		}
		catch (@SuppressWarnings("unused") final UnsupportedEncodingException exception)
		{
			return new String(toEncode, StandardCharsets.UTF_8);
		}
	}

	public static Optional<String> getEncoding(final byte[] data)
	{
		detector.handleData(data, 0, data.length - 1);
		detector.dataEnd();

		final String charset = detector.getDetectedCharset();
		detector.reset();
		return Optional.ofNullable(charset);
	}
}
