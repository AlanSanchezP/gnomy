package io.github.alansanchezp.gnomy.database.account;

import java.time.YearMonth;

import java.math.BigDecimal;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;


@Dao
public abstract class MonthlyBalanceDAO {

    // TODO: SHOULD WE WORRY ABOUT THIS MASSIVE QUERY?
    //  I really couldn't find a better way to achieve the same results
    @Transaction
    @Query("SELECT accounts.*,  " +
                // Always return the current (as of literal today) balance
                //  for comparison purposes
                "(accounts.initial_value + " +
                    "sum_until_today.confirmed) " +
                "AS current, " +
                    // PAST MONTHS: Sum initial value + confirmed until that month
                    //  ALERT THE USER THEY HAVE UNRESOLVED OPERATIONS
                "CASE WHEN strftime(\"%Y%m\", \"now\") > :month " +
                    "THEN (accounts.initial_value + " +
                            "sum_until_target_month.confirmed) " +
                    // FUTURE MONTHS: Sum initial value + all confirmed balances
                    //  until today + pending ones (they are EXPECTED
                    //  to be resolved by the end of that month, right?
                    //  "CONFIRMED" future operations are not included
                    //  because they shouldn't exist in the first place
                    "WHEN strftime(\"%Y%m\", \"now\") < :month " +
                    "THEN (accounts.initial_value + " +
                            "sum_until_today.confirmed + " +
                            "sum_until_target_month.pending)" +
                    // CURRENT MONTH: This represents the EXPECTED
                    //  balance at the end of the month, so it includes pending
                    //  incomes and expenses
                    "ELSE (accounts.initial_value + " +
                            "sum_until_today.confirmed + " +
                            "sum_until_today.pending) " +
                    "END " +
                "AS end_of_month, " +
                // Both current and future months are not expected to
                //  trigger any alert if there are still pending operations
                "CASE WHEN strftime(\"%Y%m\", \"now\") <= :month " +
                "THEN null " +
                // Only alert the user if PAST operations are unresolved
                "ELSE sum_until_target_month.pending " +
                "END AS unresolved_transactions " +
            "FROM accounts " +
            "JOIN " +
                // Always calculate the today confirmed balance
                //  for reference purposes
                "(SELECT monthly_balances.account_id, " +
                        "sum(monthly_balances.total_incomes - monthly_balances.total_expenses) " +
                        "as confirmed," +
                        "sum(monthly_balances.projected_incomes - monthly_balances.projected_expenses) " +
                        "as pending " +
                    "FROM monthly_balances " +
                    "WHERE balance_date <= strftime(\"%Y%m\", \"now\") " +
                    "GROUP BY monthly_balances.account_id" +
                ") as sum_until_today " +
            "ON accounts.account_id = sum_until_today.account_id " +
            "LEFT OUTER JOIN " +
                // The query is basically the same because we want to retrieve
                //  equivalent data but for other months
                "(SELECT monthly_balances.account_id, " +
                        "sum(monthly_balances.total_incomes - monthly_balances.total_expenses) " +
                        "as confirmed, " +
                        "sum(monthly_balances.projected_incomes - monthly_balances.projected_expenses) " +
                        "as pending " +
                    "FROM monthly_balances " +
                    "WHERE balance_date <= :month " +
                    "GROUP BY monthly_balances.account_id" +
                ") as sum_until_target_month " +
            "ON accounts.account_id = sum_until_target_month.account_id " +
                // This SHOULD prevent the whole second JOIN query from
                //  being executed if target month is today
                //  as results would be identical and we would
                //  be wasting resources recalculating it
                "AND strftime(\"%Y%m\", \"now\") != :month " +
            // Only include non-archived accounts because those are basically dead
            "WHERE accounts.is_archived = 0")
    protected abstract LiveData<List<AccountWithBalance>> getAllFromMonth(YearMonth month);

    @Query("SELECT " +
                "accounts.initial_value + _monthly_balances.sum " +
            "FROM accounts " +
            "JOIN " +
                "(SELECT monthly_balances.account_id, " +
                        "sum(monthly_balances.total_incomes - monthly_balances.total_expenses) as sum " +
                    "FROM monthly_balances " +
                    "WHERE monthly_balances.balance_date <= :month " +
                    "GROUP BY monthly_balances.account_id" +
                ") as _monthly_balances " +
            "ON accounts.account_id = _monthly_balances.account_id " +
            "WHERE accounts.account_id = :accountId")
    protected abstract LiveData<BigDecimal> getAccumulatedFromMonth(int accountId, YearMonth month);

    @Transaction
    @Query("SELECT monthly_balances.* " +
            "FROM monthly_balances " +
            "JOIN accounts " +
            "ON accounts.account_id = monthly_balances.account_id " +
            "WHERE monthly_balances.account_id = :accountId " +
            "AND accounts.is_archived = 0")
    protected abstract LiveData<List<MonthlyBalance>> getAllFromAccount(int accountId);

    @Query("SELECT * FROM monthly_balances WHERE account_id = :accountId AND balance_date = :month")
    protected abstract LiveData<MonthlyBalance> find(int accountId, YearMonth month);

    // SYNCHRONOUS OPERATIONS, NEVER USE DIRECTLY FROM REPOSITORY
    // WITHOUT WRAPPING THEM IN Single<> METHODS

    @Insert
    protected abstract void _insert(MonthlyBalance... balances);

    @Update
    protected abstract void _update(MonthlyBalance balance);
}
