package com.example.assignment5;

import android.graphics.Bitmap;

public class ListItem {
    Bitmap b;
    String date;
    String tag;

    public ListItem(Bitmap b, String date, String tag){
        this.b = b;
        this.date = date;
        this.tag = tag;
    }
}
