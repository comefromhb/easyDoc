package com.lin.doc;

import com.lin.doc.constants.GlobalConstants;
import com.lin.doc.builder.MarkDownBuilder;
import com.lin.doc.domain.ApiDoc;
import com.lin.doc.utils.ApiDocGenerator;
import com.lin.doc.utils.FileUtil;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

public class AppMain {
    /**
     * 方法入口
     * @param args
     */
    public static void main(String[] args) {

        if (GlobalConstants.ONE_FILE){
            outPutInOneFile();
        }else {
            outPutInMultiFile();
        }
    }

    /**
     * 输出到多个文件
     */
    public static void outPutInMultiFile(){
        List<ApiDoc> apiDocList = ApiDocGenerator.getSortedDocList();
        if (CollectionUtils.isEmpty(apiDocList)){
            System.out.println("没有可导出接口=============OVER=====");
            return;
        }
        int i = 1;
        System.out.println("===========开始导出接口文档，路径"+ GlobalConstants.FILE_SAVE_DIR+"===========");
        for (ApiDoc apiDoc : apiDocList){
            String text = MarkDownBuilder.generateMarkdown(apiDoc);
            String url = apiDoc.getUrl();
            String name = url.substring(url.lastIndexOf("/")+1);
            try {
                String path = GlobalConstants.FILE_SAVE_DIR+ GlobalConstants.FILE_PREFIX_NAME+name+i+".md";  //文件路径
                FileUtil.writeFile(text,path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
        }
        System.out.println("===========导出文档成功！===========");
        System.out.println("===========共导出接口文档"+(i-1)+"件===========");

    }
    /**
     * 输出到1个文件
     */
    public static void outPutInOneFile(){
        List<ApiDoc> apiDocList = ApiDocGenerator.getSortedDocList();
        if (CollectionUtils.isEmpty(apiDocList)){
            System.out.println("没有可导出接口=============OVER=====");
            return;
        }
        int i = 1;
        StringBuilder text = new StringBuilder();
        for (ApiDoc apiDoc : apiDocList){
            String text1 = MarkDownBuilder.generateMarkdown(apiDoc);
            text.append(text1).append("\n\n");
            i++;
        }
        System.out.println("===========开始导出接口文档，路径"+ GlobalConstants.FILE_SAVE_DIR+"===========");
        String name = "com/lin/doc";
        try {
            String path = GlobalConstants.FILE_SAVE_DIR+ GlobalConstants.FILE_PREFIX_NAME+name+i+".md";  //文件路径
            FileUtil.writeFile(text.toString(),path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("===========导出文档成功！===========");
        System.out.println("===========共导出接口"+(i-1)+"个===========");

    }
}
