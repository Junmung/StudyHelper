package com.example.junmung.StudyHelper.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Memo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{
    public abstract MemoDAO memoDAO();

    private static volatile AppDatabase instance;

    static AppDatabase getDatabase(final Context context){
        if(instance == null){
            synchronized (AppDatabase.class){
                if(instance == null){
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "memos")
                            .build();
                }
            }
        }
        return instance;
    }
}

