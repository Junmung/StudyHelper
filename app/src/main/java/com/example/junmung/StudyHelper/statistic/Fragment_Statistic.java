package com.example.junmung.studyhelper.statistic;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junmung.studyhelper.calendar.DayItem;
import com.example.junmung.studyhelper.data.calendar.DatabaseHelper;
import com.example.junmung.studyhelper.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import info.hoang8f.widget.FButton;

public class Fragment_Statistic extends Fragment{
    static final int DAY_CASE = 0x600;
    static final int MONTH_CASE = 0x601;
    static final int WEEK_CASE = 0x602;

    static final int WEEK = 7;
    static final int TWOWEEK = 14;

    private int Chart_Case;

    private ConstraintLayout layout;
    private FButton btn_select;
    private Switch switch_value;
    private TextView text_topTime, text_averageTime;
    private LineChart chart;

    private Cursor cursor;

    private ArrayList<DayItem> dayItems;
    private ArrayList<Integer> weekStudyTimes;
    private ArrayList<Integer> monthStudyTimes;

    private ArrayList<Entry> entries;
    private LineDataSet lineDataSet;

    private String[] xAxisValues;

    private int startMonth, startDay;
    private int endMonth, endDay;
    private int topStudyTime;
    private int averageStudyTime;


    public Fragment_Statistic() { }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (ConstraintLayout)inflater.inflate(R.layout.fragment_statistic, container, false);
        getID_SetListener();

        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDayItemsFromCursor();


        entries = new ArrayList<>();
        switch (Chart_Case){
            case DAY_CASE:      createEntriesForDay(true);   break;
            case WEEK_CASE:     createEntriesForWeek();                  break;
            case MONTH_CASE:    createEntriesForMonth();                 break;
        }

        getTopAndAverageStudyTime();

        createLineDataSet();

        LineData lineData = new LineData(lineDataSet);

        lineData.setValueFormatter(new MyValueFormatter());
        setChartAndDraw(lineData);
    }


    // 차트옵션설정 및 그리기
    private void setChartAndDraw(LineData lineData){
        chart.setData(lineData);
        chart.getAxisRight().setEnabled(false);

        IMarker marker = new MyMarkerView(getContext(), R.layout.chart_marker);
        chart.setMarker(marker);
        setYAxisOptions(chart);
        setXAxisOptions(chart);
        chart.getDescription().setEnabled(false);
        chart.animateY(2000, Easing.EasingOption.EaseInQuart);
        chart.invalidate();

    }

    // LineDataSet 만들기
    private void createLineDataSet() {
        lineDataSet = new LineDataSet(entries, "공부시간");

        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.chart_color);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillDrawable(drawable);
        lineDataSet.setColor(R.color.colorPrimary);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setValueTextSize(10);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setCircleColor(R.color.colorInvisible);
        lineDataSet.setCircleColorHole(R.color.colorBlack);
        lineDataSet.setLabel("");
    }

    private void createEntriesForDay(boolean isFirstStart) {
        int studyTime;

        // 시작시 2주 기간의 데이터만 보여준다.
        if(isFirstStart){
            int j = 0;
            xAxisValues = new String[TWOWEEK];
            for(int i = dayItems.size() - TWOWEEK; i < dayItems.size(); i++){
                studyTime = dayItems.get(i).getStudyTime();
                xAxisValues[j] = String.format("%d월 %d일", dayItems.get(i).getMonth(), dayItems.get(i).getDay());
                entries.add(new Entry(j, convertTimeToPercent(studyTime)));
                j++;
            }
        }
        else{
            entries.clear();
            getSelectedDayItemsFromDB(startMonth, startDay, endMonth, endDay);
            xAxisValues = new String[dayItems.size()];
            int i;
            for(i = 0; i < dayItems.size(); i++){
                studyTime = dayItems.get(i).getStudyTime();
                xAxisValues[i] = String.format("%d월 %d일", dayItems.get(i).getMonth(), dayItems.get(i).getDay());
                entries.add(new Entry(i, convertTimeToPercent(studyTime)));
            }
        }
    }

    // 주단위 list 생성
    private void createEntriesForWeek() {
        int studyTime;

        convertWeekUnit();
        for(int i = 0; i < weekStudyTimes.size(); i++){
            studyTime = weekStudyTimes.get(i);
            entries.add(new Entry(i, convertTimeToPercent(studyTime)));
        }
    }

    // 월단위 list 생성
    private void createEntriesForMonth() {
        int studyTime;

        convertMonthUnit();
        for(int i = 0; i < monthStudyTimes.size(); i++){
            studyTime = monthStudyTimes.get(i);
            entries.add(new Entry(i, convertTimeToPercent(studyTime)));
        }
    }

    private void getID_SetListener() {
        chart = layout.findViewById(R.id.fragment_statistic_Chart);
        btn_select = layout.findViewById(R.id.fragment_statistic_Button_select);
        btn_select.setButtonColor(getResources().getColor(R.color.colorPrimary));
        btn_select.setShadowEnabled(true);
        btn_select.setTextColor(getResources().getColor(R.color.colorWhite));
        btn_select.setTextSize(15);
        btn_select.setShadowColor(getResources().getColor(R.color.fbutton_color_asbestos));
        btn_select.setShadowHeight(5);
        btn_select.setCornerRadius(40);
        btn_select.setOnClickListener(onClickListener);
        if(Chart_Case == DAY_CASE){
            btn_select.setVisibility(View.VISIBLE);
        }
        switch_value = layout.findViewById(R.id.fragment_statistic_switch);
        switch_value.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    lineDataSet.setDrawValues(true);
                else
                    lineDataSet.setDrawValues(false);
                lineDataSet.notifyDataSetChanged();
                chart.notifyDataSetChanged();
                chart.invalidate();
            }
        });


        text_topTime = layout.findViewById(R.id.fragment_statistic_TextView_TopStudyTime);
        text_averageTime = layout.findViewById(R.id.fragment_statistic_TextView_AverageStudyTime);
    }


    // Cursor 에서 데이터를 가져와 세팅한다.
    private void getDayItemsFromCursor(){
        dayItems = new ArrayList<>();
        DayItem dayItem;
        int month, day, studyTime, targetTime;
        boolean hasMemo;

        while(cursor.moveToNext()){
            month = cursor.getInt(0);
            day = cursor.getInt(1);
            studyTime = cursor.getInt(2);
            targetTime = cursor.getInt(3);
            hasMemo = cursor.getInt(4) > 0;

            dayItem = new DayItem(month, day, studyTime, targetTime, hasMemo);
            dayItems.add(dayItem);
        }
        cursor.close();
    }

    // dayItems 를 월단위 List 로 바꿔준다. x축 값도 추가로 저장한다.ㅇ
    private void convertMonthUnit(){
        monthStudyTimes = new ArrayList<>();
        int monthStudyTime = 0;
        int i, j;
        xAxisValues = new String[getCurrentMonth()];
        int count = 0;


        for(i = 0; i < getCurrentMonth()-1; i++){
            int size = dayItems.get(count).getLastDayOfMonth();

            for(j = 0; j < size; j++) {
                monthStudyTime += dayItems.get(count).getStudyTime();
                count++;
            }
            monthStudyTimes.add(monthStudyTime);
            monthStudyTime = 0;
            xAxisValues[i] = String.format("%d월", i+1);
        }

        for(j = 0; j < getCurrentDay(); j++){
            monthStudyTime += dayItems.get(count).getStudyTime();
            count++;
        }
        monthStudyTimes.add(monthStudyTime);
        xAxisValues[i] = String.format("%d월", i+1);
    }

    // dayItems 를 주단위 List 로 바꿔준다. x축 값도 추가로 저장한다.
    private void convertWeekUnit(){
        weekStudyTimes = new ArrayList<>();
        int weekStudyTime = 0;
        int count = 0;
        int month, day;
        int i;
        int j = 0;
        xAxisValues = new String[dayItems.size()/WEEK + 1];

        for(i = 0; i < dayItems.size(); i++){
            if(count < WEEK){
                weekStudyTime += dayItems.get(i).getStudyTime();
                if(count == 0){
                    month = dayItems.get(i).getMonth();
                    day = dayItems.get(i).getDay();
                    xAxisValues[j] = String.format("%d/%d ~ ", month, day);
                }
                else if(count == 6){
                    month = dayItems.get(i).getMonth();
                    day = dayItems.get(i).getDay();
                    xAxisValues[j] += String.format("%d/%d", month, day);
                    j++;
                }

                count++;
            }

            if(count == WEEK){
                weekStudyTimes.add(weekStudyTime);
                count = 0;
                weekStudyTime = 0;
            }
        }

        if(count < WEEK){
            weekStudyTimes.add(weekStudyTime);
            month = dayItems.get(i-1).getMonth();
            day = dayItems.get(i-1).getDay();
            xAxisValues[j] += String.format("%d/%d", month, day);
        }
    }

    // 최고시간과 평균시간을 계산한다.
    private void getTopAndAverageStudyTime(){
        List<Integer> studyTimes = new ArrayList<>();
        long sum = 0;

        switch(Chart_Case){
            case DAY_CASE:
                for(int i = 0; i < dayItems.size(); i++) {
                    int itemStudyTime = dayItems.get(i).getStudyTime();
                    studyTimes.add(itemStudyTime);
                    sum += itemStudyTime;
                }
                topStudyTime = Collections.max(studyTimes);
                averageStudyTime = (int) (sum / studyTimes.size());
                break;

            case WEEK_CASE:
                for(Integer item : weekStudyTimes)
                    sum += item;
                topStudyTime = Collections.max(weekStudyTimes);
                averageStudyTime = (int) (sum / weekStudyTimes.size());
                break;

            case MONTH_CASE:
                for(Integer item : monthStudyTimes)
                    sum += item;
                topStudyTime = Collections.max(monthStudyTimes);
                averageStudyTime = (int) (sum / monthStudyTimes.size());
                break;
        }


        text_topTime.setText("최고 : " + convertTimeToPercent(topStudyTime) + " 시간");
        text_averageTime.setText("평균 : " + convertTimeToPercent(averageStudyTime) + " 시간");
    }

    // 목표설정버튼
    Button.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.fragment_statistic_Button_select){
                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // 시작날짜를 전역변수로 저장해놓는다.
                        startMonth = month + 1;
                        startDay = dayOfMonth;
                        endDateChooseDialog();
                        Toast.makeText(getContext(), "마지막 날짜를 선택하세요", Toast.LENGTH_SHORT).show();

                    }
                }, 2018, getCurrentMonth(), getCurrentDay());
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                dialog.setMessage("시작 날짜를 선택하세요");
                dialog.show();
            }
        }
    };


    
    // 마지막날짜 선택 Dialog 띄우기
    private void endDateChooseDialog(){
        DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                endMonth = month + 1;
                endDay = dayOfMonth;
                
                // 날짜 선택을 완료 했으므로 그래프를 다시 그려줘야한다.

                createEntriesForDay(false);
                createLineDataSet();
                LineData lineData = new LineData(lineDataSet);
                lineData.setValueFormatter(new MyValueFormatter());
                setChartAndDraw(lineData);

            }
        }, 2018, startMonth - 1, startDay);
        dialog.getDatePicker().setMaxDate(new Date().getTime());
        dialog.setMessage("마지막 날짜를 선택하세요");
        dialog.show();
    }

    // 선택된 날짜기간만큼 데이터를 dayItems 에 가져오고, x축 값들을 위한 Strings 도 저장한다.
    private void getSelectedDayItemsFromDB(int startMonth, int startDay, int endMonth, int endDay) {
        DatabaseHelper db = DatabaseHelper.getInstance(getContext());
        Cursor cursor = db.getSelectedDatas(startMonth, startDay, endMonth, endDay);

        DayItem dayItem;
        int month, day, studyTime, targetTime;
        boolean hasMemo;

        dayItems.clear();
        while (cursor.moveToNext()) {
            month = cursor.getInt(0);
            day = cursor.getInt(1);
            studyTime = cursor.getInt(2);
            targetTime = cursor.getInt(3);
            hasMemo = cursor.getInt(4) > 0;

            dayItem = new DayItem(month, day, studyTime, targetTime, hasMemo);
            dayItems.add(dayItem);
        }
        cursor.close();
    }


    // 시간형식에서 퍼센트 형식으로 변환
    private float convertTimeToPercent(int time){
        int hour = time / 3600;
        float reminder = time % 3600;
        float min = reminder / 3600f;

        return Math.round((hour+min)*10f)/10f;
    }

    // 그래프 Y축 옵션
    private void setYAxisOptions(LineChart chart){
        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setDrawGridLines(false);
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setSpaceBottom(1f);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setTextSize(12f);
    }

    // 그래프 X축 옵션세팅
    private void setXAxisOptions(LineChart chart){
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        switch(Chart_Case){
            case DAY_CASE:  xAxis.setGranularity(1f);   break;
            case WEEK_CASE:  xAxis.setGranularity(weekStudyTimes.size()/4);   break;
            case MONTH_CASE:  xAxis.setGranularity(1f);   break;
        }
        xAxis.setValueFormatter(new MyXAxisValueFormatter(xAxisValues));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    }



    public void setCursor(Cursor cursor){
        this.cursor = cursor;
    }

    public void setChart_Case(int chartCase){
        Chart_Case = chartCase;
    }

    private int getCurrentDay(){
        String day_str = new SimpleDateFormat("d").format(new Date());
        return Integer.parseInt(day_str);
    }

    private int getCurrentMonth(){
        String month_str = new SimpleDateFormat("M").format(new Date());
        return Integer.parseInt(month_str);
    }


    // 데이터값 포맷형식 클래스
    private class MyValueFormatter implements IValueFormatter {
        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("#.#");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }

    // X축값 포맷형식 클래스
    private class MyXAxisValueFormatter implements IAxisValueFormatter {
        private  String[] values;

        public MyXAxisValueFormatter(String[] values) {
            this.values = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            String result = null;
            try {
                result = values[(int) value];
            }catch (IndexOutOfBoundsException e){
                axis.setGranularityEnabled(false);
            }
            return result;
        }
    }



    public class MyMarkerView extends MarkerView {
        private TextView tvContent;


        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);

            // find your layout components
            tvContent = findViewById(R.id.chart_marker_TextView);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            String msg = e.getY() + " 시간\n" +
                    xAxisValues[(int)e.getX()];
            tvContent.setText(msg);

            // this will perform necessary layouting
            super.refreshContent(e, highlight);
        }

        private MPPointF mOffset;

        @Override
        public MPPointF getOffset() {
            if(mOffset == null) {
                // center the marker horizontally and vertically
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
            }

            return mOffset;
        }
    }


}











