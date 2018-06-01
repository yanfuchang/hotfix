package com.ss.hotfixdemo.clzpatch;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;


import com.tt.nowfix.core.ChangeQuickRedirect;
import com.tt.nowfix.core.PatchedClassInfo;
import com.tt.nowfix.core.PatchesInfo;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;

import dalvik.system.DexClassLoader;

/**
 * Created by yanfuchang on 2018/3/30.
 */

public class HotPatchApplication extends Application {
    private static final String PATCH_DEX_PATH = "/aapatch_demo/patch_dex.jar";
    private static final String HACK_DEX_PATH = "/aapatch_demo/hack_dex.jar";

    private DexClassLoader mLoader;

    @Override
    protected void attachBaseContext(Context base) {
//        boolean isSucc = loadDex(base);
//        Log.d("HotPatchApplication", "load dex issucc:"+isSucc);
//        if(isSucc){
//            Log.d("HotPatchApplication", "start patch");
//            patch();
//        }
        super.attachBaseContext(base);

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
                //加载所有修复类对象
                ChangeQuickRedirect redirectObj = (ChangeQuickRedirect)mLoader.loadClass(
                        info.getPatchClassName()).newInstance();
                //获取待修复旧类类型
                Class<?> fixClass = mLoader.loadClass(info.getFixClassName());
                //将修复类对象设置到待修复旧类的changeQuickRedirect变量中
                Field redirectF = fixClass.getField("changeQuickRedirect");
                redirectF.set(null, redirectObj);
            }
            Log.d("HotPatchApplication", "patch succ");
        }catch(Throwable e){
            Log.d("HotPatchApplication", "patch error:"+Log.getStackTraceString(e));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        //加载单独生成的一个dex
//        doPatch("/aapatch_demo/hack_dex.jar");
//
//        // 加载补丁包
//        doPatch("/aapatch_demo/patch_dex.jar");
    }

    private void doPatch(String path) {
        String p = Environment.getExternalStorageDirectory().getAbsolutePath() + path;
        File file = new File(p);
        Log.v("HotPatchApplication", "p : " + p);
        if (file.exists()) {
            inject(p);
        } else {
            Log.v("HotPatchApplication", "path 不存在");
        }
    }

    private void inject(String path) {

        try {
            Class<?> cl = Class.forName("dalvik.system.BaseDexClassLoader");

            //获取 DexPathList 对象
            Object pathList = getField(cl, "pathList", getClassLoader());
            //获取 DexPathList 对象中的 dexElements 数组
            Object baseElements = getField(pathList.getClass(), "dexElements", pathList);
            Log.e("HotPatchApplication", " base_dex length = " + Array.getLength(baseElements));

            // 获取patch_dex的dexElements（需要先加载dex）
            String dexopt = getDir("dexopt", 0).getAbsolutePath();
            DexClassLoader dexClassLoader = new DexClassLoader(path, dexopt, dexopt, getClassLoader());
            Object obj = getField(cl, "pathList", dexClassLoader);
            Object dexElements = getField(obj.getClass(), "dexElements", obj);

            Log.e("HotPatchApplication", " hack_dex length = " + Array.getLength(dexElements));

            // 合并两个 Elements 数组
            Object combineElements = combineArray(dexElements, baseElements);


            // 将合并后的Element数组重新赋值给app的classLoader
            setField(pathList.getClass(), "dexElements", pathList, combineElements);

            //======== 以下是测试是否成功注入 =================
            Object object = getField(pathList.getClass(), "dexElements", pathList);
            int length = Array.getLength(object);
            Log.e("HotPatchApplication", "合并length = " + length);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过反射获取对象的属性值
     */
    private Object getField(Class<?> cl, String fieldName, Object object) throws NoSuchFieldException, IllegalAccessException {
        Field field = cl.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }


    /**
     * 通过反射设置对象的属性值
     */
    private void setField(Class<?> cl, String fieldName, Object object, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = cl.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    /**
     * 通过反射合并两个数组
     */
    private Object combineArray(Object firstArr, Object secondArr) {
        int firstLength = Array.getLength(firstArr);
        int secondLength = Array.getLength(secondArr);
        int length = firstLength + secondLength;

        Class<?> componentType = firstArr.getClass().getComponentType();
        Object newArr = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            if (i < firstLength) {
                Array.set(newArr, i, Array.get(firstArr, i));
            } else {
                Array.set(newArr, i, Array.get(secondArr, i - firstLength));
            }
        }
        return newArr;
    }
}
