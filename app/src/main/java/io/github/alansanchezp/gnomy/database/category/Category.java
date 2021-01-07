package io.github.alansanchezp.gnomy.database.category;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import io.github.alansanchezp.gnomy.util.ISpinnerItem;

@Entity(tableName = "categories")
public class Category implements ISpinnerItem {
    @Ignore
    public static final int EXPENSE_CATEGORY = 1;
    @Ignore
    public static final int INCOME_CATEGORY = 2;
    @Ignore
    public static final int BOTH_CATEGORY = 3;
    @Ignore
    protected static final int HIDDEN_CATEGORY = 4;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="category_id")
    private int id;

    @ColumnInfo(name="category_name")
    @NonNull
    private String name = "";

    @ColumnInfo(name="category_icon")
    @NonNull
    private String iconResName = "";

    @ColumnInfo(name="category_type")
    private int type = EXPENSE_CATEGORY;

    @ColumnInfo(name="can_delete")
    private boolean deletable = true;

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
    public String getIconResName() {
        return iconResName;
    }

    public void setIconResName(@NonNull String iconResName) {
        this.iconResName = iconResName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        switch(type) {
            case EXPENSE_CATEGORY:
            case INCOME_CATEGORY:
            case BOTH_CATEGORY:
            case HIDDEN_CATEGORY:
                this.type = type;
                break;
            default:
                throw new RuntimeException("Invalid category type.");
        }
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public final boolean isDeletable() {
        return deletable;
    }

    protected void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Nullable
    @Override
    @Ignore
    public String getDrawableResourceName() {
        if (iconResName.equals("")) return null;
        return iconResName;
    }

    @Override
    @Ignore
    public int getDrawableColor() {
        return backgroundColor;
    }
}
