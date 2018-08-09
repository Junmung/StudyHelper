package com.example.junmung.StudyHelper;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.example.junmung.StudyHelper.Calendar.Fragment_Calendar;
import com.example.junmung.StudyHelper.DataBase.Calendar.DatabaseHelper;
import com.example.junmung.StudyHelper.Drawer.EmailActivity;
import com.example.junmung.StudyHelper.Record.Fragment_Record;
import com.example.junmung.StudyHelper.Memo.Fragment_Memo;
import com.example.junmung.StudyHelper.Statistic.StudyStatisticActivity;
import com.example.junmung.StudyHelper.Study.Fragment_Study;
import com.example.junmung.StudyHelper.Drawer.DrawerItemAdapter;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_CONTACTS = 0x20;
    static final int REQUEST_SEND = 0x21;
    static final int REQUEST_CALL = 0x22;

    private ConstraintLayout mainContainer;

    private Toolbar toolbar;
    private ActionBar actionBar;
    private ViewPager viewPager;
//    private TabLayout tabLayout;
    private BottomNavigationView bottomView;
    private MenuItem bottomMenuItem;


    private ListView menuListView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(this, SplashActivity.class));

        new InitThread().start();

    }

    class InitThread extends Thread {
        @Override
        public void run() {
            super.run();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    getID_setListener();
                    setMyActionBar();


                    if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{
                                        Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.CALL_PHONE
                                },0);
                    }

                    // 키패드가 UI 가리게하기
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


                    DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
                    if(databaseHelper.isEmpty())
                        databaseHelper.init();

                    Realm.init(getBaseContext());
                }
            });
        }
    }


    // 액션바 세팅
    private void setMyActionBar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);    // 커스텀하기위해 필요
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
        actionBar.setDisplayHomeAsUpEnabled(true);      // 뒤로가기버튼 생성

        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);    // 뒤로가기 이미지 따로 넣기
    }


    // UI ID값 가져오는 메소드
    private void getID_setListener() {
        mainContainer = findViewById(R.id.activity_main_container);
        toolbar = findViewById(R.id.mainActivity_Toolbar);

        bottomView = findViewById(R.id.activity_main_BottomNavigationView);
//        tabLayout = findViewById(R.id.activity_main_TabLayout);

        // 뷰페이저 세팅
        viewPager = findViewById(R.id.activity_main_Viewpager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(pageChangeListener);
        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(0);



        bottomView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottom_item_study:    viewPager.setCurrentItem(0);    return true;
                    case R.id.bottom_item_record:   viewPager.setCurrentItem(1);    return true;
                    case R.id.bottom_item_calendar: viewPager.setCurrentItem(2);    return true;
                    case R.id.bottom_item_memo:     viewPager.setCurrentItem(3);    return true;
                }
                return false;
            }
        });


        // tabLayout
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                viewPager.setCurrentItem(tab.getPosition());
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });



        // 드로어 리스트뷰 세팅
        menuListView = findViewById(R.id.activity_main_ListView_menu);
        menuListView.setAdapter(new DrawerItemAdapter());
        menuListView.setOnItemClickListener(new DrawerItemClickListener());

        drawerLayout = findViewById(R.id.activity_main_drawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer){
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if(bottomMenuItem != null){
                bottomMenuItem.setChecked(false);
            }
            bottomMenuItem = bottomView.getMenu().getItem(position);
            bottomMenuItem.setChecked(true);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    // 뷰페이저 Adapter
    private class PagerAdapter extends FragmentStatePagerAdapter{
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:     return new Fragment_Study();
                case 1:     return new Fragment_Record();
                case 2:     return new Fragment_Calendar();
                case 3:     return new Fragment_Memo();
                default:    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }


    // 드로어 아이템 클릭했을때 처리해야할 기능들
    private class DrawerItemClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent;
            switch (position){
                case 0:

                    break;

                    // 파트장에게 질문하기
                case 1:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("선택창")
                            .setMessage("파트장님께 질문할 방법을 선택하세요.")
                            .setPositiveButton("전화", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri callUri = Uri.parse("tel:01022096432");
                                    Intent callIntent = new Intent(Intent.ACTION_CALL, callUri);


                                    startActivity(callIntent);
                                }
                            })
                            .setNeutralButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("문자", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri uri = Uri.parse("smsto:01022096432");
                                    Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);

                                    startActivity(sendIntent);
                                }
                            }).show();
                    break;

                    // 공부시간보고
                case 2:
                    Intent contactIntent = new Intent(Intent.ACTION_PICK);
                    contactIntent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(contactIntent, REQUEST_CONTACTS);


                    break;


                    // 통계
                case 3:
                    Intent studyStatisticIntent = new Intent(getApplicationContext(), StudyStatisticActivity.class);
                    startActivity(studyStatisticIntent);

                    break;


                    // 블로그 방문
                case 4:
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.naver.com/manadra"));
                    startActivity(intent);

                    break;

                    // 설정
                case 5:
//                    intent = new Intent(MainActivity.this, SettingActivity.class);
//                    startActivity(intent);
                    intent = new Intent(MainActivity.this, EmailActivity.class);
                    startActivity(intent);
                    break;

                    // 개발자에게 문의 (이메일)
                case 6:
                    intent = new Intent(MainActivity.this, EmailActivity.class);
                    startActivity(intent);
                    break;

            }
            drawerLayout.closeDrawer(menuListView);
        }
    }



    // Intent 결과값 받아오는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK)
            return;

        if(requestCode == REQUEST_CONTACTS){
            Cursor cursor = getContentResolver().query(data.getData(),
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
            cursor.moveToFirst();
            String number = cursor.getString(0);
            cursor.close();

            Uri uri = Uri.parse("smsto:"+number);
            Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, sendIntent, 0);

            String smsBody = "목표시간 : 12시간\n공부시간 : 10시간 32분 5초";
            sendIntent.putExtra("sms_body", smsBody);

            startActivityForResult(sendIntent,REQUEST_SEND);
        }
        else if( requestCode == REQUEST_SEND){
            Toast.makeText(this, "전송완료", Toast.LENGTH_SHORT).show();
        }

        else if( requestCode == REQUEST_CALL){

        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    // 메뉴 버튼 토글
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
}
