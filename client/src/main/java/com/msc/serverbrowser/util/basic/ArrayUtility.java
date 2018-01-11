package com.msc.serverbrowser.util.basic;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Marcel
 * @since 23.09.2017
 */
public final class ArrayUtility {
	private ArrayUtility() {
		// Constructor to prevent instantiation
	}

	/**
	 * Returns the longer of two arrays.
	 *
	 * @param arrayOne
	 *            first array
	 * @param arrayTwo
	 *            second array
	 * @return the array that is longer, or none if they are of the same size
	 * @throws NullPointerException
	 *             if any of the arrays is null
	 */
	public static <T> Optional<T[]> getLonger(final T[] arrayOne, final T[] arrayTwo) throws NullPointerException {
		if (arrayOne.length > arrayTwo.length) {
			return Optional.of(arrayOne);
		} else if (arrayTwo.length > arrayOne.length) {
			return Optional.of(arrayTwo);
		}

		return Optional.empty();
	}

	/**
	 * Checks if the array contains the given item.
	 *
	 * @param array
	 *            the array to search in for
	 * @param searchFor
	 *            the item to search for
	 * @return true if the array contains the item, otherwise false
	 */
	public static <T> boolean contains(final T[] array, final T searchFor) {
		if (Objects.isNull(array) || array.length == 0) {
			return false;
		}

		for (final T object : array) {
			if (object.equals(searchFor)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Concats two or more byte arrays the follwing way:
	 * <p>
	 * <code>arrayOne + arrayTwo + ...</code>
	 * </p>
	 *
	 * @param arrayOne
	 *            Array one
	 * @param arrays
	 *            following arrays
	 * @return the combined byte array
	 */
	public static byte[] merge(final byte[] arrayOne, final byte[]... arrays) {
		final int lengthNew = arrayOne.length + Arrays.asList(arrays).stream().mapToInt(arr -> arr.length).sum();
		final byte[] toReturn = new byte[lengthNew];

		System.arraycopy(arrayOne, 0, toReturn, 0, arrayOne.length);

		int startPos = arrayOne.length;
		for (final byte[] arr : arrays) {
			System.arraycopy(arr, 0, toReturn, startPos, arr.length);
			startPos += arr.length;
		}

		return toReturn;
	}
}
