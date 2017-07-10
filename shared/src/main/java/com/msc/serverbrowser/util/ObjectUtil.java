package com.msc.serverbrowser.util;

import java.util.Objects;

/**
 * Contains Utility Methods for interacting with objects of any kind.
 *
 * @author Marcel
 */
public class ObjectUtil
{
	/**
	 * Returns an object if it is non-null or the alternatively passed object.
	 *
	 * @param objectOne
	 *            the object to return if it is non-null
	 * @param objectTwo
	 *            the object that will alternatively returned
	 * @return objectOne if non-null, otherwise objectTwo
	 */
	public static <T> T orElse(final T objectOne, final T objectTwo)
	{
		return Objects.isNull(objectOne) ? Objects.requireNonNull(objectTwo) : objectOne;
	}
}