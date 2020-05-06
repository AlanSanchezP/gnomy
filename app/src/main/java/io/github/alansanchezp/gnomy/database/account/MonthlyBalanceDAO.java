package io.github.alansanchezp.gnomy.database.account;

import org.threeten.bp.YearMonth;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;


@Dao
public abstract class MonthlyBalanceDAO {
    @Transaction
    @Query("SELECT accounts.*,  " +
                "(accounts.initial_value + " +
                    "_monthly_balances.balance) " +
                    "as accumulated, " +
                "(accounts.initial_value + " +
                    "_monthly_balances.balance +" +
                    "_monthly_balances.projected) " +
                    "as projected " +
            "FROM accounts " +
            "JOIN " +
                "(SELECT monthly_balances.account_id, " +
                    "sum(monthly_balances.total_incomes - monthly_balances.total_expenses) as balance, " +
                    "sum(monthly_balances.projected_incomes - monthly_balances.projected_expenses) as projected " +
                    "FROM monthly_balances " +
                    "GROUP BY monthly_balances.account_id" +
                ") as _monthly_balances " +
            "ON accounts.account_id = _monthly_balances.account_id " +
            "WHERE accounts.is_archived = 0")
    abstract LiveData<List<AccountWithBalance>> _getAllLatest();

    @Transaction
    @Query("SELECT accounts.*,  " +
                "(accounts.initial_value + " +
                    "_monthly_balances.balance) " +
                    "as accumulated, " +
                "(accounts.initial_value + " +
                    "latest.balance) " +
                    "as projected " +
            "FROM accounts " +
            "JOIN " +
                "(SELECT monthly_balances.account_id, " +
                    "sum(monthly_balances.total_incomes - monthly_balances.total_expenses) as balance " +
                    "FROM monthly_balances " +
                    "WHERE balance_date <= :month " +
                    "GROUP BY monthly_balances.account_id" +
                ") as _monthly_balances " +
            "ON accounts.account_id = _monthly_balances.account_id " +
            "JOIN " +
                "(SELECT monthly_balances.account_id, " +
                    "sum(monthly_balances.total_incomes - monthly_balances.total_expenses) as balance " +
                    "FROM monthly_balances " +
                    "GROUP BY monthly_balances.account_id" +
                ") as latest " +
            "ON accounts.account_id = latest.account_id " +
            "WHERE accounts.is_archived = 0")
    abstract LiveData<List<AccountWithBalance>> _getAllFromMonth(YearMonth month);

    public LiveData<List<AccountWithBalance>> getAllFromMonth(YearMonth month) {
        if (YearMonth.now().equals(month)) {
            return _getAllLatest();
        } else {
            return _getAllFromMonth(month);
        }
    }

    @Query("SELECT monthly_balances.* " +
            "FROM monthly_balances " +
            "JOIN accounts " +
            "ON accounts.account_id = monthly_balances.account_id " +
            "WHERE monthly_balances.account_id = :accountId " +
            "AND accounts.is_archived = 0")
    abstract LiveData<List<MonthlyBalance>> getAllFromAccount(int accountId);

    @Query("SELECT * FROM monthly_balances WHERE account_id = :accountId AND balance_date = :month")
    abstract MonthlyBalance find(int accountId, YearMonth month);

    @Insert
    abstract void insert(MonthlyBalance... balances);

    @Update
    abstract void update(MonthlyBalance balance);
}
