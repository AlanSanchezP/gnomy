package io.github.alansanchezp.gnomy.database.account;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.GnomyDatabase;

public class AccountRepository {
    private LiveData<List<Account>> allAccounts;
    private AccountDAO accountDAO;

    public AccountRepository(Context context) {
        GnomyDatabase db;
        db = GnomyDatabase.getInstance(context, "");
        accountDAO = db.accountDAO();
        allAccounts = accountDAO.getAll();
    }

    public LiveData<List<Account>> getAll() {
        return allAccounts;
    }

    public LiveData<Account> find(Account account) {
        return accountDAO.find(account.getId());
    }

    public void insert(Account account) {
        InsertAsyncTask task = new InsertAsyncTask(accountDAO);
        task.execute(account);
    }

    public void delete(Account account) {
        DeleteAsyncTask task = new DeleteAsyncTask(accountDAO);
        task.execute(account);
    }

    public void update(Account account) {
        UpdateAsyncTask task = new UpdateAsyncTask(accountDAO);
        task.execute(account);
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
            asyncTaskDao.delete(params);
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
}
