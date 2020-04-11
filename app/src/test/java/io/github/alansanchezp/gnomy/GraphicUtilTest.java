package io.github.alansanchezp.gnomy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.github.alansanchezp.gnomy.util.GraphicUtil;

public class GraphicUtilTest {
    @Test
    public void generates_valid_color() {
        for(int i=0; i<=100; i++) {
            int color = GraphicUtil.getRandomColor();
            assertTrue(0xff000000 <= color && color <= 0xffffffff);
        }
    }

    @Test
    public void text_color_is_accurate() {
        // light background
        int bgColor1 = 0XFF9999FF;
        int bgColor2 = 0XFFFFEF85;
        int bgColor3 = 0XFF33CC00;
        int bgColor4 = 0XFFFB80FF;

        assertEquals(0XFF000000,GraphicUtil.getTextColor(bgColor1));
        assertEquals(0XFF000000,GraphicUtil.getTextColor(bgColor2));
        assertEquals(0XFF000000,GraphicUtil.getTextColor(bgColor3));
        assertEquals(0XFF000000,GraphicUtil.getTextColor(bgColor4));

        // dark backgrounds
        int bgColor5 = 0XFF750000;
        int bgColor6 = 0XFF000000;
        int bgColor7 = 0XFF6705B3;
        int bgColor8 = 0XFF457E8A;

        assertEquals(0XFFFFFFFF,GraphicUtil.getTextColor(bgColor5));
        assertEquals(0XFFFFFFFF,GraphicUtil.getTextColor(bgColor6));
        assertEquals(0XFFFFFFFF,GraphicUtil.getTextColor(bgColor7));
        assertEquals(0XFFFFFFFF,GraphicUtil.getTextColor(bgColor8));
    }
}