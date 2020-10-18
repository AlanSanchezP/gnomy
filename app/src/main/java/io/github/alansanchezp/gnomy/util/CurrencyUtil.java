package io.github.alansanchezp.gnomy.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;

public class CurrencyUtil {
    // TODO generate complete set of currencies
    //  consider limitations of free services for conversion rates
    private static final String[] CURRENCIES = {
        "USD",
        "MXN",
        "EUR"
    };

    // TODO: Evaluate if '---' should be replaced by something else
    public static final String NULL_NUMBER_STRING = "---";

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
        if (number == null) return NULL_NUMBER_STRING;

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

    public static BigDecimal[] sumAccountListBalances(
            List<AccountWithBalance> awbList,
            String baseCurrencyCode)
            throws GnomyCurrencyException {
        // TODO: Adjust logic when currency support is implemented
        //  This loop is here just so we can display something
        //  It's possible that the whole method (including signature) will change
        // totals[0] represents accumulated/current balance
        // totals[1] represents projected/past balance
        BigDecimal[] totals = {null,null};
        totals[0] = new BigDecimal("0");

        for (AccountWithBalance awb : awbList) {
            if (awb.accumulatedBalance != null) {
                totals[0] = totals[0].add(awb.accumulatedBalance);
            }
            if (awb.projectedBalance != null) {
                if (totals[1] == null) totals[1] = new BigDecimal("0");
                totals[1] = totals[1].add(awb.projectedBalance);
            }
        }

        return totals;
    }
}
