package io.github.alansanchezp.gnomy.database.account;

import org.threeten.bp.OffsetDateTime;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "monthly_balances",
        foreignKeys = @ForeignKey(
            entity = Account.class,
            parentColumns = "account_id",
            childColumns = "account_id",
            onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("account_id")
)
public class MonthlyBalance {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "balance_id")
    private int id;

    @ColumnInfo(name = "account_id")
    private int accountId;

    @ColumnInfo(name = "balance_date")
    @NonNull
    private OffsetDateTime date = OffsetDateTime.now();

    @ColumnInfo(name = "total_incomes")
    @NonNull
    private BigDecimal totalIncomes = new BigDecimal(0);

    @ColumnInfo(name = "total_expenses")
    @NonNull
    private BigDecimal totalExpenses = new BigDecimal(0);

    @ColumnInfo(name = "accumulated_before")
    @NonNull
    private BigDecimal accumulatedBefore = new BigDecimal(0);

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    @NonNull
    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(@NonNull OffsetDateTime date) {
        this.date = date;
    }

    @NonNull
    public BigDecimal getTotalIncomes() {
        return totalIncomes;
    }

    public void setTotalIncomes(@NonNull BigDecimal totalIncomes) {
        this.totalIncomes = totalIncomes;
    }

    @NonNull
    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(@NonNull BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    @NonNull
    public BigDecimal getAccumulatedBefore() {
        return accumulatedBefore;
    }

    public void setAccumulatedBefore(@NonNull BigDecimal accumulatedBefore) {
        this.accumulatedBefore = accumulatedBefore;
    }
}
