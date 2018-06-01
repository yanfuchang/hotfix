package com.ss.asmcore;

import java.io.File;

public class InsertClassFile {

    public static void insertCode(String path) {
        File srcClassFile = new File(path);
        if (!srcClassFile.exists()) {
            System.out.println("classfile:" + srcClassFile.getName() + " 不存在!!");
            return;
        }
        System.out.println("准备插入：" + srcClassFile.getName());

        String tempClassFileStr = srcClassFile.getParentFile().getAbsolutePath() + File.separator + srcClassFile.getName() + "_tmp";
        File tempClassFile = new File(tempClassFileStr);
        if (!tempClassFile.exists()) {
            try {
                tempClassFile.createNewFile();
            } catch (Exception e) {
            }
        }
        boolean isSucc = InsertCodeUtils.operateClassByteCode(srcClassFile, tempClassFile);

        if (!isSucc) {
            if (tempClassFile.exists()) {
                tempClassFile.delete();
            }
            System.out.println("insert code fail,classname:" + srcClassFile.getName());
            return;
        }
        srcClassFile.delete();
        tempClassFile.renameTo(srcClassFile);
        System.out.println("插入成功:"+srcClassFile.getName());
    }
}
