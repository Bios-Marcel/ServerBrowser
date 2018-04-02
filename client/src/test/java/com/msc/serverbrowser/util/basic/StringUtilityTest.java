package com.msc.serverbrowser.util.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import serverbrowser.util.basic.StringUtility;

/**
 * @author Marcel
 * @since 21.09.2017
 */
@SuppressWarnings("javadoc")
public class StringUtilityTest {
	@Test
	public void testStringToBoolean() {
		assertTrue(StringUtility.stringToBoolean("true"));
		assertTrue(StringUtility.stringToBoolean("1"));
		assertTrue(StringUtility.stringToBoolean("True"));
		assertTrue(StringUtility.stringToBoolean("TRUE"));

		assertFalse(StringUtility.stringToBoolean(null));
		assertFalse(StringUtility.stringToBoolean("0"));
		assertFalse(StringUtility.stringToBoolean("1 "));
		assertFalse(StringUtility.stringToBoolean(" 1"));
		assertFalse(StringUtility.stringToBoolean("Kauderwelsch"));
		assertFalse(StringUtility.stringToBoolean(""));
		assertFalse(StringUtility.stringToBoolean("false"));
	}

	@Test
	public void testFixUrl() {
		assertEquals("https://google.com", StringUtility.fixUrlIfNecessary("https://google.com"));
		assertEquals("http://google.com", StringUtility.fixUrlIfNecessary("http://google.com"));
		assertEquals("http://google.com", StringUtility.fixUrlIfNecessary("google.com"));
		assertEquals("http://", StringUtility.fixUrlIfNecessary(""));
		assertThrows(NullPointerException.class, () -> StringUtility.fixUrlIfNecessary(null));
	}
}
