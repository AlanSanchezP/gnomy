package io.github.alansanchezp.gnomy.database;

import net.sqlcipher.database.SQLiteConstraintException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.LiveDataTestUtil;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class MoneyTransactionRepositoryTest {

    private MoneyTransactionRepository repository;
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setUp() {
        MockDatabaseOperationsUtil.disableMocking();
        repository = new MoneyTransactionRepository(
                InstrumentationRegistry.getInstrumentation().getContext());
        AccountRepository accountRepository = new AccountRepository(
                InstrumentationRegistry.getInstrumentation().getContext());
        // Create a monthly balance row with predefined values.
        Account account = new Account();
        account.setDefaultCurrency("USD");
        account.setCreatedAt(DateUtil.OffsetDateTimeNow().minusMonths(2));
        accountRepository.insert(account).blockingGet();
        MonthlyBalance firstBalance = new MonthlyBalance();
        firstBalance.setAccountId(1);
        firstBalance.setDate(DateUtil.now().minusMonths(2));
        firstBalance.setTotalIncomes(BigDecimalUtil.fromString("30"));
        firstBalance.setTotalExpenses(BigDecimalUtil.fromString("172"));
        firstBalance.setProjectedIncomes(BigDecimalUtil.fromString("425"));
        firstBalance.setProjectedExpenses(BigDecimalUtil.fromString("100"));
        accountRepository.update(firstBalance).blockingGet();
        // Insert second account (for update testing)
        accountRepository.insert(account).blockingGet();
    }

    @After
    public void cleanDatabase() {
        GnomyDatabase.cleanUp();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void custom_insert_method_works() throws InterruptedException {
        MonthlyBalance resultBalance;
        // New transaction at existing monthly balance date
        MoneyTransaction testTransaction = new MoneyTransaction();
        testTransaction.setAccount(1);
        testTransaction.setCategory(1);
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(2));
        testTransaction.setCurrency("USD");
        testTransaction.setOriginalValue("567");

        // Confirmed income
        testTransaction.setType(MoneyTransaction.INCOME);
        testTransaction.setConfirmed(true);
        repository.insert(testTransaction).blockingGet();
        resultBalance = LiveDataTestUtil.getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        // 30 + 567 = 597
        assertEquals(BigDecimalUtil.fromString("597"), resultBalance.getTotalIncomes());
        // 172 (stays the same)
        assertEquals(BigDecimalUtil.fromString("172"), resultBalance.getTotalExpenses());
        // 425 (stays the same)
        assertEquals(BigDecimalUtil.fromString("425"), resultBalance.getProjectedIncomes());
        // 100 (stays the same)
        assertEquals(BigDecimalUtil.fromString("100"), resultBalance.getProjectedExpenses());

        // Confirmed expense
        testTransaction.setType(MoneyTransaction.EXPENSE);
        testTransaction.setConfirmed(true);
        repository.insert(testTransaction).blockingGet();
        resultBalance = LiveDataTestUtil.getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        // 597 (stays the same)
        assertEquals(BigDecimalUtil.fromString("597"), resultBalance.getTotalIncomes());
        // 172 + 567 = 769
        assertEquals(BigDecimalUtil.fromString("739"), resultBalance.getTotalExpenses());
        // 425 (stays the same)
        assertEquals(BigDecimalUtil.fromString("425"), resultBalance.getProjectedIncomes());
        // 100 (stays the same)
        assertEquals(BigDecimalUtil.fromString("100"), resultBalance.getProjectedExpenses());

        // Pending income
        testTransaction.setType(MoneyTransaction.INCOME);
        testTransaction.setConfirmed(false);
        repository.insert(testTransaction).blockingGet();
        resultBalance = LiveDataTestUtil.getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        // 597 (stays the same)
        assertEquals(BigDecimalUtil.fromString("597"), resultBalance.getTotalIncomes());
        // 739 (stays the same)
        assertEquals(BigDecimalUtil.fromString("739"), resultBalance.getTotalExpenses());
        // 425 + 567 = 992
        assertEquals(BigDecimalUtil.fromString("992"), resultBalance.getProjectedIncomes());
        // 100 (stays the same)
        assertEquals(BigDecimalUtil.fromString("100"), resultBalance.getProjectedExpenses());

        // Pending expense
        testTransaction.setType(MoneyTransaction.EXPENSE);
        testTransaction.setConfirmed(false);
        repository.insert(testTransaction).blockingGet();
        resultBalance = LiveDataTestUtil.getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        // 597 (stays the same)
        assertEquals(BigDecimalUtil.fromString("597"), resultBalance.getTotalIncomes());
        // 739 (stays the same)
        assertEquals(BigDecimalUtil.fromString("739"), resultBalance.getTotalExpenses());
        // 992 (stays the same)
        assertEquals(BigDecimalUtil.fromString("992"), resultBalance.getProjectedIncomes());
        // 100 + 567 = 667
        assertEquals(BigDecimalUtil.fromString("667"), resultBalance.getProjectedExpenses());

        // Insert into a different balance (same account)
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(1));
        testTransaction.setType(MoneyTransaction.INCOME);
        testTransaction.setConfirmed(true);
        repository.insert(testTransaction).blockingGet();
        // New balance is generated using provided transaction value
        resultBalance = LiveDataTestUtil.getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(1)));
        assertEquals(BigDecimalUtil.fromString("567"), resultBalance.getTotalIncomes());
        assertEquals(BigDecimalUtil.fromString("0"), resultBalance.getTotalExpenses());
        assertEquals(BigDecimalUtil.fromString("0"), resultBalance.getProjectedIncomes());
        assertEquals(BigDecimalUtil.fromString("0"), resultBalance.getProjectedExpenses());

        // Try to insert into faulty balance (invalid account id)
        testTransaction.setAccount(10);
        try {
            repository.insert(testTransaction).blockingGet();
        } catch (SQLiteConstraintException e) {
            return;
        }
        // TODO: Use fail() on all tests that require it
        fail();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void custom_update_method_works() {
        // Not testing insertOrIgnore since custom_insert_method_works() covers it
        // Attempt to update non-existing transaction
        // Insert transaction
        // Modify local object with different scenarios
        // Check involved balances
        // Test what happens with invalid accounts or categories (some error)

        assert true;
    }
}
