package io.github.alansanchezp.gnomy.database.transaction;
// TODO: Transaction, Transfer and ReccurrentTransaction repositories
// Create them after before (or in parallel) to their corresponding
// UI classes.
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;

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
    @Ignore
    public static final BigDecimal MIN_INITIAL = BigDecimalUtil.ZERO;
    @Ignore
    public static final BigDecimal MAX_INITIAL = BigDecimalUtil.fromString("900000000000");


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
    private BigDecimal originalValue = BigDecimalUtil.ZERO;

    @ColumnInfo(name="calculated_value")
    @NonNull
    private BigDecimal calculatedValue = BigDecimalUtil.ZERO;

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
    public BigDecimal getOriginalValue() {
        return originalValue;
    }

    @Deprecated
    public void setOriginalValue(@NonNull BigDecimal originalValue) {
        this.originalValue = originalValue;
    }

    @NonNull
    public BigDecimal getCalculatedValue() {
        return calculatedValue;
    }

    @Deprecated
    public void setCalculatedValue(@NonNull BigDecimal calculatedValue) {
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

    // Custom setters
    public void setOriginalValue(@NonNull String stringValue)
            throws NumberFormatException {
        this.originalValue = BigDecimalUtil.fromString(stringValue);
    }

    public void setCalculatedValue(@NonNull String stringValue)
            throws NumberFormatException {
        this.originalValue = BigDecimalUtil.fromString(stringValue);
    }

    // Custom methods
    @Ignore
    public MoneyTransaction getInverse() {
        MoneyTransaction inverted = new MoneyTransaction();
        inverted.calculatedValue = this.calculatedValue;
        inverted.date = this.date;
        inverted.account = this.account;
        inverted.isConfirmed = this.isConfirmed;

        if (this.type == INCOME) {
            inverted.type = EXPENSE;
        } else if (this.type == EXPENSE) {
            inverted.type = INCOME;
        } else if (this.type == TRANSFERENCE_INCOME) {
            inverted.type = TRANSFERENCE_EXPENSE;
        } else {
            inverted.type = TRANSFERENCE_INCOME;
        }
        return inverted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoneyTransaction that = (MoneyTransaction) o;

        return id == that.id &&
                account == that.account &&
                category == that.category &&
                isConfirmed == that.isConfirmed &&
                type == that.type &&
                concept.equals(that.concept) &&
                currency.equals(that.currency) &&
                date.equals(that.date) &&
                originalValue.equals(that.originalValue) &&
                calculatedValue.equals(that.calculatedValue) &&
                description.equals(that.description) &&
                notes.equals(that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, concept, currency, account, category, date, originalValue, calculatedValue, description, isConfirmed, type, notes);
    }
}
