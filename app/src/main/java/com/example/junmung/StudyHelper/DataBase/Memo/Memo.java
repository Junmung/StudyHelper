package com.example.junmung.StudyHelper.DataBase.Memo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Memo extends RealmObject {
    private String title;
    private String contents;
    private Date date;
    private RealmList<Image> images;

    public Memo() {
    }

    public Memo(String title, String contents, Date date) {
        this.title = title;
        this.contents = contents;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getContents() {
        return contents;
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


    public RealmList<Image> getImages() {
        return images;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setImages(RealmList<Image> images) {
        this.images = images;
    }

}

