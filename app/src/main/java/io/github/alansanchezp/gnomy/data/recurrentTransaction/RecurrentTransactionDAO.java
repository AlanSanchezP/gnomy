package io.github.alansanchezp.gnomy.data.recurrentTransaction;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

/**
 * Ignore until {@link RecurrentTransaction} module is actually implemented.
 */
@Dao
public interface RecurrentTransactionDAO {
    @Query("SELECT * from recurrent_transactions")
    List<RecurrentTransaction> getAll();

    @Insert
    void insertAll(RecurrentTransaction... transactions);
}
