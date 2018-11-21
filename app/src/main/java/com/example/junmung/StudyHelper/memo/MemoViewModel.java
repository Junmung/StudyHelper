package com.example.junmung.StudyHelper.memo;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableBoolean;
import android.support.annotation.NonNull;

import com.example.junmung.StudyHelper.data.Memo;
import com.example.junmung.StudyHelper.data.MemoRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MemoViewModel extends AndroidViewModel{
    private MemoRepository memoRepository;
    private MutableLiveData<List<Memo>> memos;
    private MutableLiveData<List<Memo>> searchMemos = new MutableLiveData<>();
    private ObservableBoolean removeMode;

    public MemoViewModel(@NonNull Application application) {
        super(application);
        memoRepository = new MemoRepository(application);
        memos = memoRepository.getAllMemos();
        removeMode = new ObservableBoolean(false);
    }


    public void setRemoveMode(boolean removeMode) {
        this.removeMode = new ObservableBoolean(removeMode);
    }

    public LiveData<List<Memo>> getMemos(){
        if (memos == null)
            memos = new MutableLiveData<>();
        return memos;
    }

    public LiveData<List<Memo>> getSearchMemos(){
        if (searchMemos == null)
            memos = new MutableLiveData<>();
        return searchMemos;
    }

    public void addSearchMemo(Memo memo){
        searchMemos.getValue().add(memo);
    }

    public void clearSearchMemos(){
        searchMemos.getValue().clear();
    }

    public ObservableBoolean getRemoveMode() {
        return removeMode;
    }


    public void insert(Memo memo){
        memoRepository.insert(memo);
    }

    public void delete(int index){
        memoRepository.delete(index);
        memos.getValue().remove(index);
    }

    public void selectRemove(HashMap<Integer, Boolean> checkedList){
        List<Integer> list = new ArrayList<>();
        list.addAll(checkedList.keySet());

        memoRepository.selectDelete(list);
        for(Iterator<Memo> iterator = memos.getValue().iterator(); iterator.hasNext();){
            int memoIndex = iterator.next().get_id();

            if (list.contains(memoIndex))
                iterator.remove();
        }
    }
}
