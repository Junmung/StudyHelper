package com.example.junmung.studyhelper.memo;

import android.databinding.ObservableBoolean;
import android.support.annotation.NonNull;

import com.example.junmung.studyhelper.data.Memo;
import com.example.junmung.studyhelper.databinding.ItemMemoBinding;
import com.example.junmung.studyhelper.view.MyBaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MemoAdapter extends MyBaseAdapter {
    private final int layoutId;
    private List<Memo> memoList;
    private HashMap<Integer, Boolean> checkedList;
    private ObservableBoolean removeMode;
    private MemoClickListener memoClickListener;

    public MemoAdapter(ObservableBoolean removeMode, int layoutId, MemoClickListener memoClickListener){
        this.checkedList = new HashMap<>();
        this.removeMode = removeMode;
        this.layoutId = layoutId;
        this.memoClickListener = memoClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ((ItemMemoBinding)holder.binding).checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int memoIndex = getMemoIndexForPosition(holder.getAdapterPosition());
            if (checkedList.containsKey(memoIndex))
                checkedList.replace(memoIndex, isChecked);
            else
                checkedList.put(memoIndex, isChecked);
        });
    }

    @Override
    protected Object getObjForPosition(int position) {
        return memoList.get(position);
    }

    @Override
    protected int getLayoutId() {
        return layoutId;
    }

    private int getMemoIndexForPosition(int position){
        return memoList.get(position).get_id();
    }

    public void onItemClick(Memo memo){
        memoClickListener.onClick(memo);
    }

    public boolean onItemLongClick(Memo memo){
        memoClickListener.onLongClick(memo);
        return true;
    }

    public boolean isRemoveMode(){
        return removeMode.get();
    }

    public ArrayList<Integer> getCheckedMemoIndexes(){
        return new ArrayList<>(checkedList.keySet());
    }

    public void resetCheckedList(){
        checkedList.clear();
    }

    public void setMemoList(List<Memo> memoList){
        this.memoList = memoList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (memoList != null)
            return memoList.size();
        else
            return 0;
    }
    // Filter 로직
//    @Override
//    public Filter getFilter() {
//        return new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//
//                List<Memo> searchList = new ArrayList<>();
//                if(constraint.length() == 0)
//                    searchList.addAll(memoList);
//                else
//                    searchList = getSearchedList(constraint.toString());
//
//                FilterResults results = new FilterResults();
//                results.values = searchList;
//                return results;
//            }
//
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults results) {
//                setMemoList((List<Memo>)results.values);
//            }
//        };
//    }
//    private List<Memo> getSearchedList(String title) {
//        List<Memo> searchList = new ArrayList<>();
//
//        for (Memo item : memoList) {
//            if (item.getTitle().contains(title))
//                searchList.add(item);
//        }
//
//        return searchList;
//    }
}
