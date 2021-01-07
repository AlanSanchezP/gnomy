package io.github.alansanchezp.gnomy.database;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

import java.math.BigDecimal;

import androidx.room.TypeConverter;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;

/**
 * Utility class to support complex objects into the database, by converting them
 * into primitive types supported by SQLite, and reverse methods to retrieve that
 * same data for application use.
 */
public class GnomyTypeConverters {
    @TypeConverter
    public static OffsetDateTime timestampToDate(Long timestamp) {
        if (timestamp != null) {
            return OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp),
                    ZoneId.systemDefault());
        }
        return null;
    }

    @TypeConverter
    public static Long dateToTimestamp(OffsetDateTime date) {
        if (date != null) {
            return date.toInstant().toEpochMilli();
        }
        return null;
    }

    @TypeConverter
    public static BigDecimal longToDecimal(Long longNumber) {
        if (longNumber != null) {
            return BigDecimalUtil.fromLong(longNumber);
        }
        return null;
    }

    @TypeConverter
    public static Long decimalToLong(BigDecimal decimalNumber) {
        if (decimalNumber != null) {
            return BigDecimalUtil.toLong(decimalNumber);
        }
        return null;
    }



    @TypeConverter
    public static int yearMonthToInt(YearMonth yearMonth) {
        if (yearMonth == null) return 0;
        return Integer.parseInt(yearMonth.toString()
                .replace("-", ""));
    }

    @TypeConverter
    public static YearMonth intToYearMonth(int stamp) {
        if (stamp < 101) return null;

        try {
            String tmp = Integer.toString(stamp);
            int year = Integer.parseInt(tmp.substring(0, tmp.length() - 2));
            int month = Integer.parseInt(tmp.substring(tmp.length() - 2));

            return YearMonth.of(year, month);
        } catch (DateTimeException e) {
            return null;
        }
    }
}
