package com.msc.serverbrowser.util.basic;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Contains methods for working with {@link Optional}.
 *
 * @author marcel
 * @since Jan 11, 2018
 */
public final class OptionalUtility {
	private OptionalUtility() {
		// Prevent instanziation
	}

	/**
	 * Returns an {@link Optional} of the first non-null Object found within the given objects.
	 *
	 * @param objects
	 *            objects to get the first non-null from
	 * @return first non-null object or an empty {@link Optional}
	 */
	@SafeVarargs
	public static <T> Optional<T> firstNonNullOrEmpty(final T... objects) {
		for (final T t : objects) {
			if (Objects.nonNull(t)) {
				return Optional.of(t);
			}
		}

		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<T> cast(final Object object) {
		try {
			return Optional.ofNullable((T) object);
		}
		catch (@SuppressWarnings("unused") final Exception ignored) {
			// Ignoring the exception, because in case of failure, we just want Optional#empty
		}
		return Optional.empty();
	}

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
