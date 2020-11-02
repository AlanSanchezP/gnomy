package io.github.alansanchezp.gnomy.database.account;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import io.reactivex.Single;
import io.reactivex.SingleObserver;

@Dao
public abstract class AccountDAO  {

    @Query("SELECT * FROM accounts WHERE is_archived = 0")
    protected abstract LiveData<List<Account>> getAll();

    @Query("SELECT * FROM accounts WHERE is_archived = 1")
    protected abstract LiveData<List<Account>> getArchivedAccounts();

    @Query("SELECT * FROM accounts WHERE account_id = :id")
    protected abstract LiveData<Account> find(int id);

    @Insert
    protected abstract Single<Long[]> insert(Account... accounts);

    @Delete
    protected abstract Single<Integer> delete(Account... accounts);

    @Query("UPDATE OR ABORT accounts SET is_archived = 1 WHERE account_id = :id")
    protected abstract Single<Integer> archive(int id);

    @Query("UPDATE OR ABORT accounts SET is_archived = 0 WHERE account_id = :id")
    protected abstract Single<Integer> restore(int id);

    @Query("UPDATE OR ABORT accounts SET is_archived = 0 WHERE is_archived = 1")
    protected abstract Single<Integer> restoreAll();

    public Single<Integer> update(Account account) {
        return new Single<Integer>() {
            @Override
            protected void subscribeActual(SingleObserver<? super Integer> observer) {
                try {
                    int result = syncSafeUpdate(account);
                    observer.onSuccess(result);
                    if (result == -1) observer.onError(new Throwable("It is not allowed to change an account's currency"));
                } catch (Throwable throwable) {
                    observer.onError(throwable);
                }
            }
        };
    }

    // SYNCHRONOUS OPERATIONS, NEVER USE DIRECTLY FROM REPOSITORY
    // WITHOUT WRAPPING THEM IN Single<> METHODS

    @Query("SELECT * FROM accounts WHERE account_id = :id")
    protected abstract Account syncFind(int id);

    @Update
    protected abstract int syncUpdate(Account account);

    // TODO: Test the (hopefully) few manually implemented db operations
    //  Current implementation blocks this possibility as Repositories
    //  always use mocked DAOs directly
    @Transaction
    protected int syncSafeUpdate(Account account) {
        Account original = syncFind(account.getId());
        if (original.equals(account)) return 0;

        // TODO: Analyze what we should do here
        //  Current behavior: Reject update if it contains altered currency
        if (original.getDefaultCurrency().equals(
                account.getDefaultCurrency())) {
            return syncUpdate(account);
        }

        return -1;
    }
}
