package com.ss.nowpatch;

import com.tt.nowfix.core.PatchedClassInfo;
import com.tt.nowfix.core.PatchesInfo;

import java.util.ArrayList;
import java.util.List;

public class PatchesInfoImpl implements PatchesInfo {
    public List<PatchedClassInfo> getPatchedClassesInfo() {
        List<PatchedClassInfo> patchedClassesInfos = new ArrayList<PatchedClassInfo>();
        PatchedClassInfo patchedClass = new PatchedClassInfo(
                "com.ss.hotfixdemo.nowpatch.core.MoneyBean",
                MoneyBeanPatch.class.getCanonicalName());
        patchedClassesInfos.add(patchedClass);
        return patchedClassesInfos;
    }
}
