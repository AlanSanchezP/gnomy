package io.github.alansanchezp.gnomy.util;

public class GraphicUtil {
    // DO NOT INCLUDE # SYMBOL HERE
    private static final int[] COLORS = {
        0XFFC0F291,0XFF7DE8E4,0XFFA8F1FF,0XFFCDCFFF,0XFFFFABFF,0XFFFFB0A8,0XFFFECE9A,0XFFFFCC00,
        0XFF63EB6E,0XFF3ACFC2,0XFF66D1FF,0XFF9999FF,0XFFFB80FF,0XFFFF7066,0XFFFFAC69,0XFFE0CA1F,
        0XFF33CC00,0XFF37A6A6,0XFF2BA3FF,0XFF6701CB,0XFFCC4EBB,0XFFFF0000,0XFFFF7A14,0XFFCCB639,
        0XFF009900,0XFF457E8A,0XFF1260E6,0XFF6705B3,0XFFA31086,0XFFD90007,0XFFCC6D08,0XFFCC9933,
        0XFFBBBBBB,0XFF999999,0XFF000000,                      0XFF750000,0XFF6E4D42,0XFF916F24
    };

    public static int getRandomColor() {
        int randomIndex = (int) (Math.random() * (COLORS.length-1));
        return COLORS[randomIndex];
    }

    public static int getTextColor(int bgColor) {
        int red = (bgColor >> 16) & 0xFF;
        int green = (bgColor >> 8) & 0xFF;
        int blue = bgColor & 0xFF;

        double luminance = ( 0.299 * red + 0.587 * green + 0.114 * blue)/255;

        return luminance > 0.5 ? 0xff000000 : 0xffffffff;
    }
}
