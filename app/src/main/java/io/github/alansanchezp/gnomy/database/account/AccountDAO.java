package io.github.alansanchezp.gnomy.database.account;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import io.reactivex.Single;

@Dao
public abstract class AccountDAO  {

    @Query("SELECT * FROM accounts WHERE is_archived = 0")
    protected abstract LiveData<List<Account>> getAll();

    @Query("SELECT * FROM accounts WHERE is_archived = 1")
    protected abstract LiveData<List<Account>> getArchivedAccounts();

    @Query("SELECT * FROM accounts WHERE account_id = :id")
    protected abstract LiveData<Account> find(int id);

    @Delete
    protected abstract Single<Integer> delete(Account... accounts);

    @Query("UPDATE OR ABORT accounts SET is_archived = 1 WHERE account_id = :id")
    protected abstract Single<Integer> archive(int id);

    @Query("UPDATE OR ABORT accounts SET is_archived = 0 WHERE account_id = :id")
    protected abstract Single<Integer> restore(int id);

    @Query("UPDATE OR ABORT accounts SET is_archived = 0 WHERE is_archived = 1")
    protected abstract Single<Integer> restoreAll();

    // SYNCHRONOUS OPERATIONS, NEVER USE DIRECTLY FROM REPOSITORY
    // WITHOUT WRAPPING THEM IN Single<> METHODS

    @Query("SELECT * FROM accounts WHERE account_id = :id")
    protected abstract Account _find(int id);

    @Insert
    protected abstract Long _insert(Account account);

    @Update
    protected abstract int _update(Account account);
}
