package io.github.alansanchezp.gnomy.util;

/**
 * Helper class to retrieve color codes for UI purposes.
 */
public class ColorUtil {
    /**
     * Array of colors to show in color pickers. Not using
     * android resources so that this class can be unit tested.
     */
    private static final int[] COLORS = {
        0XFFC0F291,0XFF63EB6E,0XFF33CC00,0XFF009900,0XFF006300,
        0XFF7DE8E4,0XFF3ACFC2,0XFF37A6A6,0XFF457E8A,0XFF2E545C,
        0XFFA8F1FF,0XFF66D1FF,0XFF2BA3FF,0XFF1260E6,0XFF0A3580,
        0XFFCDCFFF,0XFF9999FF,0XFF6701CB,0XFF6705B3,0XFF3E036B,
        0XFFFFABFF,0XFFFB80FF,0XFFCC4EBB,0XFFA31086,0XFF6E0B5A,
        0XFFFFB0A8,0XFFFF7066,0XFFFF0000,0XFFD90007,0XFF750000,
        0XFFFECE9A,0XFFFFAC69,0XFFFF7A14,0XFFCC6D08,0XFF6E4D42,
        0XFFFFCC00,0XFFE0CA1F,0XFFCCB639,0XFFCC9933,0XFF916F24
    };

    /**
     * Simple getter for {@link #COLORS}.
     *
     * @return  Array of colors.
     */
    public static int[] getColors() {
        return COLORS;
    }

    /**
     * Gets a random color from {@link #COLORS}.
     *
     * @return  Random color.
     */
    public static int getRandomColor() {
        int randomIndex = (int) (Math.random() * (COLORS.length-1));
        return COLORS[randomIndex];
    }

    /**
     * Gets an appropriate color to use in text and drawables
     * on top of a surface that has the given background color.
     *
     * @param bgColor   Color of the underlying surface.
     * @return          Appropriate color to use (either black or white).
     */
    public static int getTextColor(int bgColor) {
        int[] rgb = getRGB(bgColor);

        double luminance = ( 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2])/255;

        return luminance > 0.5 ? 0xff000000 : 0xffffffff;
    }

    /**
     * Takes any color and returns a dark variant of it.
     * This factor seems to get decently close enough to
     * the material design guidelines for color shades.
     *
     * @param color     Base color.
     * @return          Dark variant.
     */
    public static int getDarkVariant(int color) {
        return getVariantByFactor(color, 0.73f);
    }

    /**
     * Takes any color and returns a light variant of it.
     * This factor seems to get decently close enough to
     * the material design guidelines for color shades.
     *
     * @param color     Base color.
     * @return          Light variant.
     */
    public static int getLightVariant(int color) {
        return getVariantByFactor(color, 1.27f);
    }

    /**
     * Takes any color and returns a variant of it that
     * makes ripple animations look more or less similar
     * to the material design specs.
     *
     * Feel free to improve this method.
     *
     * @param color     Base color.
     * @return          Ripple variant.
     */
    public static int getRippleVariant(int color) {
        int[] rgb = getRGB(color);

        double luminance = ( 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2])/255;

        if (luminance > 0.5) return getDarkVariant(color);
        if (luminance > 0.3) return getLightVariant(color);
        return getVariantByFactor(color, 2.1f);
    }

    /**
     * Takes any color and returns a variant of by multiplying
     * each RGB component individually by the given factor.
     *
     * @param color     Base color
     * @param ratio     Factor to use.
     * @return          New color.
     */
    public static int getVariantByFactor(int color, float ratio) {
        int[] rgb = getRGB(color);

        int a = (color >> 24) & 0xFF;
        int newRed = (int) (rgb[0] * ratio);
        int newGreen = (int) (rgb[1] * ratio);
        int newBlue = (int) (rgb[2] * ratio);

        newRed = Math.max(Math.min(newRed, 0XFF), 0X00);
        newGreen = Math.max(Math.min(newGreen, 0XFF), 0X00);
        newBlue = Math.max(Math.min(newBlue, 0XFF), 0X00);

        return a << 24 | newRed << 16 | newGreen << 8 | newBlue;
    }

    /**
     * Returns the RGB components of a given color.
     *
     * @param color Color
     * @return      RGB components in array form.
     *                  Index 0 = Red
     *                  Index 1 = Green
     *                  Index 2 = Blue
     */
    public static int[] getRGB(int color) {
        return new int[]{(color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF};
    }
}
