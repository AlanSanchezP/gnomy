package io.github.alansanchezp.gnomy.util.android;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Menu;
import android.widget.Switch;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import io.github.alansanchezp.gnomy.util.ColorUtil;

public class ViewTintingUtil {

    public static void tintMenuItems(@NonNull Menu menu,
                                     @NonNull int[] menuItemsResIds,
                                     @ColorInt int color) {
        for (int resId : menuItemsResIds) {
            menu.findItem(resId)
                    .getIcon()
                    .setTint(color);
        }
    }

    public static void tintFAB(@NonNull FloatingActionButton fab,
                               @ColorInt int bgColor,
                               @ColorInt int drawableColor) {
        tintFAB(fab, bgColor, drawableColor, ColorUtil.getRippleVariant(bgColor));
    }

    public static void tintFAB(@NonNull FloatingActionButton fab,
                               @ColorInt int bgColor,
                               @ColorInt int drawableColor,
                               @ColorInt int rippleColor) {
        fab.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        fab.getDrawable().mutate().setTint(drawableColor);
        fab.setRippleColor(rippleColor);
    }

    public static void tintTextInputLayout(@NonNull TextInputLayout til,
                                           @ColorInt int strokeColor) {
        til.setBoxStrokeColor(strokeColor);
        til.setHintTextColor(getTextInputLayoutColorStateList(strokeColor));
    }

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
    }

    public static void tintSwitch(@NonNull Switch _switch,
                                  @ColorInt int themeColor) {
        ColorStateList themeCSL = getSwitchColorStateList(themeColor);
        _switch.getThumbDrawable().setTintList(themeCSL);
        _switch.getTrackDrawable().setTintList(themeCSL);
    }

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
