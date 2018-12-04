package com.example.junmung.studyhelper.memo;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.databinding.ObservableBoolean;
import android.support.annotation.NonNull;

import com.example.junmung.studyhelper.data.Memo;
import com.example.junmung.studyhelper.data.MemoRepository;

import java.util.ArrayList;
import java.util.List;

public class MemoViewModel extends AndroidViewModel {
    private MemoRepository memoRepository;
    public MutableLiveData<String> searchWord = new MutableLiveData<>();
    public LiveData<List<Memo>> memos = Transformations.switchMap(searchWord, title -> searchList(title));
    public ObservableBoolean removeMode;

    public MemoViewModel(@NonNull Application application) {
        super(application);
        memoRepository = new MemoRepository(application);
//        memos = memoRepository.getAllMemos();
        removeMode = new ObservableBoolean(false);
        searchWord.setValue("");
    }

    public void delete(int index){
        memoRepository.delete(index);
    }

    public void selectRemove(ArrayList<Integer> checkedList){
        memoRepository.selectDelete(checkedList);
    }

    public LiveData<List<Memo>> searchList(String title) {
        return memoRepository.searchList(title);
    }

    public LiveData<Memo> getMemo(int index) {
        return memoRepository.getMemo(index);
    }

    public void setRemoveMode(boolean removeMode) {
        this.removeMode.set(removeMode);
    }
}
