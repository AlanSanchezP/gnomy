package io.github.alansanchezp.gnomy.database.account;

import android.content.Context;

import java.time.YearMonth;

import java.math.BigDecimal;
import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.GnomyDatabase;
import io.reactivex.Single;

public class AccountRepository {
    private LiveData<List<Account>> allAccounts;
    private AccountDAO accountDAO;
    private MonthlyBalanceDAO balanceDAO;

    public AccountRepository(Context context) {
        GnomyDatabase db;
        db = GnomyDatabase.getInstance(context, "");
        accountDAO = db.accountDAO();
        balanceDAO = db.monthlyBalanceDAO();
        allAccounts = accountDAO.getAll();
    }

    public LiveData<List<Account>> getAll() {
        return allAccounts;
    }

    public LiveData<List<Account>> getArchivedAccounts() {
        return accountDAO.getArchivedAccounts();
    }

    public LiveData<Account> getAccount(int accountId) {
        return accountDAO.find(accountId);
    }

    public LiveData<BigDecimal> getAccumulatedFromMonth(int accountId, YearMonth month) {
        return balanceDAO.getAccumulatedFromMonth(accountId, month);
    }

    public Single<Long[]> insert(Account account) {
        return accountDAO.insert(account);
    }

    public Single<Integer> delete(Account account) {
        return accountDAO.delete(account);
    }

    public Single<Integer> update(Account account) {
        return accountDAO.update(account);
    }

    public Single<Integer> archive(int accountId) {
        return accountDAO.archive(accountId);
    }

    public Single<Integer> restore(int accountId) {
        return accountDAO.restore(accountId);
    }

    // WRAPPER METHODS
    // TODO: Delete if never used (when project matures more)
    public Single<Integer> archive(Account account) {
        return archive(account.getId());
    }

    public Single<Integer> restore(Account account) {
        return restore(account.getId());
    }

    public Single<Integer> restoreAll() {
        return accountDAO.restoreAll();
    }

    // Monthly balance methods

    public LiveData<List<AccountWithBalance>> getAllFromMonth(YearMonth month) {
        return balanceDAO.getAllFromMonth(month);
    }

    public LiveData<List<MonthlyBalance>> getAllFromAccount(Account account) {
        return balanceDAO.getAllFromAccount(account.getId());
    }

    public LiveData<MonthlyBalance> getBalanceFromMonth(int accountId, YearMonth month) {
        return balanceDAO.find(accountId, month);
    }

    /*
    TODO: Find a way to automatically create monthly balances every month
    public void createMonthlyBalance(Account account) {
        MonthlyBalance currentBalance = getBalanceFromMonth(account.getId(), YearMonth.now());

        if (currentBalance == null) {
            MonthlyBalance mb = new MonthlyBalance(account);

            InsertBalanceAsyncTask asyncTask = new InsertBalanceAsyncTask(balanceDAO);
            asyncTask.execute(mb);
        }
    }
    */
}
