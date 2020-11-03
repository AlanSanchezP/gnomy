package io.github.alansanchezp.gnomy.util;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static void setFixedClockAtTime(String timeString) {
        try {
            Instant instant = Instant.parse(timeString);
            setClock(Clock.fixed(instant, ZoneId.systemDefault()));
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

    public static String getYearMonthString(YearMonth yearMonth) {
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
}
