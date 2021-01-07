package io.github.alansanchezp.gnomy.util;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import androidx.annotation.NonNull;

/**
 * Helper class to use in date-related operations.
 * Avoid direct calls to now() methods of date classes
 * just so that they can benefit from fixed clock during tests.
 */
public class DateUtil {
    private static final Logger LOGGER = Logger.getLogger("tests");
    private static Clock CLOCK;

    private static Clock getClock() {
        if (CLOCK == null) return Clock.systemDefaultZone();
        return CLOCK;
    }

    private static void setClock(Clock clock) {
        try {
            Class.forName("io.github.alansanchezp.gnomy.DateUtilTest");
            CLOCK = clock;
        } catch(ClassNotFoundException cnfe) {
            LOGGER.log(Level.WARNING, "[DateUtil] setClock: Only allowed in tests. Using system clock.");
        }
    }

    /**
     * ONLY FOR TESTING PURPOSES. WILL DO NOTHING IN NORMAL BUILDS.
     *
     * Sets a fixed clock to use for all "now()" methods in this class.
     *
     * @param timeString
     * @param offset
     */
    public static void setFixedClockAtTime(String timeString, ZoneOffset offset) {
        try {
            Instant instant = Instant.parse(timeString);
            if (offset == null) offset = ZoneOffset.of(ZoneId.systemDefault().getId());
            setClock(Clock.fixed(instant, offset));
        } catch(DateTimeParseException dtpe) {
            LOGGER.log(Level.WARNING, "[DateUtil] setClock: Invalid timeString. Fallback to current system Instant.");
            setClock(Clock.fixed(Instant.now(), ZoneId.systemDefault()));
        }
    }

    public static YearMonth now() {
        return YearMonth.now(getClock());
    }

    public static OffsetDateTime OffsetDateTimeNow() {
        return OffsetDateTime.now(getClock());
    }

    /**
     * Returns a readable and friendly representation of a {@link YearMonth} object.
     *
     * @param yearMonth     Object to format.
     * @return              Month name if the given {@link YearMonth} object is
     *                      in the same year as the current instant. Both month name
     *                      and 4-digits year otherwise.
     */
    public static String getYearMonthString(@NonNull YearMonth yearMonth) {
        String formatterPattern;
        String monthString;

        if (yearMonth.getYear() == now().getYear()) {
            formatterPattern = "MMMM";
        } else {
            formatterPattern = "MMMM yyyy";
        }

        monthString = yearMonth.format(DateTimeFormatter.ofPattern(formatterPattern));
        /* This is needed as spanish localization (and possibly others too)
           returns first character as lowercase */
        monthString = monthString.substring(0, 1).toUpperCase()
                + monthString.substring(1);
        return monthString;
    }

    /**
     * Returns the custom {@link String} representation of a
     * {@link OffsetDateTime} object that will be used in the app.
     *
     * @param dateTime      OffsetDateTime object to format.
     * @param includeTime   Indicates if String should include hours and minutes or
     *                      just display date.
     * @return              String representation.
     */
    public static String getOffsetDateTimeString(OffsetDateTime dateTime, boolean includeTime) {
        // TODO: Are these patters the best choice?
        String pattern;
        if (includeTime) {
            pattern = "YYYY/MM/dd hh:mm a";
        } else {
            pattern = "YYYY/MM/dd";
        }
        return dateTime.toLocalDateTime().format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Returns the custom {@link String} representation of a
     * {@link OffsetDateTime} object that will be used in screens
     * that require some daily separator.
     *
     * @param dateTime  OffsetDateTime object to format.
     * @return          String representation.
     */
    public static String getDayString(OffsetDateTime dateTime) {
        String string = dateTime.format(DateTimeFormatter.ofPattern("EEEE, d"));
        /* This is needed as spanish localization (and possibly others too)
           returns first character as lowercase */
        string = string.substring(0, 1).toUpperCase()
                + string.substring(1);
        return string;
    }

    /**
     * Returns the first instant of the specified {@link YearMonth}, as well
     * as its last SECOND, using the local ZoneOffset value.
     *
     * @param month     YearMonth instance.
     * @return          An array of two elements. The first element (index 0)
     *                  corresponds to the first instant of the month. The
     *                  second element (index 1) corresponds to the last
     *                  second of the month.
     */
    public static OffsetDateTime[] getMonthBoundaries(YearMonth month) {
        ZoneOffset localOffset = OffsetDateTimeNow().getOffset();
        LocalDateTime firstInstant = month.atDay(1).atStartOfDay();
        LocalDateTime lastSecond = month.atEndOfMonth().atStartOfDay().plusDays(1);
        return new OffsetDateTime[] {
                firstInstant.atOffset(localOffset),
                lastSecond.atOffset(localOffset)};
    }

    /**
     * Returns an Integer representation of a given {@link OffsetDateTime} object,
     * using the format {@code uuuuMMdd}    (4 digits year, 2 digits monthOfYear and 2 digits dayOfMonth)
     * example: 20200101 for January 1st of 2020.
     *
     * @param dateTime  OffsetDateTime object to parse.
     * @return          Integer representation of the given object.
     */
    public static int getDayId(@NonNull OffsetDateTime dateTime) {
        return Integer.parseInt(dateTime.format(DateTimeFormatter.ofPattern("uuuuMMdd")));
    }
}
