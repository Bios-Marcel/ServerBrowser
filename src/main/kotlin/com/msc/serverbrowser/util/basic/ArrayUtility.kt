package com.msc.serverbrowser.util.basic

import java.util.*

/**
 * @author Marcel
 * @since 23.09.2017
 */
object ArrayUtility {

    /**
     * Returns the longer of two arrays or an empty [Optional].
     *
     * @param arrayOne first array
     * @param arrayTwo second array
     * @return the array that is longer, or none if they are of the same size
     * @throws NullPointerException if any of the arrays is null
     */
    @JvmStatic
    @Throws(NullPointerException::class)
    fun <T> getLongestArray(arrayOne: Array<T>?, arrayTwo: Array<T>?): Optional<Array<T>> {
        if (arrayOne == null || arrayTwo == null) {
            return OptionalUtility.firstNonNullOrEmpty(arrayOne, arrayTwo)
        }

        if (arrayOne.size > arrayTwo.size) {
            return Optional.of(arrayOne)
        } else if (arrayTwo.size > arrayOne.size) {
            return Optional.of(arrayTwo)
        }

        return Optional.empty()
    }

    /**
     * Checks if the array contains the given item.
     *
     * @param array the array to search in for
     * @param searchFor the item to search for
     * @return true if the array contains the item, otherwise false
     */
    @JvmStatic
    fun <T> contains(array: Array<T>, searchFor: T): Boolean {
        if (Objects.isNull(array) || array.isEmpty()) {
            return false
        }

        for (`object` in array) {
            if (`object` == searchFor) {
                return true
            }
        }
        return false
    }

    /**
     * Concatenates two or more byte arrays the following way:
     *
     *
     * `arrayOne + arrayTwo + ...`
     *
     *
     * @param arrayOne Array one
     * @param arrays following arrays
     * @return the combined byte array
     */
    @JvmStatic
    fun merge(arrayOne: ByteArray, vararg arrays: ByteArray): ByteArray {
        val lengthNew = arrayOne.size + Arrays.stream(arrays).mapToInt { arr -> arr.size }.sum()
        val toReturn = ByteArray(lengthNew)

        System.arraycopy(arrayOne, 0, toReturn, 0, arrayOne.size)

        var startPos = arrayOne.size
        for (arr in arrays) {
            System.arraycopy(arr, 0, toReturn, startPos, arr.size)
            startPos += arr.size
        }

        return toReturn
    }
}// Constructor to prevent instantiation
