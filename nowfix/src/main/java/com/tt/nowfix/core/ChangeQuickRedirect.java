package com.tt.nowfix.core;

public interface ChangeQuickRedirect {
    public boolean isSupport(String methodSignature, Object[] paramArrayOfObject);

    public Object accessDispatch(String methodSignature, Object[] paramArrayOfObject);
}
