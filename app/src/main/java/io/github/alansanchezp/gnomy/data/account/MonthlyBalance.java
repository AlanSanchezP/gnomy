package io.github.alansanchezp.gnomy.data.account;

import java.time.YearMonth;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(tableName = "monthly_balances",
        primaryKeys = {
            "account_id",
            "balance_date"
        },
        foreignKeys = @ForeignKey(
            entity = Account.class,
            parentColumns = "account_id",
            childColumns = "account_id",
            onDelete = ForeignKey.CASCADE
        ),
        indices = {
            @Index("account_id"),
            @Index("balance_date")
        }
)
public class MonthlyBalance {
    @ColumnInfo(name = "account_id")
    private int accountId;

    @ColumnInfo(name = "balance_date")
    @NonNull
    private YearMonth date = YearMonth.now();

    @ColumnInfo(name = "total_incomes", defaultValue = "0")
    @NonNull
    private BigDecimal totalIncomes = new BigDecimal(0);

    @ColumnInfo(name = "total_expenses", defaultValue = "0")
    @NonNull
    private BigDecimal totalExpenses = new BigDecimal(0);

    @ColumnInfo(name = "projected_incomes", defaultValue = "0")
    @NonNull
    private BigDecimal projectedIncomes = new BigDecimal(0);

    @ColumnInfo(name = "projected_expenses", defaultValue = "0")
    @NonNull
    private BigDecimal projectedExpenses = new BigDecimal(0);

    @Ignore
    public MonthlyBalance(Account account) {
        this.setAccountId(account.getId());
    }

    public MonthlyBalance() { }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    @NonNull
    public YearMonth getDate() {
        return date;
    }

    public void setDate(@NonNull YearMonth date) {
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
    public BigDecimal getProjectedIncomes() {
        return projectedIncomes;
    }

    public void setProjectedIncomes(@NonNull BigDecimal projectedIncomes) {
        this.projectedIncomes = projectedIncomes;
    }

    @NonNull
    public BigDecimal getProjectedExpenses() {
        return projectedExpenses;
    }

    public void setProjectedExpenses(@NonNull BigDecimal projectedExpenses) {
        this.projectedExpenses = projectedExpenses;
    }
}
