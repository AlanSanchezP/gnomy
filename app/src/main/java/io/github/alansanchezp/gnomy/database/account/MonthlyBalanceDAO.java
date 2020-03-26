package io.github.alansanchezp.gnomy.database.account;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface MonthlyBalanceDAO {
    @Query("SELECT * from monthly_balances")
    List<MonthlyBalance> getAll();

    @Insert
    void insertAll(MonthlyBalance... balances);
}
