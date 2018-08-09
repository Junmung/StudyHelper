package com.example.junmung.StudyHelper.Timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.junmung.StudyHelper.MainActivity;
import com.example.junmung.StudyHelper.R;


public class TimerService extends Service {
    private NotificationManager notiManager;
    private Notification noti;
    private TimerThread thread;


    public TimerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int time = intent.getIntExtra("TIMER", 0);
        notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        TimerHandler handler = new TimerHandler();
        thread = new TimerThread(handler, time);
        thread.start();




        return START_STICKY;
    }



    class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int number = msg.arg1;

            Intent intent = new Intent(TimerService.this, MainActivity.class);
            // 알림을 눌러서 액티비티를 시작하게 되면 Main이 다시 시작되니까
            // 액티비티가 새로 생성 될때마다 목표시간이 설정되어있다면 화면 생성할때 목표시간만큼 표시를 다시 해줘서 시작해야함

            PendingIntent pendingIntent = PendingIntent.getActivity(TimerService.this,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


            noti = new Notification.Builder(getApplicationContext())
                    .setContentTitle("타이머")
                    .setContentText("시간이 다 되었습니다.")
//                    .setSmallIcon(R.drawable.timer_24dp)

                  //  .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.add_camera_24dp))
                    .setShowWhen(true)
                    .setWhen(System.currentTimeMillis())
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .build();


            notiManager.notify(123, noti);

            stopSelf();
            Log.d("TimerHandler", "Notify");

        }




    }
}
