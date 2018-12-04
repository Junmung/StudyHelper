package com.example.junmung.studyhelper.memo;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.junmung.studyhelper.MainActivity;
import com.example.junmung.studyhelper.data.Memo;
import com.example.junmung.studyhelper.R;
import com.example.junmung.studyhelper.databinding.FragmentMemoBinding;
import com.github.clans.fab.FloatingActionButton;

public class Fragment_Memo extends Fragment{
    private final int layoutId = R.layout.item_memo;
    private final static int REQUEST_MEMO_ADD = 0x200;
    private final static int REQUEST_MEMO_DELETE = 0x201;

    private MemoAdapter adapter;

    private MemoViewModel vm;

    private FragmentMemoBinding binding;

    /**
     *  removemode 처리하기
     */

    public Fragment_Memo() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModelInit();
    }

    private void viewModelInit(){
        vm = ViewModelProviders.of(this).get(MemoViewModel.class);
        vm.memos.observe(this, memos -> adapter.setMemoList(memos));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_memo, container, false);
        bind();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        return binding.getRoot();
    }

    private void bind(){
        binding.setLifecycleOwner(this);
        binding.setViewModel(vm);

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
            // selector 처리하기
            if(opened)
                binding.fabMenu.setBackgroundColor(getResources().getColor(R.color.colorGray));
            else
                binding.fabMenu.setBackgroundColor(getResources().getColor(R.color.colorInvisible));
        });

        // RecyclerView
        binding.memoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0)
                    binding.fabMenu.hideMenuButton(true);
                else
                    binding.fabMenu.showMenuButton(true);
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        binding.memoList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.memoList.setHasFixedSize(true);

        MemoClickListener memoClickListener = new MemoClickListener() {
            @Override
            public void onClick(Memo memo) {
                Intent intent = new Intent(getContext(), MemoOpenActivity.class);
                intent.putExtra("MemoIndex", memo.get_id());
                startActivityForResult(intent, REQUEST_MEMO_DELETE);
            }

            @Override
            public boolean onLongClick(Memo memo) {
                new AlertDialog.Builder(getContext())
                        .setMessage("수정하시겠습니까?")
                        .setPositiveButton("확인", (dialog, which) -> {
                            Intent intent = new Intent(getContext(), MemoApplyActivity.class);
                            intent.putExtra("Purpose", "Modify");
                            intent.putExtra("MemoIndex", memo.get_id());
                            startActivity(intent);
                        })
                        .setNegativeButton("취소", (dialog, which) -> {
                        })
                        .show();
                return true;
            }
        };
        adapter = new MemoAdapter(vm.removeMode, layoutId, memoClickListener);

        binding.memoList.setAdapter(adapter);

    }


    // 취소, 선택삭제 버튼클릭리스너 -> 메뉴 버튼으로 바꿔야함
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
                    vm.setRemoveMode(false);
                    binding.editSearch.setText("");
                    binding.editSearch.clearFocus();
                    hideKeyboard(binding.editSearch.getWindowToken());
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
                    Intent intent = new Intent(getActivity(), MemoApplyActivity.class);
                    intent.putExtra("isApply", true);
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

        if(resultCode == MainActivity.RESULT_OK){
            switch (requestCode) {
                case REQUEST_MEMO_ADD:
                    break;
                case REQUEST_MEMO_DELETE:
                    vm.delete(data.getIntExtra("MemoIndex", -1));
                    Toast.makeText(getContext().getApplicationContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}









