package serverbrowser.util.basic

import java.util.*
import java.util.function.Supplier

/**
 * Contains methods for working with [Optional].
 *
 * @author marcel
 * @since Jan 11, 2018
 */
object OptionalUtility {

    /**
     * Returns an [Optional] of the first non-null Object found within the given objects.
     *
     * @param objects objects to get the first non-null from
     * @return first non-null object or an empty [Optional]
     */
    @SafeVarargs
    fun <T> firstNonNullOrEmpty(vararg objects: T?): Optional<T> {
        for (t in objects) {
            if (t != null) {
                return Optional.of(t)
            }
        }

        return Optional.empty()
    }
}
