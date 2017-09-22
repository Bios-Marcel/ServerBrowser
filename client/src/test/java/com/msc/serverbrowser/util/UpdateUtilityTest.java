package com.msc.serverbrowser.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.msc.serverbrowser.util.UpdateUtility.CompareResult;

/**
 * @author Marvin
 * @author Marcel
 * @since 22.09.2017
 */
@SuppressWarnings("javadoc")
public class UpdateUtilityTest
{
	@Test
	public void testCompareVersions()
	{
		Assertions.assertThrows(NullPointerException.class, () -> UpdateUtility.compareVersions(null, null));
		Assertions.assertThrows(NullPointerException.class, () -> UpdateUtility.compareVersions(null, "1.6.8"));
		Assertions.assertThrows(NullPointerException.class, () -> UpdateUtility.compareVersions("1.6.8", null));

		Assertions.assertThrows(IllegalArgumentException.class, () -> UpdateUtility.compareVersions("", ""));
		Assertions.assertThrows(IllegalArgumentException.class, () -> UpdateUtility.compareVersions("", "1"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> UpdateUtility.compareVersions("f", ""));
		Assertions.assertThrows(IllegalArgumentException.class, () -> UpdateUtility.compareVersions("1.2.8.7", "1"));

		Assertions.assertThrows(NumberFormatException.class, () -> UpdateUtility.compareVersions("A", "1"));
		Assertions.assertThrows(NumberFormatException.class, () -> UpdateUtility.compareVersions("1", "A"));
		Assertions.assertThrows(NumberFormatException.class, () -> UpdateUtility.compareVersions("A", "A"));

		Assertions.assertEquals(CompareResult.EQUAL, UpdateUtility.compareVersions("1", "1"), "'1' and '1' should have been equal.");
		Assertions.assertEquals(CompareResult.EQUAL, UpdateUtility.compareVersions("3.4", "3.4"), "'3.4' and '3.4' should have been equal.");
		Assertions.assertEquals(CompareResult.EQUAL, UpdateUtility.compareVersions("1.6.8", "1.6.8"), "'1.6.8' and '1.6.8' should have been equal.");
		Assertions.assertEquals(CompareResult.EQUAL, UpdateUtility.compareVersions("1.6.1", "1.6.1"), "'1.6.1' and '1.6.1' should have been equal.");
		Assertions.assertEquals(CompareResult.EQUAL, UpdateUtility.compareVersions("2.0", "2"), "'2.0' and '2' should have been equal.");

		Assertions.assertEquals(CompareResult.GREATER, UpdateUtility.compareVersions("2", "1"), "'2' and '1' should have been greater.");
		Assertions.assertEquals(CompareResult.GREATER, UpdateUtility.compareVersions("1.2", "1"), "'1.2' and '1' should have been greater.");
		Assertions.assertEquals(CompareResult.GREATER, UpdateUtility.compareVersions("1.2", "1.1"), "'1.2' and '1.1' should have been greater.");
		Assertions.assertEquals(CompareResult.GREATER, UpdateUtility.compareVersions("1.2.0", "1.1.0"), "'1.2.0' and '1.1.0' should have been greater.");
		Assertions.assertEquals(CompareResult.GREATER, UpdateUtility.compareVersions("1.2.7", "1"), "'1.2.7' and '1' should have been greater.");
		Assertions.assertEquals(CompareResult.GREATER, UpdateUtility.compareVersions("1.2.7", "1.2"), "'1.2.7' and '1.2' should have been greater.");
		Assertions.assertEquals(CompareResult.GREATER, UpdateUtility.compareVersions("1.2.9", "1.2.8"), "'1.2.9' and '1.2.8' should have been greater.");
		Assertions.assertEquals(CompareResult.GREATER, UpdateUtility.compareVersions("1.8", "1.3"), "'1.8' and '1.3' should have been greater.");
		Assertions.assertEquals(CompareResult.GREATER, UpdateUtility.compareVersions("2.1", "2"), "'2.1' and '2' should have been greater.");

		Assertions.assertEquals(CompareResult.LESS, UpdateUtility.compareVersions("1.1", "2"), "'1.1' and '2' should have been less.");
		Assertions.assertEquals(CompareResult.LESS, UpdateUtility.compareVersions("1.0", "2"), "'1.0' and '2' should have been less.");
		Assertions.assertEquals(CompareResult.LESS, UpdateUtility.compareVersions("1", "2"), "'1' and '2' should have been less.");
		Assertions.assertEquals(CompareResult.LESS, UpdateUtility.compareVersions("1", "1.2"), "'1' and '1.2' should have been less.");
		Assertions.assertEquals(CompareResult.LESS, UpdateUtility.compareVersions("1.1", "1.2"), "'1.1' and '1.2' should have been less.");
		Assertions.assertEquals(CompareResult.LESS, UpdateUtility.compareVersions("1.1.0", "1.2.0"), "'1.1.0' and '1.2.0' should have been less.");
		Assertions.assertEquals(CompareResult.LESS, UpdateUtility.compareVersions("1", "1.2.8"), "'1' and '1.2.8' should have been less.");
		Assertions.assertEquals(CompareResult.LESS, UpdateUtility.compareVersions("1.2", "1.2.8"), "'1.2' and '1.2.8' should have been less.");
		Assertions.assertEquals(CompareResult.LESS, UpdateUtility.compareVersions("1.2.7", "1.2.8"), "'1.2.7' and '1.2.8' should have been less.");
		Assertions.assertEquals(CompareResult.LESS, UpdateUtility.compareVersions("1.3", "1.8"), "'1.3' and '1.8' should have been less.");
	}
}