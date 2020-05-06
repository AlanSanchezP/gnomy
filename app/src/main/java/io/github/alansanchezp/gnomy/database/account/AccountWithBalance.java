package io.github.alansanchezp.gnomy.database.account;

import java.math.BigDecimal;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class AccountWithBalance {
    @Embedded
    public Account account;

    @ColumnInfo(name="accumulated")
    public BigDecimal accumulatedBalance;
    @ColumnInfo(name="projected")
    public BigDecimal projectedBalance;

    public String toString() {
        return account.getName() + " " + accumulatedBalance.toString();
    }
}
