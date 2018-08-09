package com.example.junmung.StudyHelper.Memo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.junmung.StudyHelper.DataBase.Calendar.DatabaseHelper;
import com.example.junmung.StudyHelper.DataBase.Memo.Memo;
import com.example.junmung.StudyHelper.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import info.hoang8f.widget.FButton;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class Fragment_Memo extends Fragment{
    private final static int REQUEST_MEMO_ADD = 0x200;

    // 위젯들
    private FloatingActionButton fab_Add, fab_Remove;
    private FloatingActionMenu fab_menu;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private FButton btn_cancel, btn_selectRemove;
    private EditText edit_search;

    public static MemoItemAdapter adapter;

    public static ArrayList<MemoItem> memoItems;
    private ArrayList<MemoItem> tempMemoList;

    private ArrayList<Integer> deletedPosition;

    // EditText 포커싱 잃게 해주는것
    InputMethodManager imm;


    public Fragment_Memo() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        CoordinatorLayout layout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_memo, container, false);

        // 위젯참조, 리스너 세팅
        getID_SetListener(layout);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tempMemoList = new ArrayList<>();
        memoItems = new ArrayList<>();

        // realm 에서 데이터 받아오기
        List<Memo> realmMemoItems = loadItemsFromRealm();
        if(realmMemoItems != null && realmMemoItems.size() > 0)
            setThumbnailAtItems(realmMemoItems);

        // 리사이클러뷰 이 아래 부분을 메모들 다 받아오고 난 뒤에 해야할듯
        adapter = new MemoItemAdapter(memoItems);

        recyclerView.setAdapter(adapter);

        adapter.refresh();
    }

    private void setThumbnailAtItems(List<Memo> realmMemoItems) {
        int size = realmMemoItems.size();

        for(int i = 0; i < size; i++){
            if(realmMemoItems.get(i).getImages().size() > 0){
                byte[] byteImage = realmMemoItems.get(i).getImages().get(0).getImage();
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                bitmap = resizeImage(bitmap, 128, 128, 100);
                memoItems.get(i).setThumbnailImage(bitmap);
            }
        }
    }

    // 취소, 선택삭제 버튼클릭리스너
    private Button.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.fragment_memo_Button_cancel:
                    btn_cancel.setVisibility(View.GONE);
                    btn_selectRemove.setVisibility(View.GONE);
                    adapter.changeListState(MemoItemAdapter.LIST_STATE_NORMAL);

                    // 모든항목 체크해제
                    for(MemoItem item : memoItems)
                        item.setCheck(false);

                    adapter.refresh();

                    break;

                    // 선택삭제 버튼
                case R.id.fragment_memo_Button_selectRemove:
                    btn_cancel.setVisibility(View.GONE);
                    btn_selectRemove.setVisibility(View.GONE);
                    edit_search.setText("");
                    edit_search.clearFocus();
                    adapter.changeListState(MemoItemAdapter.LIST_STATE_NORMAL);
                    imm.hideSoftInputFromWindow(edit_search.getWindowToken(), 0);

                    ArrayList<MemoItem> removedItems = selectRemove();

                    // 삭제하려는 아이템이 있을경우만 삭제
                    if(removedItems.size() != 0) {
                        removeFromRealm(removedItems);
                        updateMemoStateInDB(removedItems);
                    }

                    break;
            }
        }
    };


    // FAButton 클릭 했을시
    private FloatingActionButton.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                    // 플로팅 추가 버튼
                case R.id.fragment_memo_Fab_add:
                    fab_menu.close(true);
                    Intent intent = new Intent(getContext(), MemoApplyActivity.class);
                    intent.putExtra("Purpose", "Add");
                    startActivityForResult(intent, REQUEST_MEMO_ADD);
                    break;

                    // 플로팅 삭제 버튼
                case R.id.fragment_memo_Fab_remove:
                    fab_menu.close(true);
                    // 리스트뷰에 변화를 줘야한다. --> 체크박스가 표시되게
                    btn_cancel.setVisibility(View.VISIBLE);
                    btn_selectRemove.setVisibility(View.VISIBLE);
                    adapter.changeListState(MemoItemAdapter.LIST_STATE_REMOVE);
                    adapter.refresh();


                    break;
            }
        }
    };


    // Intent 결과값 받아오는 콜백메소드
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == getActivity().RESULT_OK){
            if(requestCode == REQUEST_MEMO_ADD)
                adapter.updateList(memoItems);
        }
    }


    // 위젯참조, 리스너 세팅
    private void getID_SetListener(CoordinatorLayout layout){
        // 버튼
        btn_cancel = layout.findViewById(R.id.fragment_memo_Button_cancel);
        btn_cancel.setButtonColor(getResources().getColor(R.color.colorPrimary));
        btn_cancel.setShadowEnabled(true);
        btn_cancel.setTextColor(getResources().getColor(R.color.colorWhite));
        btn_cancel.setTextSize(15);
        btn_cancel.setShadowColor(getResources().getColor(R.color.fbutton_color_asbestos));
        btn_cancel.setShadowHeight(5);
        btn_cancel.setCornerRadius(40);
        btn_cancel.setOnClickListener(btnClickListener);

        btn_selectRemove = layout.findViewById(R.id.fragment_memo_Button_selectRemove);
        btn_selectRemove.setButtonColor(getResources().getColor(R.color.colorPrimary));
        btn_selectRemove.setShadowEnabled(true);
        btn_selectRemove.setTextColor(getResources().getColor(R.color.colorWhite));
        btn_selectRemove.setTextSize(15);
        btn_selectRemove.setShadowColor(getResources().getColor(R.color.fbutton_color_asbestos));
        btn_selectRemove.setShadowHeight(5);
        btn_selectRemove.setCornerRadius(40);
        btn_selectRemove.setOnClickListener(btnClickListener);


        // 플로팅버튼
        fab_Add = layout.findViewById(R.id.fragment_memo_Fab_add);
        fab_Add.setOnClickListener(fabClickListener);
        fab_Remove = layout.findViewById(R.id.fragment_memo_Fab_remove);
        fab_Remove.setOnClickListener(fabClickListener);
        fab_menu = layout.findViewById(R.id.fragment_memo_Fab_menu);
        fab_menu.setClosedOnTouchOutside(true);
        fab_menu.bringToFront();
        fab_menu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if(opened)
                    fab_menu.setBackgroundColor(getResources().getColor(R.color.colorGray));
                else
                    fab_menu.setBackgroundColor(getResources().getColor(R.color.colorInvisible));
            }
        });

        // 리싸이클러뷰
        recyclerView = layout.findViewById(R.id.fragment_memo_RecyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0)
                    fab_menu.hideMenuButton(true);
                else
                    fab_menu.showMenuButton(true);

                super.onScrolled(recyclerView, dx, dy);
            }
        });


        edit_search = layout.findViewById(R.id.fragment_memo_EditText_search);
        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(edit_search.getText().toString());
            }
        });
    }


    // 글자 필터링하기
    private void filter(String text) {
        tempMemoList.clear();
        if(text.length() == 0)
            tempMemoList.addAll(memoItems);
        else{
            for(MemoItem item : memoItems){
                if(item.getTitle().contains(text))
                    tempMemoList.add(item);
            }
        }

        adapter.updateList(tempMemoList);
    }

    // 선택삭제
    private ArrayList<MemoItem> selectRemove(){
        deletedPosition = new ArrayList<>();
        ArrayList<MemoItem> deletedItems = new ArrayList<>();

        int i = 0;
        for(Iterator<MemoItem> iterator = memoItems.iterator(); iterator.hasNext();){
            MemoItem item = iterator.next();

            if(item.isChecked()) {
                // 체크된 item 에서 제목을 저장해두고
                // 목록에서 삭제 한 뒤에 저장한 목록을 반환한다.
                deletedItems.add(item);
                iterator.remove();
                deletedPosition.add(i);
            }
            i++;
        }

        return deletedItems;
    }

    private List<Memo> loadItemsFromRealm(){
        Realm realm = Realm.getDefaultInstance();

        List<Memo> memoList = null;

        RealmResults<Memo> memos = realm.where(Memo.class).findAllSorted("date", Sort.DESCENDING);

        if(!memos.isEmpty()){
            memoList = memos.subList(0, memos.size());
            String title;
            Date date;
            for(int i = 0; i < memoList.size(); i++){
                title = memoList.get(i).getTitle();
                date = memoList.get(i).getDate();
                memoItems.add( new MemoItem(title, date));
            }
        }


        return memoList;
    }

    private void removeFromRealm(ArrayList<MemoItem> deletedItems){
        final String[] items = new String[deletedItems.size()];
        for(int i = 0; i < deletedItems.size(); i++)
            items[i] = deletedItems.get(i).getTitle();

        Realm realm = Realm.getDefaultInstance();


        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Memo> memos = realm.where(Memo.class)
                        .in("title", items)
                        .findAll();

                for(int i = 0; i < memos.size(); i++)
                    memos.get(i).getImages().deleteAllFromRealm();

                memos.deleteAllFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                adapter.updateList(memoItems);
                Toast.makeText(getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        });


        // 다 삭제하고 난 뒤에 리스트 업데이트
        adapter.updateList(memoItems);

        realm.close();
    }


    private void updateMemoStateInDB(ArrayList<MemoItem> removedItems){
        int month, day;

        for(int i = 0; i < removedItems.size(); i++){
            month = removedItems.get(i).getMonth();
            day = removedItems.get(i).getDay();

            if(isLastMemo(month, day)) {
                DatabaseHelper db = DatabaseHelper.getInstance(getContext());
                db.updateMemoState(month, day, 0);
            }
        }

        removedItems.clear();
    }

    private boolean isLastMemo(int month, int day){
        Realm realm = Realm.getDefaultInstance();

        int memoCount = 0;
        RealmResults results = realm.where(Memo.class).findAll();

        for(Object item : results){
            int memoMonth = ((Memo)item).getMonth();
            int memoDay = ((Memo)item).getDay();

            if(memoMonth == month && memoDay == day){
                memoCount++;
                if(memoCount > 1)
                    return false;
            }
        }

        if(memoCount == 1)
            return true;
        else
            return false;
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


}









