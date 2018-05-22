package com.ss.hotfixdemo.core.utils;

import java.lang.reflect.Array;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class DexHelper {

    public static void injectDex(String dexPath, String defaultDexOptPath) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, defaultDexOptPath, dexPath, getPathClassLoader());
        Object baseDexElements = getDexElements(getPathList(getPathClassLoader()));
        Object newDexElements = getDexElements(getPathList(dexClassLoader));
        Object allDexElements = combineArray(newDexElements, baseDexElements);
        Object pathList = getPathList(getPathClassLoader());
        ReflectionUtils.setField(pathList, pathList.getClass(), "dexElements", allDexElements);
    }

    /**
     * 获取系统类加载器
     */
    private static PathClassLoader getPathClassLoader() {
        PathClassLoader pathClassLoader = (PathClassLoader) DexHelper.class.getClassLoader();
        return pathClassLoader;
    }

    /**
     * 获取dex数组
     */
    private static Object getDexElements(Object paramObject)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return ReflectionUtils.getField(paramObject, paramObject.getClass(), "dexElements");
    }

    /**
     * 获取dexPathList
     */
    private static Object getPathList(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return ReflectionUtils.getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    /**
     * 合并dex数组
     */
    private static Object combineArray(Object firstArray, Object secondArray) {
        Class<?> localClass = firstArray.getClass().getComponentType();
        int firstArrayLength = Array.getLength(firstArray);
        int allLength = firstArrayLength + Array.getLength(secondArray);
        Object result = Array.newInstance(localClass, allLength);
        for (int k = 0; k < allLength; ++k) {
            if (k < firstArrayLength) {
                Array.set(result, k, Array.get(firstArray, k));
            } else {
                Array.set(result, k, Array.get(secondArray, k - firstArrayLength));
            }
        }
        return result;
    }
}
