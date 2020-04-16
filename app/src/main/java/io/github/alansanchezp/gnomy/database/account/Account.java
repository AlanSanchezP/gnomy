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
    // TODO Icon handling
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
    private int type;

    @ColumnInfo(name="default_currency_code")
    @NonNull
    private String defaultCurrency = "USD";

    @ColumnInfo(name="show_in_dashboard")
    private boolean showInDashboard;

    @ColumnInfo(name="is_archived")
    private boolean isArchived = false;

    @ColumnInfo(name="bg_color")
    // TODO Apply color conversion logic for this field too
    private int backgroundColor;

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
        this.type = type;
    }
}
