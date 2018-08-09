package com.example.junmung.StudyHelper;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setStatusBar(getWindow());


        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                finish();    // 액티비티 종료
            }
        };

        handler.sendEmptyMessageDelayed(0, 1300);
    }

    private void setStatusBar(Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getColor(R.color.colorWhite));
    }
}
