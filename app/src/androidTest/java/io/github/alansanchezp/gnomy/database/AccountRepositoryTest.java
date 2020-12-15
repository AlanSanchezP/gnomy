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
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;

import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertThrows;
import static io.github.alansanchezp.gnomy.LiveDataTestUtil.getOrAwaitValue;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class AccountRepositoryTest {

    private AccountRepository repository;
    private MoneyTransactionRepository transactionRepository;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        repository = new AccountRepository(
                InstrumentationRegistry.getInstrumentation().getContext());
        transactionRepository = new MoneyTransactionRepository(
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

        account.setId(3);
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.update(account).blockingGet());

        account.setId((int)(long)result);
        account.setDefaultCurrency(CurrencyUtil.getCurrencyCode(0));
        assertThrows(GnomyIllegalQueryException.class,
                () -> repository.update(account).blockingGet());

        // Reset to avoid exceptions
        account.setId((int)(long)result);
        account.setDefaultCurrency(CurrencyUtil.getCurrencyCode(1));
        // Change to avoid a matching equals() call
        account.setName("Other name");
        assertEquals(Integer.valueOf(1), repository.update(account).blockingGet());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void custom_delete_method_works() throws InterruptedException {
        // Will create three accounts (A,B,C) and insert the following transfers:
        // A -> C
        // B -> C
        // B -> A
        // C -> B
        // Test will check that:
        //  - After deleting C, B has two orphan transfers,
        //  one becoming income and the other expense. A will have one orphan expense.
        //  - After deleting B, A will have an additional orphan income.
        Account testAccount = new Account();
        testAccount.setName("Test account");
        int A = (int)(long) repository.insert(testAccount).blockingGet();
        int B = (int)(long) repository.insert(testAccount).blockingGet();
        int C = (int)(long) repository.insert(testAccount).blockingGet();

        // Shared test data
        MoneyTransaction testTransaction = new MoneyTransaction();
        testTransaction.setType(MoneyTransaction.TRANSFER);
        testTransaction.setConcept("test");
        testTransaction.setOriginalValue("10");
        testTransaction.setCategory(1);

        // A -> C
        testTransaction.setAccount(A);
        testTransaction.setTransferDestinationAccount(C);
        transactionRepository.insert(testTransaction).blockingGet(); // id 2 (mirror id 1)

        // B -> C
        testTransaction.setAccount(B);
        transactionRepository.insert(testTransaction).blockingGet(); // id 4 (mirror id 3)

        // B -> A
        testTransaction.setTransferDestinationAccount(A);
        transactionRepository.insert(testTransaction).blockingGet(); // id 6 (mirror id 5)

        // C -> B
        testTransaction.setAccount(C);
        testTransaction.setTransferDestinationAccount(B);
        transactionRepository.insert(testTransaction).blockingGet(); // id 8 (mirror id 7)

        repository.delete(new Account(C)).blockingGet();

        MoneyTransaction modifiedTransfer1 = getOrAwaitValue(transactionRepository.find(2));
        MoneyTransaction modifiedTransfer2 = getOrAwaitValue(transactionRepository.find(4));
        MoneyTransaction modifiedTransfer3 = getOrAwaitValue(transactionRepository.find(7));
        assertEquals(MoneyTransaction.EXPENSE, modifiedTransfer1.getType());
        assertNull(modifiedTransfer1.getTransferDestinationAccount());
        assertEquals(MoneyTransaction.EXPENSE, modifiedTransfer2.getType());
        assertNull(modifiedTransfer2.getTransferDestinationAccount());
        assertEquals(MoneyTransaction.INCOME, modifiedTransfer3.getType());
        assertNull(modifiedTransfer3.getTransferDestinationAccount());

        repository.delete(new Account(B)).blockingGet();
        MoneyTransaction modifiedTransfer4 = getOrAwaitValue(transactionRepository.find(5));
        assertEquals(MoneyTransaction.INCOME, modifiedTransfer4.getType());
        assertNull(modifiedTransfer4.getTransferDestinationAccount());
    }

    // TESTS FOR MonthlyBalanceDAO

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void filters_AccountWithBalance_by_month() throws InterruptedException {
        // TEST description:
        //  Will test 4 accounts [A,B,C,D] across 7 months, from 4 months
        //  previous to "today" and 2 in the future
        //  (P is past, T is today and F is future)
        //  [P4,P3,P2,P1,T,F1,F2].
        //  Across those past 4 months, some monthly balances will be inserted
        //  to emulate real transactions
        //   (incomes: +, expenses: -, confirmed: #, projected: ?)
        //   [#+, #-, ?+, ?-]
        //  and and see how the query responds to the
        //  absence of balances in some of those months.
        //  The furthest month is expected to return null numbers
        //  as it will predate the creation of all 4 accounts.
        //  Calculations are represented as
        //  [Current: [T], End of month/projected: [E], Unresolved: [!]]
        //  Example of operations:
        //      Account creation: [A=20] for initial balance
        //      Account balance update: A#+100 for confirmed incomes
        //
        // TEST flow:
        //   |    P4    |    P3    |    P2    |    P1    |    T     |    F1    |    F2    |
        //   ------------------------------------------------------------------------------
        // O |          | [A=104]  | A#+10    |          | A?-10    |          | B?+10    |
        // P |          | [B=62]   | A?+20    |  [D=135] | C#+20    |          |          |
        // E |          | B?-50    | [C=135]  |          | C?+10    |          |          |
        // R.|          |          |          |          | B#+20    |          |          |
        //   ------------------------------------------------------------------------------
        // A |          |          |          |          |A[T]=114  |          |          |
        // C |  Results |A[E]=104  |A[E]=114  |A[E]=114  |A[E]=124  |A[E]=124  |A[E]=124  |
        // C |   size   |A[!]=null |A[!]=20   |A[!]=20   |A[!]=20   |A[!]=0    |A[!]=0    |
        // O |    is    |          |          |          |B[T]=82   |          |          |
        // U |     0    |B[E]=62   |B[E]=62   |B[E]=62   |B[E]=32   |B[E]=32   |B[E]=42   |
        // N |          |B[!]=50   |B[!]=50   |B[!]=50   |B[!]=50   |B[!]=0    |B[!]=0    |
        // T |          |          |          |          |C[T]=155  |          |          |
        // S |          |          |C[E]=135  |C[E]=135  |C[E]=165  |C[E]=165  |C[E]=165  |
        //   |          |          |C[!]=0    |C[!]=0    |C[!]=0    |C[!]=0    |C[!]=0    |
        //   ------------------------------------------------------------------------------
        Account[] testAccounts = new Account[4];
        testAccounts[0] = new Account();
        testAccounts[0].setInitialValue("104");
        testAccounts[0].setCreatedAt(DateUtil.OffsetDateTimeNow().minusMonths(3));
        testAccounts[1] = new Account();
        testAccounts[1].setInitialValue("62");
        testAccounts[1].setCreatedAt(DateUtil.OffsetDateTimeNow().minusMonths(3));
        testAccounts[2] = new Account();
        testAccounts[2].setInitialValue("135");
        testAccounts[2].setCreatedAt(DateUtil.OffsetDateTimeNow().minusMonths(2));
        // Archived account that shouldn't get included in calculations
        testAccounts[3] = new Account();
        testAccounts[3].setInitialValue("135");
        testAccounts[3].setCreatedAt(DateUtil.OffsetDateTimeNow().minusMonths(1));
        testAccounts[3].setArchived(true);

        repository.insert(testAccounts[0]).blockingGet(); // ID should be 1
        repository.insert(testAccounts[1]).blockingGet(); // ID should be 2
        repository.insert(testAccounts[2]).blockingGet(); // ID should be 3
        repository.insert(testAccounts[3]).blockingGet(); // ID should be 4

        MonthlyBalance[] testBalances = new MonthlyBalance[6];
        testBalances[0] = new MonthlyBalance();
        // Here we will just replace the already generated balance
        testBalances[0].setAccountId(2);
        testBalances[0].setDate(DateUtil.now().minusMonths(3));
        testBalances[0].setTotalIncomes(new BigDecimal("0"));
        testBalances[0].setTotalExpenses(new BigDecimal("0"));
        testBalances[0].setProjectedIncomes(new BigDecimal("0"));
        testBalances[0].setProjectedExpenses(new BigDecimal("50"));
        testBalances[1] = new MonthlyBalance();
        testBalances[1].setAccountId(1);
        testBalances[1].setDate(DateUtil.now().minusMonths(2));
        testBalances[1].setTotalIncomes(new BigDecimal("10"));
        testBalances[1].setProjectedIncomes(new BigDecimal("20"));
        testBalances[2] = new MonthlyBalance();
        testBalances[2].setAccountId(1);
        testBalances[2].setDate(DateUtil.now());
        testBalances[2].setProjectedExpenses(new BigDecimal("10"));
        testBalances[3] = new MonthlyBalance();
        testBalances[3].setAccountId(3);
        testBalances[3].setDate(DateUtil.now());
        testBalances[3].setProjectedIncomes(new BigDecimal("10"));
        testBalances[3].setTotalIncomes(new BigDecimal("20"));
        testBalances[4] = new MonthlyBalance();
        testBalances[4].setAccountId(2);
        testBalances[4].setDate(DateUtil.now());
        testBalances[4].setTotalIncomes(new BigDecimal("20"));
        testBalances[5] = new MonthlyBalance();
        testBalances[5].setAccountId(2);
        testBalances[5].setDate(DateUtil.now().plusMonths(2));
        testBalances[5].setProjectedIncomes(new BigDecimal("10"));

        // We will insert some pending and confirmed transactions to get more accurate data
        repository.update(testBalances[0]).blockingGet();
        repository.insert(testBalances[1]).blockingGet();
        repository.insert(testBalances[2]).blockingGet();
        repository.insert(testBalances[3]).blockingGet();
        repository.insert(testBalances[4]).blockingGet();
        repository.insert(testBalances[5]).blockingGet();

        // Get all results
        // todayResults = special today accumulates list that doesnt care about
        //  unresolved transactions nor differentiates current and past balances
        List<AccountWithAccumulated> todayResults = LiveDataTestUtil.getOrAwaitValue(
                repository.getTodayAccumulatesList());
        // todayResultsB = normal accumulates list retrieved the same way as
        //  any other month
        List<AccountWithAccumulated> todayResultsB = LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatesListAtMonth(DateUtil.now()));
        List<AccountWithAccumulated> minus1MonthResults = LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatesListAtMonth(DateUtil.now().minusMonths(1)));
        List<AccountWithAccumulated> minus2MonthsResults = LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatesListAtMonth(DateUtil.now().minusMonths(2)));
        List<AccountWithAccumulated> minus3MonthsResults = LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatesListAtMonth(DateUtil.now().minusMonths(3)));
        List<AccountWithAccumulated> minus4MonthsResults = LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatesListAtMonth(DateUtil.now().minusMonths(4)));
        List<AccountWithAccumulated> plus1MonthResults = LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatesListAtMonth(DateUtil.now().plusMonths(1)));
        List<AccountWithAccumulated> plus2MonthsResults = LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatesListAtMonth(DateUtil.now().plusMonths(2)));

        // Assert that, despite there being 4 accounts in the database,
        //  the archived one is never included in these calculations
        assertEquals(3, todayResults.size());
        assertEquals(3, todayResultsB.size());
        assertEquals(3, minus1MonthResults.size());
        assertEquals(3, minus2MonthsResults.size());
        assertEquals(2, minus3MonthsResults.size());
        assertEquals(0, minus4MonthsResults.size());
        assertEquals(3, plus1MonthResults.size());
        assertEquals(3, plus2MonthsResults.size());

        // Assertions for P3

        assertThat(new BigDecimal("104"), comparesEqualTo(minus3MonthsResults.get(0).getBalanceAtEndOfMonth()));
        assertThat(BigDecimal.ZERO, comparesEqualTo(minus3MonthsResults.get(0).getUnresolvedTransactions()));
        assertThat(new BigDecimal("62"), comparesEqualTo(minus3MonthsResults.get(1).getBalanceAtEndOfMonth()));
        assertThat(new BigDecimal("50"), comparesEqualTo(minus3MonthsResults.get(1).getUnresolvedTransactions()));

        // Assertions for P2

        assertThat(new BigDecimal("114"), comparesEqualTo(minus2MonthsResults.get(0).getBalanceAtEndOfMonth()));
        assertThat(new BigDecimal("20"), comparesEqualTo(minus2MonthsResults.get(0).getUnresolvedTransactions()));
        assertThat(new BigDecimal("62"), comparesEqualTo(minus2MonthsResults.get(1).getBalanceAtEndOfMonth()));
        assertThat(new BigDecimal("50"), comparesEqualTo(minus2MonthsResults.get(1).getUnresolvedTransactions()));
        assertThat(new BigDecimal("135"), comparesEqualTo(minus2MonthsResults.get(2).getBalanceAtEndOfMonth()));
        assertThat(BigDecimal.ZERO, comparesEqualTo(minus2MonthsResults.get(2).getUnresolvedTransactions()));

        // Assertions for P1

        assertThat(new BigDecimal("114"), comparesEqualTo(minus1MonthResults.get(0).getBalanceAtEndOfMonth()));
        assertThat(new BigDecimal("20"), comparesEqualTo(minus1MonthResults.get(0).getUnresolvedTransactions()));
        assertThat(new BigDecimal("62"), comparesEqualTo(minus1MonthResults.get(1).getBalanceAtEndOfMonth()));
        assertThat(new BigDecimal("50"), comparesEqualTo(minus1MonthResults.get(1).getUnresolvedTransactions()));
        assertThat(new BigDecimal("135"), comparesEqualTo(minus1MonthResults.get(2).getBalanceAtEndOfMonth()));
        assertThat(BigDecimal.ZERO, comparesEqualTo(minus1MonthResults.get(2).getUnresolvedTransactions()));

        // Assertions for T

        // Confirmed accumulates values
        assertThat(new BigDecimal("114"), comparesEqualTo(todayResults.get(0).getConfirmedAccumulatedBalanceAtMonth()));
        assertThat(new BigDecimal("82"), comparesEqualTo(todayResults.get(1).getConfirmedAccumulatedBalanceAtMonth()));
        assertThat(new BigDecimal("155"), comparesEqualTo(todayResults.get(2).getConfirmedAccumulatedBalanceAtMonth()));

        // End of month and unresolved transactions
        assertThat(new BigDecimal("124"), comparesEqualTo(todayResultsB.get(0).getBalanceAtEndOfMonth()));
        assertThat(new BigDecimal("20"), comparesEqualTo(todayResultsB.get(0).getUnresolvedTransactions()));
        assertThat(new BigDecimal("32"), comparesEqualTo(todayResultsB.get(1).getBalanceAtEndOfMonth()));
        assertThat(new BigDecimal("50"), comparesEqualTo(todayResultsB.get(1).getUnresolvedTransactions()));
        assertThat(new BigDecimal("165"), comparesEqualTo(todayResultsB.get(2).getBalanceAtEndOfMonth()));
        assertThat(BigDecimal.ZERO, comparesEqualTo(todayResultsB.get(2).getUnresolvedTransactions()));

        // Assertions for F1

        assertThat(new BigDecimal("124"), comparesEqualTo(plus1MonthResults.get(0).getBalanceAtEndOfMonth()));
        assertThat(BigDecimal.ZERO, comparesEqualTo(plus1MonthResults.get(0).getUnresolvedTransactions()));
        assertThat(new BigDecimal("32"), comparesEqualTo(plus1MonthResults.get(1).getBalanceAtEndOfMonth()));
        assertThat(BigDecimal.ZERO, comparesEqualTo(plus1MonthResults.get(1).getUnresolvedTransactions()));
        assertThat(new BigDecimal("165"), comparesEqualTo(plus1MonthResults.get(2).getBalanceAtEndOfMonth()));
        assertThat(BigDecimal.ZERO, comparesEqualTo(plus1MonthResults.get(2).getUnresolvedTransactions()));

        // Assertions for F2

        assertThat(new BigDecimal("124"), comparesEqualTo(plus2MonthsResults.get(0).getBalanceAtEndOfMonth()));
        assertThat(BigDecimal.ZERO, comparesEqualTo(plus2MonthsResults.get(0).getUnresolvedTransactions()));
        assertThat(new BigDecimal("42"), comparesEqualTo(plus2MonthsResults.get(1).getBalanceAtEndOfMonth()));
        assertThat(BigDecimal.ZERO, comparesEqualTo(plus2MonthsResults.get(1).getUnresolvedTransactions()));
        assertThat(new BigDecimal("165"), comparesEqualTo(plus2MonthsResults.get(2).getBalanceAtEndOfMonth()));
        assertThat(BigDecimal.ZERO, comparesEqualTo(plus2MonthsResults.get(2).getUnresolvedTransactions()));

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void returns_correct_accumulated_balance() throws InterruptedException {
        Account account = new Account();
        account.setInitialValue("40");
        account.setCreatedAt(DateUtil.OffsetDateTimeNow().minusMonths(3));
        repository.insert(account).blockingGet();

        assertThat(new BigDecimal("40"), comparesEqualTo(LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatedAtMonth(1, DateUtil.now()))
                .getConfirmedAccumulatedBalanceAtMonth()));
        assertThat(new BigDecimal("40"), comparesEqualTo(LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatedAtMonth(1, DateUtil.now().minusMonths(1)))
                .getConfirmedAccumulatedBalanceAtMonth()));
        assertThat(new BigDecimal("40"), comparesEqualTo(LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatedAtMonth(1, DateUtil.now().plusMonths(2)))
                .getConfirmedAccumulatedBalanceAtMonth()));
        assertThat(new BigDecimal("40"), comparesEqualTo(LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatedAtMonth(1, DateUtil.now().minusMonths(3)))
                .getConfirmedAccumulatedBalanceAtMonth()));
        assertThat(BigDecimal.ZERO, comparesEqualTo(LiveDataTestUtil.getOrAwaitValue(
                repository.getAccumulatedAtMonth(1, DateUtil.now().minusMonths(4)))
                .getConfirmedAccumulatedBalanceAtMonth()));
    }


}
