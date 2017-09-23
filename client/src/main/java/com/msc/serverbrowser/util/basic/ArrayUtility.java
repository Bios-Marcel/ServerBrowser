package com.msc.serverbrowser.util.basic;

import java.util.Optional;

/**
 * @author Marcel
 * @since 23.09.2017
 */
public final class ArrayUtility
{
	private ArrayUtility()
	{
		// Constructor to prevent instantiation
	}

	/**
	 * Returns the longer of two arrays.
	 *
	 * @param arrayOne
	 *            first array
	 * @param arrayTwo
	 *            second error
	 * @return the error that is longer
	 * @throws NullPointerException
	 *             if any of the arrays is null
	 */
	public static <T> Optional<T[]> getLonger(final T[] arrayOne, final T[] arrayTwo) throws NullPointerException
	{
		if (arrayOne.length > arrayTwo.length)
		{
			return Optional.of(arrayOne);
		}
		else if (arrayTwo.length > arrayOne.length)
		{
			return Optional.of(arrayTwo);
		}

		return Optional.empty();
	}

}
