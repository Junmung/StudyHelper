package com.example.junmung.studyhelper.record;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junmung.studyhelper.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Fragment_Record extends Fragment {
    private final static int REQUEST_ADD_RECORD = 0x100;
    private final static int STATE_PLAYER_PAUSE = 2;
    private final static int STATE_PLAYER_START = 3;
    private final static int STATE_PLAYER_PLAY = 4;

    private final static int RECORD_PAUSE = 0x10;
    private final static int RECORD_STOP = 0x11;



    private ImageButton btn_record, btn_recordStop, btn_pause;
    private RecordTimeThread timeThread;
    private TextView textView_recordTime;
    private int recordingTime;

    private ArrayList<RecordItem> recordItems;
    private RecordItemAdapter recordAdapter;
    private ListView listView;


    // 미디어 플레이어 부분
    private MediaPlayer mediaPlayer;
    private int playerState;
    private ImageButton dialog_btn_play = null;
    private ImageButton dialog_btn_pause = null;


    private String recordFileName;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Gson gson;
    Type myDataType = new TypeToken<ArrayList<RecordItem>>(){}.getType();



    public Fragment_Record() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_record, container, false);
        getID_SetListener(layout);

        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        preferences = getContext().getSharedPreferences("RecordFile", Context.MODE_PRIVATE);
        editor = preferences.edit();

        boolean isFirstRun = preferences.getBoolean("isFirstRun", true);
        if(isFirstRun){
            editor.putBoolean("isFirstRun", false);
            editor.putString("Record_Json", "");
            editor.putBoolean("isStopRecording", false);
            editor.commit();
        }

        String json = preferences.getString("Record_Json","");

        gson = new Gson();
        recordItems = gson.fromJson(json, myDataType);
        if(recordItems == null)
            // 데이터가 아예 없을 경우
            recordItems = new ArrayList<>();

        recordAdapter = new RecordItemAdapter(recordItems);
        listView.setAdapter(recordAdapter);
    }

    private void getID_SetListener(ConstraintLayout layout){
        btn_record = layout.findViewById(R.id.fragment_record_Button_record);
        btn_record.setOnClickListener(btnClickListener);
        btn_pause= layout.findViewById(R.id.fragment_record_Button_pause);
        btn_pause.setOnClickListener(btnClickListener);
        btn_recordStop = layout.findViewById(R.id.fragment_record_Button_recordStop);
        btn_recordStop.setOnClickListener(btnClickListener);

        textView_recordTime = layout.findViewById(R.id.fragment_record_TextView_time);

        listView = layout.findViewById(R.id.fragment_record_ListView_files);
        listView.setOnItemClickListener(itemClickListener);
        listView.setOnItemLongClickListener(itemLongClickListener);
    }


    // 리스트뷰 아이템 오래 클릭했을 때 처리하는 기능
    private ListView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

            // 롱클릭 했을 때 해당되는 아이템의 정보 가져오기
            final int posi = position;
            RecordItem recordItem = (RecordItem)recordAdapter.getItem(position);
            final String originFileName = recordItem.getTitle();
            final String filePath = recordItem.getFilePath();


            // 수정 + 삭제 다이얼로그
            new AlertDialog.Builder(getContext())
                    .setMessage("수행할 작업을 선택하세요.")
                    .setNegativeButton("수정", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            View modifyDialog = getLayoutInflater().inflate(R.layout.dialog_record_filename, null);
                            final EditText userInputFileName = modifyDialog.findViewById(R.id.dialog_record_FileName_EditText);
                            userInputFileName.setText(originFileName);

                            builder.setView(modifyDialog)
                                    .setMessage("수정할 제목을 입력하세요.")
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String fileName = userInputFileName.getText().toString();
                                            modifyFileName(filePath, fileName);

                                            recordAdapter.modify(posi, fileName);
                                            recordAdapter.refresh();
                                            saveCurrentData();

                                            Toast.makeText(getContext(), "수정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();
                        }
                    })
                    .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            File removeFile = new File(filePath);

                            if(removeFile.delete())
                                Toast.makeText(getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getContext(), "삭제실패", Toast.LENGTH_SHORT).show();

                            recordAdapter.removeItem(posi);
                            recordAdapter.refresh();
                            saveCurrentData();

                        }
                    })
                    .setNeutralButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();

            return true;
        }
    };


    // 녹음파일 리스트 아이템 클릭 리스너
    private ListView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(final AdapterView<?> parent, View view, int position, long id) {

            // 리스트아이템에서 아이템값 얻어오기
            final RecordItem dialog_item = (RecordItem) recordAdapter.getItem(position);
            String title = dialog_item.getTitle();
            String date = dialog_item.getSavedDate();


            // 커스텀 다이얼로그 만들기
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View dialog = getLayoutInflater().inflate(R.layout.dialog_record_player, null);
            builder.setView(dialog)
                    .setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                    });

            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            mediaPlayer = new MediaPlayer();
            playerState = STATE_PLAYER_START;


            final TextView dialog_title = dialog.findViewById(R.id.dialog_record_player_TextView_Title);
            final TextView dialog_date = dialog.findViewById(R.id.dialog_record_player_TextView_Date);
            final TextView dialog_maxTime = dialog.findViewById(R.id.dialog_record_player_TextView_Max);
            final TextView dialog_minTime = dialog.findViewById(R.id.dialog_record_player_TextView_Min);
            dialog_title.setText(title);
            dialog_date.setText(date);

            // 파일의 재생시간 구하기
            String fileDuration = dialog_item.getFileDuration(dialog_item.getFilePath());
            String duration = RecordItemAdapter.timeToPlayTime(Integer.parseInt(fileDuration));

            dialog_maxTime.setText(duration);
            dialog_minTime.setText("00:00");


            final Handler mHandler = new Handler();

            // dialog 안에 있는 SeekBar 기능 구현
            SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(final SeekBar seekBar, int progress, final boolean fromUser) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mediaPlayer != null && fromUser) {
                                String changedStr = RecordItemAdapter.timeToPlayTime(mediaPlayer.getCurrentPosition());
                                dialog_minTime.setText(changedStr);
                            }
                            mHandler.post(this);
                        }
                    });
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.pause();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    mediaPlayer.start();
                }
            };

            final SeekBar seekBar = dialog.findViewById(R.id.dialog_record_player_SeekBar);
            seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
            seekBar.setMax(Integer.parseInt(fileDuration));


            // dialog 안에 있는 재생버튼 클릭 리스너
            ImageButton.OnClickListener dialogImageClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        // 재생버튼
                        case R.id.dialog_record_player_Button_Play:
                            if (playerState == STATE_PLAYER_START) {
                                try {
                                    mediaPlayer.setDataSource(dialog_item.getFilePath());
                                    mediaPlayer.prepare();
                                    playerState = STATE_PLAYER_PLAY;
                                } catch (IOException e) {
                                }
                            }
                            mediaPlayer.start();

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mediaPlayer != null) {
                                        String changedStr = RecordItemAdapter.timeToPlayTime(mediaPlayer.getCurrentPosition());
                                        dialog_minTime.setText(changedStr);
                                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                    }
                                    mHandler.postDelayed(this, 100);
                                }
                            });


                            dialog_btn_play.setVisibility(View.GONE);
                            dialog_btn_pause.setVisibility(View.VISIBLE);
                            break;


                        // 일시정지 버튼
                        case R.id.dialog_record_player_Button_Pause:

                            mediaPlayer.pause();
                            dialog_btn_play.setVisibility(View.VISIBLE);
                            dialog_btn_pause.setVisibility(View.GONE);

                            break;
                    }
                }
            };


            // 다이얼로그에서 위젯값 받아오기
            dialog_btn_play = dialog.findViewById(R.id.dialog_record_player_Button_Play);
            dialog_btn_play.setOnClickListener(dialogImageClickListener);
            dialog_btn_play.setVisibility(View.VISIBLE);
            dialog_btn_pause = dialog.findViewById(R.id.dialog_record_player_Button_Pause);
            dialog_btn_pause.setOnClickListener(dialogImageClickListener);
            dialog_btn_pause.setVisibility(View.GONE);


            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    };


    // 녹음버튼, 중지버튼 클릭리스너

    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){

                // 녹음시작버튼
                case R.id.fragment_record_Button_record:
                    showRecordStartDialog();

                    break;

                case R.id.fragment_record_Button_recordStop:
                    // 녹음 서비스 중지
                    Intent recordStopIntent = new Intent(getActivity(), RecordService.class);
                    getActivity().stopService(recordStopIntent);


                    // 쓰레드 중지
                    timeThread.stopThread(RECORD_STOP);

                    new LoadingTask().execute();
                    break;


                case R.id.fragment_record_Button_pause:

                    timeThread.stopThread(RECORD_PAUSE);

                    break;
            }
        }
    };

    private class RecordTimeThread extends Thread {
        RecordHandler mHandler;
        boolean Run;

        public RecordTimeThread(RecordHandler handler){
            mHandler = handler;
            Run = true;
            recordingTime = 0;
        }

        @Override
        public void run() {
            super.run();
            while(Run){
                Message msg = mHandler.obtainMessage();
                msg.arg1 = recordingTime;
                mHandler.sendMessage(msg);
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                recordingTime += 1000;
                if(preferences.getBoolean("isStopRecording", false))
                    stopThread(RECORD_STOP);

            }
        }

        public void stopThread(int MODE){
            Run = false;
            switch (MODE) {
                case RECORD_PAUSE:
                    break;

                case RECORD_STOP:
                    recordingTime = 0;
                    break;
            }
        }
    }


    private class RecordHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            textView_recordTime.setText(timeToRecordingTime(msg.arg1));
        }
    }




    // 로딩 다이얼로그 띄우기 및 데이터 저장
    private class LoadingTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(getContext(), android.R.style.Theme_DeviceDefault_Dialog);

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("저장중 입니다...");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                for(int i = 0; i < 2; i ++)
                    Thread.sleep(500);

            }catch (InterruptedException e){}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();

            String json = preferences.getString("Record_Json","");

            recordItems = gson.fromJson(json, myDataType);

            recordAdapter.updateList(recordItems);

            editor.putBoolean("isStopRecording", false);
            editor.commit();
            textView_recordTime.setText("00:00");
            btn_record.setImageDrawable(getResources().getDrawable(R.drawable.record_128dp));
            btn_record.setClickable(true);
            super.onPostExecute(aVoid);
        }
    }

    private void showRecordStartDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialog = getLayoutInflater().inflate(R.layout.dialog_record_filename, null);
        final EditText userInputFileName = dialog.findViewById(R.id.dialog_record_FileName_EditText);

        builder.setView(dialog)
                .setMessage("제목을 입력하세요.")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        recordFileName = userInputFileName.getText().toString();
                        btn_record.setImageDrawable(getResources().getDrawable(R.drawable.record_128dp_invisible));
                        btn_record.setClickable(false);
                        editor.putBoolean("isStopRecording", false);
                        editor.commit();

                        timeThread = new RecordTimeThread(new RecordHandler());
                        timeThread.start();


                        Intent recordIntent = new Intent(getActivity(), RecordService.class);
                        recordIntent.putExtra("isStop", "START");
                        recordIntent.putExtra("FILENAME", recordFileName);

                        getActivity().startService(recordIntent);

                        Toast.makeText(getContext(), "녹음이 시작되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    // 파일이름을 입력받아 수정된 파일경로를 리턴한다.
    private String modifyFileName(String originFilePath, String recordFileName) {
        File filePre = new File(originFilePath);
        File fileNow = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/",  recordFileName + ".3gp");

        if(filePre.exists()) {
            Log.d("file.exists()", "파일 존재함");

            filePre.renameTo(fileNow);
        }

        return filePre.getAbsolutePath();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == getActivity().RESULT_OK){
            if(requestCode == REQUEST_ADD_RECORD){


            } else if (requestCode == REQUEST_ADD_RECORD) {
               // recordThread = (RecordThread)data.getExtras().get("RecordThread");

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveCurrentData(){
        String json = gson.toJson(recordItems, myDataType);
        editor.putString("Record_Json", json);
        editor.commit();
    }

    private String timeToRecordingTime(int time){
        int min = (time % 3600000 ) / 60000;
        int sec = ((time % 3600000 ) % 60000 ) / 1000;

        return String.format("%02d:%02d", min, sec);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isStopRecording = preferences.getBoolean("isStopRecording", false);
//        if(isStopRecording)
//            btnClickListener.onClick(getView().findViewById(R.id.fragment_record_Button_recordStop));

    }



}
