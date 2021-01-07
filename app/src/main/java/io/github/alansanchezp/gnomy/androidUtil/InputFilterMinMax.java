package io.github.alansanchezp.gnomy.androidUtil;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import java.math.BigDecimal;

import io.github.alansanchezp.gnomy.util.BigDecimalUtil;

/**
 * InputFilter to limit numeric inputs to a value range (inclusive).
 */
public class InputFilterMinMax implements InputFilter {
    private final BigDecimal min;
    private final BigDecimal max;
    private final int decimalScale;

    /**
     *
     * @param min           Lower limit.
     * @param max           Upper limit.
     * @param decimalScale  Decimal scale to use in the comparison.
     */
    public InputFilterMinMax(BigDecimal min, BigDecimal max, int decimalScale) {
        this.min = min;
        this.max = max;
        this.decimalScale = decimalScale;
    }

    /**
     * Constructor that creates {@link BigDecimal} objects
     * from a {@link String} representation.
     *
     * @param min           Lower limit.
     * @param max           Upper limit.
     * @param decimalScale  Decimal scale to use in the comparison.
     */
    public InputFilterMinMax(String min, String max, int decimalScale) {
        this(new BigDecimal(min), new BigDecimal(max), decimalScale);
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
