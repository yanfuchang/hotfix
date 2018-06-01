package com.ss.hotfixdemo.nowpatch.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.tt.nowfix.core.ChangeQuickRedirect;
import com.tt.nowfix.core.PatchedClassInfo;
import com.tt.nowfix.core.PatchesInfo;

import java.io.File;
import java.lang.reflect.Field;

import java.util.HashSet;
import java.util.List;

import java.util.Set;

import dalvik.system.DexClassLoader;

public class NowPatchExecutor {
    private static volatile NowPatchExecutor INSTANCE;
    private Set<String> mPatchedSet;
    private Context mContext;
    private DexClassLoader mLoader;

    public NowPatchExecutor(Context context) {
        this.mContext = context;
        mPatchedSet = new HashSet<>();
    }

    public static NowPatchExecutor getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NowPatchExecutor.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NowPatchExecutor(context);
                }
            }
        }
        return INSTANCE;
    }

    public void doPatch() {
        if (loadDex(mContext)) {
            patch();
        }
    }

    @SuppressLint({ "SdCardPath"})
    private boolean loadDex(Context ctx){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aapatch_demo/nowpatch_dex.jar";

        File dexFile = new File(path);
        if(!dexFile.exists()){
            Log.d("HotPatchApplication", "patch.dex is not exist!");
            return false;
        }

        try{
            File odexDir = new File(ctx.getFilesDir()+File.separator+"odex"+File.separator);
            if(!odexDir.exists()){
                odexDir.mkdirs();
            }
            mLoader = new DexClassLoader(dexFile.getAbsolutePath(),  odexDir.getAbsolutePath(), null, ctx.getClassLoader());
            Log.i("HotPatchApplication", "mloader;"+ mLoader);
            return true;
        }catch(Throwable e){
            Log.d("HotPatchApplication", "load patch error:"+Log.getStackTraceString(e));
        }
        return false;
    }

    @SuppressLint("NewApi")
    private void patch(){
        try{
            //先得到修复包中的PatchesInfoImpl类
            Class<?> patchInfoClazz = mLoader.loadClass("com.ss.nowpatch.PatchesInfoImpl");
            PatchesInfo patchInfo = (PatchesInfo)patchInfoClazz.newInstance();
            //获取修复包中所有待修复类信息
            List<PatchedClassInfo> infoList = patchInfo.getPatchedClassesInfo();
            for(PatchedClassInfo info : infoList){
                if (mPatchedSet.contains(info.getFixClassName()))
                    continue;
                //加载所有修复类对象
                ChangeQuickRedirect redirectObj = (ChangeQuickRedirect)mLoader.loadClass(
                        info.getPatchClassName()).newInstance();
                //获取待修复旧类类型
                Class<?> fixClass = mLoader.loadClass(info.getFixClassName());
                //将修复类对象设置到待修复旧类的changeQuickRedirect变量中
                Field redirectF = fixClass.getField("changeQuickRedirect");
                redirectF.set(null, redirectObj);
                mPatchedSet.add(info.getFixClassName());
            }
            Log.d("HotPatchApplication", "patch succ");
        }catch(Throwable e){
            Log.d("HotPatchApplication", "patch error:"+Log.getStackTraceString(e));
        }
    }

}
