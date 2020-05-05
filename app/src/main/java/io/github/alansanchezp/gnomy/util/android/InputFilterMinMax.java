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

    private boolean isInRange(BigDecimal a, BigDecimal b, BigDecimal c) {
        // b > a
        if (b.compareTo(a) == 1) {
            // c >= a && c <= b
            return c.compareTo(a) >= 0 && c.compareTo(b) <= 0;
        } else {
            // c >= b && c <= a
            return c.compareTo(b) >= 0 && c.compareTo(a) <= 0;
        }
    }
}
