package com.msc.serverbrowser.util.basic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link ArrayUtility} class.
 *
 * @author marcel
 * @since Jan 10, 2018
 */
@SuppressWarnings("javadoc")
public class ArrayUtilityTest {
	@Test
	@DisplayName("Test merging three arrays of different length")
	public void testMerge() {
		final byte[] arr = { 1, 2, 3 };
		final byte[] arr2 = { 4, 5, 6, 7 };
		final byte[] arr3 = { 8, 9 };

		final byte[] arrMerged = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		
		assertTrue(Arrays.equals(arrMerged, ArrayUtility.merge(arr, arr2, arr3)));
	}
}
