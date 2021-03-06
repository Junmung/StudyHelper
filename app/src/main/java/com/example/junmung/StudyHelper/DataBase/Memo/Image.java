package com.example.junmung.StudyHelper.DataBase.Memo;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.example.junmung.StudyHelper.R;

import io.realm.RealmObject;

public class Image extends RealmObject {
    private String memoTitle;
    private long index;
    private byte[] image;

    public Image() {
    }

    public Image(String memoTitle, long index, byte[] image) {
        this.memoTitle = memoTitle;
        this.index = index;
        this.image = image;
    }

    public String getMemoTitle() {
        return memoTitle;
    }

    public long getIndex() {
        return index;
    }

    public byte[] getImage() {
        return image;
    }

    public void setMemoTitle(String memoTitle) {
        this.memoTitle = memoTitle;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
