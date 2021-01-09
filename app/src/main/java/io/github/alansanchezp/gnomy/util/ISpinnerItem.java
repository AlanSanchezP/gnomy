package io.github.alansanchezp.gnomy.util;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.Nullable;

/**
 * Helper interface to allow objects to display an icon inside
 * spinners.
 */
public interface ISpinnerItem {
    /**
     * Finds the first item in a {@link List} that matches the given
     * object id. Useful to use spinner's setSelection() method.
     *
     * @param list      List to scan.
     * @param itemId    Id to match.
     * @return          Index of the first matching element.
     */
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

    /**
     * Returns the resource NAME of the associated icon.
     * Not using resource id so that Room Entity classes
     * can be loaded for unit tests.
     *
     * @return  Name of drawable resource. Null if no icon is desired for
     *          a specific item.
     */
    @Nullable String getDrawableResourceName();

    /**
     * Gets the color to use for the item's icon.
     *
     * @return  Color.
     */
    int getDrawableColor();

    /**
     * Gets a unique identifier (among items of the same class)
     * for the given object.
     * @return  Unique identifier.
     */
    int getId();
}