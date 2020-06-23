package com.lin.doc.utils;

import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class DocClassUtil {

    public static String getTypeName(Type type){
        if (hasOnlyGenericType(type))
            return ((ParameterizedType) type).getActualTypeArguments()[0].getTypeName();
        else return null;
    }

    /*
     * 返回泛型基类
     * 例如List<String>--->List,
     * BaseResponse<Merchant>--->BaseResponse
     * @param type
     * @return class
     * @throws ClassNotFoundException
     */
//    public static Class<?> getNoGenericType(Type type) throws ClassNotFoundException {
//        if (hasOnlyGenericType(type)){
//            String name = getNoGenericTypeName(getTypeName(type));
//            return Class.forName(name);
//        }
//        else return null;
//    }
    public static Class<?> getNoGenericType(String typeName) throws ClassNotFoundException {
        String name = getNoGenericTypeName(typeName);
        return ClassScanner.loadClass(name);
    }

    public static String getNoGenericTypeName(String name){
        if (!name.contains("<"))return name;
        return name.substring(0,name.indexOf("<")).trim();
    }
    public static String getGenericImplTypeName(String genericTypeName){
        if (genericTypeName.indexOf("<")==-1)return null;
        return genericTypeName.substring(genericTypeName.indexOf("<")+1,genericTypeName.lastIndexOf(">")).trim();
    }

    public static boolean hasOnlyGenericType(Type type){
        return type instanceof ParameterizedType &&
                ((ParameterizedType) type).getActualTypeArguments().length==1;
    }

    /**
     * 判断该类是否不可再分
     * @param clazz
     * @return
     */

    public static boolean isAtomicType(Class<?> clazz){
        //System.out.println(Map.class.isAssignableFrom(clazz));
        //System.out.println("className=="+clazz.getName());
        return  clazz.getModifiers()== (Modifier.PUBLIC | Modifier.FINAL)//包装类
                ||clazz.getModifiers()== (Modifier.PUBLIC | Modifier.FINAL | Modifier.ABSTRACT)//基本类型
                ||clazz.getModifiers()== (Modifier.PUBLIC | Modifier.INTERFACE | Modifier.ABSTRACT)//接口
                ||Map.class.isAssignableFrom(clazz)
                ||clazz.getName().equals("java.util.Date")
                ||clazz.getName().equals("java.lang.Object")
                ||clazz.getSuperclass().getName().equals("java.lang.Number")//科学计算类
                ||clazz.getSuperclass().getName().equals("java.util.Date");//时间戳类
    }

    public static void test04(Class<?> clazz) {
        ReflectionUtils.findField(clazz,"getId");
        // Spring的提供工具类,用于获取继承的父类是泛型的信息

        ResolvableType resolvableType = ResolvableType.forClass(clazz);
        System.out.println(resolvableType);
        Class<?> resolve = resolvableType.getGeneric(0).resolve();
        System.out.println(resolve);
    }

    /**
     * 根据name判断对象能否分解展开,
     * 内外有至少一个可分即为真
     * response<?> --true
     * MyObj --true
     * List<MyObj> --true
     * List<String> --false
     * Map<A,A> --false
     * @param typeName
     * @return
     */

    public static boolean canOpen(String typeName) throws ClassNotFoundException {
        //System.out.println(genericTypeName+"---canOpen---");
        String genericTypeName = typeName;
        if ("?".equals(genericTypeName)||"T".equals(genericTypeName))return false;
        if (!StringUtils.isEmpty(genericTypeName)){
            if (!genericTypeName.contains("<")){//非泛型
                //多泛型java.util.Map<java.lang.String, java.lang.String>的内部
                if (genericTypeName.contains(","))return false;
                return !isAtomicType(ClassScanner.loadClass(genericTypeName));
            }else {//单泛型，内外有至少一个可分即为真
                String innerName = getGenericImplTypeName(genericTypeName);
              //  System.out.println(genericTypeName+"--innerName->"+innerName);
                return !isAtomicType(getNoGenericType(genericTypeName))||canOpen(innerName);
            }
        }
        return false;
    }


}
