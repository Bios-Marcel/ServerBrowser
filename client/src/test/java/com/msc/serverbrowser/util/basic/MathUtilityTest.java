package com.msc.serverbrowser.util.basic;

import static com.msc.serverbrowser.util.basic.MathUtility.limitUpperAndLower;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link MathUtility} class.
 * 
 * @author marcel
 * @since 26.02.2018
 */
public class MathUtilityTest {

	/**
	 * Tests the {@link MathUtility#limitUpperAndLower(int, int, int)} method for
	 * correct input (decrease input, increase input and keep input) and illegal
	 * input.
	 */
	@Test
	public void testLimitUpperAndLower() {
		// Keep
		assertEquals(0, limitUpperAndLower(0, 0, 0), "The given number should have been kept.");
		assertEquals(2, limitUpperAndLower(2, 1, 5), "The given number should have been kept.");
		assertEquals(5, limitUpperAndLower(5, 1, 5), "The given number should have been kept.");

		// Use lower limit
		assertEquals(1, limitUpperAndLower(0, 1, 5), "The given number should have been increased from 0 to 1.");

		// Use upper limit
		assertEquals(5, limitUpperAndLower(6, 1, 5), "The given number should have been decreased from 6 to 5.");

		// invalid input
		assertThrows(IllegalArgumentException.class, () -> limitUpperAndLower(6, 5, 1));
	}
}
