package com.example.junmung.StudyHelper.memo;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.junmung.StudyHelper.data.Memo;
import com.example.junmung.StudyHelper.data.MemoRepository;

import java.util.List;

public class MemoViewModel extends AndroidViewModel{
    private MemoRepository memoRepository;
    private LiveData<List<Memo>> allMemos;

    public MemoViewModel(@NonNull Application application) {
        super(application);
        memoRepository = new MemoRepository(application);
        allMemos = memoRepository.getAllMemos();
    }

    public LiveData<List<Memo>> getAllMemos(){
        return allMemos;
    }

    public void insert(Memo memo){
        memoRepository.insert(memo);
    }
}
