package io.github.alansanchezp.gnomy.database.recurrentTransaction;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface RecurrentTransactionDAO {
    @Query("SELECT * from recurrent_transactions")
    List<RecurrentTransaction> getAll();

    @Insert
    void insertAll(RecurrentTransaction... transactions);
}
