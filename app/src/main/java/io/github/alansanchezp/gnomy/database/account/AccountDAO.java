package io.github.alansanchezp.gnomy.database.account;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public interface AccountDAO {
    @Transaction
    @Query("SELECT * from accounts")
    List<Account> getAll();

    @Insert
    void insertAll(Account... accounts);
}
