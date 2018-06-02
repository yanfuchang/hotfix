package com.yy.plugin

import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtBehavior
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtField
import javassist.CtMethod
import javassist.NotFoundException
import javassist.bytecode.AccessFlag
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
    static void injectDir(String path, Project project) {
        System.out.println("++++++开始插入+++++++")
        pool.appendClassPath(path)
//        pool.appendClassPath(project.android.bootClasspath[0].toString())
        File dir = new File(path)


        String mode = getMode(project)
        System.out.println("修复模式：  " + mode)

        if (dir.isDirectory()) {
            System.out.println("++++++进入目录+++++++")
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                System.out.println("遍历到：" + filePath)

                if (shouldInsertCode(filePath)) {
                    System.out.println("++++++获取类文件+++++++   " + filePath)
                    // 这里是应用包名，也能从清单文件中获取，先写死
                    int index = filePath.indexOf("com\\ss\\hotfixdemo")
                    if (index != -1) {
                        int end = filePath.length() - 6 // .class = 6
                        String className = filePath.substring(index, end).replace('\\', '.').replace('\\', '.')
                        System.out.println("++++++类名转换++++  " + className)

                        if (mode.equals("1")) {
                            injectClass(className, path)
                        } else {
                            insertCode(className, path)
                        }
                    }
                }
            }
        }
    }


    static String getMode(Project project) {
        Properties properties = new Properties();
        InputStream inputStream = project.rootProject.file('local.properties').newDataInputStream()
        properties.load(inputStream)

        String mode = properties.getProperty('HOTFIX_MODE')
        return mode
    }

    /**
     *  过来不插桩的类
     */
    static boolean shouldInsertCode(String filePath) {
        return (filePath.endsWith(".class")
                && !filePath.contains("ChangeQuickRedirect")
                && !filePath.contains('R$')
                && !filePath.contains('R.class')
                && !filePath.contains('PatchedClassInfo.class')
                && !filePath.contains('PatchesInfo.class')
                && !filePath.contains("PatchProxy.class")
                && !filePath.contains("NowPatchExecutor.class")
                && !filePath.contains("BuildConfig.class")
                && !filePath.contains("HotPatchApplication.class"))
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
        System.out.println("写回：name : " + className + "   ---path---   " + path)
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

    protected static void insertCode(String className, String path) {
        CtClass ctClass = pool.getCtClass(className)
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }
        boolean addIncrementalChange = false;
        System.out.println("准备循环")
        String INSERT_FIELD_NAME = "changeQuickRedirect"
        String INTERFACE_NAME = "com.tt.nowfix.core.ChangeQuickRedirect";

        for (CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {

            if (!addIncrementalChange) {
                addIncrementalChange = true
                //insert the field
                ClassPool classPool = ctBehavior.getDeclaringClass().getClassPool();
                CtClass type = classPool.getCtClass(INTERFACE_NAME)
                if (type == null) {
                    System.out.println("ChangeQuickRedirect 为空！！！！")
                    break
                }
                CtField ctField = new CtField(type, INSERT_FIELD_NAME, ctClass);
                ctField.setModifiers(AccessFlag.PUBLIC | AccessFlag.STATIC);
                // 在每一个class内设changeQuickRedirect
                ctClass.addField(ctField);
                System.out.println(className + "    插入Field")
            }

            if (!isQualifiedMethod(ctBehavior)) {
                continue
            }

            //here comes the method will be inserted code
            try {
                if (ctBehavior.getMethodInfo().isMethod()) {
                    CtMethod ctMethod = (CtMethod) ctBehavior;
                    boolean isStatic = (ctMethod.getModifiers() & AccessFlag.STATIC) != 0;
                    CtClass returnType = ctMethod.getReturnType();
                    String returnTypeString = returnType.getName();
                    //construct the code will be inserted in string format
                    String body = "Object argThis = null;";
                    if (!isStatic) {
                        //$0 表示当前圂对象
                        body += "argThis = \$0;";
                    }
                    //$arg表示当前方法胡所有参数new Object[] { savedInstanceState }
                    body += "   if (com.ss.hotfixdemo.nowpatch.core.PatchProxy.isSupport(\$args, argThis, " + INSERT_FIELD_NAME + ", " + isStatic + ")) {";
                    body += getReturnStatement(returnTypeString, isStatic);
                    body += "   }";
                    //finish the insert-code body ,let`s insert it
                    ctBehavior.insertBefore(body);
                }
                System.out.println(className + "    语句成功")

            } catch (Throwable t) {
                //here we ignore the error
                t.printStackTrace();
                System.out.println("ctClass: " + ctClass.getName() + " error: " + t.getMessage());
            }
        }

        ctClass.writeFile(path)
        ctClass.detach()
        System.out.println("写回：name : " + className + "   ---path---   " + path)
    }

    /**
     * 根据传入类型判断调用PathProxy的方法
     *
     * @param type 返回类型
     * @param isStatic 是否是静态方法
     * @param methodNumber 方法数
     * @return 返回return语句
     */
    private static String getReturnStatement(String type, boolean isStatic) {
        switch (type) {
            case Constants.CONSTRUCTOR:
                return "    com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatchVoid( \$args, argThis, changeQuickRedirect, " + isStatic + ");  ";
            case Constants.LANG_VOID:
                return "    com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatchVoid( \$args, argThis, changeQuickRedirect, " + isStatic + ");   return null;";

            case Constants.VOID:
                return "    com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatchVoid( \$args, argThis, changeQuickRedirect, " + isStatic + ");   return ;";

            case Constants.LANG_BOOLEAN:
                return "   return ((java.lang.Boolean)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + "));";
            case Constants.BOOLEAN:
                return "   return ((java.lang.Boolean)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch(\$args, argThis, changeQuickRedirect, " + isStatic + ")).booleanValue();";

            case Constants.INT:
                return "   return ((java.lang.Integer)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + ")).intValue();";
            case Constants.LANG_INT:
                return "   return ((java.lang.Integer)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + ")); ";

            case Constants.LONG:
                return "   return ((java.lang.Long)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + ")).longValue();";
            case Constants.LANG_LONG:
                return "   return ((java.lang.Long)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + "));";

            case Constants.DOUBLE:
                return "   return ((java.lang.Double)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + ")).doubleValue();";
            case Constants.LANG_DOUBLE:
                return "   return ((java.lang.Double)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + "));";

            case Constants.FLOAT:
                return "   return ((java.lang.Float)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + ")).floatValue();";
            case Constants.LANG_FLOAT:
                return "   return ((java.lang.Float)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + "));";

            case Constants.SHORT:
                return "   return ((java.lang.Short)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + ")).shortValue();";
            case Constants.LANG_SHORT:
                return "   return ((java.lang.Short)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + "));";

            case Constants.BYTE:
                return "   return ((java.lang.Byte)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + ")).byteValue();";
            case Constants.LANG_BYTE:
                return "   return ((java.lang.Byte)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + "));";
            case Constants.CHAR:
                return "   return ((java.lang.Character)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + ")).charValue();";
            case Constants.LANG_CHARACTER:
                return "   return ((java.lang.Character)com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + "));";
            default:
                return "   return (" + type + ")com.ss.hotfixdemo.nowpatch.core.PatchProxy.accessDispatch( \$args, argThis, changeQuickRedirect, " + isStatic + ");";
        }
    }


    private static boolean isQualifiedMethod(CtBehavior it) {

        //静态代码块
        if (it.getMethodInfo().isStaticInitializer()) {
            return false;
        }

        // synthetic 方法暂时不aop 比如AsyncTask 会生成一些同名 synthetic方法,对synthetic 以及private的方法也插入的代码，主要是针对lambda表达式
        if ((it.getModifiers() & AccessFlag.SYNTHETIC) != 0 && !AccessFlag.isPrivate(it.getModifiers())) {
            return false;
        }

        //构造函数
        if (it.getMethodInfo().isConstructor()) {
            return false;
        }

        //抽象
        if ((it.getModifiers() & AccessFlag.ABSTRACT) != 0) {
            return false;
        }

        //native方法
        if ((it.getModifiers() & AccessFlag.NATIVE) != 0) {
            return false;
        }

        //接口
        if ((it.getModifiers() & AccessFlag.INTERFACE) != 0) {
            return false;
        }

        return true;

        /*
        if (it.getMethodInfo().isMethod()) {
            if (AccessFlag.isPackage(it.getModifiers())) {
                it.setModifiers(AccessFlag.setPublic(it.getModifiers()));
            }
            boolean flag = isMethodWithExpression((CtMethod) it);
            if (!flag) {
                return false;
            }
        }
        //方法过滤
        if (isExceptMethodLevel && exceptMethodList != null) {
            for (String exceptMethod : exceptMethodList) {
                if (it.getName().matches(exceptMethod)) {
                    return false;
                }
            }
        }

        if (isHotfixMethodLevel && hotfixMethodList != null) {
            for (String name : hotfixMethodList) {
                if (it.getName().matches(name)) {
                    return true;
                }
            }
        }
        return !isHotfixMethodLevel;

       */
    }

}
