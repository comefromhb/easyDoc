package com.lin.doc.utils;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * detail: JavaDoc 读取类
 * @author Ttt
 */
public final class JavaDocReader {

    private JavaDocReader() {
    }

    // Doc 信息临时存储
    private static RootDoc mRoot;

    /**
     * detail: 一个简单 Doclet, 收到 RootDoc 对象保存起来供后续使用
     * @author Ttt
     */
    public static class Doclet {

        public Doclet() {
        }

        /**
         * Doclet 中, 方法 start 必须为静态
         * @param root {@link RootDoc}
         * @return true
         */
        public static boolean start(final RootDoc root) {
            mRoot = root;
            return true;
        }
    }

    // ================
    // = 回调通知接口 =
    // ================

    /**
     * detail: 读取回调
     * @author Ttt
     */
    public interface CallBack {

        /**
         * 回调通知
         * @param path      文件路径
         * @param className 文件名 ( 类名 )
         * @param rootDoc   根 Doc 信息
         * @param classDocs 类 Doc 信息
         * @return 处理后的文档信息
         */
        ClassDoc[] callback(String path, String className, RootDoc rootDoc, ClassDoc[] classDocs);

        /**
         * 异常回调
         * @param e 异常信息
         */
        void error(Exception e);
    }

    // ============
    // = 读取处理 =
    // ============

    /**
     * 读取处理
     * @param callBack  读取回调
     * @param path      文件路径
     * @param className 文件名 ( 类名 )
     * @return 处理后的文档信息
     */
    private static ClassDoc[] read(final CallBack callBack, final String path, final String className) {
        // 类 Doc 信息
        ClassDoc[] classDocs = null; // 如果有内部类, 则长度大于 1, 否则为 1 ( 指定的 className)
        // 防止为 null
        if (mRoot != null) {
            classDocs = mRoot.classes();
        }
        // 触发回调
        if (callBack != null) {
            return callBack.callback(path, className, mRoot, classDocs);
        }
        return null;
    }

    // ================
    // = 对外提供方法 =
    // ================

    /**
     * 读取文档处理
     * @param callBack      读取回调
     * @param path          文件路径
     * @param className     文件名 ( 类名 )
     * @param executeParams 执行参数
     * @return 处理后的文档信息
     */
    public static ClassDoc[] readDoc(final CallBack callBack, final String path, final String className, final String[] executeParams) {
        try {
            // 调用 com.sun.tools.javadoc.Main 执行 javadoc, 具体参数百度搜索
            com.sun.tools.javadoc.Main.execute(executeParams);
            // 进行读取
            return read(callBack, path, className);
        } catch (Exception e) {
            e.printStackTrace();
            if (callBack != null) {
                callBack.error(e);
            }
        }
        return null;
    }

    // =

    /**
     * 创建执行参数
     * <pre>
     *     根据自己的需求创建, 对应需要的执行参数
     *     该方法, 读取指定类
     * </pre>
     * @param readAll   是否读取全部 (-private 显示所有类和成员 )
     * @param path      文件路径
     * @param className 文件名 ( 类名 )
     * @return 执行参数
     */
    public static String[] getExecuteParams(final boolean readAll, final String path, final String className) {
        if (readAll) {
            return new String[]{
                    "-private", "-doclet", Doclet.class.getName(),
                    "-encoding", "utf-8",
                    "-classpath", "", path + className};
        } else {
            return new String[]{
                    "-doclet", Doclet.class.getName(),
                    "-encoding", "utf-8",
                    "-classpath", "", path + className};
        }
    }
    /**
     * 创建执行参数
     * <pre>
     *     根据自己的需求创建, 对应需要的执行参数
     *     该方法, 读取包下所有类
     * </pre>
     * @param readAll   是否读取全部 (-private 显示所有类和成员 )
     * @param path      文件路径
     * @param packageName 文件名 ( 包名 )
     * @return 执行参数
     */
    public static String[] getExecuteParamsForPackage(final boolean readAll, final String path, final String packageName) {
        if (readAll) {
            return new String[]{
                    "-private", "-doclet", Doclet.class.getName(),
                    "-encoding", "utf-8",
                    "-sourcepath",  path , packageName};
        } else {
            return new String[]{
                    "-doclet", Doclet.class.getName(),
                    "-encoding", "utf-8",
                    "-sourcepath",  path , packageName};
        }
    }

    /**
     * 按上级包逐层读取类,包名.类名作为KEY
     * @param path
     * @param packageName
     * @return map
     */
    public static Map<String,ClassDoc> readMultiJavaDoc(final String path, final String packageName) {
        Map<String,ClassDoc> ret = new HashMap<>();
        Set<String> packageNameSet1 = null;
        try {
            packageNameSet1 = FileUtil.getAllSubPackageNameSet(path,packageName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String pack:packageNameSet1){
//            System.out.println(pack);
            ClassDoc[] classDocs = readJavaDoc(path,pack);
            Map<String, ClassDoc> classDocMap = Arrays.asList(classDocs).stream().collect(
                    Collectors.toMap(ClassDoc::toString, Function.identity(), (key1, key2) -> key2));
            ret.putAll(classDocMap);
        }
        return ret;
    }

    public static ClassDoc[] readJavaDoc(final String path,final String packageName) {

        final String[] executeParams = getExecuteParamsForPackage(true, path, packageName);

        // 读取文档
        ClassDoc[] classDoces = readDoc(new JavaDocReader.CallBack() {
            @Override
            public ClassDoc[] callback(String path, String className, RootDoc rootDoc, ClassDoc[] classDocs) {
                //下面是测试用的代码

                StringBuilder buffer = new StringBuilder();
                buffer.append("\n\n");
                buffer.append("\n================");
                buffer.append("\n= 读取 JavaDoc =");
                buffer.append("\n================");
                buffer.append("\n");
                // 拼接信息
                buffer.append("\n文件路径: ").append(path + className)
                        .append("\n文件名字: ").append(className)
                        .append("\n执行参数: ").append(Arrays.toString(executeParams));

                if (classDocs != null) {
                    StringBuffer classBuffer = new StringBuffer();
                    // 循环 Class Doc 信息
                    for (ClassDoc classDoc : classDocs) {
                        classBuffer.append("\n\n");
                        classBuffer.append("\n= " + classDoc.name() + " =\n");
                        // 包名.类名
                        classBuffer.append("\n包名.类名: ").append(classDoc.toString());
                        // 类注释
                        classBuffer.append("\n类注释: ").append(classDoc.commentText());
//                        FieldDoc[] fieldDocs = classDoc.fields();
//                        for (FieldDoc fieldDoc : fieldDocs) {
//                            System.out.println(fieldDoc.commentText());
//                            System.out.println(fieldDoc.type());
//                        }
                        // ============
                        // = 读取方法 =
                        // ============
                        StringBuffer methodBuffer = new StringBuffer();
                        FieldDoc[] fields = classDoc.fields();
                        if (fields.length != 0) {
                            methodBuffer.append("\n\n| 字段信息 |");
                        }
                        for (FieldDoc fieldDoc : fields) {
                            methodBuffer.append("\n");
                            // 方法名
                            methodBuffer.append("\n字段名: " + fieldDoc.name());
                            //methodBuffer.append("\n字段名全: " + fieldDoc.qualifiedName());
                            // 方法注释
                            methodBuffer.append("\n字段注释: " + fieldDoc.commentText());
                            //methodBuffer.append("\nrowComment="+ fieldDoc.getRawCommentText());
                            //methodBuffer.append("\nposition="+ fieldDoc.position().toString());
                        }
                        // 获取方法 Doc 信息数组
                        MethodDoc[] methodDocs = classDoc.methods();
                        // 防止不存在方法
                        if (methodDocs.length != 0) {
                            methodBuffer.append("\n\n| 方法信息 |");
                        }
                        // 循环读取方法信息
                        for (MethodDoc methodDoc : methodDocs) {
                            methodBuffer.append("\n");
                            // 方法名
                            methodBuffer.append("\n方法名: " + methodDoc.name());
                            // 方法注释
                            methodBuffer.append("\n方法注释: " + methodDoc.commentText());
                            //methodBuffer.append("\nrowComment="+ methodDoc.getRawCommentText());
                            methodBuffer.append("\nposition="+ methodDoc.position().toString());
                        }
                        // 保存方法信息
                        classBuffer.append(methodBuffer);
                    }
                    // 保存类信息
                    buffer.append(classBuffer);
                }

                buffer.append("\n\n");
                buffer.append("\n=====================");
                buffer.append("\n= 读取 JavaDoc 结束 =");
                buffer.append("\n=====================");
                buffer.append("\n");
                // 打印文档信息
                //System.out.println(buffer.toString());
                //.........以上代码测试用
                // 返回文档信息
                return classDocs;
            }

            @Override
            public void error(Exception e) {
                System.out.println(e);
            }
        }, path, packageName, executeParams);
        return classDoces;
    }

    public static void main(String[] args) {
//        ClassDoc[] classDocs = JavaDocReader.readJavaDoc(GlobalConstants.OPERATION_DAO_SRC, GlobalConstants.PACK_CONTROLLER);
//        System.out.println(classDocs);
    }
}
