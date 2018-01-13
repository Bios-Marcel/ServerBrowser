package com.msc.serverbrowser.util.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * @author Marcel
 * @since 21.09.2017
 */
@SuppressWarnings("javadoc")
public class StringUtilityTest {
	@Test
	public void testStringToBoolean() {
		assertEquals(true, StringUtility.stringToBoolean("true"));
		assertEquals(true, StringUtility.stringToBoolean("1"));
		assertEquals(true, StringUtility.stringToBoolean("True"));
		assertEquals(true, StringUtility.stringToBoolean("TRUE"));

		assertEquals(false, StringUtility.stringToBoolean(null));
		assertEquals(false, StringUtility.stringToBoolean("0"));
		assertEquals(false, StringUtility.stringToBoolean("1 "));
		assertEquals(false, StringUtility.stringToBoolean(" 1"));
		assertEquals(false, StringUtility.stringToBoolean("Kauderwelsch"));
		assertEquals(false, StringUtility.stringToBoolean(""));
		assertEquals(false, StringUtility.stringToBoolean("false"));
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
