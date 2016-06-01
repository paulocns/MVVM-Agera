package com.android.mvvmagera;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.widget.ImageView;


public class ImageViewBindingAdapter {
    @BindingAdapter("bitmap")
    public static void setBitmap(ImageView view, Bitmap bitmap) {
        view.setImageBitmap(bitmap);
    }
}
