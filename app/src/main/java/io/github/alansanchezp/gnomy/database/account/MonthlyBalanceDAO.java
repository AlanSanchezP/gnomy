package io.github.alansanchezp.gnomy.database.account;

import java.time.YearMonth;

import androidx.lifecycle.LiveData;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import static androidx.room.OnConflictStrategy.IGNORE;

public interface MonthlyBalanceDAO {

    @Query("SELECT * FROM monthly_balances WHERE account_id = :accountId AND balance_date = :month")
    LiveData<MonthlyBalance> findBalance(int accountId, YearMonth month);

    // SYNCHRONOUS OPERATIONS, NEVER USE DIRECTLY FROM REPOSITORY
    // WITHOUT WRAPPING THEM IN Single<> METHODS

    @Insert(onConflict = IGNORE)
    void _insertOrIgnoreBalance(MonthlyBalance balance);

    @Update
    int _updateBalance(MonthlyBalance balance);
}
