package io.github.alansanchezp.gnomy.data.account;

import java.math.BigDecimal;
import java.time.YearMonth;

import androidx.annotation.NonNull;
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

    /**
     * !!!  NEVER USE DIRECTLY FROM REPOSITORY
     *      WITHOUT WRAPPING IN Single<> METHOD  !!!
     * @param balance    Value to insert.
     */
    @Insert(onConflict = IGNORE)
    void _insertOrIgnoreBalance(MonthlyBalance balance);

    /**
     * !!!  NEVER USE DIRECTLY FROM REPOSITORY
     *      WITHOUT WRAPPING IN Single<> METHOD  !!!
     * @param balance   New value.
     * @return          Amount of affected rows.
     */
    @Update
    int _updateBalance(MonthlyBalance balance);

    /**
     * !!!  NEVER USE DIRECTLY FROM REPOSITORY
     *      WITHOUT WRAPPING IN Single<> METHOD  !!!
     *
     * Updates a balance total values.
     *
     * @param accountId             Account id. Needed for composite PK.
     * @param balanceDate           Balance month. Needed for composite PK.
     * @param additionalIncomes     Incomes to add to existing value. Can be negative.
     * @param additionalExpenses    Expenses to add to existing value. Can be negative.
     * @param additionalProjectedIncomes    Projected incomes to add to existing value. Can be negative.
     * @param additionalProjectedExpenses   Projected expenses to add to existing value. Can be negative.
     * @return          Amount of affected rows.
     */
    @SuppressWarnings("UnusedReturnValue")
    @Query("UPDATE monthly_balances SET " +
            "total_incomes = total_incomes + :additionalIncomes, " +
            "total_expenses = total_expenses + :additionalExpenses, " +
            "projected_incomes = projected_incomes + :additionalProjectedIncomes, " +
            "projected_expenses = projected_expenses + :additionalProjectedExpenses " +
            "WHERE account_id=:accountId AND balance_date=:balanceDate;")
    int _adjustBalance(int accountId,
                       @NonNull YearMonth balanceDate,
                       @NonNull BigDecimal additionalIncomes,
                       @NonNull BigDecimal additionalExpenses,
                       @NonNull BigDecimal additionalProjectedIncomes,
                       @NonNull BigDecimal additionalProjectedExpenses);
}
