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

import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertThrows;
import static io.github.alansanchezp.gnomy.LiveDataTestUtil.getOrAwaitValue;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MoneyTransactionRepositoryTest {

    private MoneyTransactionRepository repository;
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setUp() {
        // Fixing test clock to avoid changes in dates when calling DateUtil.OffsetDateTimeNow()
        DateUtil.setFixedClockAtTime("2018-01-08T15:34:42.00Z");
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

        // New transfer
        //  Assert two monthly balances with equivalent changes in incomes and expenses
        testTransaction.setType(MoneyTransaction.TRANSFER);
        // Null destination account throws error if transfer
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.insert(testTransaction).blockingGet());
        // Unconfirmed transfer throws error
        testTransaction.setConfirmed(false);
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.insert(testTransaction).blockingGet());
        // Throws error if destination and origin accounts are the same
        testTransaction.setConfirmed(true);
        testTransaction.setTransferDestinationAccount(1);
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.insert(testTransaction).blockingGet());

        // Throws error if non-transfer has destination account id
        testTransaction.setType(MoneyTransaction.INCOME);
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.insert(testTransaction).blockingGet());

        // Throws error if attempting to directly insert a mirror transfer
        testTransaction.setType(4);
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.insert(testTransaction).blockingGet());

        // Throws error if transfer is future
        testTransaction.setType(MoneyTransaction.TRANSFER);
        testTransaction.setTransferDestinationAccount(2);
        // Not able to test using plusNanos() since transactions
        // are only stored with millisecond precision
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().plusSeconds(1));
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.insert(testTransaction).blockingGet());

        // Effective insertion
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(1));
        testTransaction.setConfirmed(true);
        testTransaction.setOriginalValue("30");
        repository.insert(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(1)));
        MonthlyBalance destinationBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(2, DateUtil.now().minusMonths(1)));
        testResultBalance(resultBalance,
                "567", // (stays the same)
                "30", // 0 + 30 = 30
                "0", // (stays the same)
                "0"); // (stays the same)
        testResultBalance(destinationBalance,
                "30", // 0 + 30 = 30
                "0", // (stays the same)
                "0", // (stays the same)
                "0"); // (stays the same)
        // Try to insert into faulty balance (invalid account id)
        testTransaction.setAccount(10);
        assertThrows(SQLiteConstraintException.class,
                () -> repository.insert(testTransaction).blockingGet());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void custom_update_method_works() throws InterruptedException {
        MonthlyBalance resultBalance,
                        secondaryResultBalance;
        // Not testing insertOrIgnore since custom_insert_method_works() covers it
        // Not testing addTransactionAmountToBalance since custom_insert_method_works() covers it
        MoneyTransaction testTransaction = getDefaultTestTransaction();
        testTransaction.setId(0);
        testTransaction.setOriginalValue("10");
        // Attempt to update non-existing transaction
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.update(testTransaction).blockingGet());
        // Insert transaction
        repository.insert(testTransaction).blockingGet();
        testTransaction.setType(MoneyTransaction.TRANSFER);
        testTransaction.setDate(DateUtil.OffsetDateTimeNow());
        testTransaction.setTransferDestinationAccount(2);
        // Insert transfer. Two transactions will be generated, with Ids 3 and 2 (mirror is inserted first)
        //  Using a different month to avoid conflicts with other assertions
        repository.insert(testTransaction).blockingGet();
        // Return to default state
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(2));
        testTransaction.setId(1);
        testTransaction.setTransferDestinationAccount(null);
        /* At this point, monthly balance values are:
              Total incomes: 40 (30 + 10)
              Total expenses: 172
              Projected incomes: 425
              Projected expenses: 100 */

        // Cannot change transaction type
        testTransaction.setType(MoneyTransaction.EXPENSE);
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.update(testTransaction).blockingGet());
        testTransaction.setType(MoneyTransaction.INCOME);

        // Alters amount but keeps account and date intact
        testTransaction.setOriginalValue("20");
        repository.update(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "50", // 40 - 10 + 20
                "172", // (stays the same)
                "425", // (stays the same)
                "100"); // (stays the same)
        // From confirmed to unconfirmed
        testTransaction.setConfirmed(false);
        repository.update(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "30", // 50 - 20
                "172", // (stays the same)
                "445", // 425 + 20
                "100"); // (stays the same)

        // From unconfirmed to confirmed (have to check this case too)
        testTransaction.setConfirmed(true);
        repository.update(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "50", // 30 + 20
                "172", // (stays the same)
                "425", // 425 - 20
                "100"); // (stays the same)

        // Changes date
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(1));
        repository.update(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        secondaryResultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(1)));
        testResultBalance(resultBalance,
                "30", // 50 - 20
                "172", // (stays the same)
                "425", // (stays the same)
                "100"); // (stays the same)
        testResultBalance(secondaryResultBalance,
                "20", // 0 + 20
                "0", // (initial value)
                "0", // (initial value)
                "0"); // (initial value)

        // Changes account
        testTransaction.setAccount(2);
        repository.update(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(1)));
        secondaryResultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(2, DateUtil.now().minusMonths(1)));
        testResultBalance(resultBalance,
                "0", // 20 - 20
                "0", // (stays the same)
                "0", // (stays the same)
                "0"); // (stays the same)
        testResultBalance(secondaryResultBalance,
                "20", // 0 + 20
                "0", // (initial value)
                "0", // (initial value)
                "0"); // (initial value)

        // Changes account AND month. New MonthlyBalance = first evaluated one
        // This is done in order to use at least one non-zero value on the results
        testTransaction.setAccount(1);
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(2));
        repository.update(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(2, DateUtil.now().minusMonths(1)));
        secondaryResultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "0", // 20 - 20
                "0", // (stays the same)
                "0", // (stays the same)
                "0"); // (stays the same)
        testResultBalance(secondaryResultBalance,
                "50", // 30 + 20
                "172", // (stays the same)
                "425", // (stays the same)
                "100"); // (stays the same)

        // All amount, status, date and account change
        testTransaction.setOriginalValue("30");
        testTransaction.setAccount(2);
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(1));
        testTransaction.setConfirmed(false);
        repository.update(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        secondaryResultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(2, DateUtil.now().minusMonths(1)));
        testResultBalance(resultBalance,
                "30", // 50 - 20
                "172", // (stays the same)
                "425", // (stays the same)
                "100"); // (stays the same)
        testResultBalance(secondaryResultBalance,
                "0", // 20 - 20
                "0", // (stays the same)
                "30", // 0 + 30
                "0"); // (stays the same)

        // Tying to move into invalid account
        testTransaction.setAccount(10);
        assertThrows(SQLiteConstraintException.class,
                () -> repository.update(testTransaction).blockingGet());
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(2, DateUtil.now().minusMonths(1)));
        testResultBalance(resultBalance,
                "0", // (stays the same)
                "0", // (stays the same)
                "30", // (stays the same)
                "0"); // (stays the same)

        // TODO: Test currency changes
        // Test what happens with invalid accounts or categories (some error)
        // Update transfer
        testTransaction.setAccount(2);
        testTransaction.setTransferDestinationAccount(1);
        // Cannot update mirror transfers (autogenerated mirror transfer ID should be 2)
        testTransaction.setType(4);
        testTransaction.setId(2);
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.update(testTransaction).blockingGet());
        // Cannot update non-null destination account on incomes and expenses
        testTransaction.setId(1);
        testTransaction.setType(MoneyTransaction.INCOME);
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.update(testTransaction).blockingGet());

        testTransaction.setType(MoneyTransaction.TRANSFER);
        testTransaction.setId(3);
        // Cannot update transfer as not confirmed
        testTransaction.setConfirmed(false);
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.update(testTransaction).blockingGet());
        testTransaction.setConfirmed(true);
        // Cannot update null destination on transfers
        testTransaction.setTransferDestinationAccount(null);
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.update(testTransaction).blockingGet());
        // Throws error if transfer is future
        testTransaction.setTransferDestinationAccount(1);
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().plusSeconds(1));
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.update(testTransaction).blockingGet());

        /* Previous balance states are
            Origin balance                          Destination balance
            Incomes: 0                              Incomes: 10
            Expenses: 10                            Expenses: 0
            ProjectedIncomes: 0                     ProjectedIncomes: 0
            ProjectedExpenses: 0                    ProjectedExpenses: 0
         */
        // Actual transfer update. Changing both amount and swapping accounts
        testTransaction.setOriginalValue("50");
        testTransaction.setDate(DateUtil.OffsetDateTimeNow());
        repository.update(testTransaction).blockingGet();
        // Using only two balance instances for now
        // resultBalance: Origin (transfer from)
        // secondaryResultBalance: Destination (transfer to)
        resultBalance = getOrAwaitValue(repository.getBalanceFromMonth(2, DateUtil.now()));
        secondaryResultBalance = getOrAwaitValue(repository.getBalanceFromMonth(1, DateUtil.now()));
        testResultBalance(resultBalance,
                "0", //  10 - 10 = 0
                "50", // 0 + 50 = 0
                "0", // (stays the same)
                "0"); // (stays the same)
        testResultBalance(secondaryResultBalance,
                "50", // 0 + 50 = 0
                "0", // 10 - 10 = 0
                "0", // (stays the same)
                "0"); // (stays the same)

        // Altering 4 balances: Changing transfer date
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(1));
        repository.update(testTransaction).blockingGet();
        MonthlyBalance originalOriginBalance, originalDestinationBalance,
                newOriginBalance, newDestinationBalance;
        originalOriginBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(2, DateUtil.now()));
        newOriginBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(2, DateUtil.now().minusMonths(1)));
        originalDestinationBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now()));
        newDestinationBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(1)));

        testResultBalance(originalOriginBalance,
                "0", //  (stays the same)
                "0", // 50 - 50 = 0
                "0", // (stays the same)
                "0"); // (stays the same)
        testResultBalance(originalDestinationBalance,
                "0", // 50 - 50 = 0
                "0", // (stays the same)
                "0", // (stays the same)
                "0"); // (stays the same)
        testResultBalance(newOriginBalance,
                "0", //  (stays the same)
                "50", // 0 + 50 = 50
                "30", // (stays the same)
                "0"); // (stays the same)
        testResultBalance(newDestinationBalance,
                "50", // 0 + 50 = 50
                "0", // (stays the same)
                "0", // (stays the same)
                "0"); // (stays the same)
        assert true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void custom_delete_method_works() throws InterruptedException {
        // Default AccountA monthly balance at minus 2 months
        //  Incomes             30
        //  Expenses            172
        //  ProjectedIncomes    425
        //  ProjectedExpenses   100
        // Default AccountB monthly balance at minus 2 months is all zeroes
        MonthlyBalance resultBalance, mirrorResultBalance;
        MoneyTransaction testTransaction = getDefaultTestTransaction();
        testTransaction.setOriginalValue("10");
        // Inserting all 3 types of transaction
        repository.insert(testTransaction).blockingGet(); // MB incomes: 40

        testTransaction.setType(MoneyTransaction.EXPENSE);
        repository.insert(testTransaction).blockingGet(); // MB Expenses: 182

        testTransaction.setType(MoneyTransaction.TRANSFER);
        testTransaction.setTransferDestinationAccount(2);

        // Manually altering ids so that Room can recognize each transaction
        testTransaction.setId(1);
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "30", // 40 - 10 = 30
                "192", // (stays the same)
                "425", // (stays the same)
                "100"); // (stays the same)

        testTransaction.setId(2);
        repository.delete(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "30", // (stays the same)
                "182", // 192 - 10 = 182
                "425", // (stays the same)
                "100"); // (stays the same)

        testTransaction.setId(3); // Mirror transfer: Expecting error
        assertThrows(GnomyIllegalQueryException.class, () ->
                repository.delete(testTransaction).blockingGet());

        testTransaction.setId(4); // Transfer has a +1 id from its mirrored version
        repository.delete(testTransaction).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        mirrorResultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(2, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "30", // (stays the same)
                "172", // 182 - 10 = 172
                "425", // (stays the same)
                "100"); // (stays the same)
        testResultBalance(mirrorResultBalance,
                "0", // 10 - 10 = 0
                "0", // (stays the same)
                "0", // (stays the same)
                "0"); // (stays the same)
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
        testTransaction.setConcept("Test transaction");
        testTransaction.setType(MoneyTransaction.INCOME);
        testTransaction.setConfirmed(true);
        return testTransaction;
    }
}
