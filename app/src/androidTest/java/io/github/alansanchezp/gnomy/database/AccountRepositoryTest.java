package io.github.alansanchezp.gnomy.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import io.github.alansanchezp.gnomy.LiveDataTestUtil;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class AccountRepositoryTest {
    // TODO: Evaluate if should be using DAOs directly instead
    private AccountRepository repository;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        MockDatabaseOperationsUtil.disableMocking();
        repository = new AccountRepository(
                InstrumentationRegistry.getInstrumentation().getContext());
    }

    @After
    public void cleanDatabase() {
        GnomyDatabase.cleanUp();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void generates_first_monthly_balance() throws InterruptedException {
        // In-memory database is empty before this test
        assertNull(LiveDataTestUtil.getOrAwaitValue(
                repository.getAccount(1)));
        assertNull(LiveDataTestUtil.getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now())));

        Account account = new Account();
        repository.insert(account).blockingGet();

        // Actual assertion
        assertNotNull(LiveDataTestUtil.getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now())));

        account.setCreatedAt(DateUtil.OffsetDateTimeNow().minusMonths(2));
        repository.insert(account).blockingGet();

        assertNull(LiveDataTestUtil.getOrAwaitValue(
                repository.getBalanceFromMonth(2, DateUtil.now())));

        assertNotNull(LiveDataTestUtil.getOrAwaitValue(
                repository.getBalanceFromMonth(2, DateUtil.now().minusMonths(2))));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void custom_update_method_works() {
        Account account = new Account();
        account.setName("Test account");
        account.setDefaultCurrency(CurrencyUtil.getCurrencyCode(1));
        account.setBackgroundColor(ColorUtil.getRandomColor());
        account.setType(Account.INFORMAL);

        Long result = repository.insert(account).blockingGet();
        account.setId((int)(long)result);
        assertEquals(Integer.valueOf(0), repository.update(account).blockingGet());

        try {
            account.setId(3);
            repository.update(account).blockingGet();
            assert false;
        } catch (GnomyIllegalQueryException e) {
            // Didn't update as there is no matching account
            assert true;
        }

        try {
            account.setId((int)(long)result);
            account.setDefaultCurrency(CurrencyUtil.getCurrencyCode(0));
            repository.update(account).blockingGet();
            assert false;
        } catch (GnomyIllegalQueryException e) {
            // Didn't update as there are conflicts
            assert true;
        }

        // Reset to avoid exceptions
        account.setId((int)(long)result);
        account.setDefaultCurrency(CurrencyUtil.getCurrencyCode(1));
        // Change to avoid a matching equals() call
        account.setName("Other name");
        assertEquals(Integer.valueOf(1), repository.update(account).blockingGet());
    }

    // TESTS FOR MonthlyBalanceDAO

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void filters_AccountWithBalance_by_month() throws InterruptedException {
        // TODO: Really make sure this test and the one below it are satisfying
        //  all possible cases of the massive query. The fact that it didn't throw any
        //  assertion error at all after the refactor bothers me a bit
        Account account = new Account();
        account.setInitialValue("50");
        repository.insert(account).blockingGet();
        account.setCreatedAt(DateUtil.OffsetDateTimeNow().minusMonths(1));
        account.setInitialValue("70");
        repository.insert(account).blockingGet();
        account.setCreatedAt(DateUtil.OffsetDateTimeNow().minusMonths(2));
        account.setInitialValue("80");
        repository.insert(account).blockingGet();
        account.setArchived(true);
        repository.insert(account).blockingGet();

        List<AccountWithBalance> todayResults = LiveDataTestUtil.getOrAwaitValue(
                repository.getAllFromMonth(DateUtil.now()));
        List<AccountWithBalance> minus1MonthResults = LiveDataTestUtil.getOrAwaitValue(
                repository.getAllFromMonth(DateUtil.now().minusMonths(1)));
        List<AccountWithBalance> minus2MonthsResults = LiveDataTestUtil.getOrAwaitValue(
                repository.getAllFromMonth(DateUtil.now().minusMonths(2)));

        assertEquals(3, todayResults.size());
        assertThat(new BigDecimal("50"), comparesEqualTo(todayResults.get(0).endOfMonthBalance));
        assertThat(new BigDecimal("50"), comparesEqualTo(todayResults.get(0).currentBalance));
        assertThat(new BigDecimal("70"), comparesEqualTo(todayResults.get(1).endOfMonthBalance));
        assertThat(new BigDecimal("70"), comparesEqualTo(todayResults.get(1).currentBalance));
        assertThat(new BigDecimal("80"), comparesEqualTo(todayResults.get(2).endOfMonthBalance));
        assertThat(new BigDecimal("80"), comparesEqualTo(todayResults.get(2).currentBalance));

        assertEquals(3, minus1MonthResults.size());
        assertNull(minus1MonthResults.get(0).endOfMonthBalance);
        assertThat(new BigDecimal("50"), comparesEqualTo(minus1MonthResults.get(0).currentBalance));
        assertThat(new BigDecimal("70"), comparesEqualTo(minus1MonthResults.get(1).endOfMonthBalance));
        assertThat(new BigDecimal("70"), comparesEqualTo(minus1MonthResults.get(1).currentBalance));
        assertThat(new BigDecimal("80"), comparesEqualTo(minus1MonthResults.get(2).endOfMonthBalance));
        assertThat(new BigDecimal("80"), comparesEqualTo(minus1MonthResults.get(2).currentBalance));

        assertEquals(3, minus2MonthsResults.size());
        assertNull(minus2MonthsResults.get(0).endOfMonthBalance);
        assertThat(new BigDecimal("50"), comparesEqualTo(minus2MonthsResults.get(0).currentBalance));
        assertNull(minus2MonthsResults.get(1).endOfMonthBalance);
        assertThat(new BigDecimal("70"), comparesEqualTo(minus2MonthsResults.get(1).currentBalance));
        assertThat(new BigDecimal("80"), comparesEqualTo(minus2MonthsResults.get(2).endOfMonthBalance));
        assertThat(new BigDecimal("80"), comparesEqualTo(minus2MonthsResults.get(2).currentBalance));

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void returns_correct_accumulated_balance() throws InterruptedException {
        Account account = new Account();
        account.setInitialValue("40");
        repository.insert(account).blockingGet();

        assertThat(new BigDecimal("40"), comparesEqualTo(LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatedFromMonth(1, DateUtil.now()))));
        assertNull(LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatedFromMonth(1, DateUtil.now().minusMonths(1))));
        assertNull(LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatedFromMonth(1, DateUtil.now().minusMonths(2))));
    }


}
