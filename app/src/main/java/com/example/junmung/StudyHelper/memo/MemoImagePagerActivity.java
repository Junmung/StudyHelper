package com.example.junmung.studyhelper.memo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.junmung.studyhelper.data.origmemo.Memo;
import com.example.junmung.studyhelper.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

import io.realm.Realm;

public class MemoImagePagerActivity extends AppCompatActivity {

    private CustomViewPager viewPager;

    private ArrayList<Bitmap> originImages;

    private TextView text_currentPage, text_totalPage;
    private ImageButton btn_cancel;

    private String memoTitle;
    private int currentPage;
    private ConstraintLayout layout;
    private boolean isLayoutShow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_image_pager);
        setStatusBar(getWindow());

        originImages = new ArrayList<>();

        Intent intent = getIntent();
        memoTitle = intent.getStringExtra("MemoTitle");
        currentPage = intent.getIntExtra("CurrentPage", 0);

        getImagesInRealm(memoTitle);

        getID_SetListener();

        text_currentPage.setText(String.format("%d", currentPage + 1));
        text_totalPage.setText(String.format("%d", originImages.size()));
    }



    private void getImagesInRealm(String memoTitle) {
        Realm realm = Realm.getDefaultInstance();

        Memo memo = realm.where(Memo.class).equalTo("title", memoTitle).findFirst();

        int imageCount = memo.getImages().size();

        if(imageCount > 0){
            for(int i = 0; i < imageCount; i++){
                byte[] imageByte = memo.getImages().get(i).getImage();
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                originImages.add(bitmap);
            }
        }
    }


    private void getID_SetListener(){
        layout = findViewById(R.id.activity_memo_image_pager_Layout);
        text_currentPage = findViewById(R.id.activity_memo_image_pager_currentPage);
        text_totalPage = findViewById(R.id.activity_memo_image_pager_totalPage);
        btn_cancel = findViewById(R.id.activity_memo_image_pager_finish);
        btn_cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        viewPager = findViewById(R.id.activity_memo_image_pager_ViewPager);
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setOffscreenPageLimit(originImages.size() - 1);
        viewPager.setAdapter(new ImageAdapter(this));
        viewPager.setCurrentItem(currentPage);
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            int current = position + 1;
            text_currentPage.setText(String.format("%d", current));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public class ImageAdapter extends PagerAdapter {
        Context context;

        public ImageAdapter(Context context) {
            this.context = context;
        }


        // Pager 에서 사용할 View 를 생성하고 등록한다.
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(context);
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isLayoutShow){
                        layout.setVisibility(View.INVISIBLE);
                        isLayoutShow = false;
                    }
                    else{
                        layout.setVisibility(View.VISIBLE);
                        isLayoutShow = true;
                    }
                }
            });

            photoView.setImageBitmap(originImages.get(position));
            container.addView(photoView);

            return photoView;
        }

        @Override
        public int getCount() {
            return originImages.size();
        }


        // instantiateItem 에서 생성한 객체를 사용할지 판단한다.
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private void setStatusBar(Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getColor(R.color.colorBlack));
    }
}
























