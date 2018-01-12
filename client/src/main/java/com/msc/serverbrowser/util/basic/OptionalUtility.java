package com.msc.serverbrowser.util.basic;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Contains methods for working with {@link Optional}.
 *
 * @author marcel
 * @since Jan 11, 2018
 */
public class OptionalUtility {
	/**
	 * Attempts retrieving an object from the given {@link Supplier}, in case the retrieval fails or
	 * the retrieved {@link Object} equals <code>null</code>, {@link Optional#empty()} will be
	 * returned.
	 *
	 * @param supplier
	 *            {@link Supplier} that supplies the returned (wrapped) object
	 * @return An {@link Optional} of the {@link Object} returned by the {@link Supplier} or
	 *         {@link Optional#empty()}
	 */
	public static <T> Optional<T> attempt(final Supplier<T> supplier) {
		try {
			return Optional.ofNullable(supplier.get());
		}
		catch (@SuppressWarnings("unused") final Throwable exception) {
			return Optional.empty();
		}
	}
}
