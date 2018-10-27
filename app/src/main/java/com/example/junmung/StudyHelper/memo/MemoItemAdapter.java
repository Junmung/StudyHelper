package com.example.junmung.StudyHelper.memo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.junmung.StudyHelper.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MemoItemAdapter extends RecyclerView.Adapter<MemoItemAdapter.ViewHolder> {
    static final int LIST_STATE_NORMAL = 0;
    static final int LIST_STATE_REMOVE = 1;

    public ArrayList<MemoItem> items;
    private int listState;
    private Context context;
    private int lastPosition = -1;

    public MemoItemAdapter(ArrayList<MemoItem> items){
        this.items = items;
        listState = LIST_STATE_NORMAL;
    }


    // 메모 아이템 추가
    public void addItem(String title, Date date, Bitmap thumbnailImg) {
        MemoItem item = new MemoItem(title, date, thumbnailImg);

        items.add(0, item);
    }


    @Override
    public MemoItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        // 새로운 뷰 만들기
        View view = LayoutInflater.from(context).inflate(R.layout.item_memo, parent, false);

        // 뷰사이즈 세팅, 마진, 패딩 등등 세팅하는 곳인가 봄

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MemoItem memoItem = items.get(position);
        Bitmap thumbnailImage = memoItem.getThumbnailImage();

        // 기본상태면 체크박스 숨기기
        if(listState == LIST_STATE_NORMAL)
            holder.checkBox.setVisibility(View.GONE);
        else
            holder.checkBox.setVisibility(View.VISIBLE);



        // 이미지가 없다면 기본이미지 생성
        if(thumbnailImage == null)
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        else
            holder.imageView.setImageBitmap(thumbnailImage);


        String date = new SimpleDateFormat("MM월 dd일").format(memoItem.getDate());

        holder.title.setText(memoItem.getTitle());
        holder.date.setText(date);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(memoItem.isChecked());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                memoItem.setCheck(isChecked);
            }
        });


        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);

        holder.itemView.setAnimation(animation);
        holder.itemView.startAnimation(animation);
        lastPosition = position;
        Log.e("lastPosition", ""+position);

    }

    public void updateList(ArrayList<MemoItem> list){
        items = list;

        refresh();
    }



    class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener, View.OnLongClickListener{
        ImageView imageView;
        TextView title;
        TextView date;
        CheckBox checkBox;

        public ViewHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            imageView = itemView.findViewById(R.id.item_memo_ImageView);
            title = itemView.findViewById(R.id.item_memo_TextView_Title);
            date = itemView.findViewById(R.id.item_memo_TextView_Date);
            checkBox = itemView.findViewById(R.id.item_memo_CheckBox);
        }


        @Override
        public void onClick(View v) {
            // item 눌렀을때는 보기 액티비티가 실행되어야함
            Intent intent = new Intent(context, MemoOpenActivity.class);
            intent.putExtra("MemoTitle", title.getText());
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            new AlertDialog.Builder(context)
                    .setMessage("수정하시겠습니까?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(context, MemoApplyActivity.class);
                            intent.putExtra("Purpose", "Modify");
                            intent.putExtra("MemoTitle", title.getText());
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
            return true;
        }

    }


    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    public void refresh(){
        this.notifyDataSetChanged();
    }

    public void changeListState(int state){
        listState = state;
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
