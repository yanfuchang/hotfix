package com.ss.hotfixdemo;

import android.Manifest;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button mButton;
    private Dog mDog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        mButton = findViewById(R.id.main_shot);
        mDog = new Dog();
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToast();
            }
        });
    }

    private void sendToast() {
        Toast.makeText(MainActivity.this, Dog.name + mDog.shout(), Toast.LENGTH_SHORT).show();
        String p = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aapatch_demo/patch_dex.jar";
        Log.v("HotPatchApplication", p);

    }
}
