package io.github.alansanchezp.gnomy.util;

public class GraphicUtil {
    // DO NOT INCLUDE # SYMBOL HERE
    private static final int[] COLORS = {
        0XFFC0F291,0XFF63EB6E,0XFF33CC00,0XFF009900,0XFF006300,
        0XFF7DE8E4,0XFF3ACFC2,0XFF37A6A6,0XFF457E8A,0XFF2E545C,
        0XFFA8F1FF,0XFF66D1FF,0XFF2BA3FF,0XFF1260E6,0XFF0A3580,
        0XFFCDCFFF,0XFF9999FF,0XFF6701CB,0XFF6705B3,0XFF3E036B,
        0XFFFFABFF,0XFFFB80FF,0XFFCC4EBB,0XFFA31086,0XFF6E0B5A,
        0XFFFFB0A8,0XFFFFB0A8,0XFFFF0000,0XFFD90007,0XFF750000,
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
        int red = (bgColor >> 16) & 0xFF;
        int green = (bgColor >> 8) & 0xFF;
        int blue = bgColor & 0xFF;

        double luminance = ( 0.299 * red + 0.587 * green + 0.114 * blue)/255;

        return luminance > 0.5 ? 0xff000000 : 0xffffffff;
    }
}
