package com.example.junmung.StudyHelper.memo;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.junmung.StudyHelper.data.Memo;
import com.example.junmung.StudyHelper.R;
import com.example.junmung.StudyHelper.data.MemoRepository;
import com.example.junmung.StudyHelper.databinding.ActivityMemoOpenBinding;

import java.io.ByteArrayOutputStream;

import io.realm.Realm;

public class MemoOpenActivity extends AppCompatActivity {
    private ActivityMemoOpenBinding binding;
    private Memo memo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_memo_open);
        memo = getData();
        binding();
    }

    private Memo getData(){
        int index = getIntent().getIntExtra("MemoIndex", -1);
        return new MemoRepository(getApplication()).getMemo(index);
    }

    private void binding() {
        binding.setMemo(memo);
        binding.setImageClickListener(imageClickListener);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);    // 커스텀하기위해 필요
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);      // 뒤로가기버튼 생성
        actionBar.setTitle("메모");
    }

    // 이미지뷰 클릭리스너
    private View.OnClickListener imageClickListener = view -> {
        switch (view.getId()) {
            case R.id.image_1:
            case R.id.activity_memo_open_Image_2:
            case R.id.activity_memo_open_Image_3:
            case R.id.activity_memo_open_Image_4:
                Intent intent = new Intent(MemoOpenActivity.this, MemoImagePagerActivity.class);
                intent.putExtra("MemoIndex", memo.get_id());
                intent.putExtra("CurrentPage", (int)view.getTag());
                startActivity(intent);
                break;
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
                Intent intent = new Intent(MemoOpenActivity.this, MemoApplyActivity.class);
                intent.putExtra("isApply", false);
                intent.putExtra("MemoIndex", memo.get_id());
                startActivity(intent);
                finish();
                break;

            case R.id.action_delete:
                remove(memo.get_id());

                Toast.makeText(getApplicationContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void remove(int index) {
        new MemoRepository(getApplication()).delete(index);
    }
}
