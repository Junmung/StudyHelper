package com.example.junmung.studyhelper.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface MemoDAO {
    @Insert
    void insert(Memo memo);


    @Query("DELETE FROM memos WHERE _id IN (:ids) ")
    void deleteCheckedMemos(List<Integer> ids);

    @Query("DELETE FROM memos WHERE _id = :id")
    void delete(int id);

    @Query("DELETE FROM memos")
    void deleteAll();


    @Query("SELECT * FROM memos ORDER BY registerDate DESC")
    LiveData<List<Memo>> getAllMemos();

    @Query("SELECT * FROM memos WHERE _id = :id")
    LiveData<Memo> getMemoById(int id);

    @Update(onConflict = REPLACE)
    void update(Memo memo);

    @Query("SELECT * FROM memos WHERE title LIKE :title")
    LiveData<List<Memo>> searchList(String title);

}
