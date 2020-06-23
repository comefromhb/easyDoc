package com.lin.doc.utils;

import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class DocMethodUtil {

    private static ParameterNameDiscoverer pnd;
    /**
     * 判断该类是否不可再分
     * @param method
     * @return
     */
    public static String[] getParameterNames(Method method){
        if (pnd==null){
            pnd = new LocalVariableTableParameterNameDiscoverer();
        }
        return pnd.getParameterNames(method);//返回的就是方法中的参数名列表了
    }
    public static String[] getParameterNames(MethodDoc method){
        Parameter[] parameters = method.parameters();
        String[] ret = new String[parameters.length];
        for (int i = 0;i<parameters.length;i++){
            ret[i] = parameters[i].name();
        }
        return ret;
    }

    public static String[] getParameterRequireArr(Method method){
        Annotation[][] methodParameterAnnotations = method.getParameterAnnotations();
        String[] ret = new String[methodParameterAnnotations.length];
        for (int i=0;i<methodParameterAnnotations.length ; i++){
            ret[i] = AnnoUtil.isRequired(methodParameterAnnotations[i])?"Y":"N";
        }
        return ret;
    }

    /**
     * 判断第index个参数是否含Valid注解
     * @param method
     * @param index
     * @return
     */
    public static boolean isValid(Method method,int index){
        Annotation[][] methodParameterAnnotations = method.getParameterAnnotations();
        Annotation[] annotations = methodParameterAnnotations[index];
        return AnnoUtil.hasValid(annotations);
    }
//
//    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException {
//        Class<?> clazz = Class.forName("cc.eslink.controller.etbc.MerchantPayMethodController");
//        Method mes = clazz.getDeclaredMethod("deletePaymentMethod", HttpServletRequest.class,Integer.class);
//        String[] strs = getParameterRequireArr(mes);
//        for (String str : strs) System.out.println(str);
//    }



}
