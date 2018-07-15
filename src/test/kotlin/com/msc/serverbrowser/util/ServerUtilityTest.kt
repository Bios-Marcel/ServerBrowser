package com.msc.serverbrowser.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ServerUtilityTest {
    @Test
    fun isPortValid() {
        for (port in 0..65535) {
            assertTrue(ServerUtility.isPortValid(port.toString()), "$port should have been valid.")
        }

        assertFalse(ServerUtility.isPortValid("-1"), "Ports below 0 should be invalid")
        assertFalse(ServerUtility.isPortValid("65536"), "Ports above 65535 should be invalid")

        assertFalse(ServerUtility.isPortValid("1.1"), "Only Integers are valid")
        assertFalse(ServerUtility.isPortValid("1,1"), "Only Integers are valid")

        assertFalse(ServerUtility.isPortValid("A"), "Letters aren't valid ports.")
    }
}