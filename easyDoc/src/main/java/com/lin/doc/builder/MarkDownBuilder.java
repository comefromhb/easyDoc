package com.lin.doc.builder;

import com.lin.doc.domain.ApiDoc;
import com.lin.doc.domain.ParamDto;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

public class MarkDownBuilder {

    public static String generateMarkdown(ApiDoc apiDoc){

        //组织输出md文件内容
        StringBuilder text = new StringBuilder("## 接口路径："+ apiDoc.getUrl()+"\n\n");
        text.append("## 请求方式："+ apiDoc.getMethod()+"\n\n") ;
        text.append("## 接口描述："+ apiDoc.getDescription()+"\n\n");
        text.append("## 作者："+ apiDoc.getAuthor()+"\n\n");
        List<ParamDto> paramDtoList = apiDoc.getRequestParam();
        text.append("## 请求参数：\n\n");
        text.append("参数名称|字段名|字段类型|必填|描述\n");
        text.append(":---:|:---:|:---:|:---:|:---:\n");
        text.append("渠道类型|channelType|String|Y|渠道类型WECHAT,WEB,APP,MINI\n");
        text.append("环境标识|develop|String|N|环境标识 true:开发环境 false:测试环境\n");
        //可设置固定参数
        if (!CollectionUtils.isEmpty(paramDtoList)){
            for (ParamDto paramDto :paramDtoList) {
                String name = "无";
                String des = "无";
                if(!StringUtils.isEmpty(paramDto.getName())){
                    name=paramDto.getName();
                }
                if(!StringUtils.isEmpty(paramDto.getDescription())){
                    des=paramDto.getDescription();
                }
                text.append(name+"|"+paramDto.getCode()+"|"+paramDto.getType()+"|"+paramDto.getRequire()+"|"+des+"\n");

            }
        }else {
            //无入参
        }
        text.append("## 响应参数：\n\n");
        List<ParamDto> resParamDtoList = apiDoc.getResponseParam();
        text.append("参数名称|字段名|字段类型|父节点|描述\n");
        text.append(":---:|:---:|:---:|:---:|:---:\n");
        if (CollectionUtils.isEmpty(resParamDtoList)){
            return text.append("无|无|无|无|无\n").toString();
        }
        for (ParamDto paramDto :resParamDtoList) {
            String name = "无";
            String des = "无";
            String parentNode = "无";
            if(!StringUtils.isEmpty(paramDto.getName())){
                name=paramDto.getName();
            }
            if(!StringUtils.isEmpty(paramDto.getDescription())){
                des=paramDto.getDescription();
            }
            if(!StringUtils.isEmpty(paramDto.getParentNode())){
                parentNode=paramDto.getParentNode();
            }
            text.append(name+"|"+paramDto.getCode()+"|"+paramDto.getType()+"|"+parentNode+"|"+des+"\n");

        }
        return text.toString();
    }
}
