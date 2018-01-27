package com.msc.serverbrowser.util.basic;

/**
 * @author Marcel
 * @since 27.01.2018
 */
public class MathUtility {

	/**
	 * Decreases or increases a number if its out of the given bounds.
	 *
	 * @param number the number to adjust
	 * @param lowerLimit lower limit that the number might get increased to
	 * @param upperLimit upper limit that the number might get decreased to
	 * @return the number or lowerLimit / upperLimit
	 */
	public static int limitUpperAndLower(final int number, final int lowerLimit, final int upperLimit) {
		return Math.max(Math.min(upperLimit, number), lowerLimit);
	}
}
