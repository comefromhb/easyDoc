package com.lin.doc.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

public class DocFieldUtil {

    /**
     * 判断字段是否含单泛型
     * @param field
     * @return
     */
    public static boolean hasOnlyGenericType(Field field){
        return field.getGenericType() instanceof ParameterizedType
                &&((ParameterizedType) field.getGenericType()).getActualTypeArguments().length==1;
    }

    /**
     * 判断字段是否有必填标记
     * @param field
     * @return
     */
    public static String getFieldRequire(Field field){
        Annotation[] an = field.getDeclaredAnnotations();
        return AnnoUtil.isRequired(an)?"Y":"N";
    }
}
