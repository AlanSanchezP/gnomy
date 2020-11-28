package io.github.alansanchezp.gnomy;

import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;

public class CurrencyUtilTest {
    @Test
    public void format_throws_unsupported_code_exception() {
        // Not using ErrorUtil.assertThrows() since code would be duplicated (different source-sets)
        //  and this is probably the only use of custom exceptions in plain java tests.
        //  RECONSIDER this decision if more suitable usages are found later.
        BigDecimal number = new BigDecimal("20.150");

        try {
            CurrencyUtil.format(number, "BMD");
        } catch(GnomyCurrencyException e) {
            assertEquals("Currency code is not supported.", e.getMessage());
        }
    }

    @Test
    public void format_is_correct() throws GnomyCurrencyException {
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
    }

    @Test
    public void display_name_is_correct() throws GnomyCurrencyException {
        String code = "USD";

        Locale.setDefault(new Locale("es", "es"));
        String displayName = CurrencyUtil.getDisplayName(code);
        assertEquals("USD - Dólar estadounidense", displayName);

        Locale.setDefault(new Locale("en", "uk"));
        displayName = CurrencyUtil.getDisplayName(code);
        assertEquals("USD - US Dollar",displayName);
    }

    @Test
    public void display_array_is_correct() throws GnomyCurrencyException {
        String[] codesArray = CurrencyUtil.getCurrencies();
        String[] displayArray = CurrencyUtil.getDisplayArray();

        assertEquals(codesArray.length, displayArray.length);
    }

    @Test
    public void sum_mixed_currencies() {
        // TODO: Adjust test when currency support is fully implemented
        String baseCurrency = "USD";
        List<AccountWithAccumulated> awaList = new ArrayList<>();
        AccountWithAccumulated[] awaArray = new AccountWithAccumulated[3];
        BigDecimal result;

        awaArray[0] = mock(AccountWithAccumulated.class);
        awaArray[0].account = mock(Account.class);
        when(awaArray[0].account.getDefaultCurrency()).thenReturn("USD");
        when(awaArray[0].getBalanceAtEndOfMonth()).thenReturn(BigDecimal.ONE);
        when(awaArray[0].getConfirmedAccumulatedBalanceAtMonth()).thenReturn(BigDecimal.TEN);

        awaArray[1] = mock(AccountWithAccumulated.class);
        awaArray[1].account = mock(Account.class);
        when(awaArray[1].account.getDefaultCurrency()).thenReturn("MXN");
        when(awaArray[1].getBalanceAtEndOfMonth()).thenReturn(BigDecimal.ONE);
        when(awaArray[1].getConfirmedAccumulatedBalanceAtMonth()).thenReturn(BigDecimal.TEN);

        awaArray[2] = mock(AccountWithAccumulated.class);
        awaArray[2].account = mock(Account.class);
        when(awaArray[2].account.getDefaultCurrency()).thenReturn("EUR");
        when(awaArray[2].getBalanceAtEndOfMonth()).thenReturn(BigDecimal.ONE);
        when(awaArray[2].getConfirmedAccumulatedBalanceAtMonth()).thenReturn(BigDecimal.TEN);

        result = CurrencyUtil.sumAccountAccumulates(true, awaList, baseCurrency);
        assertNull("Sum is null if list is empty", result);

        result = CurrencyUtil.sumAccountAccumulates(false, awaList, baseCurrency);
        assertNull("Sum is null if list is empty", result);

        awaList.add(awaArray[0]);
        awaList.add(awaArray[1]);
        awaList.add(awaArray[2]);
        // Sums 1 + 1 + 1
        result = CurrencyUtil.sumAccountAccumulates(false, awaList, baseCurrency);
        assertThat(new BigDecimal("3"), comparesEqualTo(result));

        // Sums 10 + 10 + 10
        result = CurrencyUtil.sumAccountAccumulates(true, awaList, baseCurrency);
        assertThat(new BigDecimal("30"), comparesEqualTo(result));

        awaList.clear();
        awaList.add(awaArray[2]);
        // Sums 1
        result = CurrencyUtil.sumAccountAccumulates(false, awaList, baseCurrency);
        assertThat(new BigDecimal("1"), comparesEqualTo(result));

        // Sums 10
        result = CurrencyUtil.sumAccountAccumulates(true, awaList, baseCurrency);
        assertThat(new BigDecimal("10"), comparesEqualTo(result));
    }
}
