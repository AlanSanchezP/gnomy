package io.github.alansanchezp.gnomy.database.transaction;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public abstract class MoneyTransactionDAO {
    @Query("SELECT * from transactions")
    protected abstract List<MoneyTransaction> getAll();

    @Insert
    protected abstract void insertAll(MoneyTransaction... transactions);
}
