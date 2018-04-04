package com.msc.serverbrowser.util.basic

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Marcel
 * @since 21.09.2017
 */
class StringUtilityTest {
    @Test
    fun testStringToBoolean() {
        assertTrue(StringUtility.stringToBoolean("true"))
        assertTrue(StringUtility.stringToBoolean("1"))
        assertTrue(StringUtility.stringToBoolean("True"))
        assertTrue(StringUtility.stringToBoolean("TRUE"))

        assertFalse(StringUtility.stringToBoolean(null))
        assertFalse(StringUtility.stringToBoolean("0"))
        assertFalse(StringUtility.stringToBoolean("1 "))
        assertFalse(StringUtility.stringToBoolean(" 1"))
        assertFalse(StringUtility.stringToBoolean("Kauderwelsch"))
        assertFalse(StringUtility.stringToBoolean(""))
        assertFalse(StringUtility.stringToBoolean("false"))
    }

    @Test
    fun testFixUrl() {
        assertEquals("https://google.com", StringUtility.fixUrlIfNecessary("https://google.com"))
        assertEquals("http://google.com", StringUtility.fixUrlIfNecessary("http://google.com"))
        assertEquals("http://google.com", StringUtility.fixUrlIfNecessary("google.com"))
        assertEquals("http://", StringUtility.fixUrlIfNecessary(""))
        assertThrows(NullPointerException::class.java) { StringUtility.fixUrlIfNecessary(null) }
    }
}
