package io.github.alansanchezp.gnomy.database.transaction;

import java.time.OffsetDateTime;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalanceDAO;
import io.github.alansanchezp.gnomy.database.category.Category;

@Dao
public abstract class MoneyTransactionDAO implements MonthlyBalanceDAO {
    public static final String BASE_JOIN_FOR_QUERIES =
            "SELECT transactions.*," +
                    "categories.category_name, " +
                    "categories.bg_color AS category_color, " +
                    "categories.category_icon AS category_resource_name, " +
                    "accounts.account_name AS account_name," +
                    "transfer_accounts.account_name AS transfer_destination_account_name " +
                    "FROM transactions " +
                    "JOIN categories " +
                    "USING(category_id) " +
                    "JOIN accounts " +
                    "USING(account_id) " +
                    "LEFT OUTER JOIN accounts AS transfer_accounts " +
                    "ON transactions.transfer_destination_account_id = transfer_accounts.account_id " +
                    "WHERE accounts.is_archived = 0 ";

    @RawQuery(observedEntities = {MoneyTransaction.class, Account.class, Category.class})
    protected abstract LiveData<List<TransactionDisplayData>> getWithRawQuery(SupportSQLiteQuery query);

    @Insert
    protected abstract long _insert(MoneyTransaction transaction);

    @Query("SELECT * FROM transactions WHERE transaction_id = :id;")
    protected abstract LiveData<MoneyTransaction> find(int id);

    @Query("SELECT * FROM transactions WHERE transaction_id = :id;")
    protected abstract MoneyTransaction _find(int id);

    @Delete
    protected abstract int _delete(MoneyTransaction... transaction);

    @Query("SELECT * FROM transactions WHERE transaction_date = :date " +
            "AND account_id = :destinationAccountId " +
            "AND transfer_destination_account_id = :originAccountId;")
    protected abstract MoneyTransaction _findMirrorTransfer(OffsetDateTime date,
                                                            int originAccountId,
                                                            Integer destinationAccountId);

    @Update
    protected abstract Integer _update(MoneyTransaction transaction);
}
