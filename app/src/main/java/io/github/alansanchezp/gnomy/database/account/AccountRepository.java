package io.github.alansanchezp.gnomy.database.account;

import android.content.Context;

import java.time.YearMonth;

import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.GnomyDatabase;
import io.github.alansanchezp.gnomy.database.GnomyIllegalQueryException;
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

    public Single<Integer> delete(Account account) {
        return db.toSingleInTransaction(() -> {
           int savedOrphans = accountDAO._savePotentiallyOrphanTransfers(account.getId());
           return accountDAO._delete(account) + savedOrphans;
        });
    }

    // TODO: Test the (hopefully) few manually implemented db operations
    public Single<Integer> update(Account account) {
        return db.toSingleInTransaction(() -> {
            Account original = accountDAO._find(account.getId());
            // TODO: Analyze what we should do here
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

    // TODO: Remove this method when transactions module is ready
    //  so that we can test using actual insertion of individual
    //  transactions, right now we are just dummy updating balances
    public Single<Integer> insert(MonthlyBalance balance) {
        return db.toSingleInTransaction(()-> {
            accountDAO._insertOrIgnoreBalance(balance);
            return 1;
        });
    }

    // TODO: When transactions module is ready, evaluate if this is still needed
    public Single<Integer> update(MonthlyBalance balance) {
        return db.toSingleInTransaction(()-> accountDAO._updateBalance(balance));
    }

    // TODO: This method is used only in tests so far, evaluate deleting it later
    public LiveData<MonthlyBalance> getBalanceFromMonth(int accountId, YearMonth month) {
        return accountDAO.findBalance(accountId, month);
    }
}
