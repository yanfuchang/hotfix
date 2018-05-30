package com.yy.plugin

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtMethod
import org.gradle.api.Project

class CodeInject {
    private static ClassPool pool = ClassPool.getDefault()

    /**
     * 添加classPath到ClassPool
     * @param libPath
     */
    static void appendClassPath(String libPath) {
        pool.appendClassPath(libPath)
    }

    /**
     * 遍历该目录下的所有class，对所有class进行代码注入。
     * 其中以下class是不需要注入代码的：
     * --- 1. R文件相关
     * --- 2. 配置文件相关（BuildConfig）
     * --- 3. Application
     * @param path 目录的路径
     */
    static void injectDir(String path,Project project) {
        System.out.println("++++++开始插入+++++++")
        pool.appendClassPath(path)
//        pool.appendClassPath(project.android.bootClasspath[0].toString())
        File dir = new File(path)
        if (dir.isDirectory()) {
            System.out.println("++++++进入目录+++++++")
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                System.out.println("++++++获取类文件+++++++   " + filePath)
                if (filePath.endsWith(".class")
                        && !filePath.contains('R$')
                        && !filePath.contains('R.class')
                        && !filePath.contains("BuildConfig.class")
                        // 这里是application的名字，可以通过解析清单文件获得，先写死了
                        && !filePath.contains("HotPatchApplication.class")) {
                    // 这里是应用包名，也能从清单文件中获取，先写死
                    int index = filePath.indexOf("com\\ss\\hotfixdemo")
                    if (index != -1) {
                        int end = filePath.length() - 6 // .class = 6
                        String className = filePath.substring(index, end).replace('\\', '.').replace('\\', '.')
                        System.out.println("++++++类名转换++++  " + className)
                        injectClass(className, path)
                    }
                }
            }
        }
    }

    private static void injectClass(String className, String path) {
        CtClass c = pool.getCtClass(className)
        if (c.isFrozen()) {
            c.defrost()
        }

        CtConstructor[] cts = c.getDeclaredConstructors()

        if (cts == null || cts.length == 0) {
            insertNewConstructor(c)
        } else {
            cts[0].insertBeforeBody("System.out.println(com.yan.hack.LazyHack.class);")
//            cts[0].insertBeforeBody("System.out.println(666);")
        }
        c.writeFile(path)
        c.detach()
        System.out.println("写回：name : " + className +"   ---path---   " + path)
    }

    private static void insertNewConstructor(CtClass c) {
        CtConstructor constructor = new CtConstructor(new CtClass[0], c)
        constructor.insertBeforeBody("System.out.println(com.yan.hack.LazyHack.class);")
        c.addConstructor(constructor)
    }

    /**
     * ClassPool 是 CtClass 对象的容器
     *
     * CtClass 相当于一个可修改的class对象
     */
    private static void addSuperClass(String className, String father) {

        ClassPool pool = ClassPool.getDefault()

        //获取 CtClass
        CtClass cc = pool.getCtClass(className)

        //将 father 设置为 className 的父类
        cc.setSuperclass(pool.get(father))
        //写回源文件
        cc.writeFile()
    }

    /**
     * 添加一个方法
     */
    private static void addMethod(String className) {
        CtClass cc = pool.getCtClass(className)
        CtMethod method = CtMethod.make("public void fuck(){}", cc)
        cc.addMethod(method)
        cc.writeFile()
    }

    /**
     * 添加一个方法
     */
    private static void addMethodBody(String className, String methodName) {
        CtClass cc = pool.getCtClass(className)
        CtMethod method = cc.getDeclaredMethod(methodName)
        String code = "System.out.println(666);"
        method.insertBefore(code)
        cc.writeFile()
    }
}
