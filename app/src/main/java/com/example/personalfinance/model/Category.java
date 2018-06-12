package com.example.personalfinance.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;

public class Category {

    public final int id;
    public final String title;
    public final String image;
    public final int type;

    public Category(int id, String title, String image, int type) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.type = type;
    }

    public Drawable getImageDrawable(Context context) {
        AssetManager am = context.getAssets();
        InputStream stream = null;
        try {
            stream = am.open(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (stream != null) {
            return Drawable.createFromStream(stream, null);
        }
        return null;
    }
}
