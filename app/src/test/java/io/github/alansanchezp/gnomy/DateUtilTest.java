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
        DateUtil.setFixedClockAtTime("2018-01-08T15:34:42.00Z");
        assertEquals("2018-01", DateUtil.now().toString());
    }

    @Test
    public void setting_clock_fallbacks_to_current_instant() {
        DateUtil.setFixedClockAtTime("dka0");
        assertEquals(YearMonth.now().toString(), DateUtil.now().toString());
    }

    @Test
    public void uses_system_clock_by_default() {
        assertEquals(YearMonth.now().toString(), DateUtil.now().toString());
    }

    @Test
    public void year_month_string_is_correct() {
        Locale.setDefault(Locale.ENGLISH);
        DateUtil.setFixedClockAtTime("2018-01-01T15:34:42.00Z");
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
        DateUtil.setFixedClockAtTime("2018-01-01T15:34:42.00Z");
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
}
