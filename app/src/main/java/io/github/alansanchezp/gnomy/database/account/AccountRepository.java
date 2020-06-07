package io.github.alansanchezp.gnomy.database.account;

import android.content.Context;
import android.os.AsyncTask;

import org.threeten.bp.YearMonth;

import java.math.BigDecimal;
import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.GnomyDatabase;

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

    public void insert(Account account) {
        InsertAsyncTask accountTask = new InsertAsyncTask(accountDAO);
        accountTask.execute(account);
    }

    public void delete(Account account) {
        DeleteAsyncTask task = new DeleteAsyncTask(accountDAO);
        task.execute(account);
    }

    public void update(Account account) {
        UpdateAsyncTask task = new UpdateAsyncTask(accountDAO);
        task.execute(account);
    }

    public void archive(Account account) {
        ArchiveAsyncTask task = new ArchiveAsyncTask(accountDAO);
        task.execute(account);
    }

    public void restore(Account account) {
        RestoreAsyncTask task = new RestoreAsyncTask(accountDAO);
        task.execute(account);
    }

    public void restoreAll() {
        RestoreAllAsyncTask task = new RestoreAllAsyncTask(accountDAO);
        task.execute();
    }

    // Monthly balance methods

    public LiveData<List<AccountWithBalance>> getAllFromMonth(YearMonth month) {
        return balanceDAO.getAllFromMonth(month);
    }

    public LiveData<List<MonthlyBalance>> getAllFromAccount(Account account) {
        return balanceDAO.getAllFromAccount(account.getId());
    }

    public MonthlyBalance getBalanceFromMonth(Account account, YearMonth month) {
        return balanceDAO.find(account.getId(), month);
    }

    public void createMonthlyBalance(Account account) {
        MonthlyBalance currentBalance = getBalanceFromMonth(account, YearMonth.now());

        if (currentBalance == null) {
            MonthlyBalance mb = new MonthlyBalance(account);

            InsertBalanceAsyncTask asyncTask = new InsertBalanceAsyncTask(balanceDAO);
            asyncTask.execute(mb);
        }
    }

    // AsyncTask classes

    private static class InsertAsyncTask extends AsyncTask<Account, Void, Void> {

        private AccountDAO asyncTaskDao;

        InsertAsyncTask(AccountDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Account... params) {
            asyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Account, Void, Void> {

        private AccountDAO asyncTaskDao;

        DeleteAsyncTask(AccountDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Account... params) {
            asyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Account, Void, Void> {

        private AccountDAO asyncTaskDao;

        UpdateAsyncTask(AccountDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Account... accounts) {
            asyncTaskDao.update(accounts[0]);
            return null;
        }
    }

    private static class InsertBalanceAsyncTask extends AsyncTask<MonthlyBalance, Void, Void> {

        private MonthlyBalanceDAO asyncTaskDao;

        InsertBalanceAsyncTask(MonthlyBalanceDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final MonthlyBalance... params) {
            asyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class ArchiveAsyncTask extends AsyncTask<Account, Void, Void> {

        private AccountDAO asyncTaskDao;

        ArchiveAsyncTask(AccountDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Account... accounts) {
            asyncTaskDao.archive(accounts[0].getId());
            return null;
        }
    }

    private static class RestoreAsyncTask extends AsyncTask<Account, Void, Void> {

        private AccountDAO asyncTaskDao;

        RestoreAsyncTask(AccountDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Account... accounts) {
            asyncTaskDao.restore(accounts[0].getId());
            return null;
        }
    }

    private static class RestoreAllAsyncTask extends AsyncTask<Void, Void, Void> {

        private AccountDAO asyncTaskDao;

        RestoreAllAsyncTask(AccountDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... params) {
            asyncTaskDao.restoreAll();
            return null;
        }
    }
}
