package com.example.junmung.StudyHelper.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.junmung.StudyHelper.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DaysItemAdapter extends BaseAdapter {
    private ArrayList<DayItem> dayItems;
    private LayoutInflater inflater;


    public DaysItemAdapter(ArrayList<DayItem> dayItems) {
        this.dayItems = dayItems;

    }


    // 뷰홀더
    public class ViewHolder {
        public TextView textView;
        public ProgressBar progressBar;
        public ConstraintLayout container;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        ViewHolder viewHolder;

        if(convertView == null){
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_day, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.textView = convertView.findViewById(R.id.item_day_TextView_day);
            viewHolder.progressBar = convertView.findViewById(R.id.item_day_Progress);
            viewHolder.container = convertView.findViewById(R.id.item_day_container);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder)convertView.getTag();



        DayItem dayItem = dayItems.get(position);

        setDayItems(viewHolder, dayItem, context);


        return convertView;
    }

    private void setDayItems(ViewHolder viewHolder, DayItem item, Context context){
        Calendar calendar = item.getCalendar();
        Date date = calendar.getTime();
        int day = item.getDay();
        int studyTime = item.getStudyTime();
        int targetTime = item.getTargetTime();

        if(studyTime == 100){
            viewHolder.textView.setVisibility(View.INVISIBLE);
            viewHolder.progressBar.setVisibility(View.INVISIBLE);
        }
        else{
            int DayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            // 토요일, 일요일이면 TextColor 바꿈
            if(DayOfWeek == Calendar.SATURDAY)
                viewHolder.textView.setTextColor(Color.BLUE);
            else if(DayOfWeek == Calendar.SUNDAY)
                viewHolder.textView.setTextColor(Color.RED);
            else
                viewHolder.textView.setTextColor(Color.BLACK);

            // 오늘이랑 날짜가 같다면 배경색 바꾸기
            if(isTodayDate(date))
                viewHolder.container.setBackgroundColor(context.getResources().getColor(R.color.colorTodayCheck));


            viewHolder.textView.setText(String.format("%d", day));
            viewHolder.progressBar.setMax(targetTime);
            viewHolder.progressBar.setProgress(studyTime);

        }
    }


    public void refresh(){
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dayItems.size();
    }

    @Override
    public Object getItem(int position) {
        return dayItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private boolean isTodayDate(Date date){
        long currentTime = System.currentTimeMillis();
        Date todayDate = new Date(currentTime);

        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        String today = format.format(todayDate);
        String viewDate = format.format(date);

        if(today.compareTo(viewDate) == 0)
            return true;
        else
            return false;
    }
}
