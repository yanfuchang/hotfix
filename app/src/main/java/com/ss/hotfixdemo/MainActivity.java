package com.ss.hotfixdemo;

import android.app.Activity;

import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.name);
        Sunflower sunflower = new Sunflower();
        mTextView.setText(sunflower.name);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
