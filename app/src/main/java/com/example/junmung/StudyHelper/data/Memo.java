package com.example.junmung.StudyHelper.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

@Entity(tableName = "memos")
public class Memo {
    @PrimaryKey
    @NonNull
    private int _id;

    @NonNull
    private String title;
    private String contents;

    @NonNull
    @ColumnInfo(name = "registerDate")
    private Date date;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] image;

    public Memo(int _id, String title, String contents, Date date, @Nullable byte[] image){
        this._id = _id;
        this.title = title;
        this.contents = contents;
        this.date = date;
        this.image = image;
    }

    @NonNull
    public int get_id() {
        return _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(@NonNull Date date) {
        this.date = date;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
