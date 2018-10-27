package com.example.junmung.StudyHelper.memo;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.junmung.StudyHelper.data.calendar.DatabaseHelper;
import com.example.junmung.StudyHelper.data.memo.Image;
import com.example.junmung.StudyHelper.data.memo.Memo;
import com.example.junmung.StudyHelper.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class MemoApplyActivity extends AppCompatActivity {
    static final int REQUEST_CAMERA_ACTION = 0x300;
    static final int REQUEST_ALBUM_ACTION = 0x301;
    static final int REQUEST_CROP_FROM_CAMERA = 0x302;
    static final boolean ADD = true;
    static final boolean MODIFY = false;
    static final int IMAGEVIEW_COUNT = 4;

    private ActionBar actionBar;
    private Toolbar toolbar;

    private ArrayList<Bitmap> thumbImages = new ArrayList<>();
    private ImageView[] imageViews = new ImageView[4];
    private boolean[] isInImage = new boolean[4];
    private ArrayList<byte[]> bytes = new ArrayList<>();

    private EditText edit_title, edit_contents;
    private ImageButton btn_Camera, btn_Album;
    private Uri photoUri;


    private boolean isAddActivity;
    private String intentedMemoTitle;
    private Date intentedMemoDate;


    private int defaultImage = android.R.drawable.ic_menu_gallery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_apply);

        Intent intent = getIntent();
        String purpose = intent.getStringExtra("Purpose");
        if(purpose.equals("Add"))
            isAddActivity = ADD;
        else{
            isAddActivity = MODIFY;
            intentedMemoTitle = intent.getStringExtra("MemoTitle");

        }

        getID_SetListener();

        if(isAddActivity == MODIFY)
            setOriginData(intentedMemoTitle);

    }


    private void getID_SetListener() {
        toolbar = findViewById(R.id.memoActivity_Toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);    // 커스텀하기위해 필요
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);      // 뒤로가기버튼 생성

        if (isAddActivity)
            actionBar.setTitle("메모등록");
        else
            actionBar.setTitle("메모수정");


        edit_title = findViewById(R.id.activity_memo_apply_EditText_Title);
        edit_contents = findViewById(R.id.activity_memo_apply_EditText_Contents);


        for (int i = 0; i < 4; i++) {
            imageViews[i] = findViewById(R.id.activity_memo_apply_ImageView_1 + i);
            imageViews[i].setOnClickListener(imageClickListener);

        }

        btn_Camera = findViewById(R.id.activity_memo_apply_Button_Camera);
        btn_Camera.setOnClickListener(btnClickListener);
        btn_Album = findViewById(R.id.activity_memo_apply_Button_Album);
        btn_Album.setOnClickListener(btnClickListener);
    }


    // 이미지뷰 클릭리스너
    private View.OnClickListener imageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.activity_memo_apply_ImageView_1:
                    showDeleteWarning(0);
                    break;

                case R.id.activity_memo_apply_ImageView_2:
                    showDeleteWarning(1);
                    break;

                case R.id.activity_memo_apply_ImageView_3:
                    showDeleteWarning(2);
                    break;

                case R.id.activity_memo_apply_ImageView_4:
                    showDeleteWarning(3);
                    break;
            }
        }
    };


    // 카메라, 앨범버튼 클릭 리스너
    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.activity_memo_apply_Button_Camera:
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    File photoFile = null;


                    // 이미지 저장할 빈 파일 만들어놓기
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(),
                                "오류!", Toast.LENGTH_SHORT).show(); }

                    if (photoFile != null) {
                        photoUri = FileProvider.getUriForFile(getApplicationContext(),
                                "com.example.junmung.StudyHelper.provider", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                        startActivityForResult(intent, REQUEST_CAMERA_ACTION);
                    }

                    break;

                case R.id.activity_memo_apply_Button_Album:
                    pickAlbum();
                    break;
            }
        }
    };


    // 이미지파일 만들기
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("MM-dd").format(new Date());
        String imageFileName = "IP" + timeStamp + "_";

        File storageDir = new File(Environment.getExternalStorageDirectory() + "/photo/");
        if (!storageDir.exists())
            storageDir.mkdirs();

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_CAMERA_ACTION) {
            cropImage();

            // 앨범에 사진을 보여주기 위해 scan을 한다.
            MediaScannerConnection.scanFile(MemoApplyActivity.this,
                    new String[]{photoUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {

                        }
                    });


        } else if (requestCode == REQUEST_CROP_FROM_CAMERA) {
            try { // bitmap 형태의 이미지로 가져오기 위해 아래와 같이 작업하였으며 Thumbnail 을 추출
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(bitmap, 128, 128);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, bs); //이미지가 클 경우 OutOfMemoryException 발생이 예상되어 압축

                //여기서는 ImageView 에 setImageBitmap 을  활용하여 해당 이미지에 그림을 띄운다
                int index = IMAGEVIEW_COUNT - getEmptyImageCount();
                saveOriginImageToByteArray(index, bitmap);
                imageViews[index].setImageBitmap(thumbImage);
                thumbImages.add(thumbImage);

            } catch (Exception e) { Log.e("ERROR", e.getMessage().toString()); }

        } else if (requestCode == REQUEST_ALBUM_ACTION) {
            if (data == null)
                return;

            Uri uri = data.getData();
            ClipData clipData = data.getClipData();

            // 다중선택했을시
            if (clipData != null) {
                int clipItemCount = clipData.getItemCount();
                int emptyImageCount = getEmptyImageCount();
                int suitableCount = emptyImageCount;

                // 남은 공간보다 선택한 사진 수가 더 많다면 메시지출력하고 리턴
                if(clipItemCount > emptyImageCount){
                    Toast.makeText(this, "선택한 사진의 갯수가 너무 많습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }else if(emptyImageCount > clipItemCount)
                    suitableCount = clipItemCount;


                for (int i = 0; i < suitableCount; i++) {
                    Uri clipUri = clipData.getItemAt(i).getUri();
                    int currentIndex = bytes.size();

                    // 비트맵가져오기
//                    String imagePath = clipUri.getPath();
//                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

//                    bitmap = getCorrectImage(bitmap, imagePath);



                    Bitmap bitmap = getBitmapFromUri(clipUri);

                    saveOriginImageToByteArray(currentIndex, bitmap);

                    bitmap = resizeImage(bitmap, 128, 128, 100);

                    thumbImages.add(bitmap);

                    switch (currentIndex) {
                        case 0: imageViews[currentIndex].setImageBitmap(bitmap);    break;
                        case 1: imageViews[currentIndex].setImageBitmap(bitmap);    break;
                        case 2: imageViews[currentIndex].setImageBitmap(bitmap);    break;
                        case 3: imageViews[currentIndex].setImageBitmap(bitmap);    break;
                    }
                }
            }
            else if (uri != null) {
//                String imagePath = uri.getPath();
//                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
//                bitmap = getCorrectImage(bitmap, imagePath);

                Bitmap bitmap = getBitmapFromUri(uri);
                int index = IMAGEVIEW_COUNT - getEmptyImageCount();
                saveOriginImageToByteArray(index, bitmap);

                bitmap = resizeImage(bitmap, 128, 128, 100);

                imageViews[index].setImageBitmap(bitmap);
                thumbImages.add(bitmap);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap getCorrectImage(Bitmap bitmap, String imagePath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);
        bitmap = rotate(bitmap, exifDegree);

        return bitmap;
    }

    // 회전각도를 리턴한다.
    private int exifOrientationToDegrees(int exifOrientation) {
        switch(exifOrientation){
            case ExifInterface.ORIENTATION_ROTATE_90:   return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:   return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:   return 270;
            default:return 0;
        }
    }


    // 이미지를 회전시킨다.
    private Bitmap rotate(Bitmap bitmap, int degrees){
        if(degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            }
            catch(OutOfMemoryError ex) {// 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }

    private Bitmap getBitmapFromUri(Uri uri){
        InputStream is = null;
        try{
            is = getContentResolver().openInputStream(uri);
        } catch (Exception e) { Log.e("ERROR", e.getMessage().toString()); }

        return BitmapFactory.decodeStream(is);
    }

//    private Bitmap getBitmapFromUri(Uri uri){
//        InputStream is = null;
//        try{
//            is = getContentResolver().openInputStream(uri);
//        } catch (Exception e) { Log.e("ERROR", e.getMessage().toString()); }
//
//        Bitmap bitmap = BitmapFactory.decodeStream(is);
//
//        ExifInterface exif = null;
//        try {
//            exif = new ExifInterface(is);
//        } catch (IOException e) { e.printStackTrace();}
//
//
//        int exifOrientation = exif.getAttributeInt(
//                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//        int exifDegree = exifOrientationToDegrees(exifOrientation);
//        bitmap = rotate(bitmap, exifDegree);
//
//        return bitmap;
//
////        return BitmapFactory.decodeStream(is);
//    }

    private int getEmptyImageCount(){
        int count = 0;

        for(int i = 0; i < IMAGEVIEW_COUNT; i++){
            if(!isInImage[i])
                count++;
        }

        return count;
    }

    private Bitmap getThumbnailImageIfExist() {
        if (bytes.size() == 0)
            return null;
        else {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViews[0].getDrawable();
            return bitmapDrawable.getBitmap();
        }
    }

    // 바이트형태로 원본이미지 저장
    private void saveOriginImageToByteArray(int index, Bitmap origin){
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        origin.compress(Bitmap.CompressFormat.JPEG, 100, bs);

        bytes.add(bs.toByteArray());
        isInImage[index] = true;
    }

    // 이미지 자르기 메소드
    private void cropImage() {

        // 안드로이드 카메라 패키지에서 플래그로 권한요청보내기
        this.grantUriPermission("com.android.camera", photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (list.size() == 0) {
            Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);

            File croppedFileName = null;
            try {
                croppedFileName = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + "/photo/");
            File tempFile = new File(folder.toString(), croppedFileName.getName());

            photoUri = FileProvider.getUriForFile(MemoApplyActivity.this,
                    "com.example.junmung.StudyHelper.provider", tempFile);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //Bitmap 형태로 받기 위해 해당 작업 진행

            Intent cropIntent = new Intent(intent);
            ResolveInfo res = list.get(0);
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            grantUriPermission(res.activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            cropIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(cropIntent, REQUEST_CROP_FROM_CAMERA);

        }
    }

    private void pickAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(Intent.createChooser(intent, "다중 선택은 '포토'를 선택하세요"), REQUEST_ALBUM_ACTION);
    }

    private Bitmap resizeImage(Bitmap srcBitmap, int resizedWidth, int resizedHeight, int quality) {
        // 원본 이미지의 정보
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();


        float ratioX = resizedWidth / (float)srcWidth;
        float ratioY = resizedHeight / (float)srcHeight;

        int dstWidth = Math.round(srcWidth * ratioX);
        int dstHeight = Math.round(srcHeight * ratioY);

        // 파일 리사이즈
        Bitmap output = Bitmap.createScaledBitmap(srcBitmap, dstWidth, dstHeight, false);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        output.compress(Bitmap.CompressFormat.JPEG, quality, bs);

        return output;
    }


    // 삭제하겠냐는 다이얼로그 띄우기
    private void showDeleteWarning(final int position) {
        new AlertDialog.Builder(MemoApplyActivity.this)
                .setMessage("삭제하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bytes.remove(position);

                        int i, j;
                        for(i = 0; i < bytes.size(); i++)
                            isInImage[i] = true;
                        for(j = i; j < IMAGEVIEW_COUNT; j++)
                            isInImage[j] = false;

                        thumbImages.remove(position);


                        for(j = 0; j < thumbImages.size(); j++)
                            imageViews[j].setImageBitmap(thumbImages.get(j));
                        imageViews[j].setImageResource(defaultImage);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    @Override   // 등록버튼 만들기
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_action, menu);
        return true;
    }

    @Override   // 뒤로가기, 등록버튼 기능
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;


            case R.id.action_complete:
                String title = edit_title.getText().toString();
                String contents = edit_contents.getText().toString();

                Bitmap thumbnail = getThumbnailImageIfExist();
                if(thumbnail == null)
                    thumbnail = BitmapFactory.decodeResource(getResources(), defaultImage);

                if(isAddActivity){
                    Date date = new Date();

                    MemoItem memoItem = new MemoItem(title, date, thumbnail);
                    Fragment_Memo.memoItems.add(0, memoItem);

                    // 디비에 저장
                    saveDataInRealm(title, contents, date);
                    changeMemoStateInDB(date);
                    Toast.makeText(this, "메모등록완료", Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    finish();
                }
                else{
                    Date date = intentedMemoDate;

                    // 어댑터 아이템 수정
                    for(MemoItem memoItem :Fragment_Memo.memoItems){
                        if(memoItem.getTitle().equals(intentedMemoTitle)){
                            memoItem.setTitle(title);
                            memoItem.setDate(date);
                            memoItem.setThumbnailImage(thumbnail);
                        }
                    }

                    // Realm 수정
                    updateRealm(intentedMemoTitle, title, contents, bytes);

                    Fragment_Memo.adapter.refresh();
                    Toast.makeText(this, "메모수정완료", Toast.LENGTH_SHORT).show();

                    finish();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateRealm(String originTitle, String modifyTitle, String contents, ArrayList<byte[]> bytes) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Memo memo = realm.where(Memo.class).equalTo("title", originTitle).findFirst();
        memo.setTitle(modifyTitle);
        memo.setContents(contents);


        if(memo.getImages().size() > 0)
            memo.getImages().deleteAllFromRealm();

        RealmList<Image> images = new RealmList<>();
        for(int i = 0; i < bytes.size(); i++)
            images.add(realm.copyToRealm(new Image(modifyTitle, i, bytes.get(i))));

        memo.setImages(images);

        realm.commitTransaction();
        realm.close();
    }

    private void setOriginData(String title){
        Realm realm = Realm.getDefaultInstance();

        Memo memo = realm.where(Memo.class).equalTo("title", title).findFirst();
        String contents = memo.getContents();
        intentedMemoDate = memo.getDate();

        int imageCount = memo.getImages().size();

        if(imageCount > 0){
            for(int i = 0; i < imageCount; i++){
                byte[] imageByte = memo.getImages().get(i).getImage();
                bytes.add(imageByte);

                Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                bitmap = resizeImage(bitmap, 128, 128, 100);
                thumbImages.add(bitmap);
            }
        }


        edit_title.setText(title);
        edit_contents.setText(contents);
        for(int i = 0; i < thumbImages.size(); i++)
            imageViews[i].setImageBitmap(thumbImages.get(i));

    }


    // Realm 에 데이터를 저장한다.
    private void saveDataInRealm(String title, String contents, Date date){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Memo memo = new Memo(title, contents, date);

        RealmList<Image> images = new RealmList<>();
        for(int i = 0; i < bytes.size(); i++)
            images.add(new Image(title, i, bytes.get(i)));

        memo.setImages(images);

        realm.insert(memo);


        realm.commitTransaction();
        realm.close();
    }

    // 해당하는 날짜의 메모 유무를 바꾼다.
    private void changeMemoStateInDB(Date date){
        String monthStr = new SimpleDateFormat("M").format(date);
        String dayStr = new SimpleDateFormat("d").format(date);
        int month = Integer.parseInt(monthStr);
        int day = Integer.parseInt(dayStr);

        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
        db.updateMemoState(month, day, 1);
    }



}
