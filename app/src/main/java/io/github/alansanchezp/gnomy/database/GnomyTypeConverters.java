package io.github.alansanchezp.gnomy.database;

import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;

import java.math.BigDecimal;

import androidx.room.TypeConverter;

public class GnomyTypeConverters {
    @TypeConverter
    public static OffsetDateTime toDate(Long timestamp) {
        if (timestamp != null) {
            return OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp),
                    ZoneId.systemDefault());
        }
        return null;
    }

    @TypeConverter
    public static Long toTimestamp(OffsetDateTime date) {
        if (date != null) {
            return date.toInstant().toEpochMilli();
        }
        return null;
    }

    @TypeConverter
    public static BigDecimal toDecimal(Long longNumber) {
        if (longNumber != null) {
            return new BigDecimal(longNumber.toString())
                    .divide(new BigDecimal(10000));
        }
        return null;
    }

    @TypeConverter
    public static Long toLong(BigDecimal decimalNumber) {
        if (decimalNumber != null) {
            return decimalNumber
                    .multiply(new BigDecimal(10000))
                    .longValue();
        }
        return null;
    }
}
