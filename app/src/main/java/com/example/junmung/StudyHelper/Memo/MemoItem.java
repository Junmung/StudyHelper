package com.example.junmung.StudyHelper.Memo;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.CheckBox;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MemoItem {

    private String title;
    private Date date;
    private Bitmap thumbnailImage;
    private boolean checked;

    public MemoItem(String title, Date date) {
        this.title = title;
        this.date = date;
        checked = false;
    }

    public MemoItem(String title, Date date, Bitmap img){
        this.title = title;
        this.date = date;
        this.thumbnailImage = img;
        checked = false;
    }

    public String getTitle(){
        return title;
    }

    public Date getDate() {
        return date;
    }

    public int getMonth(){
        return Integer.parseInt(new SimpleDateFormat("M").format(date));
    }

    public int getDay(){
        return Integer.parseInt(new SimpleDateFormat("d").format(date));
    }

    public Bitmap getThumbnailImage() {
        return thumbnailImage;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setCheck(boolean check){
        this.checked= check;
    }

    public void setThumbnailImage(Bitmap thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }
}
