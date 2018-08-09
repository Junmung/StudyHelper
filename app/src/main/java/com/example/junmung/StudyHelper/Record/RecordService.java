package com.example.junmung.StudyHelper.Record;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.junmung.StudyHelper.MainActivity;
import com.example.junmung.StudyHelper.R;

import java.io.ByteArrayOutputStream;

public class RecordService extends Service {
    RecordThread recordThread;


    Notification noti;
    NotificationManager notiManager;
    Notification.Builder builder;
    RemoteViews remoteView;

    NotiThread notiThread;
    PendingIntent stopIntent;

    String fileName;

    public RecordService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 이 부분에서는 알림창을 실행시키는 부분
        // Thread 시작시키는 부분

        // 받아온 Intent 의 액션값이 STOP 이면 서비스 종료.
        if (intent.getExtras().getString("isStop").equals("STOP")) {
            Toast.makeText(this, "녹음이 중지 되었습니다.", Toast.LENGTH_SHORT).show();


            stopSelf();
        } else {
            fileName = intent.getExtras().getString("FILENAME");
            recordStart();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    public void recordStart() {
        recordThread = new RecordThread(fileName, getBaseContext());
        recordThread.start();


        notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Stop 버튼을 누르게 되면 펜딩인텐트로 인해서 서비스로 돌아와서 서비스를 종료시킨다.
        Intent mIntent = new Intent(getApplicationContext(), RecordService.class);
        mIntent.putExtra("isStop", "STOP");
        stopIntent = PendingIntent.getService(getApplicationContext(), 0, mIntent, 0);

        // 커스텀뷰 생성
        remoteView = new RemoteViews(getPackageName(), R.layout.notification_record);
        remoteView.setOnClickPendingIntent(R.id.notification_Button_Stop, stopIntent);
        remoteView.setTextViewText(R.id.notification_TextView_FileName, fileName);
        remoteView.setTextViewText(R.id.notification_TextView_RecordingTime, timeToRecordingTime(0));
        remoteView.setImageViewResource(R.id.notification_ImageView_recording, R.drawable.recording_48dp);
        remoteView.setImageViewResource(R.id.notification_ImageView_noti, R.drawable.record_noti);

        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent mainPending = PendingIntent.getActivity(getApplicationContext(), 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new Notification.Builder(getApplicationContext())
                // 아이콘 축소가 필요할듯
                .setSmallIcon(R.drawable.record_small)
                .setContentIntent(mainPending)
                .setCustomContentView(remoteView);
        noti = builder.build();


        NotiHandler notiHandler = new NotiHandler();
        notiThread = new NotiThread(notiHandler);
        notiThread.start();
        startForeground(123, noti);
    }


    private class NotiThread extends Thread {
        NotiHandler mHandler;
        boolean isRun;
        int recordingTime;

        public NotiThread(NotiHandler notiHandler) {
            mHandler = notiHandler;
            isRun = true;
            recordingTime = 0;
        }

        @Override
        public void run() {
            super.run();
            while (isRun) {
                recordingTime += 1000;
                Message msg = mHandler.obtainMessage();
                msg.arg1 = recordingTime;

                mHandler.sendMessage(msg);
                notiManager.notify(123, builder.setCustomContentView(remoteView).build());
                try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

            }
        }

        public void stopThread() {
            isRun = false;
        }
    }


    private class NotiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            remoteView.setTextViewText(R.id.notification_TextView_RecordingTime, timeToRecordingTime(msg.arg1));
            super.handleMessage(msg);
        }
    }


    private String timeToRecordingTime(int time) {
        int min = (time % 3600000) / 60000;
        int sec = ((time % 3600000) % 60000) / 1000;

        return String.format("%02d:%02d", min, sec);
    }

    @Override
    public void onDestroy() {
        recordThread.setState(RecordThread.STATE_STOP);
        notiThread.stopThread();
        notiManager.cancel(123);
        super.onDestroy();
        Log.d("Service onDestroy", "onDestroy");
    }
}
