package io.github.alansanchezp.gnomy;

import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;
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

            currency = "MXN";
            assertEquals(CurrencyUtil.NULL_NUMBER_STRING, CurrencyUtil.format(null, currency));
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

    @Test
    public void sum_mixed_currencies() {
        // TODO: Adjust test when currency support is fully implemented
        String baseCurrency = "USD";
        List<AccountWithBalance> awbList = new ArrayList<>();
        AccountWithBalance[] awbArray = new AccountWithBalance[3];
        BigDecimal[] results;

        awbArray[0] = new AccountWithBalance();
        awbArray[0].account = new Account();
        awbArray[0].account.setDefaultCurrency("USD");
        awbArray[0].currentBalance = new BigDecimal("710");
        awbArray[0].endOfMonthBalance = new BigDecimal("401.50");

        awbArray[1] = new AccountWithBalance();
        awbArray[1].account = new Account();
        awbArray[1].account.setDefaultCurrency("MXN");
        awbArray[1].currentBalance = new BigDecimal("812.23");
        awbArray[1].endOfMonthBalance = new BigDecimal("600");

        awbArray[2] = new AccountWithBalance();
        awbArray[2].account = new Account();
        awbArray[2].account.setDefaultCurrency("EUR");
        awbArray[2].currentBalance = new BigDecimal("409.8");
        awbArray[2].endOfMonthBalance = new BigDecimal("100.01");

        results = CurrencyUtil.sumAccountListBalances(awbList, baseCurrency);
        assertEquals(new BigDecimal("0"), results[0]);
        assertNull("Projected balance is null if list is empty", results[1]);

        awbList.add(awbArray[0]);
        awbList.add(awbArray[1]);
        awbList.add(awbArray[2]);
        results = CurrencyUtil.sumAccountListBalances(awbList, baseCurrency);

        assertEquals(new BigDecimal("1932.03"), results[0]);
        assertEquals(new BigDecimal("1101.51"), results[1]);

        awbArray[2].currentBalance = null;
        awbArray[2].endOfMonthBalance = null;
        awbList.clear();
        awbList.add(awbArray[2]);
        results = CurrencyUtil.sumAccountListBalances(awbList, baseCurrency);

        assertEquals(new BigDecimal("0"), results[0]);
        assertNull("Projected balance is null if individual " +
                "balances are null", results[1]);

    }
}
