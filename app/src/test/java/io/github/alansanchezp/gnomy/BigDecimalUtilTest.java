package io.github.alansanchezp.gnomy;

import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import io.github.alansanchezp.gnomy.util.BigDecimalUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class BigDecimalUtilTest {
    private static BigDecimal expectedDecimal_100_50,
            expectedDecimal_187_35,
            expectedDecimal_34_685,
            expectedDecimal_1980_9;

    private static Long expectedLong_100_50,
            expectedLong_187_35,
            expectedLong_34_685,
            expectedLong_1980_9;

    @BeforeClass
    public static void before_all() {
        expectedLong_100_50 = 1005000L;
        expectedLong_187_35 = 1873500L;
        expectedLong_34_685 = 346850L;
        expectedLong_1980_9 = 19809000L;

        expectedDecimal_100_50 = new BigDecimal("100.5");
        expectedDecimal_187_35 = new BigDecimal("187.35");
        expectedDecimal_34_685 = new BigDecimal("34.685");
        expectedDecimal_1980_9 = new BigDecimal("1980.9");
    }

    @Test
    public void returns_correct_object_from_string() {
        String value = "1000.00";
        BigDecimal BD1000 = new BigDecimal(value);
        BigDecimal returnedValue = BigDecimalUtil.fromString(value);

        // Same practical value, but not BigDecimal.equals()
        assertThat(BD1000, comparesEqualTo(returnedValue));
        assertNotEquals(BD1000, returnedValue);
        assertEquals("1000", returnedValue.toPlainString());

        value = "1000.504912";
        returnedValue = BigDecimalUtil.fromString(value);
        assertEquals("1000.5049", returnedValue.toPlainString());
    }

    @Test
    public void returns_correct_object_from_long() {
        assertEquals(expectedDecimal_100_50,
                BigDecimalUtil.fromLong(expectedLong_100_50));
        assertEquals(expectedDecimal_187_35,
                BigDecimalUtil.fromLong(expectedLong_187_35));
        assertEquals(expectedDecimal_34_685,
                BigDecimalUtil.fromLong(expectedLong_34_685));
        assertEquals(expectedDecimal_1980_9,
                BigDecimalUtil.fromLong(expectedLong_1980_9));
    }

    @Test
    public void returns_correct_long_object() {
        assertEquals(expectedLong_100_50,
                BigDecimalUtil.toLong(expectedDecimal_100_50));
        assertEquals(expectedLong_187_35,
                BigDecimalUtil.toLong(expectedDecimal_187_35));
        assertEquals(expectedLong_34_685,
                BigDecimalUtil.toLong(expectedDecimal_34_685));
        assertEquals(expectedLong_1980_9,
                BigDecimalUtil.toLong(expectedDecimal_1980_9));
    }

    @Test
    public void isInRange_is_correct() {
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
                BigDecimalUtil.isInRange(testNumbers[1],testNumbers[5],testNumbers[0]));
        assertTrue("0 in range [-100, 572] (parameters reversed)",
                BigDecimalUtil.isInRange(testNumbers[5],testNumbers[1],testNumbers[0]));
        assertFalse("1061 not in range [-100, 572]",
                BigDecimalUtil.isInRange(testNumbers[1],testNumbers[5],testNumbers[4]));
        assertFalse("1061 not in range [-100, 572] (parameters reversed)",
                BigDecimalUtil.isInRange(testNumbers[5],testNumbers[1],testNumbers[4]));

        assertTrue("203 in range [-100, 572]",
                BigDecimalUtil.isInRange(testNumbers[1],testNumbers[5],testNumbers[6]));
        assertTrue("203 in range [-100, 572]  (parameters reversed)",
                BigDecimalUtil.isInRange(testNumbers[5],testNumbers[1],testNumbers[6]));
        assertFalse("-3000 not in range [-100, 572]",
                BigDecimalUtil.isInRange(testNumbers[1],testNumbers[5],testNumbers[2]));
        assertFalse("-3000 not in range [-100, 572]  (parameters reversed)",
                BigDecimalUtil.isInRange(testNumbers[5],testNumbers[1],testNumbers[2]));

        //  Negative numbers range
        assertTrue("-572 in range [-3000, -100]",
                BigDecimalUtil.isInRange(testNumbers[2],testNumbers[1],testNumbers[3]));
        assertTrue("-572 in range [-3000, -100]  (parameters reversed)",
                BigDecimalUtil.isInRange(testNumbers[2],testNumbers[1],testNumbers[3]));
        assertFalse("203 not in range [-3000, -100]",
                BigDecimalUtil.isInRange(testNumbers[2],testNumbers[1],testNumbers[6]));
        assertFalse("203 not in range [-3000, -100]  (parameters reversed)",
                BigDecimalUtil.isInRange(testNumbers[2],testNumbers[1],testNumbers[6]));

        //  Positive numbers range
        assertTrue("572 in range [203, 1061]",
                BigDecimalUtil.isInRange(testNumbers[6],testNumbers[4],testNumbers[5]));
        assertTrue("572 in range [203, 1061]  (parameters reversed)",
                BigDecimalUtil.isInRange(testNumbers[6],testNumbers[4],testNumbers[5]));
        assertFalse("0 not in range [203, 1061]",
                BigDecimalUtil.isInRange(testNumbers[6],testNumbers[4],testNumbers[0]));
        assertFalse("0 not in range [203, 1061]  (parameters reversed)",
                BigDecimalUtil.isInRange(testNumbers[6],testNumbers[4],testNumbers[0]));
    }
}
