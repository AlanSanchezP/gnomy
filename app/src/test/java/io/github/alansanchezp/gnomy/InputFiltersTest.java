package io.github.alansanchezp.gnomy;

import org.junit.Test;

import java.math.BigDecimal;

import static io.github.alansanchezp.gnomy.util.android.InputFilterMinMax.isInRange;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InputFiltersTest {
    @Test
    public void MinMax_isInRange_is_correct() {
        BigDecimal[] testNumbers = new BigDecimal[7];
        testNumbers[0] = new BigDecimal("0");
        testNumbers[1] = new BigDecimal("-100");
        testNumbers[2] = new BigDecimal("-3000");
        testNumbers[3] = new BigDecimal("-572");
        testNumbers[4] = new BigDecimal("1061");
        testNumbers[5] = new BigDecimal("572");
        testNumbers[6] = new BigDecimal("203");

        //  Mixed ends (negative + positive)
        assertTrue("0 in range [-100, 572]",
                isInRange(testNumbers[1],testNumbers[5],testNumbers[0]));
        assertTrue("0 in range [-100, 572] (parameters reversed)",
                isInRange(testNumbers[5],testNumbers[1],testNumbers[0]));
        assertFalse("1061 not in range [-100, 572]",
                isInRange(testNumbers[1],testNumbers[5],testNumbers[4]));
        assertFalse("1061 not in range [-100, 572] (parameters reversed)",
                isInRange(testNumbers[5],testNumbers[1],testNumbers[4]));

        assertTrue("203 in range [-100, 572]",
                isInRange(testNumbers[1],testNumbers[5],testNumbers[6]));
        assertTrue("203 in range [-100, 572]  (parameters reversed)",
                isInRange(testNumbers[5],testNumbers[1],testNumbers[6]));
        assertFalse("-3000 not in range [-100, 572]",
                isInRange(testNumbers[1],testNumbers[5],testNumbers[2]));
        assertFalse("-3000 not in range [-100, 572]  (parameters reversed)",
                isInRange(testNumbers[5],testNumbers[1],testNumbers[2]));

        //  Negative numbers range
        assertTrue("-572 in range [-3000, -100]",
                isInRange(testNumbers[2],testNumbers[1],testNumbers[3]));
        assertTrue("-572 in range [-3000, -100]  (parameters reversed)",
                isInRange(testNumbers[2],testNumbers[1],testNumbers[3]));
        assertFalse("203 not in range [-3000, -100]",
                isInRange(testNumbers[2],testNumbers[1],testNumbers[6]));
        assertFalse("203 not in range [-3000, -100]  (parameters reversed)",
                isInRange(testNumbers[2],testNumbers[1],testNumbers[6]));

        //  Positive numbers range
        assertTrue("572 in range [203, 1061]",
                isInRange(testNumbers[6],testNumbers[4],testNumbers[5]));
        assertTrue("572 in range [203, 1061]  (parameters reversed)",
                isInRange(testNumbers[6],testNumbers[4],testNumbers[5]));
        assertFalse("0 not in range [203, 1061]",
                isInRange(testNumbers[6],testNumbers[4],testNumbers[0]));
        assertFalse("0 not in range [203, 1061]  (parameters reversed)",
                isInRange(testNumbers[6],testNumbers[4],testNumbers[0]));
    }
}
