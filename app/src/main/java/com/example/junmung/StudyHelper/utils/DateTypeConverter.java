package com.example.junmung.StudyHelper.utils;

import android.arch.persistence.room.TypeConverter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTypeConverter {
    @TypeConverter
    public static Date toDate(Long value){
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long toLong(Date value){
        return value == null ? null : value.getTime();
    }

    public static String toString(Date date){
        return new SimpleDateFormat("M월 dd일 a h:mm").format(date);
    }
}
