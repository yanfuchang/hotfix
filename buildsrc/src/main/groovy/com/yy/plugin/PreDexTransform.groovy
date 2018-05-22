package com.yy.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.gradle.api.Project

class PreDexTransform extends Transform {

    Project project

    PreDexTransform(Project project) {
        this.project = project

        def libPath = project.project(':hack').buildDir.absolutePath.concat("/intermediates/classes/debug")
        CodeInject.appendClassPath(libPath)
        CodeInject.appendClassPath("C:\\Users\\Yan fuchang\\AppData\\Local\\Android\\Sdk\\platforms\\android-27\\android.jar")
    }

    /**
     * 该task 在task列表中的名字
     */
    @Override
    String getName() {
        return "preDexyan"
    }

    /**
     * 指定input的类型
     * CLASSES代表处理的java的class文件，RESOURCES代表要处理java的资源
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指定Transfrom的作用范围
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 指明当前Transform是否支持增量编译
     */
    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * Transform中的核心方法
     * inputs中是传过来的输入流，其中有两种格式，一种是jar包格式一种是目录格式。
     */
    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        // Transfrom的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        System.out.println("++++++开始+++++++")
        inputs.each { TransformInput input ->

            input.directoryInputs.each { DirectoryInput directoryInput ->

                //TODO 注入代码
                CodeInject.injectDir(directoryInput.file.absolutePath)
                System.out.print("dirPath:  " + directoryInput.file.absolutePath)

                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            System.out.println("++++++结束+++++++")
        }
    }
}