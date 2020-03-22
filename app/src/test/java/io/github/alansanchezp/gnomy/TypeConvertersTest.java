package io.github.alansanchezp.gnomy;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.util.TimeZone;

import io.github.alansanchezp.gnomy.database.GnomyTypeConverters;

public class TypeConvertersTest {
    static private ZoneId originalZoneId;
    static private OffsetDateTime dateTime;
    static private Long timestamp;

    static private Long expectedLong_100_50;
    static private Long expectedLong_187_35;
    static private Long expectedLong_34_685;
    static private Long expectedLong_1980_9;

    static private BigDecimal expectedDecimal_100_50;
    static private BigDecimal expectedDecimal_187_35;
    static private BigDecimal expectedDecimal_34_685;
    static private BigDecimal expectedDecimal_1980_9;

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

        expectedLong_100_50 = 1005000L;
        expectedLong_187_35 = 1873500L;
        expectedLong_34_685 = 346850L;
        expectedLong_1980_9 = 19809000L;

        expectedDecimal_100_50 = new BigDecimal("100.50");
        expectedDecimal_187_35 = new BigDecimal("187.35");
        expectedDecimal_34_685 = new BigDecimal("34.685");
        expectedDecimal_1980_9 = new BigDecimal("1980.9");
    }

    @Test
    public void toTimestamp_isCorrect() {
        assertEquals(timestamp, GnomyTypeConverters.toTimestamp(dateTime));
        assertNull(GnomyTypeConverters.toTimestamp(null));
    }

    @Test
    public void toDate_isCorrect() {
        assertEquals(dateTime, GnomyTypeConverters.toDate(timestamp));
        assertNull(GnomyTypeConverters.toDate(null));
    }

    @Test
    public void toDecimal_isCorrect() {
        assertThat(expectedDecimal_100_50, comparesEqualTo(
                GnomyTypeConverters.toDecimal(expectedLong_100_50)));
        assertThat(expectedDecimal_187_35, comparesEqualTo(
                GnomyTypeConverters.toDecimal(expectedLong_187_35)));
        assertThat(expectedDecimal_34_685, comparesEqualTo(
                GnomyTypeConverters.toDecimal(expectedLong_34_685)));
        assertThat(expectedDecimal_1980_9, comparesEqualTo(
                GnomyTypeConverters.toDecimal(expectedLong_1980_9)));

        assertNull(GnomyTypeConverters.toLong(null));
    }

    @Test
    public void toLong_isCorrect() {
        assertEquals(expectedLong_100_50,
                GnomyTypeConverters.toLong(expectedDecimal_100_50));
        assertEquals(expectedLong_187_35,
                GnomyTypeConverters.toLong(expectedDecimal_187_35));
        assertEquals(expectedLong_34_685,
                GnomyTypeConverters.toLong(expectedDecimal_34_685));
        assertEquals(expectedLong_1980_9,
                GnomyTypeConverters.toLong(expectedDecimal_1980_9));

        assertNull(GnomyTypeConverters.toDecimal(null));
    }

    @AfterClass
    public static void afterAll() {
        TimeZone.setDefault(TimeZone.getTimeZone(originalZoneId.toString()));
    }
}
