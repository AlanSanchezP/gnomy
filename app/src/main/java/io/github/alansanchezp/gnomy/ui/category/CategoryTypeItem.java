package io.github.alansanchezp.gnomy.ui.category;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.alansanchezp.gnomy.util.ISpinnerItem;

import static io.github.alansanchezp.gnomy.data.category.Category.EXPENSE_CATEGORY;
import static io.github.alansanchezp.gnomy.data.category.Category.INCOME_CATEGORY;
import static io.github.alansanchezp.gnomy.data.category.Category.BOTH_CATEGORY;

public class CategoryTypeItem implements ISpinnerItem {
    private final int type;
    private final String name;
    public CategoryTypeItem(int type, String name) {
        this.type = type;
        this.name = name;
    }

    @Nullable
    @Override
    public String getDrawableResourceName() {
        switch (type) {
            case EXPENSE_CATEGORY:
                return "ic_minus_24";
            case INCOME_CATEGORY:
                return "ic_add_black_24dp";
            case BOTH_CATEGORY:
            default:
                return "ic_plus_and_minus";
        }
    }

    @Override
    public int getDrawableColor() {
        return Color.BLACK;
    }

    @Override
    public int getId() {
        return type;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof CategoryTypeItem)) return false;
        return this.type == ((CategoryTypeItem) obj).type;
    }
}
