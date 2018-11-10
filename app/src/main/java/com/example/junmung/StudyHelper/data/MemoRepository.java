package com.example.junmung.StudyHelper.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class MemoRepository {
    private MemoDAO memoDAO;
    private LiveData<List<Memo>> allMemos;

    public MemoRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        memoDAO = db.memoDAO();
        allMemos = memoDAO.getAllMemos();
    }

    public LiveData<List<Memo>> getAllMemos(){
        return allMemos;
    }

    public void insert(Memo memo){
        new insertAsyncTask(memoDAO).execute(memo);
    }

    private static class insertAsyncTask extends AsyncTask<Memo, Void, Void>{
        private MemoDAO mAsyncTaskDao;

        insertAsyncTask(MemoDAO dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Memo... memos) {
            mAsyncTaskDao.insert(memos[0]);
            return null;
        }
    }
}
