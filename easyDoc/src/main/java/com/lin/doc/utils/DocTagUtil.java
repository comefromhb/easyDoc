package com.lin.doc.utils;

import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class DocTagUtil {
    /**
     * 返回Description标签内容
     * @version s
     * @param methodDoc
     * @return text
     */
    public static String getDescriptionTagText(Doc methodDoc){
        return getTagText(methodDoc,"Description");
    }

    /**
     * 返回param标签内容
     * @param methodDoc
     * @return
     */
    public static Map<String,String> getParamTagText(Doc methodDoc){
        Tag[] tags = methodDoc.tags("param");
        Map<String,String> map = new HashMap<>();
        if (tags==null)return map;
        for (Tag tag:tags){
            if (tag.text()!=null && tag.text().length()>0 && tag.text().indexOf(" ")>0){
                int index = tag.text().indexOf(" ");
                String paramCode = tag.text().substring(0,index).trim();
                String paramDesc = tag.text().substring(index).trim();
                map.put(paramCode,paramDesc);
            }
        }
        return map;
    }


    /**
     * 返回Author标签内容
     * @param methodDoc
     * @return text
     */
    public static String getAuthorTagText(Doc methodDoc){
        return getTagText(methodDoc,"Author");
    }

    private static String getTagText(Doc methodDoc,String tagName){
        Tag[] tags = methodDoc.tags(tagName);
        if (tags==null)return "";
        StringBuilder text = new StringBuilder();
        for (Tag tag:tags){
            text.append(tag.text());
        }
        return text.toString();
    }

    /**
     * 判断类或方法是否包含某版本
     * @param doc
     * @param version
     * @return boolean
     */
    public static boolean hasTheVersion(Doc doc, String version){
        if (StringUtils.isEmpty(version))return true;
        Tag[] tags = doc.tags("version");
        if (tags==null)return false;
        for (Tag tag:tags){
            if (version.equals(tag.text()))
                return true;
        }
        return false;
    }

}
