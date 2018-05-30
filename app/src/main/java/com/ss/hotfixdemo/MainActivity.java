package com.ss.hotfixdemo;

import android.app.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button mButton;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
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
        mTextView.setText("向日葵");
        Sunflower s = new Sunflower();
    }
}
