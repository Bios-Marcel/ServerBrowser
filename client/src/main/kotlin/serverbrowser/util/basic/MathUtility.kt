package serverbrowser.util.basic

/**
 * @author Marcel
 * @since 27.01.2018
 */
object MathUtility {

    /**
     * Decreases or increases a number if its out of the given bounds.
     *
     * @param number the number to adjust
     * @param lowerLimit lower limit that the number might get increased to
     * @param upperLimit upper limit that the number might get decreased to
     * @return the number or lowerLimit / upperLimit
     */
    @JvmStatic
    fun limitUpperAndLower(number: Int, lowerLimit: Int, upperLimit: Int): Int {
        if (lowerLimit > upperLimit) {
            throw IllegalArgumentException("Lower bound can not be higher than upper bound ($lowerLimit-$upperLimit)")
        }

        return Math.max(Math.min(upperLimit, number), lowerLimit)
    }
}// Private constructor to prevent instantiation
