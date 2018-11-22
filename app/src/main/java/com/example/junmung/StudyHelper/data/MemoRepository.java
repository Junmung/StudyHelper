package com.example.junmung.StudyHelper.data;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

public class MemoRepository {
    private MemoDAO memoDAO;
    private MutableLiveData<List<Memo>> allMemos;

    public MemoRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        memoDAO = db.memoDAO();
    }

    public MutableLiveData<List<Memo>> getAllMemos(){
        allMemos = (MutableLiveData<List<Memo>>) memoDAO.getAllMemos();
        return allMemos;
    }


    public void insert(Memo memo){
        new InsertTask(memoDAO).execute(memo);
    }

    private static class InsertTask extends AsyncTask<Memo, Void, Void>{
        private MemoDAO mAsyncTaskDao;

        InsertTask(MemoDAO dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Memo... memos) {
            mAsyncTaskDao.insert(memos[0]);
            return null;
        }
    }


    public void delete(Integer index) {
        new DeleteTask(memoDAO).execute(index);
    }

    private static class DeleteTask extends AsyncTask<Integer, Void, Void>{
        private MemoDAO mAsyncTaskDao;

        DeleteTask(MemoDAO dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            mAsyncTaskDao.delete(integers[0]);
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public void selectDelete(List<Integer> list){
        new SelectDeleteTask(memoDAO).execute(list);
    }

    @SuppressWarnings("unchecked")
    private static class SelectDeleteTask extends AsyncTask<List<Integer>, Void, Void> {
        private MemoDAO mAsyncTaskDao;

        public SelectDeleteTask(MemoDAO memoDAO) {
            this.mAsyncTaskDao = memoDAO;
        }

        @Override
        protected Void doInBackground(List<Integer>... lists) {
            mAsyncTaskDao.deleteCheckedMemos(lists[0]);
            return null;
        }
    }

    public Memo getMemo(int index){
        return memoDAO.getMemoById(index).getValue();
    }
}
