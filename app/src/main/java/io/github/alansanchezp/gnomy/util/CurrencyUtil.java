package io.github.alansanchezp.gnomy.util;


import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import io.github.alansanchezp.gnomy.data.account.AccountWithAccumulated;

/**
 * Helper class to use in operations that require to
 * display currency values and (this is still open to debate)
 * perform arithmetical operations with them.
 */
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

    /**
     * Returns the currency code stored in {@link #CURRENCIES}
     * at the given index.
     *
     * @param index     Index in the array.
     * @return          Currency code.
     * @throws ArrayIndexOutOfBoundsException   If the given index
     * exceeds {@link #CURRENCIES} size.
     */
    public static String getCurrencyCode(int index) {
        return CURRENCIES[index];
    }

    /**
     * Returns the index in {@link #CURRENCIES} of the given
     * currency code. Reverse operation of {@link #getCurrencyCode(int)}.
     *
     * @param currencyCode  Currency code to search.
     * @return              Index in {@link #CURRENCIES}. -1 if no match is found.
     */
    public static int getCurrencyIndex(String currencyCode) {
        for (int i = 0; i < CURRENCIES.length; i++) {
            if (CURRENCIES[i].equals(currencyCode)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets a localized full name of the given currency code.
     *
     * @param currencyCode      Currency code to format.
     * @return                  Localized full name with the format
     *                          "CODE - Localized full name"
     * @throws GnomyCurrencyException   If the currency code is not valid or
     *                                  not included in {@link #CURRENCIES}.
     */
    public static String getDisplayName(String currencyCode) throws GnomyCurrencyException {
        Currency currency = getCurrency(currencyCode);
        String currencyName = currency.getDisplayName();

        currencyName = currencyName.substring(0, 1).toUpperCase()
                + currencyName.substring(1);

        return String.format("%s - %s", currencyCode, currencyName);
    }

    /**
     * Formats a {@link BigDecimal} value with the given currency code.
     * Useful if the local currency does not match the given code.
     *
     * @param number    Object storing the money value.
     * @param currencyCode  Currency associated with the money value.
     * @return              Java's default format for currencies for valid {@link BigDecimal}
     *                      objects. {@link #NULL_NUMBER_STRING} if number is null.
     * @throws GnomyCurrencyException   If the given currency code is not
     * a valid one of not included in {@link #CURRENCIES}.
     */
    public static String format(BigDecimal number, String currencyCode) throws GnomyCurrencyException {
        if (number == null) return NULL_NUMBER_STRING;

        Currency currency = getCurrency(currencyCode);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

        formatter.setCurrency(currency);

        return formatter.format(number);
    }

    /**
     * Returns the array of all supported currencies in a readable form.
     *
     * @return  Array of localized full-name currencies.
     * @throws GnomyCurrencyException   !!! DANGER. SHOULD NOT HAPPEN !!!
     *                                  If any of the values stored in {@link #CURRENCIES}
     *                                  is not a valid currency code supported by Java.
     */
    public static String[] getDisplayArray() throws GnomyCurrencyException {
        String[] displayArray = new String[CURRENCIES.length];

        for (int i = 0; i < CURRENCIES.length; i++) {
            String displayName = getDisplayName(CURRENCIES[i]);
            displayArray[i] = displayName;
        }

        return displayArray;
    }

    /**
     * Retrieves the {@link Currency} object that matches the
     * given currency code.
     *
     * @param currencyCode  Target currency code.
     * @return              Currency object.
     * @throws GnomyCurrencyException   If the given code is not included in
     * {@link #CURRENCIES} or (!!! DANGER !!!) it is included but is not
     * a valid Java supported currency code.
     */
    private static Currency getCurrency(String currencyCode)
            throws GnomyCurrencyException {
       if (Arrays.asList(CURRENCIES).contains(currencyCode)) {
           try {
               return Currency.getInstance(currencyCode);
           } catch(IllegalArgumentException e) {
               //  This shouldn't happen...
               throw new GnomyCurrencyException(e);
           }
       } else {
           throw new GnomyCurrencyException("Currency code is not supported.");
       }
    }

    /**
     * WARNING: NOT READY SPECS. COULD CHANGE AT ANY MOMENT.
     *
     * Returns a sum of the account accumulated balances. Used
     * in accounts fragment.
     *
     * @param forceConfirmedOnly    Tells the method if it should include
     *                              unresolved/pending transactions values
     *                              in the sum or not.
     * @param awaList               List of {@link AccountWithAccumulated} objects
     *                              that contain the pertaining historic data
     *                              of the account's balance.
     * @param baseCurrencyCode      As accounts can potentially have different
     *                              associated currencies, the final result has to
     *                              be expected to take the exchange rates into
     *                              consideration. This currency is used as a reference.
     * @return                      Value of the sum.
     */
    public static BigDecimal sumAccountAccumulates(
            boolean forceConfirmedOnly,
            List<AccountWithAccumulated> awaList,
            String baseCurrencyCode) {
        // TODO: Adjust logic when currency support is implemented
        //  This loop is here just so we can display something
        //  It's possible that the whole method (including signature) will change
        BigDecimal total = null;
        if (awaList.size() > 0) total = BigDecimal.ZERO;

        for (AccountWithAccumulated awa : awaList) {
            if (forceConfirmedOnly) {
                total = total.add(awa.getConfirmedAccumulatedBalanceAtMonth());
            } else {
                total = total.add(awa.getBalanceAtEndOfMonth());
            }
        }

        return total;
    }
}
