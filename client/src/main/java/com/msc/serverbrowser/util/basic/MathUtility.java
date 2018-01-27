package com.msc.serverbrowser.util.basic;

public class MathUtility {
	public static int limitUpperAndLower(final int number, final int lowerLimit, final int upperLimit) {
		return Math.max(Math.min(upperLimit, number), lowerLimit);
	}
}
