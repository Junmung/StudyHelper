package com.example.junmung.StudyHelper.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

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



}
