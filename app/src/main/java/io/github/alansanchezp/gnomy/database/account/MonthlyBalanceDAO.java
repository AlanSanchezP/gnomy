package io.github.alansanchezp.gnomy.database.account;

import org.threeten.bp.YearMonth;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface MonthlyBalanceDAO {
    @Query("SELECT * FROM monthly_balances " +
            "WHERE balance_date = :month")
    LiveData<List<MonthlyBalance>> getAllFromMonth(YearMonth month);

    @Query("SELECT * FROM monthly_balances " +
            "WHERE account_id = :accountId")
    LiveData<List<MonthlyBalance>> getAllFromAccount(int accountId);

    @Query("SELECT * FROM monthly_balances WHERE account_id = :accountId AND balance_date = :month")
    MonthlyBalance find(int accountId, YearMonth month);

    @Insert
    void insert(MonthlyBalance... balances);

    @Update
    void update(MonthlyBalance balance);
}
