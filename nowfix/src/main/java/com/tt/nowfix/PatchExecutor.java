package com.tt.nowfix;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.List;

import dalvik.system.DexClassLoader;

public class PatchExecutor extends Thread {
    public static final String TAG = "NowFix";
    protected Context context;
    protected PatchFetcher patchFetcher;
    protected PatchCallBack patchCallBack;

    public PatchExecutor(Context context, PatchFetcher patchFetcher, PatchCallBack patchCallBack) {
        this.context = context.getApplicationContext();
        this.patchFetcher = patchFetcher;
        this.patchCallBack = patchCallBack;
    }

    @Override
    public void run() {
        //从云端拉取补丁列表
        List<Patch> patches = fetchPatchList();
        //加载补丁
        applyPatchList(patches);
    }

    /**
     * 拉取补丁列表
     */
    protected List<Patch> fetchPatchList() {
        return patchFetcher.fetchPatchList(context);
    }

    /**
     * 应用补丁列表
     */
    protected void applyPatchList(List<Patch> patches) {
        if (null == patches || patches.isEmpty()) {
            return;
        }
        Log.d(TAG, " patchFetcher list size is " + patches.size());
        for (Patch p : patches) {
            if (p.isAppliedSuccess()) {
                Log.d(TAG, "补丁已经加载过 " + p.getName());
                continue;
            }
            if (patchFetcher.ensurePatchExist(p)) {
                boolean currentPatchResult = false;
                try {
                    currentPatchResult = patch(context, p);
                } catch (Throwable t) {
                }
                if (currentPatchResult) {
                    //设置patch 状态为成功
                    p.setAppliedSuccess(true);
                    //统计PATCH成功率 PATCH成功
                    patchCallBack.onPatchApplied(true, p);

                } else {
                    //统计PATCH成功率 PATCH失败
                    patchCallBack.onPatchApplied(false, p);
                }

            }
        }
    }

    protected boolean patch(Context context, Patch patch) {

        DexClassLoader classLoader = new DexClassLoader(patch.getTempPath(), context.getCacheDir().getAbsolutePath(),
                null, PatchExecutor.class.getClassLoader());
        patch.delete(patch.getTempPath());

        Class patchClass, oldClass;

        Class patchsInfoClass;
        PatchesInfoL patchesInfoL = null;
        try {
            patchsInfoClass = classLoader.loadClass(patch.getPatchesInfoImplClassFullName());
            patchesInfoL = (PatchesInfoL) patchsInfoClass.newInstance();
            Log.d(TAG, "PatchsInfoImpl ok");
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (patchesInfoL == null) {
            return false;
        }

        //获取需要打补丁的类
        List<PatchedClassInfoL> patchedClasses = patchesInfoL.getPatchedClassesInfo();
        if (null == patchedClasses || patchedClasses.isEmpty()) {
            return false;
        }

        for (PatchedClassInfoL patchedClassInfoL : patchedClasses) {
            String patchedClassName = patchedClassInfoL.patchedClassName;
            String patchClassName = patchedClassInfoL.patchClassName;
            if (TextUtils.isEmpty(patchedClassName) || TextUtils.isEmpty(patchClassName)) {
                continue;
            }

            try {
                oldClass = classLoader.loadClass(patchedClassName.trim());
                Field[] fields = oldClass.getDeclaredFields();

                Field changeRedirectField = null;
                for (Field field : fields) {
                    if (TextUtils.equals(field.getType().getCanonicalName(), ChangeRedirect.class.getCanonicalName()) && TextUtils.equals(field.getDeclaringClass().getCanonicalName(), oldClass.getCanonicalName())) {
                        changeRedirectField = field;
                        break;
                    }
                }
                if (changeRedirectField == null) {
                    continue;
                }
                try {
                    patchClass = classLoader.loadClass(patchClassName);
                    Object patchObject = patchClass.newInstance();
                    changeRedirectField.setAccessible(true);
                    changeRedirectField.set(null, patchObject);
                    Log.d(TAG, "patch success ");
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return true;
    }

}
