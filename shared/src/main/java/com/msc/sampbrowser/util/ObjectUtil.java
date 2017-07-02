package com.msc.sampbrowser.util;

import java.util.Objects;

public class ObjectUtil
{
	/**
	 * Returns
	 *
	 * @param objectOne
	 * @param objectTwo
	 * @return
	 */
	public static <T> T orElse(final T objectOne, final T objectTwo)
	{
		return Objects.isNull(objectOne) ? objectTwo : objectOne;
	}
}