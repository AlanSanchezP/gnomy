package io.github.alansanchezp.gnomy.database.account;

import java.time.OffsetDateTime;

import java.math.BigDecimal;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ui.ISpinnerItem;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;

@Entity(tableName = "accounts")
public class Account implements ISpinnerItem {
    @Ignore
    public static final int BANK = 1;
    @Ignore
    public static final int INFORMAL = 2;
    @Ignore
    public static final int SAVINGS = 3;
    @Ignore
    public static final int INVERSIONS = 4;
    @Ignore
    public static final int CREDIT_CARD = 5;
    @Ignore
    public static final int OTHER = 6;
    @Ignore
    public static final BigDecimal MIN_INITIAL = BigDecimalUtil.ZERO;
    @Ignore
    public static final BigDecimal MAX_INITIAL = BigDecimalUtil.fromString("900000000000000");

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="account_id")
    private int id;

    @ColumnInfo(name="account_name")
    @NonNull
    private String name = "";

    @ColumnInfo(name="created_at")
    @NonNull
    private OffsetDateTime createdAt = DateUtil.OffsetDateTimeNow();

    @ColumnInfo(name="initial_value")
    private BigDecimal initialValue;

    @ColumnInfo(name="account_type")
    private int type = BANK;

    @ColumnInfo(name="default_currency_code")
    @NonNull
    private String defaultCurrency = "USD";

    @ColumnInfo(name="show_in_dashboard")
    private boolean showInDashboard = true;

    @ColumnInfo(name="is_archived")
    private boolean isArchived = false;

    @ColumnInfo(name="bg_color")
    private int backgroundColor;

    public Account() {
        setInitialValue("0");
    }

    @Ignore
    public static int getDrawableResourceId(int type) {
        switch (type) {
            case BANK:
                return R.drawable.ic_account_balance_black_24dp;
            case INFORMAL:
                return R.drawable.ic_account_balance_piggy_black_24dp;
            case SAVINGS:
                return R.drawable.ic_account_balance_savings_black_24dp;
            case INVERSIONS:
                return R.drawable.ic_account_balance_inversion_black_24dp;
            case CREDIT_CARD:
                return R.drawable.ic_account_balance_credit_card_black_24dp;
            case OTHER:
            default:
                return R.drawable.ic_account_balance_wallet_black_24dp;
        }
    }

    @Ignore
    public static int getTypeNameResourceId(int type) {
        switch (type) {
            case BANK:
                return R.string.account_type_bank;
            case INFORMAL:
                return R.string.account_type_informal;
            case SAVINGS:
                return R.string.account_type_savings;
            case INVERSIONS:
                return R.string.account_type_inversions;
            case CREDIT_CARD:
                return R.string.account_type_credit_card;
            case OTHER:
            default:
                return R.string.account_type_other;
        }
    }

    @Ignore
    public Account(int accountId) {
        this();
        this.id = accountId;
    }

    @Ignore
    public Account(Account original) {
        this.id = original.getId();
        this.name = original.getName();
        this.createdAt = original.getCreatedAt();
        this.initialValue = original.getInitialValue();
        this.type = original.getType();
        this.defaultCurrency = original.getDefaultCurrency();
        this.showInDashboard = original.isShowInDashboard();
        this.isArchived = original.isArchived();
        this.backgroundColor = original.getBackgroundColor();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @NonNull
    public BigDecimal getInitialValue() {
        return initialValue;
    }

    @Deprecated
    public void setInitialValue(@NonNull BigDecimal initialValue) {
        this.initialValue = initialValue;
    }

    @NonNull
    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(@NonNull String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public boolean isShowInDashboard() {
        return showInDashboard;
    }

    public void setShowInDashboard(boolean showInDashboard) {
        this.showInDashboard = showInDashboard;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        switch (type) {
            case BANK:
            case INFORMAL:
            case SAVINGS:
            case INVERSIONS:
            case CREDIT_CARD:
                this.type = type;
                break;
            case OTHER:
            default:
                this.type = OTHER;
        }
    }

    // Custom Getters and setters
    public void setInitialValue(@NonNull String stringValue)
            throws NumberFormatException {
        this.initialValue = BigDecimalUtil.fromString(stringValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;

        return id == account.id &&
                type == account.type &&
                showInDashboard == account.showInDashboard &&
                isArchived == account.isArchived &&
                backgroundColor == account.backgroundColor &&
                name.equals(account.name) &&
                createdAt.equals(account.createdAt) &&
                initialValue.equals(account.initialValue) &&
                defaultCurrency.equals(account.defaultCurrency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, createdAt, initialValue, type, defaultCurrency, showInDashboard, isArchived, backgroundColor);
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }

    @Nullable
    @Override
    @Ignore
    public String getDrawableResourceName() {
        if (id == 0) return null;
        switch (type) {
            case BANK:
                return "ic_account_balance_black_24dp";
            case INFORMAL:
                return "ic_account_balance_piggy_black_24dp";
            case SAVINGS:
                return "ic_account_balance_savings_black_24dp";
            case INVERSIONS:
                return "ic_account_balance_inversion_black_24dp";
            case CREDIT_CARD:
                return "ic_account_balance_credit_card_black_24dp";
            case OTHER:
            default:
                return "ic_account_balance_wallet_black_24dp";
        }
    }

    @Override
    @Ignore
    public int getDrawableColor() {
        return backgroundColor;
    }
}
