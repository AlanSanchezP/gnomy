package io.github.alansanchezp.gnomy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import java.math.BigDecimal;
import java.util.Locale;

import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;

public class CurrencyUtilTest {
    @Test
    public void format_throws_unsupported_code_exception() {
        BigDecimal number = new BigDecimal("20.150");

        try {
            CurrencyUtil.format(number, "BMD");
        } catch(GnomyCurrencyException e) {
            assertEquals("Currency code is not supported.", e.getMessage());
        }
    }

    @Test
    public void format_is_correct() {
        try {
            BigDecimal number = new BigDecimal("20.150");
            String currency = "MXN";
            Locale.setDefault(new Locale("es", "mx"));
            assertEquals("$20.15", CurrencyUtil.format(number, currency));

            currency = "USD";
            assertEquals("US$20.15", CurrencyUtil.format(number, currency));

            Locale.setDefault(new Locale("ru", "ru"));
            assertEquals("20,15 USD", CurrencyUtil.format(number, currency));

            currency = "MXN";
            assertEquals("20,15 MXN", CurrencyUtil.format(number, currency));
        } catch(GnomyCurrencyException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void display_name_is_correct() {
        try {
            String code = "USD";

            Locale.setDefault(new Locale("es", "es"));
            String displayName = CurrencyUtil.getDisplayName(code);
            assertEquals("USD - Dólar estadounidense", displayName);

            Locale.setDefault(new Locale("en", "uk"));
            displayName = CurrencyUtil.getDisplayName(code);
            assertEquals("USD - US Dollar",displayName);
        } catch(GnomyCurrencyException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void display_array_is_correct() {
        try {
            String[] codesArray = CurrencyUtil.getCurrencies();
            String[] displayArray = CurrencyUtil.getDisplayArray();

            assertEquals(codesArray.length, displayArray.length);
        } catch(GnomyCurrencyException e) {
            e.printStackTrace();
        }
    }
}
