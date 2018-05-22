package com.ss.classpatch;

import android.content.Context;

import com.ss.classpatch.IPatch;
import com.ss.classpatch.utils.DexHelper;
import com.ss.classpatch.utils.StringUtils;

/**
 * 热修复sdk加载管理类
 */
public class HotPatchManager implements IPatch {

    /**
     * sdk入口方法
     *
     * @param context
     * @param path
     */
    @Override
    public void loadPath(Context context, String path) {
        if (context == null || StringUtils.isEmpty(path)) {
            return;
        }

        String odexPath = context.getDir("dexopt", 0).getAbsolutePath();
        try {
            DexHelper.injectDex(path, odexPath);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
