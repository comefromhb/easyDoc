package com.lin.doc.utils;

import com.lin.doc.constants.GlobalConstants;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FileUtil {

    public static void writeFile(String text,String path) throws IOException {
        FileWriter fileWriter = null;
        //文件输出
        try {
            fileWriter =  new FileWriter(path,false);
            fileWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fileWriter!=null){
                fileWriter.close();
            }
        }

    }


    public static String getClazzPackageName(String name) {
        return name.substring(0,name.lastIndexOf("."));
    }


    public static Set<String> getAllSubPackageNameSet(String path,String basePackage) throws IOException {
        Set<String> classFullNameSet =ClassScanner.getClassFullNameSet(path,
                StringUtils.tokenizeToStringArray(basePackage, ",; \t\n"));
        Set<String> packageNameSet = new HashSet<>();
        for (String className : classFullNameSet) {
            String packageName =getClazzPackageName(className);
            if (!packageNameSet.contains(packageName)){
                packageNameSet.add(packageName);
            }
        }
        return packageNameSet;
    }

    public static  Map<String, List<File>>  getFilesFormPath(String filePath) throws FileNotFoundException {
        Set<File> fileSet = new HashSet<>();
        getFilesEndWithClass(filePath,fileSet);
        Map<String, List<File>> map = fileSet.stream().collect(Collectors.groupingBy(File::getName));
        return map;
    }
    /**
     * 返回传入路径下的所有.class文件
     * @param filePath
     * @return
     * @throws FileNotFoundException
     */
    public static void getFilesEndWithClass(String filePath,Set<File> files) throws FileNotFoundException {
        File file = new File(filePath);
        if (file.exists()){
            if (file.isDirectory()) {
                File[] list = file.listFiles();
                for (File file1:list){
                    getFilesEndWithClass(file1.getPath(),files);
                }
            } else if (file.getName().endsWith(".class")){
                files.add(file);
            }
        }
         else {
            throw new FileNotFoundException("根据指定路径未找到文件："+filePath);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        Set<File> list = new HashSet<>();
        getFilesEndWithClass(GlobalConstants.USER_DIR_PATH,list);
        for (File file : list){
            System.out.println(file.getName());
            System.out.println(file.getAbsolutePath());
            System.out.println(file.getPath());
        }
    }

}


