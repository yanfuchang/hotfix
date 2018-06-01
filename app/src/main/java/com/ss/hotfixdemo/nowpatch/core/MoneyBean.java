package com.ss.hotfixdemo.nowpatch.core;

import android.annotation.SuppressLint;

import com.tt.nowfix.core.ChangeQuickRedirect;

import java.util.ArrayList;
import java.util.List;

public class MoneyBean {
    public static String desc() {
        return "MoneyBean";
    }

    @SuppressLint("UseValueOf")
    @SuppressWarnings("unchecked")
    public List<String> getInfo(String str, float f, int i, List<String> list) {

        return new ArrayList<String>();
    }

    public int getMoneyValue() {

        //原始逻辑，返回10
        return -10;
    }
}
