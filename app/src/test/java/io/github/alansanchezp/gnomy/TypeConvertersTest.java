package io.github.alansanchezp.gnomy;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.util.TimeZone;

import io.github.alansanchezp.gnomy.data.GnomyTypeConverters;

@SuppressWarnings("ConstantConditions")
public class TypeConvertersTest {
    static private ZoneId originalZoneId;
    static private OffsetDateTime dateTime;
    static private Long timestamp;

    @BeforeClass
    public static void beforeAll() {
        ZoneId utc0 = ZoneId.of("Z");
        DateTimeFormatter dtf = DateTimeFormatter
                .ofPattern("dd/MM/yyyy HH:mm X");

        originalZoneId = ZoneId.systemDefault();
        TimeZone.setDefault(TimeZone.getTimeZone(utc0.toString()));

        dateTime = OffsetDateTime
                .parse("01/01/2020 00:00 Z", dtf);
        timestamp = 1577836800000L;
    }

    @Test
    public void toTimestamp_isCorrect() {
        assertEquals(timestamp, GnomyTypeConverters.dateToTimestamp(dateTime));
        assertNull(GnomyTypeConverters.dateToTimestamp(null));
    }

    @Test
    public void toDate_isCorrect() {
        assertEquals(dateTime, GnomyTypeConverters.timestampToDate(timestamp));
        assertNull(GnomyTypeConverters.timestampToDate(null));
    }

    @Test
    public void toDecimal_isCorrect() {
        assertNull(GnomyTypeConverters.longToDecimal(null));
        assertNotNull(GnomyTypeConverters.longToDecimal(10L));
    }

    @Test
    public void toLong_isCorrect() {
        assertNull(GnomyTypeConverters.decimalToLong(null));
        assertNotNull(GnomyTypeConverters.decimalToLong(BigDecimal.ZERO));
    }

    @Test
    public void toYearMonth_isCorrect() {
        YearMonth testObj = GnomyTypeConverters.intToYearMonth(10);
        assertNull(testObj);

        testObj = GnomyTypeConverters.intToYearMonth(101);
        assertEquals("0001-01", testObj.toString());

        testObj = GnomyTypeConverters.intToYearMonth(199712);
        assertEquals("1997-12", testObj.toString());

        testObj = GnomyTypeConverters.intToYearMonth(202001);
        assertEquals("2020-01", testObj.toString());

        testObj = GnomyTypeConverters.intToYearMonth(202015);
        assertNull(testObj);

        testObj = GnomyTypeConverters.intToYearMonth(100);
        assertNull(testObj);
    }

    @Test
    public void toInt_isCorrect() {
        assertEquals(0, GnomyTypeConverters.yearMonthToInt(null));

        YearMonth testObj = YearMonth.of(1, 1);
        assertEquals(101, GnomyTypeConverters.yearMonthToInt(testObj));

        testObj = YearMonth.of(1997, 12);
        assertEquals(199712, GnomyTypeConverters.yearMonthToInt(testObj));

        testObj = YearMonth.of(2020, 1);
        assertEquals(202001, GnomyTypeConverters.yearMonthToInt(testObj));
    }

    @AfterClass
    public static void afterAll() {
        TimeZone.setDefault(TimeZone.getTimeZone(originalZoneId.toString()));
    }
}
