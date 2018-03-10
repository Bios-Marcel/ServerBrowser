package com.msc.serverbrowser.util.basic;

import java.util.function.Supplier;

/**
 * Serves the same purpose as Javas {@link Supplier}, but it its {@link #get()} function may always
 * throw a {@link Throwable}.
 *
 * @author Marcel
 * @since 01.03.2018
 * @param <T> Type of return value
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {

	/**
	 * @return the return value of Type T returned by the given implementation
	 * @throws Throwable Any exceptions which might be thrown by the implementation
	 */
	T get() throws Throwable;
}