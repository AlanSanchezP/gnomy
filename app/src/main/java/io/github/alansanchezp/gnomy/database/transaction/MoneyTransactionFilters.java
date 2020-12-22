package io.github.alansanchezp.gnomy.database.transaction;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;

import androidx.annotation.Nullable;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;

public class MoneyTransactionFilters implements Parcelable {
    private int accountId = 0;
    private int transferDestinationAccountId = 0;
    private int transactionType = ALL_TRANSACTION_TYPES;
    private int sortingMethod = MOST_RECENT;
    private int categoryId = 0;
    private int transactionStatus = ANY_STATUS;
    private OffsetDateTime startDate = null;
    private OffsetDateTime endDate = null;
    private BigDecimal minAmount = null;
    private BigDecimal maxAmount = null;

    public static final int MOST_RECENT = 0;
    public static final int LEAST_RECENT = 1;

    public static final int ALL_TRANSACTION_TYPES = 0;

    public static final int NO_STATUS = -1;
    public static final int ANY_STATUS = 0;
    public static final int CONFIRMED_STATUS = 1;
    public static final int UNCONFIRMED_STATUS = 2;

    public MoneyTransactionFilters() {
        /* Empty constructor */
    }

    private MoneyTransactionFilters(Parcel in) {
        accountId = in.readInt();
        transferDestinationAccountId = in.readInt();
        transactionType = in.readInt();
        sortingMethod = in.readInt();
        categoryId = in.readInt();
        transactionStatus = in.readInt();
    }

    public static final Parcelable.Creator<MoneyTransactionFilters> CREATOR
            = new Parcelable.Creator<MoneyTransactionFilters>() {
        public MoneyTransactionFilters createFromParcel(Parcel in) {
            return new MoneyTransactionFilters(in);
        }

        public MoneyTransactionFilters[] newArray(int size) {
            return new MoneyTransactionFilters[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(accountId);
        dest.writeInt(transferDestinationAccountId);
        dest.writeInt(transactionType);
        dest.writeInt(sortingMethod);
        dest.writeInt(categoryId);
        dest.writeInt(transactionStatus);
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getTransferDestinationAccountId() {
        return transferDestinationAccountId;
    }

    public void setTransferDestinationAccountId(int transferDestinationAccountId) {
        this.transferDestinationAccountId = transferDestinationAccountId;
    }

    public int getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(int transactionType) {
        switch (transactionType) {
            case ALL_TRANSACTION_TYPES:
            case MoneyTransaction.INCOME:
            case MoneyTransaction.EXPENSE:
            case MoneyTransaction.TRANSFER:
                this.transactionType = transactionType;
                break;
            default:
                throw new RuntimeException("Invalid transaction type.");
        }
    }

    public int getSortingMethod() {
        return sortingMethod;
    }

    public void setSortingMethod(int sortingMethod) {
        switch (sortingMethod) {
            case MOST_RECENT:
            case LEAST_RECENT:
                this.sortingMethod = sortingMethod;
                break;
            default:
                throw new RuntimeException("Invalid sorting method.");
        }
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(int transactionStatus) {
        switch (transactionStatus) {
            case NO_STATUS:
            case ANY_STATUS:
            case CONFIRMED_STATUS:
            case UNCONFIRMED_STATUS:
                this.transactionStatus = transactionStatus;
                break;
            default:
                throw new RuntimeException("Invalid transaction status.");
        }
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public void setMonth(YearMonth month) {
        OffsetDateTime[] monthBoundaries = DateUtil.getMonthBoundaries(month);
        this.startDate = OffsetDateTime.from(monthBoundaries[0]);
        this.endDate = OffsetDateTime.from(monthBoundaries[1]);
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(String value) {
        if (value == null) this.minAmount = null;
        else this.minAmount = BigDecimalUtil.fromString(value);
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(String value) {
        if (value == null) this.maxAmount = null;
        else this.maxAmount = BigDecimalUtil.fromString(value);
    }

    /**
     * Determines if the filter instance has all default values but transaction type.
     *
     *
     * @param month     YearMonth instance to match. If null, the method will
     *                  check that both startDate and endDate are null as well.
     * @return          True only if instance is a simple filter.
     */
    public boolean isSimpleFilterWithMonth(@Nullable YearMonth month) {
        if (this.sortingMethod != MOST_RECENT) return false;
        if (this.categoryId != 0) return false;
        if (this.accountId != 0) return false;
        if (this.transferDestinationAccountId != 0) return false;
        if (this.transactionStatus != ANY_STATUS) return false;
        if (this.minAmount != null) return false;
        if (this.maxAmount != null) return false;
        if (month == null) {
            return this.startDate == null && this.endDate == null;
        } else {
            OffsetDateTime[] monthBoundaries = DateUtil.getMonthBoundaries(month);
            if (!monthBoundaries[0].equals(this.startDate)) return false;
            if (!monthBoundaries[1].equals(this.endDate)) return false;
        }
        return true;
    }
}
