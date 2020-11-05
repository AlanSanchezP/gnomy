package io.github.alansanchezp.gnomy.database;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountDAO;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalanceDAO;
import io.reactivex.Single;

public class MockDatabaseOperationsUtil {
    private static MockableAccountDAO accountDAO;
    private static MockableMonthlyBalanceDAO balanceDAO;
    private static boolean useMocks = true;

    public static void disableMocking() {
        useMocks = false;
    }

    public static void setAccountDAO(MockableAccountDAO dao) {
        accountDAO = dao;
    }


    public static MockableAccountDAO getAccountDAO() {
        if (!useMocks) throw new IllegalStateException("Mocking DAOs was disabled for this test.");
        if (accountDAO == null) throw new RuntimeException("Mock DAO has not been initialized.");
        return accountDAO;
    }

    public static void setBalanceDAO(MockableMonthlyBalanceDAO dao) {
        balanceDAO = dao;
    }


    public static MockableMonthlyBalanceDAO getBalanceDAO() {
        if (!useMocks) throw new IllegalStateException("Mocking DAOs was disabled for this test.");
        if (balanceDAO == null) throw new RuntimeException("Mock DAO has not been initialized.");
        return balanceDAO;
    }

    // TODO: Implement other DAOs when correspondent modules are being used


    public static class MockableAccountDAO extends AccountDAO {
        public LiveData<List<Account>> getAll() {return null;}

        public LiveData<List<Account>> getArchivedAccounts() {
            return null;
        }

        public LiveData<Account> find(int id) {
            return null;
        }

        public Single<Long[]> insert(Account... accounts) {
            return null;
        }

        public Single<Integer> delete(Account... accounts) {
            return null;
        }

        public Single<Integer> archive(int id) {
            return null;
        }

        public Single<Integer> restore(int id) {
            return null;
        }

        public Single<Integer> restoreAll() {
            return null;
        }

        protected Account syncFind(int id) {
            return null;
        }

        protected int syncUpdate(Account account) {
            return 1;
        }
    }

    public static class MockableMonthlyBalanceDAO extends MonthlyBalanceDAO {
        protected LiveData<List<AccountWithBalance>> getAllFromMonth(YearMonth month) {
            return null;
        }

        public LiveData<BigDecimal> getAccumulatedFromMonth(int accountId, YearMonth month) {
            return null;
        }

        public LiveData<List<MonthlyBalance>> getAllFromAccount(int accountId) {
            return null;
        }

        public LiveData<MonthlyBalance> find(int accountId, YearMonth month) {
            return null;
        }

        public void insert(MonthlyBalance... balances) {
        }

        public void update(MonthlyBalance balance) {
        }
    }
}
