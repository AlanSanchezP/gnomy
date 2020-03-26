package io.github.alansanchezp.gnomy.database.currency;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "currencies")
public class Currency {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="currency_id")
    private int id;

    @ColumnInfo(name="currency_symbol")
    private char symbol;

    @ColumnInfo(name="iso_code")
    @NonNull
    private String code = "";

    @ColumnInfo(name="is_global_default")
    private boolean isGlobalDefault;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    public void setCode(@NonNull String code) {
        this.code = code;
    }

    public boolean isGlobalDefault() {
        return isGlobalDefault;
    }

    public void setGlobalDefault(boolean globalDefault) {
        isGlobalDefault = globalDefault;
    }
}
