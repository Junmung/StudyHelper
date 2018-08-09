package com.example.junmung.StudyHelper.Timer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TimerThread extends Thread {
    static final int TIMER_STATE_PAUSE = 0x500;
    static final int TIMER_STATE_RUN = 0x501;
    static final int TIMER_STATE_STOP = 0x502;
    private Handler handler;
    private int time;


    public TimerThread(Handler handler, int time) {
        this.handler = handler;
        this.time = time;
    }


    @Override
    public void run() {

        try {
            Thread.sleep(1000 * time);
        } catch (Exception e) {
        }

        Message msg = handler.obtainMessage();
        msg.arg1 = 5;
        handler.sendMessage(msg);
    }
}
