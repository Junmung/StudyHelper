package com.example.junmung.StudyHelper.Timer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.AlarmClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.example.junmung.StudyHelper.MainActivity;
import com.example.junmung.StudyHelper.R;

public class Fragment_Timer extends Fragment{
    private NumberPicker picker_Hour, picker_Min, picker_Sec;
    private Button btn_start, btn_stop;
    private ConstraintLayout layout_setting, layout_timer;


    public Fragment_Timer() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_timer, container, false);

        getID_SetListener(layout);
        setPickerOption();




        return layout;
    }

    private void getID_SetListener(ConstraintLayout layout){
        layout_setting = layout.findViewById(R.id.fragment_timer_layout_setting);
        layout_setting.setVisibility(View.VISIBLE);
        layout_timer = layout.findViewById(R.id.fragment_timer_layout_timer);
        layout_timer.setVisibility(View.GONE);

        picker_Hour = layout.findViewById(R.id.fragment_timer_Picker_hour);
        picker_Min = layout.findViewById(R.id.fragment_timer_Picker_min);
        picker_Sec = layout.findViewById(R.id.fragment_timer_Picker_sec);

        btn_start = layout.findViewById(R.id.fragment_timer_Button_start);
        btn_start.setOnClickListener(btnClickListener);
        btn_stop = layout.findViewById(R.id.button5);
        btn_stop.setOnClickListener(btnClickListener);

    }

    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.fragment_timer_Button_start:
                    // startService()
                    Intent intent = new Intent(getActivity(), TimerService.class);
                    intent.putExtra("TIMER", picker_Sec.getValue());

                    getActivity().startService(intent);
                    Log.d("ButtonClick", "StartService");

                    break;

                case R.id.button5:
                    Intent stopIntent = new Intent(getActivity(), TimerService.class);
                    getActivity().stopService(stopIntent);
                    break;
            }
        }
    };

    private void setPickerOption(){
        picker_Hour.setMaxValue(24);
        picker_Hour.setMinValue(0);
        picker_Min.setMaxValue(59);
        picker_Min.setMinValue(0);
        picker_Sec.setMaxValue(59);
        picker_Sec.setMinValue(0);

    }
}
