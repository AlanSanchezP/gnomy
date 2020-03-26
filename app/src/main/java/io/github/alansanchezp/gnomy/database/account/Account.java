package io.github.alansanchezp.gnomy.database.account;

import org.threeten.bp.OffsetDateTime;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import io.github.alansanchezp.gnomy.database.currency.Currency;

@Entity(tableName = "accounts",
        foreignKeys = @ForeignKey(
            entity = Currency.class,
            parentColumns = "currency_id",
            childColumns = "default_currency_id",
            onDelete = ForeignKey.RESTRICT
        )
)
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

    @ColumnInfo(name="default_currency_id")
    private int defaultCurrency;

    @ColumnInfo(name="include_in_total")
    private boolean includeInTotal;

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

    public int getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(int defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public boolean isIncludeInTotal() {
        return includeInTotal;
    }

    public void setIncludeInTotal(boolean includeInTotal) {
        this.includeInTotal = includeInTotal;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
