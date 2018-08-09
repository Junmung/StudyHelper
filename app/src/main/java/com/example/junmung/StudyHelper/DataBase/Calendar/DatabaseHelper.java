package com.example.junmung.StudyHelper.DataBase.Calendar;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.junmung.StudyHelper.Calendar.DayItem;

import java.util.Calendar;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DataBase_Name = "CalendarData";
    private static final String DataBase_Table = "DateInfo";

    private static DatabaseHelper mInstance = null;
    private Context context;
    private static SQLiteDatabase db;


    // _id, Month, Day, StudyTime, targetStudyTime, hasMemo


    private DatabaseHelper(Context context) {
        super(context, DataBase_Name, null, 1);
        this.context = context;
    }


    public static DatabaseHelper getInstance(Context context){
        if(mInstance == null) {
            mInstance = new DatabaseHelper(context);

            db = mInstance.getWritableDatabase();
        }

        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "Create Table " + DataBase_Table +
                "(_id Integer Primary Key autoincrement," +
                "Month Integer," +
                "Day Integer," +
                "StudyTime Integer," +
                "TargetTime Integer," +
                "HasMemo Integer);";

        db.execSQL(sql);
    }


    // 해당 월의 데이터들을 가져온다.
    public Cursor getInfoOfMonth(int month){
        Cursor cursor = db.rawQuery(
                "Select StudyTime, TargetTime, hasMemo from "+DataBase_Table +
                        " Where Month="+month+";", null);

        return cursor;
    }


    // 전체 데이터중 오늘까지의 데이터를 가져온다.
    public Cursor getAllDataUntilToday(int month, int day){
        int index = getIndex(month, day);

        Cursor result = db.rawQuery(
                "Select Month, Day, StudyTime, TargetTime, hasMemo " +
                     "From "+DataBase_Table +
                    " Where _id<="+index+";", null);
        return result;
    }


    // 기간에 해당되는 데이터들을 가져온다.
    public Cursor getSelectedDatas(int startMonth, int startDay, int endMonth, int endDay){
        int startIndex = getIndex(startMonth, startDay);
        int endIndex = getIndex(endMonth, endDay);

        Cursor result = db.rawQuery(
                "Select Month, Day, StudyTime, TargetTime, hasMemo " +
                        "From "+DataBase_Table +
                        " Where _id>="+startIndex+" AND _id<="+endIndex+";", null);

        return result;
    }

    // 월, 일에 해당되는 index 를 가져온다.
    private int getIndex(int month, int day){
        Cursor cursor = db.rawQuery(
                "Select _id " +
                        "from " + DataBase_Table+
                        " Where Month=" + month + " AND Day="+day+";", null);
        cursor.moveToFirst();
        int index = cursor.getInt(0);

        return index;
    }

    public boolean hasMemo(int month, int day){
        Cursor cursor = db.rawQuery(
                "Select hasMemo from " + DataBase_Table +
                    " Where Month=" + month + " And Day=" + day + ";"
                , null);

        cursor.moveToFirst();

        return cursor.getInt(0) > 0;
    }

    public void updateMemoState(int month, int day, int memoState){
        db.execSQL("Update " + DataBase_Table +
                    " Set HasMemo=" + memoState +
                    " Where Month=" + month + " AND Day=" + day + ";");
    }

    public void updateStudyTime(int month, int day, int studyTime){
        db.execSQL("Update " + DataBase_Table +
                    " Set StudyTime=" + studyTime +
                    " Where Month=" + month + " AND Day=" + day + ";");
    }

    public void setTargetTime(int month, int day, int targetTime){
        db.execSQL("Update " + DataBase_Table +
                " Set targetTime=" + targetTime +
                " Where Month=" + month + " AND Day=" + day + ";");
    }




    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void init(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2018);

        for(int i = 0; i < 11; i++){
            calendar.set(Calendar.MONTH, i, 1);
            String sql = "Insert Into " + DataBase_Table + " Values";
            int dayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            for(int j = 1; j <= dayOfMonth; j++){
                int day = i+1;
                if(j != dayOfMonth)
                    sql += "(null," + day + ","+ j + ", 0, 0, 0), ";
                else
                    sql += "(null," + day + ","+ j + ", 0, 0, 0);";
            }
            db.execSQL(sql);
        }
    }

    public void deleteAll(){
        db.execSQL("Delete from DateInfo;");
    }

    public void dropTable(){
        db.execSQL("Drop table "+DataBase_Table);
    }

    public boolean isEmpty(){
        Cursor cursor = db.rawQuery("Select * from "+ DataBase_Table+";",null);
        if(cursor.getCount() == 0)
            return true;
        else
            return false;

    }
}






























