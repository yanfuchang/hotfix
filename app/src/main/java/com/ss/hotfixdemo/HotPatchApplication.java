package com.ss.hotfixdemo;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.ss.hotfixdemo.core.HotPatchManager;
import com.ss.hotfixdemo.core.IPatch;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

/**
 * Created by yanfuchang on 2018/3/30.
 */

public class HotPatchApplication extends Application {
    private static final String PATCH_DEX_PATH = "/aapatch_demo/patch_dex.jar";
    private static final String HACK_DEX_PATH = "/aapatch_demo/hack_dex.jar";

    @Override
    public void onCreate() {
        super.onCreate();

        IPatch patchManager = new HotPatchManager();
        //加载单独生成的一个dex
        patchManager.loadPath(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + HACK_DEX_PATH);
        // 加载补丁dex
        patchManager.loadPath(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + PATCH_DEX_PATH);

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

            // 获取patch_dex的dexElements（需要先加载dex）
            String dexopt = getDir("dexopt", 0).getAbsolutePath();
            DexClassLoader dexClassLoader = new DexClassLoader(path, dexopt, dexopt, getClassLoader());
            Object obj = getField(cl, "pathList", dexClassLoader);
            Object dexElements = getField(obj.getClass(), "dexElements", obj);

            Log.e("HotPatchApplication", "length = " + Array.getLength(dexElements));

            // 合并两个 Elements 数组
            Object combineElements = combineArray(dexElements, baseElements);

            // 将合并后的Element数组重新赋值给app的classLoader
            setField(pathList.getClass(), "dexElements", pathList, combineElements);

            //======== 以下是测试是否成功注入 =================
            Object object = getField(pathList.getClass(), "dexElements", pathList);
            int length = Array.getLength(object);
            Log.e("HotPatchApplication", "length = " + length);

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
