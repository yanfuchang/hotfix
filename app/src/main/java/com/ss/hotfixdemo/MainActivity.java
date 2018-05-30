package com.ss.hotfixdemo;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button mButton;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        mButton = findViewById(R.id.main_shot);
        mTextView = findViewById(R.id.name);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               updateName();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sunflower sunflower = new Sunflower();
        mTextView.setText(sunflower.name);
    }

    private void updateName() {
        Intent intent = new Intent(MainActivity.this,Main2Activity.class);
        MainActivity.this.startActivity(intent);
        mTextView.setText("向日葵");
        Sunflower s = new Sunflower();
    }
}
