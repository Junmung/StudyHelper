package com.example.junmung.StudyHelper.memo;

import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.example.junmung.StudyHelper.R;
import com.example.junmung.StudyHelper.BR;
import com.example.junmung.StudyHelper.data.Memo;
import com.example.junmung.StudyHelper.databinding.ItemMemoBinding;

import java.util.HashMap;
import java.util.List;


public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {
    private List<Memo> memoList;
    private HashMap<Integer, Boolean> checkedList;
    private ObservableBoolean removeMode;

    public MemoAdapter(List<Memo> memoList, ObservableBoolean removeMode){
        this.memoList = memoList;
        this.checkedList = new HashMap<>();
        this.removeMode = removeMode;
    }

    @Override
    public MemoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_memo, parent, false);

        return new MemoViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(MemoViewHolder holder, int position) {
        final Memo memo = memoList.get(holder.getAdapterPosition());
        byte[] image = memo.getImage();

        holder.binding.setVariable(BR.memo, memo);
        holder.binding.setVariable(BR.removeMode, removeMode);

        // 이미지가 없다면 기본이미지 생성
        if(image == null)
            ((ItemMemoBinding)holder.binding).thumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
        else
            ((ItemMemoBinding)holder.binding).thumbnail.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
    }

    class MemoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ViewDataBinding binding;
        CheckBox.OnCheckedChangeListener checkedChangeListener = (buttonView, isChecked) -> {
            int memoIndex = ((ItemMemoBinding)binding).getMemo().get_id();
            if (checkedList.containsKey(memoIndex))
                checkedList.replace(memoIndex, isChecked);
            else
                checkedList.put(memoIndex, isChecked);
        };

        public MemoViewHolder(View itemView, ViewDataBinding binding){
            super(binding.getRoot());
            this.binding = binding;

            ((ItemMemoBinding)this.binding).checkbox.setOnCheckedChangeListener(checkedChangeListener);
        }

        @Override
        public void onClick(View v) {
            Memo memo = ((ItemMemoBinding)binding).getMemo();
            // item 눌렀을때는 보기 액티비티가 실행되어야함
            Intent intent = new Intent(v.getContext().getApplicationContext(), MemoOpenActivity.class);
            intent.putExtra("MemoIndex", memo.get_id());
            v.getContext().getApplicationContext().startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view) {
            Memo memo = ((ItemMemoBinding)binding).getMemo();
            new AlertDialog.Builder(view.getContext().getApplicationContext())
                    .setMessage("수정하시겠습니까?")
                    .setPositiveButton("확인", (dialog, which) -> {
                        Intent intent = new Intent(view.getContext().getApplicationContext(), MemoApplyActivity.class);
                        intent.putExtra("Purpose", "Modify");
                        intent.putExtra("MemoIndex", memo.get_id());
                        view.getContext().getApplicationContext().startActivity(intent);
                    })
                    .setNegativeButton("취소", (dialog, which) -> { })
                    .show();
            return true;
        }
    }

    public HashMap<Integer, Boolean> getCheckedMemoIndexes(){
        return checkedList;
    }

    public void resetCheckedList(){
        checkedList.clear();
    }

    public void setMemoList(List<Memo> memoList){
        this.memoList = memoList;
    }

    public void refresh(){
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return memoList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
