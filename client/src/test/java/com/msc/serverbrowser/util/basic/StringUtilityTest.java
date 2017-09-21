package com.msc.serverbrowser.util.basic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Marcel
 * @since 21.09.2017
 */
@SuppressWarnings("javadoc")
public class StringUtilityTest
{
	@Test
	public void testStringToBoolean()
	{
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
}
