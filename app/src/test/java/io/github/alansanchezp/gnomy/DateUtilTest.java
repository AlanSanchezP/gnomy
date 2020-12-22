package io.github.alansanchezp.gnomy;

import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Locale;

import io.github.alansanchezp.gnomy.util.DateUtil;

import static org.junit.Assert.assertEquals;

public class DateUtilTest {
    @Test
    public void sets_clock_with_instant_string() {
        DateUtil.setFixedClockAtTime("2018-01-08T15:34:42.00Z", ZoneOffset.ofHours(-4));
        assertEquals("2018-01", DateUtil.now().toString());
        assertEquals(8, DateUtil.OffsetDateTimeNow().getDayOfMonth());
        assertEquals(11, DateUtil.OffsetDateTimeNow().getHour());
        assertEquals(34, DateUtil.OffsetDateTimeNow().getMinute());
        assertEquals(42, DateUtil.OffsetDateTimeNow().getSecond());
    }

    @Test
    public void setting_clock_fallbacks_to_current_instant() {
        DateUtil.setFixedClockAtTime("dka0", null);
        assertEquals(YearMonth.now().toString(), DateUtil.now().toString());
    }

    @Test
    public void uses_system_clock_by_default() {
        assertEquals(YearMonth.now().toString(), DateUtil.now().toString());
    }

    @Test
    public void year_month_string_is_correct() {
        Locale.setDefault(Locale.ENGLISH);
        DateUtil.setFixedClockAtTime("2018-01-01T15:34:42.00Z", ZoneOffset.ofHours(0));
        YearMonth march2018 = YearMonth.of(2018,3);
        YearMonth march2017 = YearMonth.of(2017,3);
        YearMonth march2019 = YearMonth.of(2019,3);

        assertEquals("March", DateUtil.getYearMonthString(march2018));
        assertEquals("March 2017", DateUtil.getYearMonthString(march2017));
        assertEquals("March 2019", DateUtil.getYearMonthString(march2019));
    }

    @Test
    public void year_month_is_capitalized_across_languages() {
        // TESTING SOME LANGUAGES THAT USE "TRADITIONAL" ALPHABET
        Locale.setDefault(Locale.ENGLISH);
        DateUtil.setFixedClockAtTime("2018-01-01T15:34:42.00Z", ZoneOffset.ofHours(-4));
        YearMonth march2018 = YearMonth.of(2018,3);

        assertEquals("March", DateUtil.getYearMonthString(march2018));

        Locale.setDefault(new Locale("es", "mx"));
        assertEquals("Marzo", DateUtil.getYearMonthString(march2018));

        Locale.setDefault(Locale.ITALIAN);
        assertEquals("Marzo", DateUtil.getYearMonthString(march2018));

        Locale.setDefault(Locale.FRENCH);
        assertEquals("Mars", DateUtil.getYearMonthString(march2018));

        Locale.setDefault(Locale.GERMAN);
        assertEquals("März", DateUtil.getYearMonthString(march2018));
    }

    @Test
    public void datetime_format_is_correct() {
        OffsetDateTime dateTime = OffsetDateTime.of(2020,1,1,
                14,30,15, 103, ZoneOffset.UTC);

        assertEquals("2020/01/01 02:30 PM", DateUtil.getOffsetDateTimeString(dateTime, true));
        assertEquals("2020/01/01", DateUtil.getOffsetDateTimeString(dateTime, false));

        dateTime = dateTime.withOffsetSameInstant(ZoneOffset.of("+4"));

        assertEquals("2020/01/01 06:30 PM", DateUtil.getOffsetDateTimeString(dateTime, true));
        assertEquals("2020/01/01", DateUtil.getOffsetDateTimeString(dateTime, false));
    }

    @Test
    public void day_datetime_format_is_correct() {
        Locale.setDefault(Locale.ENGLISH);
        OffsetDateTime dateTime = OffsetDateTime.of(2020,1,1,
                14,30,15, 103, ZoneOffset.UTC);
        assertEquals("Wednesday, 1", DateUtil.getDayString(dateTime));

        Locale.setDefault(new Locale("es", "mx"));
        assertEquals("Miércoles, 1", DateUtil.getDayString(dateTime));

        Locale.setDefault(Locale.ITALIAN);
        assertEquals("Mercoledì, 1", DateUtil.getDayString(dateTime));

        Locale.setDefault(Locale.FRENCH);
        assertEquals("Mercredi, 1", DateUtil.getDayString(dateTime));

        Locale.setDefault(Locale.GERMAN);
        assertEquals("Mittwoch, 1", DateUtil.getDayString(dateTime));
    }

    @Test
    public void month_boundaries_are_correct() {
        ZoneOffset offset = ZoneOffset.ofHours(-4);
        DateUtil.setFixedClockAtTime("2018-01-08T15:34:42.00Z", offset);
        OffsetDateTime expectedFirstInstant = OffsetDateTime.parse("2018-01-01T00:00:00.00Z")
                .withOffsetSameLocal(offset);
        OffsetDateTime expectedLastSecond = OffsetDateTime.parse("2018-02-01T00:00:00.00Z")
                .withOffsetSameLocal(offset);
        OffsetDateTime[] results = DateUtil.getMonthBoundaries(DateUtil.now());
        assertEquals(expectedFirstInstant, results[0]);
        assertEquals(expectedLastSecond, results[1]);
    }

    @Test
    public void day_id_are_correct() {
        OffsetDateTime dateTime = OffsetDateTime.parse("2020-01-01T00:00:00.00Z");
        assertEquals(Integer.valueOf(20200101), DateUtil.getDayId(dateTime));

        dateTime = OffsetDateTime.parse("2000-01-31T00:00:00.00Z");
        assertEquals(Integer.valueOf(20000131), DateUtil.getDayId(dateTime));

        dateTime = OffsetDateTime.parse("1995-12-20T00:00:00.00Z");
        assertEquals(Integer.valueOf(19951220), DateUtil.getDayId(dateTime));
    }
}
