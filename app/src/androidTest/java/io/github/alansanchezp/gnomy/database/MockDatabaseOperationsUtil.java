package io.github.alansanchezp.gnomy.database;

import java.time.YearMonth;
import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountDAO;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.reactivex.Single;

public class MockDatabaseOperationsUtil {
    private static MockableAccountDAO accountDAO;
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

    // TODO: Implement other DAOs when correspondent modules are being used


    public static class MockableAccountDAO extends AccountDAO {
        public LiveData<List<AccountWithAccumulated>> getAccumulatesListAtMonth(YearMonth targetMonth) {
            return null;
        }

        public LiveData<List<AccountWithAccumulated>> getTodayAccumulatesList() {
            return null;
        }

        public LiveData<AccountWithAccumulated> getAccumulatedAtMonth(int accountId, YearMonth targetMonth) {
            return null;
        }

        public LiveData<List<Account>> getAll() {return null;}

        public LiveData<List<Account>> getArchivedAccounts() {
            return null;
        }

        public LiveData<Account> find(int id) {
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

        public Account _find(int id) {
            return null;
        }

        public Long _insert(Account account) {
            return null;
        }

        public int _update(Account account) {
            return 1;
        }

        public LiveData<MonthlyBalance> findBalance(int accountId, YearMonth month) {
            return null;
        }

        public void _insertOrIgnoreBalance(MonthlyBalance balance) {

        }

        public int _updateBalance(MonthlyBalance balance) {
            return 0;
        }
    }
}
