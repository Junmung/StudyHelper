package com.example.junmung.StudyHelper.Record;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.junmung.StudyHelper.R;

import java.util.ArrayList;

public class RecordItemAdapter extends BaseAdapter {
    ArrayList<RecordItem> items;
    int lastPosition = -1;

    public RecordItemAdapter(ArrayList<RecordItem> items) {
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_record, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.item_record_TextView_title);
            holder.date = convertView.findViewById(R.id.item_record_TextView_date);
            holder.playTime = convertView.findViewById(R.id.item_record_TextView_playTime);
            convertView.setTag(holder);
        }


        RecordItem recordItem = items.get(position);
        if(recordItem != null){
            ViewHolder holder = (ViewHolder)convertView.getTag();
            holder.title.setText(recordItem.getTitle());
            holder.date.setText(recordItem.getSavedDate());
            String playTime1 = timeToPlayTime(Integer.parseInt(recordItem.getPlayTime()));
            holder.playTime.setText(playTime1);
        }

        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);

        convertView.setAnimation(animation);
        convertView.startAnimation(animation);
        lastPosition = position;
        Log.e("lastPosition_Record", ""+position);

        return convertView;
    }

    class ViewHolder {
        TextView title;
        TextView date;
        TextView playTime;
    }


    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(String title, String date, String filePath){
        items.add(0, new RecordItem(title, date, filePath));
    }

    public void removeItem(int position){
        items.remove(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void updateList(ArrayList<RecordItem> items){
        this.items = items;
        refresh();
    }

    public void refresh(){
        notifyDataSetChanged();
    }

    public void modify(int position, String name){
        items.get(position).setTitle(name);
        String modifiedFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/" + name + ".3gp";
        items.get(position).setFilePath(modifiedFilePath);
    }

    static public String timeToPlayTime(int time){
        int duration = time / 1000;
        int min = duration / 60;
        int sec = duration - (min * 60);

        return String.format("%02d:%02d", min, sec);
    }
}
