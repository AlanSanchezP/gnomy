package io.github.alansanchezp.gnomy.database.transaction;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface MoneyTransactionDAO {
    @Query("SELECT * from transactions")
    List<MoneyTransaction> getAll();

    @Insert
    void insertAll(MoneyTransaction... transactions);
}
