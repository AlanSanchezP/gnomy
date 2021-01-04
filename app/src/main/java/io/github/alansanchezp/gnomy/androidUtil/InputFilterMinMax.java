package io.github.alansanchezp.gnomy.androidUtil;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import java.math.BigDecimal;

import io.github.alansanchezp.gnomy.util.BigDecimalUtil;

public class InputFilterMinMax implements InputFilter {
    private final BigDecimal min;
    private final BigDecimal max;
    private final int decimalScale;

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

            if (BigDecimalUtil.isInRange(min, max, input)) {
                if (input.scale() <= this.decimalScale) return null;
            }
        } catch (NumberFormatException nfe) {
            Log.w("InputFilterMinMax", "filter: Input is not a valid number", nfe);
        }
        return "";
    }
}
