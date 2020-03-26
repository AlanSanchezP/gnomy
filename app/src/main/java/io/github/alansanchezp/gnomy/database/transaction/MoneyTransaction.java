package io.github.alansanchezp.gnomy.database.transaction;

import org.threeten.bp.OffsetDateTime;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.currency.Currency;

@Entity(tableName = "transactions",
        foreignKeys = {
            @ForeignKey(
                entity = Currency.class,
                parentColumns = "currency_id",
                childColumns = "currency_id",
                onDelete = ForeignKey.RESTRICT
            ),
            @ForeignKey(
                entity = Account.class,
                parentColumns = "account_id",
                childColumns = "account_id",
                onDelete = ForeignKey.CASCADE
            ),
            @ForeignKey(
                entity = Category.class,
                parentColumns = "category_id",
                childColumns = "category_id",
                onDelete = ForeignKey.RESTRICT
            )
        },
        indices = {
            @Index("account_id"),
            @Index("category_id")
        }
)
public class MoneyTransaction {
    @Ignore
    public static final int INCOME = 1;
    @Ignore
    public static final int EXPENSE = 2;
    @Ignore
    public static final int TRANSFERENCE_INCOME = 3;
    @Ignore
    public static final int TRANSFERENCE_EXPENSE = 4;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="transaction_id")
    private int id;

    @ColumnInfo(name="currency_id")
    private int currency;

    @ColumnInfo(name="account_id")
    private int account;

    @ColumnInfo(name="category_id")
    private int category;

    @ColumnInfo(name="transaction_date")
    @NonNull
    private OffsetDateTime date = OffsetDateTime.now();

    @ColumnInfo(name="original_value")
    @NonNull
    private Long originalValue = 0L;

    @ColumnInfo(name="calculated_value")
    @NonNull
    private Long calculatedValue = 0L;

    @ColumnInfo(name="transaction_description")
    @NonNull
    private String description = "";

    @ColumnInfo(name="is_confirmed")
    private boolean isConfirmed;

    @ColumnInfo(name="transaction_type")
    private int type;

    @ColumnInfo(name="transaction_notes")
    @NonNull
    private String notes = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public int getAccount() {
        return account;
    }

    public void setAccount(int account) {
        this.account = account;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    @NonNull
    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(@NonNull OffsetDateTime date) {
        this.date = date;
    }

    @NonNull
    public Long getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(@NonNull Long originalValue) {
        this.originalValue = originalValue;
    }

    @NonNull
    public Long getCalculatedValue() {
        return calculatedValue;
    }

    public void setCalculatedValue(@NonNull Long calculatedValue) {
        this.calculatedValue = calculatedValue;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @NonNull
    public String getNotes() {
        return notes;
    }

    public void setNotes(@NonNull String notes) {
        this.notes = notes;
    }
}
