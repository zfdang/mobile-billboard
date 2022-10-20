package com.zfdang.mbb;

import android.graphics.Color;

public class Setting {

    static public int LM_SINGLE = 1;
    static public int LM_LOOP = 0;
    static public int LM_FILL_LOOP = -1;

    public String text;
    public int loopMode;
    public int textSize;
    public int speed;
    public int textColor;
    public int bgColor;

    public Setting() {
        this.text = "Text";
        this.loopMode = LM_FILL_LOOP;
        this.textSize = 800;
        this.speed = 30;
        this.textColor = Color.valueOf(1,0,0).toArgb();
        this.bgColor = Color.valueOf(0,0,0).toArgb();
    }
}
