package io.github.alansanchezp.gnomy.database.transaction;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

/**
 * Helper class to easily display a transaction data to the user.
 */
public class TransactionDisplayData {
    @Embedded
    public MoneyTransaction transaction;
    @ColumnInfo(name = "category_name")
    public String categoryName;
    @ColumnInfo(name = "category_resource_name")
    public String categoryResourceName;
    @ColumnInfo(name = "category_color")
    public int categoryColor;
    @ColumnInfo(name = "account_name")
    public String accountName;
    @ColumnInfo(name = "transfer_destination_account_name")
    public String transferDestinationAccountName;
}
