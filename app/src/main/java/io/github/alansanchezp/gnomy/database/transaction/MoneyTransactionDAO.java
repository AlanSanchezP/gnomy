package io.github.alansanchezp.gnomy.database.transaction;

import java.time.OffsetDateTime;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalanceDAO;

@Dao
public abstract class MoneyTransactionDAO implements MonthlyBalanceDAO {
    @Query("SELECT * from transactions")
    protected abstract LiveData<List<MoneyTransaction>> getAll();

    @Insert
    protected abstract long _insert(MoneyTransaction transaction);

    @Query("SELECT * FROM transactions WHERE transaction_id = :id;")
    protected abstract LiveData<MoneyTransaction> find(int id);

    @Query("SELECT * FROM transactions WHERE transaction_id = :id;")
    protected abstract MoneyTransaction _find(int id);

    @Query("SELECT * FROM transactions WHERE transaction_date = :date " +
            "AND account_id = :destinationAccountId " +
            "AND transfer_destination_account_id = :originAccountId;")
    protected abstract MoneyTransaction _findMirrorTransfer(OffsetDateTime date,
                                                            int originAccountId,
                                                            Integer destinationAccountId);

    @Update
    protected abstract Integer _update(MoneyTransaction transaction);
}
