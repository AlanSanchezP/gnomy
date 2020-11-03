package io.github.alansanchezp.gnomy.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.LiveDataTestUtil;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.util.DateUtil;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DatabaseTriggersTest {
    private AccountRepository repository;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        MockRepositoryUtility.disableMocking();
        repository = new AccountRepository(
                InstrumentationRegistry.getInstrumentation().getContext());
    }

    @After
    public void cleanDatabase() {
        GnomyDatabase.cleanUp();
    }

    // Tests Database trigger
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
}
