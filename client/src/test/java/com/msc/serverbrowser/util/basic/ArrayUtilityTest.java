package com.msc.serverbrowser.util.basic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

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
		
		assertArrayEquals(arrMerged, ArrayUtility.merge(arr, arr2, arr3));
	}
	
	@Test
	@DisplayName("Test getLongestArray()")
	public void testGetLongestArray() {
		// longer by 1
		final Byte[] arr = { 1, 2, 3 };
		final Byte[] arr2 = { 4, 5, 6, 7 };
		
		assertEquals(Optional.of(arr2), ArrayUtility.getLongestArray(arr, arr2));
		assertEquals(Optional.of(arr2), ArrayUtility.getLongestArray(arr2, arr));
		
		// Same length
		final Byte[] arr3 = { 1, 2, 3 };
		final Byte[] arr4 = { 4, 5, 6 };
		
		assertEquals(Optional.empty(), ArrayUtility.getLongestArray(arr3, arr4));
		assertEquals(Optional.empty(), ArrayUtility.getLongestArray(arr4, arr3));
		
		// both empty
		final Byte[] arr5 = {};
		final Byte[] arr6 = {};
		
		assertEquals(Optional.empty(), ArrayUtility.getLongestArray(arr5, arr6));
		assertEquals(Optional.empty(), ArrayUtility.getLongestArray(arr6, arr5));
		
		// one empty
		final Byte[] arr7 = {};
		final Byte[] arr8 = { 1 };
		
		assertEquals(Optional.of(arr8), ArrayUtility.getLongestArray(arr7, arr8));
		assertEquals(Optional.of(arr8), ArrayUtility.getLongestArray(arr8, arr7));
		
		// one null and one nonempty
		final Byte[] arr9 = null;
		final Byte[] arr10 = { 1 };
		
		assertEquals(Optional.of(arr10), ArrayUtility.getLongestArray(arr9, arr10));
		assertEquals(Optional.of(arr10), ArrayUtility.getLongestArray(arr10, arr9));
		
		// one null and one empty
		final Byte[] arr11 = null;
		final Byte[] arr12 = {};
		
		assertEquals(Optional.of(arr12), ArrayUtility.getLongestArray(arr11, arr12));
		assertEquals(Optional.of(arr12), ArrayUtility.getLongestArray(arr12, arr11));
		
		// one null and one empty
		final Byte[] arr13 = null;
		final Byte[] arr14 = null;
		
		assertEquals(Optional.empty(), ArrayUtility.getLongestArray(arr13, arr14));
		assertEquals(Optional.empty(), ArrayUtility.getLongestArray(arr14, arr13));
	}
}