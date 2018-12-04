package com.example.junmung.studyhelper;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class MyBindingAdapter {

    @BindingAdapter({"imageByte"})
    public static void loadImage(ImageView view, byte[] imageByte){
        if (imageByte == null) {
            Glide.with(view.getContext())
                    .load(android.R.drawable.ic_menu_gallery)
                    .into(view);
        } else {
            Glide.with(view.getContext())
                    .load(imageByte)
                    .into(view);
        }
    }
}
