package io.github.alansanchezp.gnomy.util;

import java.math.BigDecimal;

public class BigDecimalUtil {
    public static final int DECIMAL_SCALE = 4;
    public static final int LONG_DIVISION_FACTOR = 10000;
    // TODO: Replace direct usages of BigDecimal.ZERO with BigDecimalUtil.ZERO
    public static final BigDecimal ZERO = fromString("0");

    public static BigDecimal fromString(String stringValue)
            throws NumberFormatException {
        return new BigDecimal(stringValue)
                .setScale(DECIMAL_SCALE, BigDecimal.ROUND_HALF_EVEN)
                .stripTrailingZeros();
    }

    public static BigDecimal fromLong(Long longValue)
            throws NumberFormatException{
        return new BigDecimal(longValue.toString())
                .divide(new BigDecimal(LONG_DIVISION_FACTOR),
                        DECIMAL_SCALE,
                        BigDecimal.ROUND_HALF_EVEN)
                .stripTrailingZeros();
    }

    public static Long toLong(BigDecimal decimalNumber) {
        return decimalNumber
                .multiply(new BigDecimal(LONG_DIVISION_FACTOR))
                .longValue();
    }

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
