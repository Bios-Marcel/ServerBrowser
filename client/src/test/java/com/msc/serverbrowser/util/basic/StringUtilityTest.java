package com.msc.serverbrowser.util.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Marcel
 * @since 21.09.2017
 */
@SuppressWarnings("javadoc")
public class StringUtilityTest {
	@Test
	public void testStringToBoolean() {
		Assertions.assertEquals(true, StringUtility.stringToBoolean("true"));
		Assertions.assertEquals(true, StringUtility.stringToBoolean("1"));
		Assertions.assertEquals(true, StringUtility.stringToBoolean("True"));
		Assertions.assertEquals(true, StringUtility.stringToBoolean("TRUE"));

		Assertions.assertEquals(false, StringUtility.stringToBoolean(null));
		Assertions.assertEquals(false, StringUtility.stringToBoolean("0"));
		Assertions.assertEquals(false, StringUtility.stringToBoolean("1 "));
		Assertions.assertEquals(false, StringUtility.stringToBoolean(" 1"));
		Assertions.assertEquals(false, StringUtility.stringToBoolean("Kauderwelsch"));
		Assertions.assertEquals(false, StringUtility.stringToBoolean(""));
		Assertions.assertEquals(false, StringUtility.stringToBoolean("false"));
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
