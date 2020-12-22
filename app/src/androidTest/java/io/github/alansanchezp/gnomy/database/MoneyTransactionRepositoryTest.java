package io.github.alansanchezp.gnomy.database;

import net.sqlcipher.database.SQLiteConstraintException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZoneOffset;
import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionFilters;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.github.alansanchezp.gnomy.database.transaction.TransactionDisplayData;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;

import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertThrows;
import static io.github.alansanchezp.gnomy.LiveDataTestUtil.getOrAwaitValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MoneyTransactionRepositoryTest {

    private MoneyTransactionRepository repository;
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setUp() {
        // Fixing test clock to avoid changes in dates when calling DateUtil.OffsetDateTimeNow()
        DateUtil.setFixedClockAtTime("2018-01-08T15:34:42.00Z", ZoneOffset.UTC);
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
        // Insert third account (for filter testing)
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
        repository.insert(testTransaction).blockingGet();  // MB Expenses: 192; MB(2) Incomes +10

        repository.delete(1).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "30", // 40 - 10 = 30
                "192", // (stays the same)
                "425", // (stays the same)
                "100"); // (stays the same)

        repository.delete(2).blockingGet();
        resultBalance = getOrAwaitValue(
                repository.getBalanceFromMonth(1, DateUtil.now().minusMonths(2)));
        testResultBalance(resultBalance,
                "30", // (stays the same)
                "182", // 192 - 10 = 182
                "425", // (stays the same)
                "100"); // (stays the same)

        // Mirror transfer: Expecting error
        assertThrows(GnomyIllegalQueryException.class, () ->
                repository.delete(3).blockingGet());

        // Transfer has a +1 id from its mirrored version
        repository.delete(4).blockingGet();
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void getByFilters_works() throws InterruptedException {
        // TODO: Complement with more categories once CategoryRepository is ready
        /*
         * This test will insert several (15) money transaction rows and
         * test MoneyTransactionRepository.getByFilters() with different
         * values for each of the 10 fields that compose MoneyTransactionFilters.
         * The distribution of these money transaction instances will be:
         *
         * 1:   Confirmed income, 2 months before now, account 1, original value 10
         * 2:   Confirmed income, 2 months 1 second before now, account 1, original value 20
         * 3:   Unconfirmed income, 1 months before now, account 1, original value 30
         * 4:   Confirmed expense, 1 months after now, account 2, original value 40
         * 5:   Confirmed expense, 1 months after now, account 1, original value 50
         * 6:   Unconfirmed expense, 2 months 1 second after now, account 2, original value 60
         * 7:   Transfer, 2 months before now, from account 1 to account 2, original value 70
         * 8:   Transfer, 2 months before now, from account 2 to account 1, original value 80
         * 9:   Transfer, 2 months before now, from account 2 to account 1, original value 90
         * 10:  Transfer, 1 months before now, from account 3 to account 2, original value 100
         * 11:  Transfer, 1 months before now, from account 2 to account 3, original value 110
         * 12:  Confirmed income, this day, account 2, original value 120
         * 13:  Confirmed expense, this day, account 2, original value 130
         * 14:  Unconfirmed income, 2 months after now, account 2, original value 140
         * 15:  Unconfirmed expense, 2 months after now, account 2, original value 150
         *
         * */

        MoneyTransaction testTransaction = getDefaultTestTransaction();
        testTransaction.setOriginalValue("10");
        repository.insert(testTransaction).blockingGet(); // 1

        testTransaction.setOriginalValue("20");
        // Ensure this transaction is the LEAST recent one
        testTransaction.setDate(testTransaction.getDate().minusSeconds(1));
        repository.insert(testTransaction).blockingGet(); // 2

        testTransaction.setConfirmed(false);
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(1));
        testTransaction.setOriginalValue("30");
        repository.insert(testTransaction).blockingGet(); // 3

        testTransaction.setConfirmed(true);
        testTransaction.setType(MoneyTransaction.EXPENSE);
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().plusMonths(1));
        testTransaction.setAccount(2);
        testTransaction.setOriginalValue("40");
        repository.insert(testTransaction).blockingGet(); // 4

        testTransaction.setAccount(1);
        testTransaction.setOriginalValue("50");
        repository.insert(testTransaction).blockingGet(); // 5

        testTransaction.setConfirmed(false);
        // Ensure this transaction is the MOST recent one
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().plusMonths(2).plusSeconds(1));
        testTransaction.setAccount(2);
        testTransaction.setOriginalValue("60");
        repository.insert(testTransaction).blockingGet(); // 6

        testTransaction.setConfirmed(true);
        testTransaction.setType(MoneyTransaction.TRANSFER);
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(2));
        testTransaction.setAccount(1);
        testTransaction.setTransferDestinationAccount(2);
        testTransaction.setOriginalValue("70");
        repository.insert(testTransaction).blockingGet(); // 7

        testTransaction.setAccount(2);
        testTransaction.setTransferDestinationAccount(1);
        testTransaction.setOriginalValue("80");
        repository.insert(testTransaction).blockingGet(); // 8

        testTransaction.setOriginalValue("90");
        repository.insert(testTransaction).blockingGet(); // 9

        testTransaction.setDate(DateUtil.OffsetDateTimeNow().minusMonths(1));
        testTransaction.setAccount(3);
        testTransaction.setTransferDestinationAccount(2);
        testTransaction.setOriginalValue("100");
        repository.insert(testTransaction).blockingGet(); // 10

        testTransaction.setAccount(2);
        testTransaction.setTransferDestinationAccount(3);
        testTransaction.setOriginalValue("110");
        repository.insert(testTransaction).blockingGet(); // 11

        testTransaction.setTransferDestinationAccount(null);
        testTransaction.setType(MoneyTransaction.INCOME);
        testTransaction.setDate(DateUtil.OffsetDateTimeNow());
        testTransaction.setOriginalValue("120");
        repository.insert(testTransaction).blockingGet(); // 12

        testTransaction.setType(MoneyTransaction.EXPENSE);
        testTransaction.setOriginalValue("130");
        repository.insert(testTransaction).blockingGet(); // 13

        testTransaction.setConfirmed(false);
        testTransaction.setType(MoneyTransaction.INCOME);
        testTransaction.setDate(DateUtil.OffsetDateTimeNow().plusMonths(2));
        testTransaction.setOriginalValue("140");
        repository.insert(testTransaction).blockingGet(); // 14

        testTransaction.setType(MoneyTransaction.EXPENSE);
        testTransaction.setOriginalValue("150");
        repository.insert(testTransaction).blockingGet(); // 15

        MoneyTransactionFilters filters = new MoneyTransactionFilters();
        List<TransactionDisplayData> results;
        // Default settings: should return ALL transactions (except mirror transfers)
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(15, results.size());

        // Returns empty list if NO_STATUS is sent
        filters.setTransactionStatus(MoneyTransactionFilters.NO_STATUS);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertTrue(results.isEmpty());
        filters.setTransactionStatus(MoneyTransactionFilters.ANY_STATUS); // Back to default

        // Rejects non-transfers with destination account
        filters.setTransferDestinationAccountId(2);
        assertThrows(RuntimeException.class, () -> repository.getByFilters(filters));

        // Rejects if origin and destination accounts are the same
        filters.setAccountId(2);
        filters.setTransactionType(MoneyTransaction.TRANSFER);
        assertThrows(RuntimeException.class, () -> repository.getByFilters(filters));
        filters.setTransferDestinationAccountId(0); // Back to default
        filters.setAccountId(0); // Back to default
        filters.setTransactionType(MoneyTransactionFilters.ANY_STATUS); // Back to default

        // Testing sorting strategy
        results = getOrAwaitValue(repository.getByFilters(filters)); // default is MOST_RECENT
        assertEquals(BigDecimalUtil.fromString("60"), results.get(0).transaction.getOriginalValue());
        assertEquals(BigDecimalUtil.fromString("20"), results.get(14).transaction.getOriginalValue());

        filters.setSortingMethod(MoneyTransactionFilters.LEAST_RECENT);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(BigDecimalUtil.fromString("20"), results.get(0).transaction.getOriginalValue());
        assertEquals(BigDecimalUtil.fromString("60"), results.get(14).transaction.getOriginalValue());

        // Tests by account id
        filters.setAccountId(1);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(5, results.size());

        filters.setAccountId(2);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(9, results.size());

        filters.setAccountId(3);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(1, results.size());
        filters.setAccountId(0); // Back to default

        // Test by transaction type
        filters.setTransactionType(MoneyTransaction.INCOME);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(5, results.size());

        filters.setTransactionType(MoneyTransaction.EXPENSE);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(5, results.size());

        filters.setTransactionType(MoneyTransaction.TRANSFER);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(5, results.size());

        // Transfers by destination
        filters.setTransferDestinationAccountId(1);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(2, results.size());

        filters.setTransferDestinationAccountId(2);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(2, results.size());

        filters.setTransferDestinationAccountId(3);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(1, results.size());

        // Transfers by both accounts
        filters.setAccountId(2);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(1, results.size()); // 2 -> 3
        filters.setTransferDestinationAccountId(1);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(2, results.size()); // 2 -> 1

        filters.setAccountId(3);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertTrue(results.isEmpty()); // 3 -> 1
        filters.setTransferDestinationAccountId(2);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(1, results.size()); // 3 -> 2

        filters.setAccountId(1);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(1, results.size()); // 1 -> 2
        filters.setTransferDestinationAccountId(3);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertTrue(results.isEmpty()); // 1 -> 3
        filters.setAccountId(0); // Back to default
        filters.setTransferDestinationAccountId(0); // Back to default
        filters.setTransactionType(MoneyTransactionFilters.ALL_TRANSACTION_TYPES); // Back to default

        // Filter by status
        filters.setTransactionStatus(MoneyTransactionFilters.CONFIRMED_STATUS);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(11, results.size());

        filters.setTransactionStatus(MoneyTransactionFilters.UNCONFIRMED_STATUS);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(4, results.size());
        filters.setTransactionStatus(MoneyTransactionFilters.ANY_STATUS); // Back to default

        // Filter by date
        filters.setMonth(DateUtil.now().minusMonths(2));
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(5, results.size());

        filters.setMonth(DateUtil.now().minusMonths(1));
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(3, results.size());

        filters.setMonth(DateUtil.now());
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(2, results.size());

        filters.setMonth(DateUtil.now().plusMonths(1));
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(2, results.size());

        filters.setMonth(DateUtil.now().plusMonths(2));
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(3, results.size());

        filters.setStartDate(DateUtil.OffsetDateTimeNow().minusMonths(1)
                .minusMinutes(1)); // Not sure why, but it's necessary to adjust this
        filters.setEndDate(DateUtil.OffsetDateTimeNow().plusMonths(1));
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(7, results.size()); // Custom date range

        filters.setStartDate(null);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(12, results.size()); // All until +1 months

        filters.setStartDate(DateUtil.OffsetDateTimeNow().minusMonths(1)
                .minusMinutes(1)); // Not sure why, but it's necessary to adjust this
        filters.setEndDate(null);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(10, results.size()); // All from -1 months
        filters.setStartDate(null); // Back to default

        // Test filter by amount
        filters.setMinAmount("50");
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(11, results.size());

        filters.setMaxAmount("130");
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(9, results.size());

        filters.setMinAmount(null);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(13, results.size());
        filters.setMaxAmount(null); // Back to default

        /*
        * Now we test a few combinations of filters:
        * 1) Unconfirmed incomes in a range of [-2 months, now]
        * 2) Confirmed incomes until now
        * 3) Transfers from 1 to 2 of at least $90
        * 4) Transfers from 2 to 1 of at most $80
        * 5) Unconfirmed expenses from account 1,
        *     ranging (date) from [-1 months, +2 months], amount range [20, 70]
        * */

        filters.setTransactionStatus(MoneyTransactionFilters.UNCONFIRMED_STATUS);
        filters.setTransactionType(MoneyTransaction.INCOME);
        filters.setStartDate(DateUtil.OffsetDateTimeNow().minusMonths(2));
        filters.setEndDate(DateUtil.OffsetDateTimeNow());
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(1, results.size());

        filters.setTransactionStatus(MoneyTransactionFilters.CONFIRMED_STATUS);
        filters.setStartDate(null);
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(3, results.size());

        filters.setEndDate(null);
        filters.setTransactionType(MoneyTransaction.TRANSFER);
        filters.setTransactionStatus(MoneyTransactionFilters.ANY_STATUS);
        filters.setAccountId(1);
        filters.setTransferDestinationAccountId(2);
        filters.setMinAmount("90");
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertTrue(results.isEmpty());

        filters.setAccountId(2);
        filters.setTransferDestinationAccountId(1);
        filters.setMinAmount(null);
        filters.setMaxAmount("80");
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertEquals(1, results.size());

        filters.setAccountId(1);
        filters.setTransferDestinationAccountId(0);
        filters.setTransactionStatus(MoneyTransactionFilters.UNCONFIRMED_STATUS);
        filters.setTransactionType(MoneyTransaction.EXPENSE);
        filters.setStartDate(DateUtil.OffsetDateTimeNow().minusMonths(1));
        filters.setEndDate(DateUtil.OffsetDateTimeNow().plusMonths(2));
        filters.setMinAmount("20");
        filters.setMaxAmount("70");
        results = getOrAwaitValue(repository.getByFilters(filters));
        assertTrue(results.isEmpty());
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
