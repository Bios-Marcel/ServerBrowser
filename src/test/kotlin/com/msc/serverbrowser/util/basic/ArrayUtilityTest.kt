package com.msc.serverbrowser.util.basic

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals

import java.util.Optional

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import com.msc.serverbrowser.util.basic.ArrayUtility

/**
 * Tests the [ArrayUtility] class.
 *
 * @author marcel
 * @since Jan 10, 2018
 */
class ArrayUtilityTest {
    @Test
    @DisplayName("Test merging three arrays of different length")
    fun testMerge() {
        val arr = byteArrayOf(1, 2, 3)
        val arr2 = byteArrayOf(4, 5, 6, 7)
        val arr3 = byteArrayOf(8, 9)

        val arrMerged = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9)

        assertArrayEquals(arrMerged, ArrayUtility.merge(arr, arr2, arr3))
    }

    @Test
    @DisplayName("Test getLongestArray()")
    fun testGetLongestArray() {
        // longer by 1
        val arr = arrayOf<Byte>(1, 2, 3)
        val arr2 = arrayOf<Byte>(4, 5, 6, 7)

        assertEquals(Optional.of(arr2), ArrayUtility.getLongestArray(arr, arr2))
        assertEquals(Optional.of(arr2), ArrayUtility.getLongestArray(arr2, arr))

        // Same length
        val arr3 = arrayOf<Byte>(1, 2, 3)
        val arr4 = arrayOf<Byte>(4, 5, 6)

        assertEquals(Optional.empty<Array<Byte>>(), ArrayUtility.getLongestArray(arr3, arr4))
        assertEquals(Optional.empty<Array<Byte>>(), ArrayUtility.getLongestArray(arr4, arr3))

        // both empty
        val arr5 = arrayOf<Byte>()
        val arr6 = arrayOf<Byte>()

        assertEquals(Optional.empty<Array<Byte>>(), ArrayUtility.getLongestArray(arr5, arr6))
        assertEquals(Optional.empty<Array<Byte>>(), ArrayUtility.getLongestArray(arr6, arr5))

        // one empty
        val arr7 = arrayOf<Byte>()
        val arr8 = arrayOf<Byte>(1)

        assertEquals(Optional.of(arr8), ArrayUtility.getLongestArray(arr7, arr8))
        assertEquals(Optional.of(arr8), ArrayUtility.getLongestArray(arr8, arr7))

        // one null and one nonempty
        val arr9: Array<Byte>? = null
        val arr10 = arrayOf<Byte>(1)

        assertEquals(Optional.of(arr10), ArrayUtility.getLongestArray(arr9, arr10))
        assertEquals(Optional.of(arr10), ArrayUtility.getLongestArray(arr10, arr9))

        // one null and one empty
        val arr11: Array<Byte>? = null
        val arr12 = arrayOf<Byte>()

        assertEquals(Optional.of(arr12), ArrayUtility.getLongestArray(arr11, arr12))
        assertEquals(Optional.of(arr12), ArrayUtility.getLongestArray(arr12, arr11))

        // one null and one empty
        val arr13: Array<Byte>? = null
        val arr14: Array<Byte>? = null

        assertEquals(Optional.empty<Array<Byte>>(), ArrayUtility.getLongestArray(arr13, arr14))
        assertEquals(Optional.empty<Array<Byte>>(), ArrayUtility.getLongestArray(arr14, arr13))
    }
}