package com.tt.nowfix;

import java.io.File;

public class Patch implements Cloneable {

    private String patchesInfoImplClassFullName;
    /**
     * 补丁名称
     */
    private String name;

    /**
     * 补丁的下载url
     */
    private String url;
    /**
     * 补丁本地保存路径
     */
    private String localPath;

    /**
     * 解密后的临时路径
     */
    private String tempPath;

    /**
     * 补丁md5值
     */
    private String md5;

    /**
     * 补丁对应的的App版本号,避免应用内升级导致低版本app的补丁应用到了高版本app上
     */
    private String targetVersion;

    //补丁的编号，补丁的唯一标识符
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //原始补丁文件的路径，推荐放到私有目录
    public String getLocalPath() {
        return localPath + ".jar";
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    //原始补丁的md5，确保原始补丁文件没有被篡改
    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isAppliedSuccess() {
        return isAppliedSuccess;
    }

    public void setAppliedSuccess(boolean appliedSuccess) {
        isAppliedSuccess = appliedSuccess;
    }

    /**
     * 补丁是否已经applied success
     */
    private boolean isAppliedSuccess;

    /**
     * 删除文件
     */
    public void delete(String path) {
        File f = new File(path);
        f.delete();
    }

    public String getPatchesInfoImplClassFullName() {
        return patchesInfoImplClassFullName;
    }

    public void setPatchesInfoImplClassFullName(String patchesInfoImplClassFullName) {
        this.patchesInfoImplClassFullName = patchesInfoImplClassFullName;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }


    public String getTempPath() {
        return tempPath + "_temp" + ".jar";
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    @Override
    public Patch clone() {
        Patch clone = null;
        try {
            clone = (Patch) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        return clone;
    }
}
