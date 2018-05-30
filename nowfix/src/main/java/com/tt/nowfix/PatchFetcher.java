package com.tt.nowfix;

import android.content.Context;

import java.util.List;

public interface PatchFetcher {
    /**
     * 获取补丁列表
     *
     * @param context
     * @return 相应的补丁列表
     */
     List<Patch> fetchPatchList(Context context);
    /**
     * 努力确保补丁文件存在，验证md5是否一致。
     * 如果不存在，则动态下载
     *
     * @param patch
     * @return 是否存在
     */
    boolean ensurePatchExist(Patch patch);
}
