package serverbrowser.util.basic

import java.util.function.Supplier

/**
 * Serves the same purpose as Javas [Supplier], but it its [.get] function may always
 * throw a [Throwable].
 *
 * @author Marcel
 * @since 01.03.2018
 * @param <T> Type of return value
</T> */
@FunctionalInterface
interface ThrowingSupplier<T> {

    /**
     * @return the return value of Type T returned by the given implementation
     * @throws Throwable Any exceptions which might be thrown by the implementation
     */
    @Throws(Throwable::class)
    fun get(): T
}