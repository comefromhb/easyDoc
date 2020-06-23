package com.lin.doc.constants;

import org.springframework.util.StringUtils;

import java.io.File;

public interface GlobalConstants {

    /** api类源码所在包路径 设置这个指定要扫描的接口范围*/
    String PROJ_DIR ="D:/mall3/eslink_operation_mall";
    //依赖的jar包路径-一般是本地仓库
    String JAR_DIR ="D:/Dev/repository";
    String SUB_PACK ="etbc";
    /** api类源码所在包路径*/
    String PACK_CONTROLLER ="cc.eslink.mall.controller."+SUB_PACK;
    /** 项目所在根目录 */
    //user.dir : D:\mall\eslink_operation_mall
    String USER_DIR_PATH = StringUtils.isEmpty(PROJ_DIR)?System.getProperty("user.dir"):PROJ_DIR;
    String FILE_SP =File.separator;
    String SRC_PRE =FILE_SP+"src"+FILE_SP+"main"+FILE_SP+"java"+FILE_SP;
    /** api类源码所在module*/
    String CONTROLLER_SRC = "operation-controller";
    /** 参数实体类源码所在module*/
    String DAO_SRC = "operation-dao";
    /** api类源码文件路径*/
    //String OPERATION_CONTROLLER_SRC = "D:"+File.separator+"mall\\eslink_operation_mall\\operation-controller\\src\\main\\java\\";
    String OPERATION_CONTROLLER_SRC = USER_DIR_PATH+ "/"+CONTROLLER_SRC+SRC_PRE;
    /** 参数实体类源码文件路径*/
    String OPERATION_DAO_SRC =  USER_DIR_PATH+ "/"+DAO_SRC+SRC_PRE;

    /** 参数实体类所在包*/
    String PACK_DOMAINS ="cc.eslink.mall.domain,cc.eslink.mall.dto";
    /** 特殊处理的类，实际包名*/
    String CLASS_RESPONSE ="cc.eslink.common.base.BaseResponse";
    /** 特殊处理的类，复制后的所在包名*/
    String CLASS_RESPONSE_REPLACE ="cc.eslink.mall.dto.BaseResponse";

    /** 导出文档所在目录*/
    String FILE_SAVE_DIR ="E:\\md2\\";
    /** 导出文件名前缀*/
    String FILE_PREFIX_NAME =SUB_PACK+"_";
    //设置递归深度上限
    Integer MAX_DEPTH =3;
    //导出版本
    String VERSION ="3.2.0";
    //输出到一个文档
    Boolean ONE_FILE =true;
}
