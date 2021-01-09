package io.github.alansanchezp.gnomy.data.transaction;

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
import io.github.alansanchezp.gnomy.data.GnomyIllegalQueryException;
import io.github.alansanchezp.gnomy.data.account.Account;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;

@Entity(tableName = "transactions",
        foreignKeys = {
            @ForeignKey(
                entity = Account.class,
                parentColumns = "account_id",
                childColumns = "account_id",
                onDelete = ForeignKey.CASCADE
            ),
            @ForeignKey(
                entity = Account.class,
                parentColumns = "account_id",
                childColumns = "transfer_destination_account_id",
                onDelete = ForeignKey.SET_NULL
            ),
            @ForeignKey(
                entity = Category.class,
                parentColumns = "category_id",
                childColumns = "category_id",
                onDelete = ForeignKey.RESTRICT
            )
        },
        indices = {
            // TODO: Indices
            @Index("account_id"),
            @Index("category_id"),
            @Index("transfer_destination_account_id")
        }
)
public class MoneyTransaction {
    @Ignore
    public static final int INCOME = 1;
    @Ignore
    public static final int EXPENSE = 2;
    @Ignore
    public static final int TRANSFER = 3;
    // Special transaction type for internal business logic, not meant to
    //  ever be presented to user.
    @Ignore
    protected static final int TRANSFER_MIRROR = 4;
    @Ignore
    public static final BigDecimal MIN_VALUE = BigDecimalUtil.ZERO;
    @Ignore
    public static final BigDecimal MAX_VALUE = BigDecimalUtil.fromString("900000000000");


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

    @ColumnInfo(name="transfer_destination_account_id")
    private Integer transferDestinationAccount;

    @ColumnInfo(name="category_id")
    private int category;

    @ColumnInfo(name="transaction_date")
    @NonNull
    private OffsetDateTime date = DateUtil.OffsetDateTimeNow();

    @ColumnInfo(name="original_value")
    @NonNull
    private BigDecimal originalValue = BigDecimalUtil.ZERO;

    @ColumnInfo(name="calculated_value")
    @NonNull
    private BigDecimal calculatedValue = BigDecimalUtil.ZERO;

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

    public Integer getTransferDestinationAccount() {
        return transferDestinationAccount;
    }

    public void setTransferDestinationAccount(Integer transferDestinationAccount) {
        this.transferDestinationAccount = transferDestinationAccount;
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

    /**
     * Only meant for direct use from Room.
     * @deprecated Use {@link #setOriginalValue(String)} instead.
     * @param originalValue Original value
     */
    @Deprecated
    public void setOriginalValue(@NonNull BigDecimal originalValue) {
        this.originalValue = originalValue;
    }

    @NonNull
    public BigDecimal getCalculatedValue() {
        return calculatedValue;
    }

    /**
     * Only meant for direct use from Room.
     * @deprecated Use {@link #setCalculatedValue(String)} instead.
     * @param calculatedValue Calculated value.
     */
    @Deprecated
    protected void setCalculatedValue(@NonNull BigDecimal calculatedValue) {
        this.calculatedValue = calculatedValue;
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
        switch(type) {
            case INCOME:
            case EXPENSE:
            case TRANSFER:
            case TRANSFER_MIRROR:
                this.type = type;
                break;
            default:
                throw new RuntimeException("Invalid transaction type.");
        }
    }

    @NonNull
    public String getNotes() {
        return notes;
    }

    public void setNotes(@NonNull String notes) {
        this.notes = notes;
    }

    /**
     * Sets the original value of the transaction. Original means
     * that it is the value in the currency specified in {@link #currency}
     *
     * @param stringValue           String representation of the number.
     * @throws NumberFormatException    If String is not a valid number.
     */
    public void setOriginalValue(@NonNull String stringValue)
            throws NumberFormatException {
        this.originalValue = BigDecimalUtil.fromString(stringValue);
    }

    /**
     * Sets the calculated value of the transaction. Calculated means
     * that it is the value in the currency specified in {@link Account#getDefaultCurrency()},
     * so it has to be previously processed with an exchange rate.
     *
     * @param stringValue           String representation of the number.
     * @throws NumberFormatException    If String is not a valid number.
     */
    protected void setCalculatedValue(@NonNull String stringValue)
            throws NumberFormatException {
        this.originalValue = BigDecimalUtil.fromString(stringValue);
    }

    /**
     * Checks for errors in the data of the transaction that can be identified
     * without accessing other entities or the database itself.
     *
     * @return  Null if no error is found. GnomyIllegalQueryException instance otherwise.
     */
    @Ignore
    protected GnomyIllegalQueryException getIsolatedValidationError() {
        if (this.type == TRANSFER_MIRROR)
            return new GnomyIllegalQueryException("Direct manipulation of mirror transfers is not allowed.");
        if (this.account == 0)
            return new GnomyIllegalQueryException("Associated account cannot be null");
        if (this.category == 0 && this.type != TRANSFER) {
            return new GnomyIllegalQueryException("Associated category cannot be null");
        }
        if (!BigDecimalUtil.isInRange(MIN_VALUE, MAX_VALUE, this.originalValue))
            return new GnomyIllegalQueryException("Transaction amount out of range.");
        if (this.concept.equals(""))
            return new GnomyIllegalQueryException("Empty transaction concept.");
        if (this.type == TRANSFER) {
            this.category = 1; // Hardcoding transfer category
            if (this.transferDestinationAccount == null)
                return new GnomyIllegalQueryException("Transfers must have a destination account.");
            if (this.account == this.transferDestinationAccount)
                return new GnomyIllegalQueryException("Cannot create transfer with same origin and destination account.");
            if (!this.isConfirmed)
                return new GnomyIllegalQueryException("Transfers must be confirmed transactions.");
            if (this.date.isAfter(DateUtil.OffsetDateTimeNow()))
                return new GnomyIllegalQueryException("Transfers cannot be future transactions.");
        } else if (this.transferDestinationAccount != null)
            return new GnomyIllegalQueryException("Non-transfer transactions cannot have a destination account.");

        return null;
    }

    /**
     * Returns a new instance with the opposite {@link #calculatedValue}.
     *
     * @return  Inverse transaction.
     */
    @Ignore
    protected MoneyTransaction getInverse() {
        MoneyTransaction inverted = new MoneyTransaction();
        inverted.date = this.date;
        inverted.account = this.account;
        inverted.isConfirmed = this.isConfirmed;
        inverted.type = this.type;
        inverted.calculatedValue = this.calculatedValue.negate();

        return inverted;
    }

    /**
     * Returns the mirror of the given transfer, switching {@link #account},
     * {@link #transferDestinationAccount} and {@link #type}
     *
     * @return  Null if transaction is not a transfer-related type.
     *          Mirror transfer otherwise.
     * @throws RuntimeException If, for any reason, the transaction type is not
     * a valid one.
     */
    @Ignore
    protected MoneyTransaction getMirrorTransfer() {
        if (this.type == INCOME || this.type == EXPENSE)
            return null;
        MoneyTransaction mirror = new MoneyTransaction();
        mirror.concept = this.concept;
        mirror.originalValue = this.originalValue;
        mirror.currency = this.currency;
        mirror.date = this.date;
        mirror.category = this.category;
        mirror.isConfirmed = this.isConfirmed;
        mirror.account = this.transferDestinationAccount;
        mirror.transferDestinationAccount = this.account;
        mirror.notes = this.notes;
        if (this.type == TRANSFER)
            mirror.type = TRANSFER_MIRROR;
        else if (this.type == TRANSFER_MIRROR)
            mirror.type = TRANSFER;
        else
            throw new RuntimeException("Original transfer type is somehow invalid.");

        return mirror;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoneyTransaction that = (MoneyTransaction) o;

        return id == that.id &&
                account == that.account &&
                Objects.equals(transferDestinationAccount,
                        that.transferDestinationAccount) &&
                category == that.category &&
                isConfirmed == that.isConfirmed &&
                type == that.type &&
                concept.equals(that.concept) &&
                currency.equals(that.currency) &&
                date.equals(that.date) &&
                originalValue.equals(that.originalValue) &&
                calculatedValue.equals(that.calculatedValue) &&
                notes.equals(that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, concept, currency, account, transferDestinationAccount, category, date, originalValue, calculatedValue, isConfirmed, type, notes);
    }
}
