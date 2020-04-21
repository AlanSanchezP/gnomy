package io.github.alansanchezp.gnomy.database.account;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface AccountDAO  {
    @Query("SELECT * FROM accounts")
    LiveData<List<Account>> getAll();

    @Query("SELECT * FROM accounts WHERE account_id = :id")
    LiveData<Account> find(int id);

    @Insert
    void insert(Account... accounts);

    @Delete
    void delete(Account... accounts);

    @Update
    void update(Account account);
}
