package io.github.alansanchezp.gnomy.data.account;

import android.content.Context;

import java.time.YearMonth;

import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.data.GnomyDatabase;
import io.github.alansanchezp.gnomy.data.GnomyIllegalQueryException;
import io.reactivex.Single;

public class AccountRepository {
    private final GnomyDatabase db;
    private final AccountDAO accountDAO;

    public AccountRepository(Context context) {
        db = GnomyDatabase.getInstance(context, "");
        accountDAO = db.accountDAO();
    }

    public LiveData<List<Account>> getAll() {
        return accountDAO.getAll();
    }

    public LiveData<List<Account>> getArchivedAccounts() {
        return accountDAO.getArchivedAccounts();
    }

    public LiveData<Account> getAccount(int accountId) {
        return accountDAO.find(accountId);
    }

    public LiveData<List<AccountWithAccumulated>> getAccumulatesListAtMonth(YearMonth month) {
        return accountDAO.getAccumulatesListAtMonth(month);
    }

    public LiveData<List<AccountWithAccumulated>> getTodayAccumulatesList() {
        return accountDAO.getTodayAccumulatesList();
    }

    public LiveData<AccountWithAccumulated> getAccumulatedAtMonth(int accountId, YearMonth targetMonth) {
        return accountDAO.getAccumulatedAtMonth(accountId, targetMonth);
    }

    /**
     * Custom insert method that creates an initial {@link MonthlyBalance} row
     * associated with the given {@link Account}, using its id and creation date.
     *
     * @param account   Account to insert.
     * @return          Single object to observe. If no errors occur, it will
     *                  return the generated id of the inserted account.
     */
    public Single<Long> insert(Account account) {
        return db.toSingleInTransaction(() -> {
            Long inserted_id = accountDAO._insert(account);
            MonthlyBalance initial_balance = new MonthlyBalance();
            initial_balance.setDate(YearMonth.from(account.getCreatedAt()));
            initial_balance.setAccountId((int)(long)inserted_id);
            accountDAO._insertOrIgnoreBalance(initial_balance);
            return inserted_id;
        });
    }

    /**
     * Custom delete method that prevents orphan transfers from triggering
     * exceptions in during the application use.
     *
     * @param accountId Id of the account to delete.
     * @return          Single object to observe. If no errors occur, it will
     *                  return the total number of affected rows (including orphan transfers)
     */
    public Single<Integer> delete(int accountId) {
        return db.toSingleInTransaction(() -> {
           int savedOrphans = accountDAO._savePotentiallyOrphanMirrorTransfers(accountId);
           savedOrphans += accountDAO._savePotentiallyOrphanTransfers(accountId);
           return accountDAO._delete(new Account(accountId)) + savedOrphans;
        });
    }

    /**
     * Custom update method that prevents an account currency to be altered.
     *
     * @param account   New account value.
     * @return          Single object to observe. If no errors occur, it will
     *                  return the amount of affected rows.
     */
    public Single<Integer> update(Account account) {
        return db.toSingleInTransaction(() -> {
            Account original = accountDAO._find(account.getId());
            // XXX: [#10] Analyze what should be done here
            //  Current behavior: Reject update if it contains altered currency
            try {
                if (original.equals(account)) return 0;
                if (original.getDefaultCurrency().equals(
                        account.getDefaultCurrency())) {
                    return accountDAO._update(account);
                }

                throw new GnomyIllegalQueryException("It is not allowed to change an account's currency.");
            } catch (NullPointerException e) {
                throw new GnomyIllegalQueryException("Trying to update non-existent account.", e);
            }
        });
    }

    public Single<Integer> archive(int accountId) {
        return accountDAO.archive(accountId);
    }

    public Single<Integer> restore(int accountId) {
        return accountDAO.restore(accountId);
    }

    // WRAPPER METHODS
    // XXX: Delete if never used (when project matures more)
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

    // XXX: Should make method protected to avoid manual insertion of balances?
    public Single<Integer> insert(MonthlyBalance balance) {
        return db.toSingleInTransaction(()-> {
            accountDAO._insertOrIgnoreBalance(balance);
            return 1;
        });
    }

    // This method is used only in tests
    public Single<Integer> update(MonthlyBalance balance) {
        return db.toSingleInTransaction(()-> accountDAO._updateBalance(balance));
    }

    // This method is used only in tests
    public LiveData<MonthlyBalance> getBalanceFromMonth(int accountId, YearMonth month) {
        return accountDAO.findBalance(accountId, month);
    }
}
