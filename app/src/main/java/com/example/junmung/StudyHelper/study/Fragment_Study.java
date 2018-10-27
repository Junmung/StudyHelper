package com.example.junmung.StudyHelper.study;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junmung.StudyHelper.R;
import com.shinelw.library.ColorArcProgressBar;

import info.hoang8f.widget.FButton;

public class Fragment_Study extends Fragment{
    private static final int HOUR = 3600000;
    private static final int MIN = 60000;
    private static final int SEC = 1000;
    private static final boolean START = true;
    private static final int SettingLayout = 0x100;
    private static final int StartLayout = 0x101;
    private static final int StartButton = 0x102;
    private static final int StopButton = 0x103;
    private static final int ResetMode = 0x104;
    private static final int NormalMode = 0x105;


    private ConstraintLayout setLayout;
    private ConstraintLayout studyLayout;
    private ConstraintLayout container;

    private TextView studyRunningTime, targetTimeSet, text_targetTime;
    private ImageButton btn_start, btn_stop, btn_plus, btn_minus;
    private ImageButton btn_reset, btn_modify;
    private FButton btn_set;
    private ColorArcProgressBar progressBar_Top;

    private int targetTime;
    private int studyTime;


    private boolean isStart;
    private boolean isRunning;


    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private StudyTimeThread timeThread;


    public Fragment_Study() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        this.container = (ConstraintLayout) inflater.inflate(R.layout.fragment_study, container, false);


        return this.container;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getID_SetListener(container);
        setDatabaseIfFirstRun();

        // 시작값에 따라서 Layout 표시를 하냐마냐를 결정해야함.
        if(isStart) {
            showLayout(StartLayout);

            setTimeData();
            text_targetTime.setText("목표시간 : "  + targetTime/HOUR + "시간");

            if(isRunning){
                showButton(StopButton);

                studyTime = getStudyTimeForSleepApp();
                saveStudyTimeAndPoint(0);
                progressBar_Top.setMaxValues(targetTime/250);
                timeThreadStart();
            }
            else{
                showButton(StartButton);
                progressBar_Top.setMaxValues(targetTime/250);
                progressBar_Top.setCurrentValues(studyTime);
                studyRunningTime.setText(timeToRunningTime(studyTime));
            }
        }
        else {
            showLayout(SettingLayout);
            targetTimeSet.setText("0");
            targetTime = 0;

        }
    }

    private int getStudyTimeForSleepApp() {
        long savePoint = preferences.getLong("SavePoint", 0);
        long restartPoint;

        if(savePoint == 0)
            restartPoint = 0;
        else
            restartPoint = System.currentTimeMillis();

        studyTime += restartPoint - savePoint;

        return studyTime;
    }


    // 첫시작일경우 DB 세팅
    private void setDatabaseIfFirstRun(){
        preferences = getContext().getSharedPreferences("StudyTime", Context.MODE_PRIVATE);
        editor = preferences.edit();
        boolean isFirstRun = preferences.getBoolean("isFirstRun", true);

        if(isFirstRun){
            isStart = false;
            isRunning = false;
            editor.putBoolean("isFirstRun", false);
            editor.putBoolean("isStart", false);
            editor.putBoolean("isRunning", false);
            editor.putInt("TargetTime", 0);
            editor.putInt("StudyTime", 0);
            editor.putLong("SavePoint", 0);
            editor.putLong("RestartPoint", 0);
            editor.commit();
        }
        else{
            isStart = preferences.getBoolean("isStart", false);
            isRunning = preferences.getBoolean("isRunning", false);
        }
    }


    private void getID_SetListener(ConstraintLayout layout){
        btn_set = layout.findViewById(R.id.fragment_Study_Button_Set);
        btn_set.setButtonColor(getResources().getColor(R.color.colorPrimary));
        btn_set.setShadowEnabled(true);
        btn_set.setTextColor(getResources().getColor(R.color.colorWhite));
        btn_set.setTextSize(15);
        btn_set.setShadowColor(getResources().getColor(R.color.fbutton_color_asbestos));
        btn_set.setShadowHeight(5);
        btn_set.setCornerRadius(40);
        btn_set.setOnClickListener(onClickListener);
        btn_start = layout.findViewById(R.id.fragment_Study_Button_Start);
        btn_start.setOnClickListener(onClickListener);
        btn_stop = layout.findViewById(R.id.fragment_Study_Button_Stop);
        btn_stop.setOnClickListener(onClickListener);
        showButton(StartButton);

        btn_reset = layout.findViewById(R.id.fragment_Study_ImageButton_Reset);
        btn_reset.setOnClickListener(onResetModifyClickListener);
        btn_modify = layout.findViewById(R.id.fragment_Study_ImageButton_Modify);
        btn_modify.setOnClickListener(onResetModifyClickListener);

        btn_plus = layout.findViewById(R.id.fragment_Study_Button_PlusTime);
        btn_plus.setOnClickListener(onClickListener);
        btn_minus= layout.findViewById(R.id.fragment_Study_Button_MinusTime);
        btn_minus.setOnClickListener(onClickListener);

        setLayout = layout.findViewById(R.id.fragment_Study_Layout_Set);
        studyLayout = layout.findViewById(R.id.fragment_Study_Layout_Study);

        progressBar_Top = layout.findViewById(R.id.fragment_Study_ProgressBar_Top);

        targetTimeSet = layout.findViewById(R.id.fragment_Study_TextView_targetTimeSet);
        studyRunningTime = layout.findViewById(R.id.fragment_Study_TextView_runningTime);
        studyRunningTime.bringToFront();
        text_targetTime = layout.findViewById(R.id.fragment_Study_TextView_targetTime);
    }


    // 목표설정, 타이머 시작, 일시정지 버튼 리스너
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){

                // 목표설정버튼
                case R.id.fragment_Study_Button_Set:
                    if(targetTime != 0){
                        showLayout(StartLayout);
                        text_targetTime.setText("목표시간 : "  + targetTime/HOUR + "시간");
                        changeStartState(START);
                    }
                    else
                        Toast.makeText(getContext(), "목표시간을 설정하세요", Toast.LENGTH_SHORT).show();
                    break;


                    // 타이머 시작버튼
                case R.id.fragment_Study_Button_Start:
                    showButton(StopButton);

                    editor.putBoolean("isRunning", true);
                    isRunning = true;
                    editor.commit();

                    setTimeData();
                    progressBar_Top.setMaxValues(targetTime/250);
                    text_targetTime.setText("목표시간 : "  + targetTime/HOUR + "시간");
                    timeThreadStart();
                    break;


                    // 타이머 정지버튼
                case R.id.fragment_Study_Button_Stop:
                    timeThread.stopThread(NormalMode);
                    break;

                case R.id.fragment_Study_Button_PlusTime:
                    targetTime += HOUR;
                    targetTimeSet.setText(String.format("%d", targetTime / HOUR));
                    break;

                case R.id.fragment_Study_Button_MinusTime:
                    if(targetTime == 0)
                        targetTime = 0;
                    else
                        targetTime -= HOUR;
                    targetTimeSet.setText(String.format("%d", targetTime / HOUR));

                    break;
            }
        }
    };


    // 초기화, 목표시간 수정 버튼
    private View.OnClickListener onResetModifyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.fragment_Study_ImageButton_Reset:
                    new AlertDialog.Builder(getContext())
                            .setTitle("주의!")
                            .setMessage("오늘의 공부시간이 초기화 됩니다\n\n실행 하시겠습니까?")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(timeThread == null)
                                        timeThread = new StudyTimeThread(new TimeHandler(), studyTime);
                                    timeThread.stopThread(ResetMode);
                                    Toast.makeText(getContext(), "초기화 되었습니다", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) { }
                            })
                            .show();

                    break;

                case R.id.fragment_Study_ImageButton_Modify:
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_study_targettime, null);
                    final EditText modifiedTargetTime = dialogView.findViewById(R.id.dialog_study_EditText_targetTime);

                    new AlertDialog.Builder(getContext())
                            .setView(dialogView)
                            .setMessage("수정할 목표시간을 입력해주세요")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    targetTime = Integer.parseInt(modifiedTargetTime.getText().toString())*HOUR;
                                    if(timeThread == null)
                                        timeThread = new StudyTimeThread(new TimeHandler(), studyTime);
                                    timeThread.stopThread(NormalMode);
                                    text_targetTime.setText("목표시간 : "  + targetTime/HOUR + "시간");
                                    Toast.makeText(getContext(), "목표시간이 수정되었습니다", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();

                    break;
            }
        }
    };



    private class StudyTimeThread extends Thread {
        TimeHandler mHandler;
        int studyTime;
        boolean Run = true;

        public StudyTimeThread(TimeHandler mHandler, int studyTime) {
            this.mHandler = mHandler;
            this.studyTime = studyTime;
        }

        @Override
        public void run() {
            super.run();
            while(Run){
                Message msg = mHandler.obtainMessage();
                msg.arg1 = studyTime;
                mHandler.sendMessage(msg);

                try { Thread.sleep(SEC); } catch (InterruptedException e) { e.printStackTrace(); }
                studyTime += SEC;
            }
        }

        public void stopThread(int mode){
            Run = false;
            if(mode == NormalMode){
                editor.putInt("TargetTime", targetTime);
                editor.putInt("StudyTime", studyTime);
                editor.putBoolean("isStart", true);
            }
            else{
                isStart = false;
                targetTime = 0;
                studyTime = 0;
                editor.putBoolean("isFirstRun", false);
                editor.putBoolean("isStart", false);
                editor.putInt("TargetTime", 0);
                editor.putInt("StudyTime", 0);
                editor.putLong("SavePoint", 0);
                editor.putLong("RestartPoint", 0);
                studyRunningTime.setText("00:00:00");
                progressBar_Top.setCurrentValues(0);
                showLayout(SettingLayout);
            }

            isRunning = false;
            editor.putBoolean("isRunning", false);
            editor.commit();

            showButton(StartButton);
        }
    }

    // 공부 Handler 매 초마다 시간과 ProgressBar 업데이트함
    private class TimeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            studyRunningTime.setText(timeToRunningTime(msg.arg1));
            progressBar_Top.setCurrentValues(msg.arg1);
            studyTime = msg.arg1;
        }
    }


    // 시작여부에 따라 DB 에 목표시간과 시작여부를 저장한다.
    private void changeStartState(boolean isStart) {
        this.isStart = isStart;
        editor.putBoolean("isStart", isStart);
        editor.putInt("TargetTime", targetTime);

        editor.commit();
    }


    private void timeThreadStart(){
        timeThread = new StudyTimeThread(new TimeHandler(), studyTime);
        timeThread.start();
    }


    // DB 에서 목표, 공부시간을 가져온다.
    private void setTimeData(){
        targetTime = preferences.getInt("TargetTime", 0);
        studyTime = preferences.getInt("StudyTime", 0);
    }


    // 현재 시점의 공부시간을 저장
    private void saveStudyTimeAndPoint(long point){
        editor.putLong("SavePoint", point);
        editor.putInt("StudyTime", studyTime);
        editor.commit();
    }


    // int 형 시간을 입력받아서 HH:mm:ss String 형태로 리턴
    private String timeToRunningTime(int time){
        int hour = time / HOUR;
        int min = (time % HOUR ) / MIN;
        int sec = ((time % HOUR ) % MIN ) / SEC;

        return String.format("%02d:%02d:%02d", hour, min, sec);
    }


    // 입력값에 따라 설정Layout, 시작Layout 보여줌
    private void showLayout(int layout){
        switch(layout){
            case SettingLayout:
                setLayout.setVisibility(View.VISIBLE);
                studyLayout.setVisibility(View.GONE);
                break;

            case StartLayout:
                setLayout.setVisibility(View.GONE);
                studyLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    // 입력값에 따라 시작Button, 정지Button 보여줌
    private void showButton(int button){
        switch(button){
            case StartButton:
                btn_stop.setVisibility(View.GONE);
                btn_start.setVisibility(View.VISIBLE);
                break;

            case StopButton:
                btn_stop.setVisibility(View.VISIBLE);
                btn_start.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isRunning)
            saveStudyTimeAndPoint(System.currentTimeMillis());
    }

}
