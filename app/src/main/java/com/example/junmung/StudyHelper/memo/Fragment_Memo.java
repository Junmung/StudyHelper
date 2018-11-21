package com.example.junmung.StudyHelper.memo;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.IBinder;
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

import com.example.junmung.StudyHelper.data.Memo;
import com.example.junmung.StudyHelper.R;
import com.example.junmung.StudyHelper.databinding.FragmentMemoBinding;
import com.github.clans.fab.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Fragment_Memo extends Fragment{
    private final static int REQUEST_MEMO_ADD = 0x200;

    private MemoAdapter adapter;

    private MemoViewModel vm;

    private FragmentMemoBinding binding;


    public Fragment_Memo() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModelInit();
    }

    private void viewModelInit(){
        vm = ViewModelProviders.of(this).get(MemoViewModel.class);

        vm.getMemos().observe(this, memos -> {
            adapter.setMemoList(memos);
            adapter.refresh();
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_memo, container, false);
        binding();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        return binding.getRoot();
    }

    private void binding(){
        // 버튼
        binding.setBtnClickListener(btnClickListener);
        binding.btnRemove.setButtonColor(getResources().getColor(R.color.colorPrimary));
        binding.btnRemove.setShadowEnabled(true);
        binding.btnRemove.setTextColor(getResources().getColor(R.color.colorWhite));
        binding.btnRemove.setTextSize(15);
        binding.btnRemove.setShadowColor(getResources().getColor(R.color.fbutton_color_asbestos));
        binding.btnRemove.setShadowHeight(5);
        binding.btnRemove.setCornerRadius(40);
        binding.btnCancel.setButtonColor(getResources().getColor(R.color.colorPrimary));
        binding.btnCancel.setShadowEnabled(true);
        binding.btnCancel.setTextColor(getResources().getColor(R.color.colorWhite));
        binding.btnCancel.setTextSize(15);
        binding.btnCancel.setShadowColor(getResources().getColor(R.color.fbutton_color_asbestos));
        binding.btnCancel.setShadowHeight(5);
        binding.btnCancel.setCornerRadius(40);

        // 플로팅버튼
        binding.setFabClickListener(fabClickListener);
        binding.fabMenu.setClosedOnTouchOutside(true);
        binding.fabMenu.bringToFront();
        binding.fabMenu.setOnMenuToggleListener(opened -> {
            if(opened)
                binding.fabMenu.setBackgroundColor(getResources().getColor(R.color.colorGray));
            else
                binding.fabMenu.setBackgroundColor(getResources().getColor(R.color.colorInvisible));
        });

        // 검색창
        binding.textSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(binding.textSearch.getText().toString());
            }
        });

        // RecyclerView
        binding.memoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0)
                    binding.fabMenu.hideMenuButton(true);
                else
                    binding.fabMenu.showMenuButton(true);
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        binding.memoList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.memoList.setHasFixedSize(true);

        adapter = new MemoAdapter(vm.getMemos().getValue(), vm.getRemoveMode());
        binding.memoList.setAdapter(adapter);

    }


    // 취소, 선택삭제 버튼클릭리스너
    private Button.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_cancel:
                    vm.setRemoveMode(false);
                    adapter.resetCheckedList();
                    break;

                    // 선택삭제 버튼
                case R.id.btn_remove:
                    if (vm.getMemos().getValue().size() == 0)
                        return;
                    vm.setRemoveMode(false);
                    binding.textSearch.setText("");
                    binding.textSearch.clearFocus();
                    hideKeyboard(binding.textSearch.getWindowToken());
                    vm.selectRemove(adapter.getCheckedMemoIndexes());

                    break;
            }
        }
    };

    // FAButton 클릭 했을시
    private FloatingActionButton.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.fab_add:
                    binding.fabMenu.close(true);
                    Intent intent = new Intent(getActivity().getApplicationContext(), MemoApplyActivity.class);
                    intent.putExtra("Purpose", "Add");
                    startActivityForResult(intent, REQUEST_MEMO_ADD);
                    break;

                case R.id.fab_remove:
                    binding.fabMenu.close(true);
                    vm.setRemoveMode(true);
                    break;
            }
        }
    };

    private void hideKeyboard(IBinder token){
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(token, 0);
    }


    // Intent 결과값 받아오는 콜백메소드
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == getActivity().RESULT_OK){
            if(requestCode == REQUEST_MEMO_ADD){

            }
//                adapter.updateList(memos);
        }
    }

    // 글자 필터링하기
    private void filter(String searchWord) {
        vm.clearSearchMemos();

        if(searchWord.length() == 0)
            adapter.setMemoList(vm.getMemos().getValue());
        else{
            for(Memo item : vm.getMemos().getValue()){
                if(item.getTitle().contains(searchWord))
                    vm.addSearchMemo(item);
            }
        }

        adapter.setMemoList(vm.getSearchMemos().getValue());
    }


    private void updateMemoStateInDB(ArrayList<Memo> removedItems){
        int month, day;

        for(int i = 0; i < removedItems.size(); i++){
//            month = removedItems.get(i).getMonth();
//            day = removedItems.get(i).getDay();

//            if(isLastMemo(month, day)) {
//                DatabaseHelper db = DatabaseHelper.getInstance(getContext());
//                db.updateMemoState(month, day, 0);
//            }
        }

        removedItems.clear();
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









