package com.example.junmung.StudyHelper.statistic;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.example.junmung.StudyHelper.data.calendar.DatabaseHelper;
import com.example.junmung.StudyHelper.R;

import java.util.ArrayList;

public class StudyStatisticActivity extends AppCompatActivity {
    ArrayList<Fragment_Statistic> fragments;
    private NonSwipeViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_statistic);
        DatabaseHelper db = DatabaseHelper.getInstance(this);

        // 데이터를 SQLite 에서 받아온 뒤에 fragments 먼저 생성해야함.
        fragments = new ArrayList<>(3);

        for (int i = 0; i < 3; i++) {
            fragments.add(new Fragment_Statistic());
            fragments.get(i).setCursor(db.getAllDataUntilToday(8, 16));
        }

        fragments.get(0).setChart_Case(Fragment_Statistic.DAY_CASE);
        fragments.get(1).setChart_Case(Fragment_Statistic.WEEK_CASE);
        fragments.get(2).setChart_Case(Fragment_Statistic.MONTH_CASE);


        getID_SetListener();

    }

    private void getID_SetListener() {
        toolbar = findViewById(R.id.activity_statistic_ToolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("통계");
        tabLayout = findViewById(R.id.activity_statistic_TabLayout);

        viewPager = findViewById(R.id.activity_statistic_ViewPager);
        viewPager.setAdapter(new TabPagerAdapter(getSupportFragmentManager(), fragments));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setOffscreenPageLimit(2);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }


    private class TabPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Fragment_Statistic> fragments;

        public TabPagerAdapter(FragmentManager fm, ArrayList<Fragment_Statistic> fragments) {
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


}
