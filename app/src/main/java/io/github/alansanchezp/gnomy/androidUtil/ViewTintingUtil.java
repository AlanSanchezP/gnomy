package io.github.alansanchezp.gnomy.androidUtil;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Menu;
import android.widget.Switch;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.tiper.MaterialSpinner;

import java.util.Objects;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import io.github.alansanchezp.gnomy.util.ColorUtil;

/**
 * Helper class to tint elements without repeating too much code.
 */
public class ViewTintingUtil {

    /**
     * Tints all elements in a menu.
     *
     * @param menu              Menu that contains the items.
     * @param menuItemsResIds   Array of ids of the desired items to tint.
     * @param color             Color to use.
     *
     * @throws NullPointerException If the given menu doesn't contain any of
     * the specified items.
     */
    public static void tintMenuItems(@NonNull Menu menu,
                                     @NonNull int[] menuItemsResIds,
                                     @ColorInt int color) {
        for (int resId : menuItemsResIds) {
            menu.findItem(resId)
                    .getIcon()
                    .setTint(color);
        }
    }

    // XXX: [#45] Evaluate if a batch tinting method is necessary.

    public static void tintFAB(@NonNull FloatingActionButton fab,
                               @ColorInt int bgColor,
                               @ColorInt int drawableColor) {
        tintFAB(fab, bgColor, drawableColor, ColorUtil.getRippleVariant(bgColor));
    }

    /**
     * Tints a {@link FloatingActionButton} object.
     *
     * @param fab           FloatingActionButton instance.
     * @param bgColor       Background color to use.
     * @param drawableColor Color to use in the drawable inside the button.
     * @param rippleColor   Custom ripple color.
     */
    public static void tintFAB(@NonNull FloatingActionButton fab,
                               @ColorInt int bgColor,
                               @ColorInt int drawableColor,
                               @ColorInt int rippleColor) {
        fab.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        fab.getDrawable().mutate().setTint(drawableColor);
        fab.setRippleColor(rippleColor);
    }

    /**
     * Tints a {@link TextInputLayout} stroke and hint.
     *
     * @param til           TextInputLayout instance.
     * @param strokeColor   Color to use.
     */
    public static void tintTextInputLayout(@NonNull TextInputLayout til,
                                           @ColorInt int strokeColor) {
        til.setBoxStrokeColor(strokeColor);
        til.setHintTextColor(getTextInputLayoutColorStateList(strokeColor));
    }

    /**
     * Applies a single color to all {@link TextInputLayout} elements.
     * @param til               TextInputLayout instance.
     * @param themeTextColor    Color to use.
     */
    public static void monotintTextInputLayout(@NonNull TextInputLayout til,
                                               @ColorInt int themeTextColor) {
        ColorStateList textCSL = getTextInputLayoutColorStateList(themeTextColor);
        ColorStateList extraCSL = ColorStateList.valueOf(themeTextColor);

        til.setBoxStrokeColorStateList(textCSL);
        til.setDefaultHintTextColor(extraCSL);
        Objects.requireNonNull(til.getEditText()).setTextColor(themeTextColor);
        til.setErrorTextColor(extraCSL);
        til.setErrorIconTintList(extraCSL);
        til.setBoxStrokeErrorColor(extraCSL);
        til.setStartIconTintList(extraCSL);
        til.setEndIconTintList(extraCSL);
    }

    /**
     * Tints a {@link Switch} button.
     *
     * @param _switch       Switch instance.
     * @param themeColor    Color to use.
     */
    public static void tintSwitch(@NonNull Switch _switch,
                                  @ColorInt int themeColor) {
        ColorStateList themeCSL = getSwitchColorStateList(themeColor);
        _switch.getThumbDrawable().setTintList(themeCSL);
        _switch.getTrackDrawable().setTintList(themeCSL);
    }

    /**
     * Tints a {@link MaterialSpinner} element.
     *
     * @param spinner       Spinner instance.
     * @param themeColor    Color to use.
     */
    public static void tintSpinner(@NonNull MaterialSpinner spinner,
                                  @ColorInt int themeColor) {
        ColorStateList themeCSL = ColorStateList.valueOf(themeColor);
        spinner.setHintTextColor(themeCSL);
        spinner.setBoxStrokeColor(themeColor);
    }

    /**
     * Retrieves a {@link ColorStateList} that is appropriated for a
     * {@link TextInputLayout} object.
     *
     * @param themeColor    Color to use.
     * @return              ColorStateList object.
     */
    public static ColorStateList getTextInputLayoutColorStateList(
            @ColorInt int themeColor) {
        return new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{
                                -android.R.attr.state_focused,
                                android.R.attr.state_focused,
                        },
                        new int[]{}
                },
                new int[]{
                        Color.GRAY,
                        themeColor,
                        themeColor,
                }
        );
    }

    /**
     * Retrieves a {@link ColorStateList} that is appropriated for a
     * {@link Switch} object.
     *
     * @param themeColor    Color to use.
     * @return              ColorStateList object.
     */
    public static ColorStateList getSwitchColorStateList(
            @ColorInt int themeColor) {
        return new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_enabled},
                        new int[]{}
                },
                new int[]{
                        Color.GRAY,
                        Color.LTGRAY,
                        themeColor,
                        themeColor,
                }
        );
    }
}
