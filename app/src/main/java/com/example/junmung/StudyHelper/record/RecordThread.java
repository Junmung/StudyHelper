package com.example.junmung.StudyHelper.record;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordThread extends Thread {
    static final int STATE_START = 0x700;
    static final int STATE_RECORDING = 0x701;
    static final int STATE_STOP = 0x702;
    static final int STATE_PAUSE = 0x703;
    static final int STATE_RESUME = 0x704;
    static final int STATE_DONOTTHING = 0x705;


    private int RecordThreadState = STATE_STOP;
    private MediaRecorder recorder;
    private String filePath;
    private boolean isRun;
    private String fileName;

    private Context context;


    public RecordThread(String fileName, Context context) {
        RecordThreadState = STATE_START;
        isRun = true;
        this.fileName = fileName;
        this.context = context;

    }


    @Override
    public void run() {
        while (isRun) {
            switch (RecordThreadState) {
                case STATE_START:
                    // 알림창에 녹음 등록
                    // 핸들러를 통해서 Notification 창을 업데이트해야함

                    // 절대경로에 녹음폴더를 만든다. 없다면 새로 생성
                    String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/";
                    File dirFile = new File(dirPath);
                    if (!dirFile.exists())
                        dirFile.mkdirs();

                    File recordFile = new File(dirFile ,fileName +".3gp");
                    filePath = recordFile.getAbsolutePath();



                    if (recorder == null) {
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);//마이크 사용
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);//파일 확장자 설정
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// 인코더 설정
                        recorder.setOutputFile(filePath);

                        try {
                            recorder.prepare();
                            recorder.start();
                        } catch (IOException e) {e.printStackTrace();}
                    }
                    else {
                        recorder.stop();
                        recorder.release();
                        recorder = null;
                        recorder.getMaxAmplitude();
                    }
                    Log.d("녹음 스레드 ", "녹음시작");

                    RecordThreadState = STATE_RECORDING;
                    break;

                case STATE_RECORDING:
                    // 녹음 중이므로 계속해서 핸들러를 통해 메세지를 보내면서 알림창 + 화면을 업뎃한다.
//                    Message msg = handler.obtainMessage();
//                    msg.arg1 =

                    break;

                case STATE_PAUSE:
                    // 일시정지 됐다면 핸들러를 통해 알림창의 상태를 바꾸는 작업

                    recorder.pause();
                    RecordThreadState = STATE_DONOTTHING;
                    try {
                        this.wait();

                    } catch (InterruptedException e) {
                        RecordThreadState = STATE_RECORDING;
                        recorder.resume();
                        break;
                    }
                    break;

                case STATE_DONOTTHING:
                    // 아무것도 하지않음


                    break;

                case STATE_RESUME:

                    recorder.resume();
                    break;

                case STATE_STOP:
                    recorder.stop();
                    recorder.release();

                    isRun = false;
                    break;
            }
        }
        SharedPreferences preferences = context.getSharedPreferences("RecordFile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String json = preferences.getString("Record_Json","");

        Gson gson = new Gson();
        Type myDataType = new TypeToken<ArrayList<RecordItem>>(){}.getType();

        ArrayList<RecordItem> recordItems = gson.fromJson(json, myDataType);
        if(recordItems == null)
            // 데이터가 아예 없을 경우
            recordItems = new ArrayList<>();

        recordItems.add(0, new RecordItem(fileName, getCurrentDate(), filePath));

        json = gson.toJson(recordItems, myDataType);
        editor.putString("Record_Json", json);
        editor.putBoolean("isStopRecording", true);
        editor.commit();

        super.run();
    }

    public void setState(int state) {
        RecordThreadState = state;
    }

    private String getCurrentDate(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일");

        return sdf.format(date);
    }
}


