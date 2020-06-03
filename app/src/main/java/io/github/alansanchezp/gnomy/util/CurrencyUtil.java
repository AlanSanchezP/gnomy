package io.github.alansanchezp.gnomy.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

public class CurrencyUtil {
    // TODO generate complete set of currencies
    // consider limitations of free services for conversion rates
    private static final String[] CURRENCIES = {
        "USD",
        "MXN",
        "EUR"
    };

    public static String[] getCurrencies() {
        return CURRENCIES;
    }

    public static String getCurrencyCode(int index) {
        return CURRENCIES[index];
    }

    public static String getDisplayName(String currencyCode) throws GnomyCurrencyException {
        Currency currency = getCurrency(currencyCode);
        String currencyName = currency.getDisplayName();

        currencyName = currencyName.substring(0, 1).toUpperCase()
                + currencyName.substring(1);

        return String.format("%s - %s", currencyCode, currencyName);
    }

    public static String format(BigDecimal number, String currencyCode) throws GnomyCurrencyException {
        if (number == null) {
            // TODO: Evaluate if '---' should be replaced by something else
            return "---";
        }

        Currency currency = getCurrency(currencyCode);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

        formatter.setCurrency(currency);

        return formatter.format(number);
    }

    public static String[] getDisplayArray() throws GnomyCurrencyException {
        String[] displayArray = new String[CURRENCIES.length];

        for (int i = 0; i < CURRENCIES.length; i++) {
            String displayName = getDisplayName(CURRENCIES[i]);
            displayArray[i] = displayName;
        }

        return displayArray;
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
