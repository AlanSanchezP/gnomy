package io.github.alansanchezp.gnomy.ui;

import androidx.annotation.Nullable;

public interface ISpinnerItem {
    @Nullable String getDrawableResourceName();
    int getDrawableColor();
    int getId();
}