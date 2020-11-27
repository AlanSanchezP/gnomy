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
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;

import static io.github.alansanchezp.gnomy.LiveDataTestUtil.getOrAwaitValue;
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
        // Create an empty account
        Account account = new Account();
        account.setDefaultCurrency("USD");
        account.setCreatedAt(DateUtil.OffsetDateTimeNow().minusMonths(2));
        accountRepository.insert(account).blockingGet();

        // Update initial balance with predefined values
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
        MoneyTransaction testTransaction = getDefaultTestTransaction();

        // Confirmed income
        repository.insert(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "597", // 30 + 567 = 597
                "172", // (stays the same)
                "425", // (stays the same)
                "100"); // (stays the same)

        // Confirmed expense
        testTransaction.setType(MoneyTransaction.EXPENSE);
        testTransaction.setConfirmed(true);
        repository.insert(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "597", // (stays the same)
                "739", // 172 + 567 = 769
                "425", // (stays the same)
                "100"); // (stays the same)

        // Pending income
        testTransaction.setType(MoneyTransaction.INCOME);
        testTransaction.setConfirmed(false);
        repository.insert(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "597", // (stays the same)
                "739", // (stays the same)
                "992", // 425 + 567 = 992
                "100"); // (stays the same)

        // Pending expense
        testTransaction.setType(MoneyTransaction.EXPENSE);
        testTransaction.setConfirmed(false);
        repository.insert(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "597", // (stays the same)
                "739", // (stays the same)
                "992", // (stays the same)
                "667"); // 100 + 567 = 667

        // Insert into a different balance (same account)
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(1));
        testTransaction.setType(MoneyTransaction.INCOME);
        testTransaction.setConfirmed(true);
        repository.insert(testTransaction).blockingGet();
        // New balance is generated using provided transaction value
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(1)));
        testResultBalance(resultBalance,
                "567", // (transaction value)
                "0", // (initial value)
                "0", // (initial value)
                "0"); // (initial value)

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

    private void testResultBalance(MonthlyBalance resultBalance,
                                  String expectedTotalIncomesString,
                                  String expectedTotalExpensesString,
                                  String expectedProjectedIncomesString,
                                  String expectedProjectedExpensesString) {
        assertEquals(BigDecimalUtil.fromString(expectedTotalIncomesString), resultBalance.getTotalIncomes());
        assertEquals(BigDecimalUtil.fromString(expectedTotalExpensesString), resultBalance.getTotalExpenses());
        assertEquals(BigDecimalUtil.fromString(expectedProjectedIncomesString), resultBalance.getProjectedIncomes());
        assertEquals(BigDecimalUtil.fromString(expectedProjectedExpensesString), resultBalance.getProjectedExpenses());
    }

    private MoneyTransaction getDefaultTestTransaction() {
        MoneyTransaction testTransaction = new MoneyTransaction();
        testTransaction.setAccount(1);
        testTransaction.setCategory(1);
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(2));
        testTransaction.setCurrency("USD");
        testTransaction.setOriginalValue("567");
        testTransaction.setType(MoneyTransaction.INCOME);
        testTransaction.setConfirmed(true);
        return testTransaction;
    }
}
