package io.github.alansanchezp.gnomy.database.account;

import org.threeten.bp.OffsetDateTime;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {
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
    public static final BigDecimal MIN_INITIAL = new BigDecimal("0");
    @Ignore
    public static final BigDecimal MAX_INITIAL = new BigDecimal("900000000000000");
    @Ignore
    public static final int DECIMAL_SCALE = 4;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="account_id")
    private int id;

    @ColumnInfo(name="account_name")
    @NonNull
    private String name = "";

    @ColumnInfo(name="created_at")
    @NonNull
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @ColumnInfo(name="initial_value")
    @NonNull
    private BigDecimal initialValue = new BigDecimal(0);

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

    public Account() {}

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
    public void setInitialValue(String stringValue) throws NumberFormatException {
        this.initialValue = new BigDecimal(stringValue)
                .setScale(DECIMAL_SCALE, BigDecimal.ROUND_HALF_EVEN);
    }
}
