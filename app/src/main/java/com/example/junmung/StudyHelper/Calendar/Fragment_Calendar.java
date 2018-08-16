package com.example.junmung.StudyHelper.Calendar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.junmung.StudyHelper.DataBase.Calendar.DatabaseHelper;
import com.example.junmung.StudyHelper.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

public class Fragment_Calendar extends Fragment {
    private static final int HOUR = 3600;
    private VerticalViewPager viewPager;
    private ArrayList<GridFragment> fragments;

    private FloatingActionMenu fab_menu;
    private FloatingActionButton fab_refresh, fab_currentPage;

    private DatabaseHelper db;

    public Fragment_Calendar() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ConstraintLayout layout = (ConstraintLayout)inflater.inflate(R.layout.fragment_calendar, container, false);
        getID_SetListener(layout);


        return layout;
    }


    // 이 부분에서 달력생성
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<Calendar> calendars = new ArrayList<>();
        int i;

        for( i = 0; i < 12; i++){
            Calendar cal = new GregorianCalendar();
            cal.set(2018, i, 1);

            calendars.add(cal);
        }

        // fragment 리스트에 월별로 세팅된 Fragment 를 생성해서 추가한다.
        fragments = new ArrayList<>(12);
        for(i = 0; i < 12; i++)
            fragments.add(new GridFragment());

        for(i = 0; i < 12; i++)
            fragments.get(i).setCalendar(calendars.get(i));


        viewPager.setAdapter(new PagerAdapter(getActivity().getSupportFragmentManager(), fragments));
        viewPager.setCurrentItem(getCurrentPage());
    }

    private void getID_SetListener(ConstraintLayout layout){
        viewPager = layout.findViewById(R.id.fragment_calendar_ViewPager);

        fab_refresh = layout.findViewById(R.id.fragment_calendar_Fab_refresh);
        fab_refresh.setOnClickListener(fabClickListener);
        fab_currentPage = layout.findViewById(R.id.fragment_calendar_Fab_currentPage);
        fab_currentPage.setOnClickListener(fabClickListener);

        fab_menu = layout.findViewById(R.id.fragment_calendar_Fab_menu);
        fab_menu.setClosedOnTouchOutside(true);
        fab_menu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened)
                    fab_menu.setBackgroundColor(getResources().getColor(R.color.colorGray));
                else
                    fab_menu.setBackgroundColor(getResources().getColor(R.color.colorInvisible));
            }
        });


    }


    // 플로팅버튼 클릭 기능
    private FloatingActionButton.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                // 플로팅 갱신 버튼
                case R.id.fragment_calendar_Fab_refresh:
                    fab_menu.close(true);

                    db = DatabaseHelper.getInstance(getContext());
                    Random random = new Random();

//                    for(int i = 1; i < 30; i++){
//                        int random_ = random.nextInt(10);
//                        fragments.get(3).setTargetTime(4, i, HOUR * 10);
//                        fragments.get(3).setStudyTime(4, i, HOUR * random_);
//
//                        db.setTargetTime(j, i, HOUR * 10);
//                        db.updateStudyTime(j, i, HOUR * random_);
//                    }

                    for(int i = 1; i < 8; i++){
                        Calendar cal = new GregorianCalendar();
                        cal.set(Calendar.YEAR, 2018);
                        cal.set(Calendar.MONTH, i);
                        cal.set(Calendar.DATE, 1);
                        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                        for(int j = 1; j <= lastDay; j++){
                            int random_ = random.nextInt(10);
                            db.setTargetTime(i, j, HOUR * 10);
                            db.updateStudyTime(i, j, HOUR * random_);
                        }
                    }
                    for( int i =1; i <= 16; i++){
                        int random_ = random.nextInt(10);
                        db.setTargetTime(8, i, HOUR * 10);
                        db.updateStudyTime(8, i, HOUR * random_);
                    }

//                    fragments.get(4).listUpdate();
//
//                    fragments.get(3).setStudyTime(4, 30, HOUR);
//                    fragments.get(3).setTargetTime(4, 30, HOUR);
//                    fragments.get(3).listUpdate();

                    //Toast.makeText(getContext(), "갱신", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.fragment_calendar_Fab_currentPage:
                    fab_menu.close(true);

//                    fragments.get(3).setStudyTime(4, 23, HOUR*8+1480);
//                    fragments.get(3).setTargetTime(4, 23, HOUR * 13);
//                    fragments.get(3).listUpdate();
//
//                    db = DatabaseHelper.getInstance(getContext());
//
//                    for(int i = 16; i < 21; i++){
//
//                        db.setTargetTime(4, i, 0);
//                        db.updateStudyTime(4, i, 0);
//                    }

                    viewPager.setCurrentItem(getCurrentPage());
                    break;
            }
        }
    };

    // 뷰페이저 어댑터
    private class PagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<GridFragment> fragments;

        PagerAdapter(FragmentManager fm, ArrayList<GridFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }


    // 현재페이지수 가져오기
    private int getCurrentPage(){
        String month = new SimpleDateFormat("M").format(new Date());

        return Integer.parseInt(month) - 1;
    }
}












