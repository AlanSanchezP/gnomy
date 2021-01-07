package io.github.alansanchezp.gnomy.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class that helps preserve {@link BigDecimal} equality across
 * objects (.equals() method), by keeping rounding mode and scale
 * constant across the application.
 */
public class BigDecimalUtil {
    public static final int DECIMAL_SCALE = 4;
    public static final int LONG_DIVISION_FACTOR = 10000;
    public static final RoundingMode ROUNDING_MODE
            = RoundingMode.valueOf(BigDecimal.ROUND_HALF_EVEN);
    public static final BigDecimal ZERO = fromString("0");

    /**
     * Creates a safe-to-use {@link BigDecimal} object from a {@link String}
     * representation.
     *
     * @param stringValue   String representation
     * @return              BigDecimal instance
     * @throws NumberFormatException    If string cannot be parsed.
     */
    public static BigDecimal fromString(String stringValue)
            throws NumberFormatException {
        return new BigDecimal(stringValue)
                .setScale(DECIMAL_SCALE, ROUNDING_MODE)
                .stripTrailingZeros();
    }

    /**
     * Creates a safe-to-use {@link BigDecimal} object from a {@link Long}
     * representation, with the assumption that the long value has an amount
     * of extra digits at the end that represent the fractional part of the number
     * and it's equal to {@link #LONG_DIVISION_FACTOR};
     *
     * @param longValue     Original value.
     * @return              BigDecimal instance.
     * @throws NumberFormatException    If string resulting from {@link Long#toString()} cannot be parsed.
     */
    public static BigDecimal fromLong(Long longValue)
            throws NumberFormatException{
        return new BigDecimal(longValue.toString())
                .divide(new BigDecimal(LONG_DIVISION_FACTOR),
                        DECIMAL_SCALE,
                        ROUNDING_MODE)
                .stripTrailingZeros();
    }

    /**
     * Inverse operation of {@link #fromLong(Long)}.
     *
     * @param decimalNumber BigDecimal object to convert to Long.
     * @return              Long representation.
     */
    public static Long toLong(BigDecimal decimalNumber) {
        return decimalNumber
                .multiply(new BigDecimal(LONG_DIVISION_FACTOR))
                .longValue();
    }

    /**
     * Checks if a given {@link BigDecimal} value falls in some range (inclusive).
     *
     * @param limitA    Lower limit.
     * @param limitB    Upper limit.
     * @param target    Value to check.
     * @return          True if the target value is in the given range, false otherwise.
     * @throws NullPointerException If any of the parameters is null.
     */
    public static boolean isInRange(BigDecimal limitA,
                                    BigDecimal limitB,
                                    BigDecimal target) {
        // limitB > limitA : limitA is lowest valid number
        //noinspection ComparatorResultComparison
        if (limitB.compareTo(limitA) == 1) {
            // target >= limitA && target <= limitB
            return target.compareTo(limitA) >= 0 && target.compareTo(limitB) <= 0;
        } else { // limitB is lowest valid number
            // target >= limitB && target <= limitA
            return target.compareTo(limitB) >= 0 && target.compareTo(limitA) <= 0;
        }
    }
}
