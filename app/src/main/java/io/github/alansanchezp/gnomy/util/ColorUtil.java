package io.github.alansanchezp.gnomy.util;

public class ColorUtil {
    // DO NOT INCLUDE # SYMBOL HERE
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

    public static int[] getColors() {
        return COLORS;
    }

    public static int getRandomColor() {
        int randomIndex = (int) (Math.random() * (COLORS.length-1));
        return COLORS[randomIndex];
    }

    public static int getTextColor(int bgColor) {
        int[] rgb = getRGB(bgColor);

        double luminance = ( 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2])/255;

        return luminance > 0.5 ? 0xff000000 : 0xffffffff;
    }

    public static int getDarkVariant(int color) {
        return getVariantByFactor(color, 0.8f);
    }

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

    public static int[] getRGB(int color) {
        return new int[]{(color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF};
    }
}
