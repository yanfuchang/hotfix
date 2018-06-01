package com.ss.hotfixdemo.clzpatch;

import android.app.Activity;

import android.os.Bundle;
import android.widget.TextView;

import com.ss.hotfixdemo.R;
import com.ss.hotfixdemo.nowpatch.core.MoneyBean;
import com.ss.hotfixdemo.nowpatch.core.NowPatchExecutor;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView mTextView;
    private TextView mMoneyTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.name);
        mMoneyTv = findViewById(R.id.cost);
        Sunflower sunflower = new Sunflower();
        mTextView.setText(sunflower.name);
    }

    @Override
    protected void onResume() {
        NowPatchExecutor.getInstance(this.getApplicationContext()).doPatch();
        super.onResume();
        MoneyBean moneyBean = new MoneyBean();
        mMoneyTv.setText("价格："+ moneyBean.getMoneyValue());
    }
}
