package io.github.alansanchezp.gnomy.database.account;

import java.math.BigDecimal;
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

    @Query("UPDATE monthly_balances SET " +
            "total_incomes = total_incomes + :additionalIncomes, " +
            "total_expenses = total_expenses + :additionalExpenses, " +
            "projected_incomes = projected_incomes + :additionalProjectedIncomes, " +
            "projected_expenses = projected_expenses + :additionalProjectedExpenses " +
            "WHERE account_id=:accountId AND balance_date=:balanceDate;")
    int _adjustBalance(int accountId,
                       YearMonth balanceDate,
                       BigDecimal additionalIncomes,
                       BigDecimal additionalExpenses,
                       BigDecimal additionalProjectedIncomes,
                       BigDecimal additionalProjectedExpenses);
}
