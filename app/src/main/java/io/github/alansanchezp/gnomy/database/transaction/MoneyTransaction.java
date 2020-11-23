package io.github.alansanchezp.gnomy.database.transaction;
// TODO: Transaction, Transfer and ReccurrentTransaction repositories
// Create them after before (or in parallel) to their corresponding
// UI classes.
import java.time.OffsetDateTime;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.category.Category;

@Entity(tableName = "transactions",
        foreignKeys = {
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

    @ColumnInfo(name="transaction_concept")
    @NonNull
    private String concept = "";

    @ColumnInfo(name="transaction_currency")
    @NonNull
    private String currency = "USD";

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
    private boolean isConfirmed = true;

    @ColumnInfo(name="transaction_type")
    private int type = EXPENSE;

    @ColumnInfo(name="transaction_notes")
    @NonNull
    private String notes = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getConcept() {
        return concept;
    }

    public void setConcept(@NonNull String concept) {
        this.concept = concept;
    }

    @NonNull
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(@NonNull String currency) {
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
