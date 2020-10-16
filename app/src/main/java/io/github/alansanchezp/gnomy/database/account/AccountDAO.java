package io.github.alansanchezp.gnomy.database.account;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public abstract class AccountDAO  {
    @Query("SELECT * FROM accounts WHERE is_archived = 0")
    abstract LiveData<List<Account>> getAll();

    @Query("SELECT * FROM accounts WHERE is_archived = 1")
    abstract LiveData<List<Account>> getArchivedAccounts();

    @Query("SELECT * FROM accounts WHERE account_id = :id")
    abstract LiveData<Account> find(int id);

    @Query("SELECT * FROM accounts WHERE account_id = :id")
    abstract Account findSync(int id);

    @Insert
    abstract void insert(Account... accounts);

    @Delete
    abstract void delete(Account... accounts);

    @Transaction
    public void update(Account account) {
        Account original = findSync(account.getId());

        if (!original.getDefaultCurrency().equals(account.getDefaultCurrency())) {
            // TODO: recalculation of calculated values.
            //  See GnomyDatabase's TODOs
        }

        _update(account);
    }

    @Update
    protected abstract void _update(Account account);

    @Query("UPDATE OR ABORT accounts SET is_archived = 1 WHERE account_id = :id")
    abstract void archive(int id);

    @Query("UPDATE OR ABORT accounts SET is_archived = 0 WHERE account_id = :id")
    abstract void restore(int id);

    @Query("UPDATE OR ABORT accounts SET is_archived = 0 WHERE is_archived = 1")
    abstract void restoreAll();
}
