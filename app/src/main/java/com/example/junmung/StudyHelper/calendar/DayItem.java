package com.example.junmung.studyhelper.calendar;

import java.util.Calendar;

public class DayItem {
    private int day;
    private int studyTime;
    private int targetTime;
    private boolean hasMemo;
    private Calendar calendar;


    public DayItem(int month, int day, int studyTime, int targetTime, boolean hasMemo){
        this.calendar = Calendar.getInstance();
        this.calendar.set(Calendar.YEAR, 2018);
        this.calendar.set(Calendar.MONTH, month - 1);
        this.calendar.set(Calendar.DATE, day);
        this.day = day;
        this.studyTime = studyTime;
        this.targetTime = targetTime;
        this.hasMemo = hasMemo;
    }

    public DayItem(int day, Calendar calendar) {
        this.day = day;
        this.calendar = Calendar.getInstance();
        this.calendar.set(Calendar.YEAR, 2018);
        this.calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        this.calendar.set(Calendar.DATE, day);
    }

    public int getDay() {
        return day;
    }

    public int getMonth(){
        return calendar.get(Calendar.MONTH) + 1;
    }

    public int getLastDayOfMonth(){
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public int getStudyTime() {
        return studyTime;
    }

    public String getCommonStudyTime(){
        int hour = studyTime / 3600;
        int min = studyTime % 3600 / 60;
        int second = studyTime % 3600 % 60;

        return String.format("%d시간 %d분 %d초", hour, min, second);
    }

    public String getCommonTargetTime(){
        return String.format("%d시간", targetTime / 3600);
    }

    public String getCommonDate(){
        return String.format("%d월 %d일", getMonth(), day);
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setStudyTime(int studyTime) {
        this.studyTime = studyTime;
    }

    public int getTargetTime() {
        return targetTime;
    }

    public boolean isHasMemo() {
        return hasMemo;
    }

    public void setTargetTime(int targetTime) {
        this.targetTime = targetTime;
    }

    public void setHasMemo(boolean hasMemo) {
        this.hasMemo = hasMemo;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    @Override
    public String toString() {
        String str = "DayItem => 월 : "+getMonth()+", 일 : "+day+", 공부시간 : "+studyTime
                +", 목표시간 : "+ targetTime+", 메모유무 : "+hasMemo;
        return str;
    }
}
