package com.example.junmung.StudyHelper.memo;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junmung.StudyHelper.data.Memo;
import com.example.junmung.StudyHelper.data.calendar.DatabaseHelper;
import com.example.junmung.StudyHelper.R;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class MemoOpenActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private Toolbar toolbar;
    private TextView text_Title, text_Contents, text_Date;
    private ImageView[] imageViews = new ImageView[4];


    private ArrayList<Bitmap> thumbImages = new ArrayList<>();

    private String memoTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_open);

        Intent intent = getIntent();
        memoTitle = intent.getStringExtra("MemoTitle");

        getID_SetListener();

        setOriginData(memoTitle);
    }

    private void setOriginData(String memoTitle) {
        Realm realm = Realm.getDefaultInstance();

        com.example.junmung.StudyHelper.data.memo.Memo memo = realm.where(com.example.junmung.StudyHelper.data.memo.Memo.class).equalTo("title", memoTitle).findFirst();
        String contents = memo.getContents();
        Date date_ = memo.getDate();

        int imageCount = memo.getImages().size();

        if (imageCount > 0) {
            for (int i = 0; i < imageCount; i++) {
                byte[] imageByte = memo.getImages().get(i).getImage();

                Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                bitmap = resizeImage(bitmap, 128, 128, 100);
                thumbImages.add(bitmap);
            }
        }

        text_Title.setText(memoTitle);
        text_Contents.setText(contents);
        String date = new SimpleDateFormat("M월 dd일   a h:mm").format(date_);
        text_Date.setText(date);

        for (int i = 0; i < thumbImages.size(); i++)
            imageViews[i].setImageBitmap(thumbImages.get(i));
    }

    private void getID_SetListener() {
        toolbar = findViewById(R.id.activity_memo_open_Toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);    // 커스텀하기위해 필요
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);      // 뒤로가기버튼 생성
        actionBar.setTitle("메모");

        text_Title = findViewById(R.id.activity_memo_open_TextView_title);
        text_Contents = findViewById(R.id.activity_memo_open_TextView_contents);
        text_Date = findViewById(R.id.activity_memo_open_TextView_date);


        for (int i = 0; i < 4; i++) {
            imageViews[i] = findViewById(R.id.activity_memo_open_Image_1 + i);
            imageViews[i].setOnClickListener(imageClickListener);
            imageViews[i].setTag(i);
        }
    }

    // 이미지뷰 클릭리스너
    private View.OnClickListener imageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.activity_memo_open_Image_1:
                case R.id.activity_memo_open_Image_2:
                case R.id.activity_memo_open_Image_3:
                case R.id.activity_memo_open_Image_4:
                    Intent intent = new Intent(MemoOpenActivity.this, MemoImagePagerActivity.class);
                    intent.putExtra("MemoTitle", memoTitle);
                    intent.putExtra("CurrentPage", (int)v.getTag());
                    startActivity(intent);
                    break;
            }

        }
    };

    private Bitmap resizeImage(Bitmap srcBitmap, int resizedWidth, int resizedHeight, int quality) {
        // 원본 이미지의 정보
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();


        float ratioX = resizedWidth / (float) srcWidth;
        float ratioY = resizedHeight / (float) srcHeight;

        int dstWidth = Math.round(srcWidth * ratioX);
        int dstHeight = Math.round(srcHeight * ratioY);

        // 파일 리사이즈
        Bitmap output = Bitmap.createScaledBitmap(srcBitmap, dstWidth, dstHeight, false);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        output.compress(Bitmap.CompressFormat.JPEG, quality, bs);

        return output;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.memo_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_modify:
                Intent intent = new Intent(this, MemoApplyActivity.class);
                intent.putExtra("Purpose", "Modify");
                intent.putExtra("MemoTitle", memoTitle);
                startActivity(intent);
                finish();
                break;

            case R.id.action_delete:
                Memo memo = getMemoItemFromRealm(memoTitle);

                updateMemoStateInDB(memo);

                removeFromRealm(memoTitle);


//                for(int i = 0; i < Fragment_Memo.memos.size(); i++){
//                    if(Fragment_Memo.memos.get(i).getTitle().equals(memoTitle))
//                        Fragment_Memo.memos.remove(i);
//                }

//                Fragment_Memo.adapter.refresh();
                Toast.makeText(this, "삭제되었습니다", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private Memo getMemoItemFromRealm(String memoTitle){
        Realm realm = Realm.getDefaultInstance();

        com.example.junmung.StudyHelper.data.memo.Memo memo = realm.where(com.example.junmung.StudyHelper.data.memo.Memo.class)
                .equalTo("title", memoTitle)
                .findFirst();

//        return new Memo(memoTitle, memo.getDate());
        byte[] a = null;
        return new Memo(1,"d", "d", new Date(), a);
    }

    private void removeFromRealm(String memoTitle) {
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        realm.where(com.example.junmung.StudyHelper.data.memo.Memo.class)
                .equalTo("title", memoTitle)
                .findFirst()
                .deleteFromRealm();

        realm.commitTransaction();
        realm.close();
    }

    private void updateMemoStateInDB(Memo memo) {
//        int month = memo.getMonth();
//        int day = memo.getDay();

//        if(isLastMemo(month, day)) {
//            DatabaseHelper db = DatabaseHelper.getInstance(this);
//            db.updateMemoState(month, day, 0);
//        }
    }

    private boolean isLastMemo(int month, int day){
        Realm realm = Realm.getDefaultInstance();

        int memoCount = 0;
        RealmResults results = realm.where(com.example.junmung.StudyHelper.data.memo.Memo.class).findAll();

        for(Object item : results){
            int memoMonth = ((com.example.junmung.StudyHelper.data.memo.Memo)item).getMonth();
            int memoDay = ((com.example.junmung.StudyHelper.data.memo.Memo)item).getDay();

            if(memoMonth == month && memoDay == day){
                memoCount++;
                if(memoCount >= 2)
                    return false;
            }
        }

        if(memoCount == 1)
            return true;
        else
            return false;
    }

}
