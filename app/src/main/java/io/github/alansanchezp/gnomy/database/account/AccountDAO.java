package io.github.alansanchezp.gnomy.database.account;

import java.time.YearMonth;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;

import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.reactivex.Single;

@Dao
public abstract class AccountDAO implements MonthlyBalanceDAO {

    @Query("SELECT " +
            "accounts.*, " +
            ":targetMonth as target_month, " +
            "accumulated_before_target_month.confirmed_before_month, " +
            "accumulated_before_target_month.pending_incomes_before_month, " +
            "accumulated_before_target_month.pending_expenses_before_month, " +
            "target_month_balance.confirmed_incomes_at_month, " +
            "target_month_balance.confirmed_expenses_at_month, " +
            "target_month_balance.pending_expenses_at_month, " +
            "target_month_balance.pending_incomes_at_month " +
            "FROM accounts " +
            "LEFT OUTER JOIN " +
            "(  SELECT " +
                // We retrieve projected/pending incomes and expenses
                //  separately to be able to tell the difference between
                //  a true 0 pending transactions case (no incomes/expenses
                //  were left unhandled) and the case were both types
                //  of transaction cancel each other (100-100=0), because
                //  even if the subtraction is 0, it still means that there
                //  are unresolved transactions the user might want to check
                "monthly_balances.account_id, " +
                "sum(monthly_balances.total_incomes - monthly_balances.total_expenses) " +
                    "AS confirmed_before_month, " +
                "sum(monthly_balances.projected_incomes)" +
                    "AS pending_incomes_before_month, " +
                "sum(monthly_balances.projected_expenses) " +
                    "AS pending_expenses_before_month " +
                "FROM monthly_balances " +
                "WHERE balance_date <= :targetMonth -1 " +
                "GROUP BY monthly_balances.account_id " +
            ") AS accumulated_before_target_month " +
            "USING(account_id) " +
            "LEFT OUTER JOIN " +
            // We retrieve the target month separately solely because
            //  it's the only way to tell the difference between
            //  unresolved PAST transactions and projected incomes
            //  and expenses in the case tha targetMonth == today
            "(  SELECT " +
                "monthly_balances.account_id, " +
                "monthly_balances.total_incomes " +
                    "AS confirmed_incomes_at_month, " +
                "monthly_balances.total_expenses " +
                    "AS confirmed_expenses_at_month, " +
                "monthly_balances.projected_expenses " +
                    "AS pending_expenses_at_month, " +
                "monthly_balances.projected_incomes " +
                    "AS pending_incomes_at_month " +
                "FROM monthly_balances " +
                "WHERE balance_date = :targetMonth" +
            ") AS target_month_balance " +
            "USING(account_id) " +
            "WHERE accounts.is_archived = 0 " +
            // Don't retrieve accounts created after the target month
            "AND CAST(strftime('%Y%m', datetime(accounts.created_at/1000, 'unixepoch', 'localtime')) AS int) " +
                    " <= :targetMonth;")
    protected abstract LiveData<List<AccountWithAccumulated>>
    getAccumulatesListAtMonth(YearMonth targetMonth);

    @Query("SELECT " +
            "accounts.*, " +
            "CAST(strftime('%Y%m', DATETIME('now', 'localtime')) AS INT) AS target_month, " +
            "accumulated_balances.confirmed_incomes_at_month, " +
            "accumulated_balances.confirmed_expenses_at_month " +
            "FROM accounts " +
            "LEFT OUTER JOIN " +
            "(  SELECT " +
                "monthly_balances.account_id, " +
                "sum(monthly_balances.total_incomes) " +
                    "AS confirmed_incomes_at_month," +
                "sum(monthly_balances.total_expenses) " +
                    "AS confirmed_expenses_at_month " +
                "FROM monthly_balances " +
                "WHERE balance_date <= CAST(strftime('%Y%m', DATETIME('now', 'localtime')) AS INT) " +
                "GROUP BY monthly_balances.account_id " +
            ") AS accumulated_balances " +
            "USING(account_id) " +
            "WHERE accounts.is_archived = 0;")
    protected abstract LiveData<List<AccountWithAccumulated>>
    getTodayAccumulatesList();

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT " +
            "accounts.*, " +
            ":targetMonth as target_month, " +
            "accumulated_before_target_month.confirmed_before_month, " +
            "target_month_balance.confirmed_incomes_at_month, " +
            "target_month_balance.confirmed_expenses_at_month, " +
            "target_month_balance.pending_expenses_at_month, " +
            "target_month_balance.pending_incomes_at_month " +
            "FROM accounts " +
            "LEFT OUTER JOIN " +
            "(  SELECT " +
                "monthly_balances.account_id, " +
                "sum(monthly_balances.total_incomes - monthly_balances.total_expenses) " +
                    "AS confirmed_before_month " +
                "FROM monthly_balances " +
                "WHERE balance_date <= :targetMonth -1 " +
                "GROUP BY monthly_balances.account_id " +
            ") AS accumulated_before_target_month " +
            "LEFT OUTER JOIN" +
            "(  SELECT " +
                "monthly_balances.account_id, " +
                "monthly_balances.total_incomes " +
                    "AS confirmed_incomes_at_month, " +
                "monthly_balances.total_expenses " +
                    "AS confirmed_expenses_at_month, " +
                "monthly_balances.projected_expenses " +
                    "AS pending_expenses_at_month, " +
                "monthly_balances.projected_incomes " +
                  "AS pending_incomes_at_month " +
                "FROM monthly_balances " +
                "WHERE balance_date == :targetMonth" +
            ") AS target_month_balance " +
            "USING(account_id) " +
            "WHERE accounts.account_id = :accountId")
    protected abstract LiveData<AccountWithAccumulated>
    getAccumulatedAtMonth(int accountId, YearMonth targetMonth);

    @Query("SELECT * FROM accounts WHERE is_archived = 0")
    protected abstract LiveData<List<Account>> getAll();

    @Query("SELECT * FROM accounts WHERE is_archived = 1")
    protected abstract LiveData<List<Account>> getArchivedAccounts();

    @Query("SELECT * FROM accounts WHERE account_id = :id")
    protected abstract LiveData<Account> find(int id);

    @Query("UPDATE OR ABORT accounts SET is_archived = 1 WHERE account_id = :id")
    protected abstract Single<Integer> archive(int id);

    @Query("UPDATE OR ABORT accounts SET is_archived = 0 WHERE account_id = :id")
    protected abstract Single<Integer> restore(int id);

    @Query("UPDATE OR ABORT accounts SET is_archived = 0 WHERE is_archived = 1")
    protected abstract Single<Integer> restoreAll();

    // SYNCHRONOUS OPERATIONS, NEVER USE DIRECTLY FROM REPOSITORY
    // WITHOUT WRAPPING THEM IN Single<> METHODS

    @Query("SELECT * FROM accounts WHERE account_id = :id")
    protected abstract Account _find(int id);

    @Insert
    protected abstract Long _insert(Account account);

    @Update
    protected abstract int _update(Account account);

    /**
     * !!! ONLY USE IN TANDEM AFTER _savePotentiallyOrphanTransfers() !!!
     *
     * @param account   Account to be deleted
     * @return          Number of affected rows
     */
    @Delete
    protected abstract int _delete(Account account);

    /**
     * !!! ONLY USE IN TANDEM BEFORE _delete() !!!
     *
     * The problem: When an {@link Account} is deleted all {@link MoneyTransaction} transfer objects
     * that pointed to it as a destination will be deleted too (due to ForeignKey constraints),
     * but their mirrored versions will remain in the database. This would cause the user
     * to be unable to access those transactions' data, as direct manipulation or access
     * to mirrored transfers is not allowed by design.
     *
     * Solution: This method converts mirror transfers into regular incomes so that the user
     * can still access them, without altering existing {@link MonthlyBalance} rows.
     * Additionally, it modifies the transaction's concept to indicate its orphan nature.
     *
     * Note that mirror transfers use MoneyTransaction.account_id to refer to the
     * recipient account id, and MoneyTransaction.transfer_destination_account_id to refer
     * to the ORIGIN account where the transfer comes from. Due to ForeignKey constraints,
     * MoneyTransaction.transfer_destination_account_id will be set to NULL after the account
     * is deleted. This guarantees that orphan mirror transfers will be effectively
     * treated as regular incomes after the original transfer is gone.
     *
     * @param accountId     Account that is about to be deleted
     * @return              Number of affected rows
     */
    @Query("UPDATE OR ABORT transactions SET " +
            "transaction_type = " + MoneyTransaction.INCOME + ", " +
            "transaction_concept = '(ORPHAN) ' || transaction_concept " +
            "WHERE transaction_type = 4 " + // Cannot access TRANSFER_MIRROR constant directly
            "AND transfer_destination_account_id = :accountId;")
    protected abstract int _savePotentiallyOrphanTransfers(int accountId);
}
