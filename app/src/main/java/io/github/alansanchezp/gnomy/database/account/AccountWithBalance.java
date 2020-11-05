package io.github.alansanchezp.gnomy.database.account;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class AccountWithBalance {
    @Embedded
    public Account account;

    // TODO: Find a better naming for these 2 fields
    // queries on DAO are confusing because of these names
    @ColumnInfo(name="current")
    public BigDecimal currentBalance;
    @ColumnInfo(name="end_of_month")
    public BigDecimal endOfMonthBalance;
    @ColumnInfo(name="unresolved_transactions")
    public BigDecimal unresolvedTransactions;

    @NonNull
    public String toString() {
        return account.getName() + " " + currentBalance.toString();
    }
}
