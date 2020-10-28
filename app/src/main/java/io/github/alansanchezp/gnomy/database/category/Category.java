package io.github.alansanchezp.gnomy.database.category;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @Ignore
    public static final int EXPENSE_CATEGORY = 1;
    @Ignore
    public static final int INCOME_CATEGORY = 2;
    @Ignore
    public static final int BOTH_CATEGORY = 3;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="category_id")
    private int id;

    @ColumnInfo(name="category_name")
    @NonNull
    private String name = "";

    @ColumnInfo(name="category_icon")
    @NonNull
    private String icon = "";

    @ColumnInfo(name="category_type")
    private int type = EXPENSE_CATEGORY;

    @ColumnInfo(name="bg_color")
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
    public String getIcon() {
        return icon;
    }

    public void setIcon(@NonNull String icon) {
        this.icon = icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
