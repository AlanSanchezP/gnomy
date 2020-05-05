package io.github.alansanchezp.gnomy.database;

import org.threeten.bp.DateTimeException;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.YearMonth;
import org.threeten.bp.ZoneId;

import java.math.BigDecimal;

import androidx.room.TypeConverter;

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
            return new BigDecimal(longNumber.toString())
                    .divide(new BigDecimal(10000), 4, BigDecimal.ROUND_HALF_EVEN)
                    .stripTrailingZeros();
        }
        return null;
    }

    @TypeConverter
    public static Long decimalToLong(BigDecimal decimalNumber) {
        if (decimalNumber != null) {
            return decimalNumber
                    .multiply(new BigDecimal(10000))
                    .longValue();
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
