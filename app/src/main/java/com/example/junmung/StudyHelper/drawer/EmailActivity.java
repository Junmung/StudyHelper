package com.example.junmung.studyhelper.drawer;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.junmung.studyhelper.R;

public class EmailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ActionBar actionBar;
    private EditText edit_title, edit_contents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        getID();


    }



    private void getID(){
        toolbar = findViewById(R.id.activity_email_Toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("문의하기");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        edit_title = findViewById(R.id.activity_email_EditText_Title);
        edit_contents = findViewById(R.id.activity_email_EditText_contents);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.actionbar_action, menu);
        menu.getItem(0).setTitle("보내기");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                // 저장안된다는 다이얼로그 생성

                finish();
                break;

            case R.id.action_complete:
                String address = "9174844@naver.com";
                String contents = edit_contents.getText().toString();
                String title = edit_title.getText().toString();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain")
                        .putExtra(Intent.EXTRA_EMAIL, new String[]{address})
                        .putExtra(Intent.EXTRA_SUBJECT, title)
                        .putExtra(Intent.EXTRA_TEXT, contents);

                startActivity(intent);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
