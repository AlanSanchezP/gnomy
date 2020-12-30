package io.github.alansanchezp.gnomy.util;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.Nullable;

public interface ISpinnerItem {
    static int getItemIndexById(List<? extends ISpinnerItem> list, int itemId) {
        ListIterator<? extends ISpinnerItem> iterator = list.listIterator();
        while (iterator.hasNext()) {
            int iteration = iterator.nextIndex();
            if (iterator.next().getId() == itemId) {
                return iteration;
            }
        }
        return -1;
    }
    @Nullable String getDrawableResourceName();
    int getDrawableColor();
    int getId();
}