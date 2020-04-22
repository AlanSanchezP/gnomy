package io.github.alansanchezp.gnomy.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

public class CurrencyUtil {
    // TODO generate complete set of currencies
    // consider limitations of free services for conversion rates
    public static final String[] CURRENCIES = {
        "USD",
        "MXN",
        "EUR"
    };

    public static String format(BigDecimal number, String currencyCode) throws GnomyCurrencyException {
        Currency currency = getCurrency(currencyCode);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

        formatter.setCurrency(currency);

        return formatter.format(number);
    }

    private static Currency getCurrency(String currencyCode)
            throws GnomyCurrencyException {
       if (Arrays.asList(CURRENCIES).contains(currencyCode)) {
           try {
               return Currency.getInstance(currencyCode);
           } catch(IllegalArgumentException e) {
               //  This shouldn't happen...
               throw new GnomyCurrencyException("Invalid java.util.Currency currency code.");
           }
       } else {
           throw new GnomyCurrencyException("Currency code is not supported.");
       }
    }
}
