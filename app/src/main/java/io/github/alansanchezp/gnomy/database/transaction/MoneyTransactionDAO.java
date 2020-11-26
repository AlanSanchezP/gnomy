package io.github.alansanchezp.gnomy.database.transaction;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalanceDAO;

@Dao
public abstract class MoneyTransactionDAO implements MonthlyBalanceDAO {
    @Query("SELECT * from transactions")
    protected abstract LiveData<List<MoneyTransaction>> getAll();

    @Insert
    protected abstract long _insert(MoneyTransaction transaction);

    @Query("SELECT * FROM transactions WHERE transaction_id = :id;")
    protected abstract LiveData<MoneyTransaction> find(int id);
}
