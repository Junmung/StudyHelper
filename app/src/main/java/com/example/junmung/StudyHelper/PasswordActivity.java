package com.example.junmung.StudyHelper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class PasswordActivity extends AppCompatActivity {
    EditText[] edit_password = new EditText[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        getID();
    }

    private void getID(){
        for(int i = 0; i < edit_password.length; i++){
//            edit_password[i] = findViewById(R.id.)
        }
    }
}
