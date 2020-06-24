package com.lin.doc.cloader;


import com.lin.doc.constants.GlobalConstants;
import com.lin.doc.utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.*;

/**
 * 自定义类加载器，用于加载制定工程目录下的字节码文件
 */
public class MyClassLoader extends ClassLoader{
    static {
        String daoPath = GlobalConstants.OPERATION_DAO_SRC.replace(GlobalConstants.SRC_PRE,"/target/classes/");
        String apiPath = GlobalConstants.OPERATION_CONTROLLER_SRC.replace(GlobalConstants.SRC_PRE,"/target/classes/");
        String poi = GlobalConstants.JAR_DIR+"/org/apache/poi";
        JarLoaderUtil.loadJarPath(poi);
        JarLoaderUtil.loadJarPath(daoPath);
        JarLoaderUtil.loadJarPath(apiPath);
        System.out.println("-----自定义类加载-----");

    }

    private static Map<String, Class> cache = new HashMap<>();//自定义类加载缓存
    private Map<String, List<File>> fileListMap;//自定义类加载器所负责的文件夹下的class文件集合
    //已经加载过的目录或包
    private Set<String> loadSet = new HashSet<>();

    public MyClassLoader(String path) {
        super();
        try {
            this.fileListMap = FileUtil.getFilesFormPath(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (cache.containsKey(name))
            return cache.get(name);
        //加载依赖
        //System.out.println("---------------自定义加载类："+name+"--------------------------");
        String[] nodes = name.split("\\.");
        if (nodes.length>2){
            String subJarDir = GlobalConstants.JAR_DIR+"/"+nodes[0]+"/"+nodes[1];
            if (!loadSet.contains(subJarDir)){
                JarLoaderUtil.loadJarPath(subJarDir);
                loadSet.add(subJarDir);
            }
        }
        //通过 文件输入流 读取 指定的class文件
        Class c = readFromFileStream(name);
        if (c!=null){
            cache.put(name,c);
            return c;
        }else {
            URLClassLoader urlClassLoader = JarLoaderUtil.getUrlClassLoader();
            c = urlClassLoader.loadClass(name);
        }
        if (c!=null)
            return c;
        return super.findClass(name);
    }

    private Class readFromFileStream(String name) {
        List<File> files = fileListMap.get(name.substring(name.lastIndexOf(".")+1).concat(".class"));
        if (files ==null){
            return null;
        }
        //System.out.println("---------------自定义加载类file："+name+"--------------------------");
        File file2 = null;
        for (File file: files){
            if (file.getAbsolutePath().endsWith(name.replace(".",File.separator).concat(".class"))){
                file2 = file;
                break;
            }
        }
        if (file2!=null){
            try {
                FileInputStream fis = new FileInputStream(file2.getAbsolutePath());
                //将读取的class文件对应的 字节数据 写入到内存中
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int i ;
                while ((i = fis.read())!=-1) {
                    out.write(i);
                }
                fis.close();
                byte[] buf = out.toByteArray();//提取 写到内存中的字节数据到数组
//	public byte[] toByteArray()创建一个新分配的 byte 数组。其大小是此输出流的当前大小，并且缓冲区的有效内容已复制到该数组中。
                return  defineClass(buf, 0, buf.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        String path = "D:/mall3/eslink_operation_mall";

        String name = "cc.eslink.mall.domain.order.MemberOrder";
        String name1 = "cc.eslink.mall.controller.etbc.OrderRefundEtbcController";

        MyClassLoader loader = new MyClassLoader(path);
        Class clazz = loader.loadClass(name);
        Class clazz1 = loader.loadClass(name1);
        System.out.println(clazz.getSimpleName());
        System.out.println(clazz.getName());
        System.out.println(clazz1.getSimpleName());
        System.out.println(clazz1.getName());
        Method[] methods = clazz1.getMethods();
        for (Method method : methods) {
            System.out.println(method.toString());
        }

    }

}
