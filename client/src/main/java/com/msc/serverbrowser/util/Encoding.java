package com.msc.serverbrowser.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.mozilla.universalchardet.UniversalDetector;

/**
 * Contains utility methods for encoding and decoding strings.
 *
 * @author Marcel
 */
public final class Encoding
{
	private Encoding()
	{
		// Constructor to prevent instantiation
	}

	private static UniversalDetector detector = new UniversalDetector(null);

	/**
	 * Tries to decode a given byte array using the given charset. As a fallback
	 * {@link StandardCharsets#UTF_8} will be used.
	 *
	 * @param toDecode
	 *            the byte array to decode
	 * @param charset
	 *            the charset to be used for decoding
	 * @return the decoded string
	 */
	public static String decodeUsingCharsetIfPossible(final byte[] toDecode, final String charset)
	{
		try
		{
			return new String(toDecode, charset);
		}
		catch (@SuppressWarnings("unused") final UnsupportedEncodingException exception)
		{// In case that the given encoding is invalid, we use UTF-8 as fallback
			return new String(toDecode, StandardCharsets.UTF_8);
		}
	}

	/**
	 * Returns an {@link Optional} of the charset that has been used or an {@link Optional#empty()}
	 * if no charset could be found.
	 *
	 * @param data
	 *            the byte array that the charset should be found of
	 * @return {@link Optional} of the charset or an {@link Optional#empty()}
	 */
	public static Optional<String> getEncoding(final byte[] data)
	{
		detector.handleData(data, 0, data.length - 1);
		detector.dataEnd();

		final String charset = detector.getDetectedCharset();
		detector.reset();
		return Optional.ofNullable(charset);
	}
}
