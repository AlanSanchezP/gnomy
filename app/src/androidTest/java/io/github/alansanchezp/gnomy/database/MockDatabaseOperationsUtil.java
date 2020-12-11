package io.github.alansanchezp.gnomy.database;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountDAO;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.category.CategoryDAO;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionDAO;
import io.github.alansanchezp.gnomy.database.transaction.TransactionDisplayData;
import io.reactivex.Single;

public class MockDatabaseOperationsUtil {
    private static MockableAccountDAO accountDAO;
    private static MockableCategoryDAO categoryDAO;
    private static MockableMoneyTransactionDAO transactionDAO;
    private static boolean useMocks = true;

    public static void disableMocking() {
        useMocks = false;
    }

    public static void setAccountDAO(MockableAccountDAO dao) {
        accountDAO = dao;
    }

    public static void setTransactionDAO(MockableMoneyTransactionDAO dao) {
        transactionDAO = dao;
    }

    public static void setCategoryDAO(MockableCategoryDAO dao) {
        categoryDAO = dao;
    }

    public static MockableAccountDAO getAccountDAO() {
        if (!useMocks) throw new IllegalStateException("Mocking DAOs was disabled for this test.");
        if (accountDAO == null) throw new RuntimeException("Mock DAO has not been initialized.");
        return accountDAO;
    }

    public static MockableCategoryDAO getCategoryDAO() {
        if (!useMocks) throw new IllegalStateException("Mocking DAOs was disabled for this test.");
        if (categoryDAO == null) throw new RuntimeException("Mock DAO has not been initialized.");
        return categoryDAO;
    }

    public static MockableMoneyTransactionDAO getTransactionDAO() {
        if (!useMocks) throw new IllegalStateException("Mocking DAOs was disabled for this test.");
        if (transactionDAO == null) throw new RuntimeException("Mock DAO has not been initialized.");
        return transactionDAO;
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

        public int _delete(Account account) {
            return 0;
        }

        public int _savePotentiallyOrphanTransfers(int accountId) {
            return 0;
        }

        public LiveData<MonthlyBalance> findBalance(int accountId, YearMonth month) {
            return null;
        }

        public void _insertOrIgnoreBalance(MonthlyBalance balance) {

        }

        public int _updateBalance(MonthlyBalance balance) {
            return 0;
        }

        @Override
        public int _adjustBalance(int accountId, YearMonth balanceDate, BigDecimal additionalIncomes, BigDecimal additionalExpenses, BigDecimal additionalProjectedIncomes, BigDecimal additionalProjectedExpenses) {
            return 0;
        }
    }

    public static class MockableCategoryDAO extends CategoryDAO {
        @Override
        public LiveData<List<Category>> getAll() {
            return null;
        }

        @Override
        public LiveData<Category> find(int id) {
            return null;
        }

        @Override
        public void insert(Category... categories) {

        }

        @Override
        public void update(Category... categories) {

        }

        @Override
        public void delete(Category... categories) {

        }
    }

    public static class MockableMoneyTransactionDAO extends MoneyTransactionDAO {
        @Override
        public LiveData<List<TransactionDisplayData>> getAllFromMonth(YearMonth month) {
            return null;
        }

        @Override
        public long _insert(MoneyTransaction transaction) {
            return 0;
        }

        @Override
        public LiveData<MoneyTransaction> find(int id) {
            return null;
        }

        @Override
        public MoneyTransaction _find(int id) {
            return null;
        }

        @Override
        public int _delete(MoneyTransaction... transaction) {
            return 0;
        }

        @Override
        protected MoneyTransaction _findMirrorTransfer(OffsetDateTime date, int originAccountId, Integer destinationAccountId) {
            return null;
        }

        @Override
        public Integer _update(MoneyTransaction transaction) {
            return null;
        }

        @Override
        public LiveData<MonthlyBalance> findBalance(int accountId, YearMonth month) {
            return null;
        }

        @Override
        public void _insertOrIgnoreBalance(MonthlyBalance balance) {

        }

        @Override
        public int _updateBalance(MonthlyBalance balance) {
            return 0;
        }

        @Override
        public int _adjustBalance(int accountId, YearMonth balanceDate, BigDecimal additionalIncomes, BigDecimal additionalExpenses, BigDecimal additionalProjectedIncomes, BigDecimal additionalProjectedExpenses) {
            return 0;
        }
    }
}
