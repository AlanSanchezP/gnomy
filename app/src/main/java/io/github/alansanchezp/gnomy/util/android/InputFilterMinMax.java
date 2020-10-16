package io.github.alansanchezp.gnomy.util.android;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import java.math.BigDecimal;

public class InputFilterMinMax implements InputFilter {
    private BigDecimal min, max;
    private int decimalScale;

    public InputFilterMinMax(BigDecimal min, BigDecimal max, int decimalScale) {
        this.min = min;
        this.max = max;
        this.decimalScale = decimalScale;
    }

    public InputFilterMinMax(String min, String max, int decimalScale) {
        this.min = new BigDecimal(min);
        this.max = new BigDecimal(max);
        this.decimalScale = decimalScale;
    }

    @Override
    public CharSequence filter(CharSequence source,
                               int start,
                               int end,
                               Spanned dest,
                               int dstart,
                               int dend) {
        try {
            BigDecimal input = new BigDecimal(dest.toString() + source.toString());

            if (isInRange(min, max, input)) {
                if (input.scale() <= this.decimalScale) return null;
            }
        } catch (NumberFormatException nfe) {
            Log.e("InputFilterMinMax", "filter: Input is not a valid number", nfe);
        }
        return "";
    }

    public static boolean isInRange(BigDecimal limitA,
                                    BigDecimal limitB,
                                    BigDecimal target) {
        // limitB > limitA : limitA is lowest valid number
        if (limitB.compareTo(limitA) == 1) {
            // target >= limitA && target <= limitB
            return target.compareTo(limitA) >= 0 && target.compareTo(limitB) <= 0;
        } else { // limitB is lowest valid number
            // target >= limitB && target <= limitA
            return target.compareTo(limitB) >= 0 && target.compareTo(limitA) <= 0;
        }
    }
}
