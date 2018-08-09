package com.example.junmung.StudyHelper.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junmung.StudyHelper.DataBase.Calendar.DatabaseHelper;
import com.example.junmung.StudyHelper.DataBase.Memo.Memo;
import com.example.junmung.StudyHelper.Memo.MemoOpenActivity;
import com.example.junmung.StudyHelper.R;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


public class GridFragment extends Fragment {
    private GridView gridView;
    private ArrayList<DayItem> dayItems;
    private DaysItemAdapter itemAdapter;
    private Calendar calendar;
    private TextView text_month;
    private ConstraintLayout layout;

    private DatabaseHelper db;


    public GridFragment() { }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (ConstraintLayout)inflater.inflate(R.layout.fragment_calendar_inside, container, false);


        return layout;
    }


    // 액티비티가 만들어지고 나서 할일
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getID_SetListener(layout);

        db = DatabaseHelper.getInstance(getContext());

        int lastDayOfMonth = createEmptyDayItems();
        int lastWeekDayOfMonth = fillDaysOfLastMonth();
        fillDaysOfCurrentMonth(lastDayOfMonth, lastWeekDayOfMonth);

        itemAdapter = new DaysItemAdapter(dayItems);
        gridView.setAdapter(itemAdapter);


        setTextMonth();
    }


    // 해당되는 월의 마지막 날 만큼의 Item 을 만들고 리턴한다.
    private int createEmptyDayItems(){
        int LastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        dayItems = new ArrayList<>(LastDayOfMonth);

        return LastDayOfMonth;
    }

    // 이번달의 시작부분을 정하기 위해서 저번달의 마지막주를 채운다.
    private int fillDaysOfLastMonth(){
        Calendar cal = Calendar.getInstance();

        // 1월달 일 경우
        if(calendar.get(Calendar.MONTH) == 0){
            cal.set(Calendar.YEAR, 2017);
            cal.set(Calendar.MONTH, 11);

        }
        else {
            cal.set(Calendar.YEAR, 2018);
            cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        }
        cal.set(Calendar.DATE, 1);

        // 마지막날
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DATE, lastDay);


        // 마지막 주의 요일
        int LastWeekDayOfMonth = cal.get(Calendar.DAY_OF_WEEK);
        DayItem dayItem;

        // 저번달의 마지막이 토요일에서 끝난 경우 빈공간을 채우지않음
        if(LastWeekDayOfMonth < Calendar.SATURDAY){
            // 첫주 부터 마지막 요일 까지 빈공간을 채운다.
            for(int i = 0; i < LastWeekDayOfMonth; i++){
                dayItem = new DayItem(100, calendar);
                dayItem.setStudyTime(100);
                dayItems.add(dayItem);
            }
        }
        else
            LastWeekDayOfMonth = 0;


        return LastWeekDayOfMonth;
    }

    // 이번달의 해당되는 값들을 채운다. @@ 디비가 들어갈 곳
    private void fillDaysOfCurrentMonth(int lastDayOfMonth, int lastWeekDayOfMonth){
        DayItem dayItem;

        for(int i = 1; i <= lastDayOfMonth; i++){
            dayItem = new DayItem(i, calendar);
            dayItems.add(dayItem);
        }
        setDateInfo(dayItems, lastWeekDayOfMonth);

    }

    private void setDateInfo(ArrayList<DayItem> items, int lastWeekDayOfMonth){
        Cursor cursor = db.getInfoOfMonth(items.get(lastWeekDayOfMonth).getMonth());

        int studyTime;
        int targetStudyTime;
        boolean hasMemo ;


        int i = lastWeekDayOfMonth;
        while(cursor.moveToNext()) {
            studyTime = cursor.getInt(0);
            targetStudyTime = cursor.getInt(1);
            hasMemo = cursor.getInt(2) > 0;

            items.get(i).setStudyTime(studyTime);
            items.get(i).setTargetTime(targetStudyTime);
            items.get(i).setHasMemo(hasMemo);
            i++;
        }
        cursor.close();
    }


    public void setDayItems(ArrayList<DayItem> dayItems){
        this.dayItems = dayItems;
    }



    private void getID_SetListener(ConstraintLayout layout){
        text_month = layout.findViewById(R.id.fragment_calendar_inside_TextView_month);
        gridView = layout.findViewById(R.id.fragment_calendar_inside_GridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onGridItemClick(view, position);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                itemLongClick(view, position);
                return false;
            }
        });

    }

    private String[] getMemoTitlesFromRealm(int month, int day){
        ArrayList<String> tempMemos = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Memo> results = realm.where(Memo.class).findAll().sort("date", Sort.DESCENDING);
        if( results.size() > 0 ){
            for(int i = 0; i < results.size(); i++){
                if(isSameDate(results.get(i), month, day))
                    tempMemos.add(results.get(i).getTitle());
            }
        }

        String[] memos = new String[tempMemos.size()];
        int i = 0;
        for(String item : tempMemos){
            memos[i] = item;
            i++;
        }

        return memos;
    }

    private boolean isSameDate(Memo memo, int month, int day){
        int compareMonth = memo.getMonth();
        int compareDay = memo.getDay();

        if(month == compareMonth && day == compareDay)
            return true;
        else
            return false;
    }

    private void showMemoChooseDialog(int month, int day){
        String[] memoTitles = getMemoTitlesFromRealm(month, day);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_activated_1, memoTitles);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialog = getLayoutInflater().inflate(R.layout.dialog_calendar_choose_memo, null);

        final Spinner spinner = dialog.findViewById(R.id.dialog_calendar_choose_memo_Spinner);
        spinner.setAdapter(adapter);


        builder.setView(dialog)
                .setMessage("보고싶은 메모를 선택하세요")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedTitle = spinner.getSelectedItem().toString();
                        Intent intent = new Intent(getContext(), MemoOpenActivity.class);

                        // 스피너에서 받아온 타이틀을 넣어서 넘긴다.
                        intent.putExtra("MemoTitle", selectedTitle);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void itemLongClick(View view, int position) {
        DatabaseHelper db = DatabaseHelper.getInstance(getContext());
        DayItem item = dayItems.get(position);
        int month = item.getMonth();
        int day = item.getDay();

        if(db.hasMemo(month, day)){
            dayItems.get(position).setHasMemo(true);
            listUpdate();
            showMemoChooseDialog(month, day);
        }
        else
            Toast.makeText(getContext(), "선택한 날짜의 메모가 없습니다", Toast.LENGTH_SHORT).show();
    }

    // 그리드뷰 아이템 클릭했을시 처리 되는 부분
    private void onGridItemClick(View view, int position){

        DayItem item = dayItems.get(position);

        String studyTime = item.getCommonStudyTime();
        String targetTime = item.getCommonTargetTime();

        Snackbar.make(view,
                "목표시간 : " + targetTime+"\n" +
                        "공부시간 : " + studyTime,
                Snackbar.LENGTH_SHORT).show();
    }


    public void setCalendar(Calendar calendar){
        this.calendar = calendar;

    }


    public void setStudyTime(int month, int day, int studyTime){
        for(int i = 0; i < dayItems.size(); i++){
            if(dayItems.get(i).getDay() == day && dayItems.get(i).getMonth() == month)
                dayItems.get(i).setStudyTime(studyTime);
        }
    }

    public void setTargetTime(int month, int day, int targetTime){
        for(int i = 0; i < dayItems.size(); i++){
            if(dayItems.get(i).getDay() == day && dayItems.get(i).getMonth() == month)
                dayItems.get(i).setTargetTime(targetTime);
        }
    }

    // 달력 타이틀 월 표시
    private void setTextMonth() {
        int month = calendar.get(Calendar.MONTH);
        text_month.setText(String.format("%02d", month + 1));
    }


    public void listUpdate(){
        itemAdapter.refresh();
    }

    public ArrayList<DayItem> getDayItems() {
        return dayItems;
    }
}
