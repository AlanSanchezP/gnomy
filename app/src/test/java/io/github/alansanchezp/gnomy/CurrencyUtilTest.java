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
            System.out.println(e);
        }
    }

    @Test
    public void display_list_is_correct() {
        try {
            Locale.setDefault(new Locale("es", "es"));
            String[] displayArray = CurrencyUtil.getDisplayArray();
            assertEquals("USD - Dólar estadounidense", displayArray[0]);

            Locale.setDefault(new Locale("en", "uk"));
            displayArray = CurrencyUtil.getDisplayArray();
            assertEquals("USD - US Dollar", displayArray[0]);
        } catch(GnomyCurrencyException e) {
            System.out.println(e);
        }
    }
}
