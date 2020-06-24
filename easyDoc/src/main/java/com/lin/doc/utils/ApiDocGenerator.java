package com.lin.doc.utils;


import com.lin.doc.constants.GlobalConstants;
import com.lin.doc.domain.ApiDoc;
import com.lin.doc.domain.ParamDto;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import org.springframework.core.ResolvableType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApiDocGenerator {


    private static  Map<String, ClassDoc> paramPojoMap;
    /**
     * 缓存字段信息
     */
    private static  Map<String, Map<String, FieldDoc>> fieldMap = new HashMap<>();
    static {
        if (paramPojoMap==null){
            System.out.println("-------------开始扫描参数类文件----------");
            long start = System.currentTimeMillis();
            paramPojoMap = JavaDocReader.readMultiJavaDoc(GlobalConstants.OPERATION_DAO_SRC, GlobalConstants.PACK_DOMAINS);
            if (CollectionUtils.isEmpty(paramPojoMap))new NullPointerException("未加载到参数类信息");
            long end = System.currentTimeMillis();
            System.out.println("------------paramPojoMap加载完成---数量："+paramPojoMap.size()+"--耗时"+(end-start)+"ms----------------------------\n");
        }
    }
    /**
     * 根据完整类名获取字段列表
     * @param className
     * @return FieldDocMap
     */
    public static  Map<String, FieldDoc> getParamPojoFieldDocMap(String className){
        //System.out.println("-------------查询类名----------"+className);
        if (fieldMap.containsKey(className)){
            return fieldMap.get(className);
        }
        if (paramPojoMap==null)throw new NullPointerException();
        if (!paramPojoMap.containsKey(className)){
            //处理未加载到源码的类
            return null;
        }
        ClassDoc classDoc =paramPojoMap.get(className);
        if (classDoc==null)throw new NullPointerException("未查到类信息结果："+className);
        List<FieldDoc> methodDocList=Arrays.asList(classDoc.fields());
        Map<String, FieldDoc> fieldDocMap = methodDocList.stream().collect(
                Collectors.toMap(FieldDoc::qualifiedName, Function.identity(), (key1, key2) -> key2));
        fieldMap.put(className,fieldDocMap);
        return fieldDocMap;
    }

    public static List<ApiDoc> getInterfaceDocList(String basePackages) {
        long start = System.currentTimeMillis();
        System.out.println("-------------开始扫描控制层文件----------");
        Set<Class> aClasses = ClassScanner.scan(basePackages, RestController.class);
        long end = System.currentTimeMillis();
        System.out.println("-------------扫描控制层类文件完成，耗时"+(end-start)+"ms----------");
        //pojo类
        //Map<String, ClassDoc> paramPojoMap = JavaDocReader.readMultiJavaDoc(GlobalConstants.SRC_MAIN_JAVA,GlobalConstants.MYBOOT_DOMAIN);
        List<ApiDoc> apiDocList = new ArrayList<>();
        //获取javadoc
        ClassDoc[] classDocs = JavaDocReader.readJavaDoc(GlobalConstants.OPERATION_CONTROLLER_SRC, basePackages );
        if (classDocs==null)throw new NullPointerException();
        long end1 = System.currentTimeMillis();
        System.out.println("-------------扫描控制层类源码文件完成，耗时"+(end1-start)+"ms----------");
        List<ClassDoc> classDocList= Arrays.asList(classDocs);
        //包、类名匹配
        Map<String, ClassDoc> classDocMap = classDocList.stream().collect(
                Collectors.toMap(ClassDoc::toString, Function.identity(), (key1, key2) -> key2));
        for (Class aClass : aClasses) {
            String classKey = aClass.getName();
            //排除内部类
            if (classKey.contains("$"))
                continue;
            ClassDoc classDoc = classDocMap.get(classKey);
            if (classDoc==null){
                throw new NullPointerException("缺少源文件");
            }
            StringBuilder classUrl = new StringBuilder();
            RequestMapping requestMapping = (RequestMapping) aClass.getAnnotation(RequestMapping.class);
            if (requestMapping!=null){
                classUrl.append(requestMapping.value()[0]);
            }
            boolean hasTheClassVersion = DocTagUtil.hasTheVersion(classDoc,GlobalConstants.VERSION);
            MethodDoc[] methodDocs = classDoc.methods();
            List<MethodDoc> methodDocList=Arrays.asList(methodDocs);
            //包、类名匹配
            Map<String, MethodDoc> methodDocMap = methodDocList.stream().collect(
                    Collectors.toMap(MethodDoc::name, Function.identity(), (key1, key2) -> key2));

            Method[] methods = aClass.getDeclaredMethods();
            for (int i=0;i<methods.length;i++){
                Method method = methods[i];
                MethodDoc methodDoc = methodDocMap.get(method.getName());
                ApiDoc apiDoc = new ApiDoc();
                RequestMapping mRequestMapping = method.getAnnotation(RequestMapping.class);
                String requestMethod ="GET,POST";
                if (mRequestMapping!=null){
                    //获取请求方法
                    RequestMethod[] requestMethods = mRequestMapping.method();
                    if (requestMethods!=null&&requestMethods.length==1){
                        requestMethod = mRequestMapping.method()[0].name();
                    }
                    StringBuilder url = new StringBuilder(classUrl);
                    String methodPath = mRequestMapping.value()[0];
                    if (!methodPath.startsWith("/")&& !url.toString().endsWith("/")){
                        methodPath = "/"+methodPath;
                    }
                    url.append(methodPath);
                    boolean hasTheMethodVersion = DocTagUtil.hasTheVersion(methodDoc,GlobalConstants.VERSION);
                    apiDoc.setHasVersion(hasTheClassVersion||hasTheMethodVersion);
                    //System.out.println(url.toString()+"-描述："+ DocTagUtil.getDescriptionTagText(methodDoc));
                    apiDoc.setUrl(url.toString());//url
                    String desc = methodDoc.commentText()+ DocTagUtil.getDescriptionTagText(methodDoc);
                    apiDoc.setDescription(desc);//描述
                    apiDoc.setMethod(requestMethod);
                    apiDoc.setAuthor(DocTagUtil.getAuthorTagText(methodDoc));
                    List<ParamDto> params = getRequestParam(method,methodDoc);
                    apiDoc.setRequestParam(params);
                    List<ParamDto> ret = getResponseParam(method);
                    apiDoc.setResponseParam(ret);
                    // System.out.println("getCanonicalName=="+returnType.getCanonicalName());
                    apiDocList.add(apiDoc);
                }
            }
        }
        return apiDocList;
    }

    private static List<ParamDto> getRequestParam(Method method, MethodDoc methodDoc) {
        //参数名：注释
        Map<String, String> paramMap = DocTagUtil.getParamTagText(methodDoc);
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = DocMethodUtil.getParameterNames(methodDoc);
        String[] parameterRequireArr = DocMethodUtil.getParameterRequireArr(method);
        if (parameters==null||parameters.length==0)return null;
        List<ParamDto> params = new ArrayList<>();
        int i=0;
        for (Parameter parameter : parameters) {
            //System.out.println(parameter);
            Class<?> clazz = parameter.getType();
            String parameterName = parameterNames[i];
            String parameterTypeName = clazz.getSimpleName();
            if (!parameterTypeName.endsWith("HttpServletRequest")){
                ParamDto paramDto;
                String paramDesc = StringUtils.isEmpty(paramMap.get(parameterName))?parameterName : paramMap.get(parameterName);
                //基本类型或map或math类
                if (DocClassUtil.isAtomicType(clazz)){
                    paramDto = new ParamDto();
                    paramDto.setCode(parameterName);
                    paramDto.setName(paramDesc);
                    paramDto.setRequire(parameterRequireArr[i]);
                    paramDto.setType(parameterTypeName);
                    paramDto.setDescription(paramDesc);
                    params.add(paramDto);
                }else {
                    //对象参数解析到字段
                    boolean isValid = DocMethodUtil.isValid(method, i);
                    for(Field f  : clazz.getDeclaredFields()){
                        AtomicInteger count = new AtomicInteger(0);
                        resolveClassParam(params,f,parameterName,count,clazz.getName(),false,isValid);
                    }
                }
            }
            i++;
        }
        //System.out.println("list="+params.toString());
        return params;
    }

    /**
     * 获取返回参数列表
     * @param method
     * @return
     */


    private static List<ParamDto> getResponseParam(Method method) {
        List<ParamDto> params = new ArrayList<>();
        ParamDto paramDto;
        //返回是否泛型
        Type genericReturnType = method.getGenericReturnType();
        try {
            String typeNameTemp = genericReturnType.getTypeName();
            //System.out.println("------returnType------"+typeNameTemp);
            if ("void".equals(typeNameTemp))return params;
            //递归
            while (!StringUtils.isEmpty(typeNameTemp)&& DocClassUtil.canOpen(typeNameTemp)){//可分解
                Class<?> outClass =DocClassUtil.getNoGenericType(typeNameTemp);
                //先判断外面类是否可分解
                if (DocClassUtil.isAtomicType(outClass)){//原子的
                    paramDto = new ParamDto();
                    paramDto.setCode(outClass.getSimpleName());
                    paramDto.setName(outClass.getSimpleName());
                    paramDto.setType(outClass.getSimpleName());
                    paramDto.setDescription("无");
                    params.add(paramDto);
                }else {
                    String parentNode = GlobalConstants.CLASS_RESPONSE.equals(outClass.getName())?null:"result";
                    //拆开
                    for(Field f  : outClass.getDeclaredFields()){
                        AtomicInteger level = new AtomicInteger(0);
                        resolveClassParam(params,f,parentNode,level, outClass.getName(),true,false);
                    }
                }
                //递归处理泛型定义的类
                typeNameTemp = DocClassUtil.getGenericImplTypeName(typeNameTemp);
                if (StringUtils.isEmpty(typeNameTemp))break;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //System.out.println("retParams="+params.toString());
        return params;
    }


    /**
     * 使用递归方式将自定义的对象的属性逐层展开，
     * 由于解嵌套的可能引起栈溢出，故设置了最大递归深度。
     * @param params
     * @param field
     * @param parentNode
     * @param level
     * @param className
     * @param showFlag
     */
    private static void resolveClassParam(List<ParamDto> params, Field field, String parentNode,
                                          AtomicInteger level, String className,boolean showFlag,boolean isValid) {
        level.addAndGet(1);//进入方法递归深度加1
        if (level.get()>GlobalConstants.MAX_DEPTH||"serialVersionUID".equals(field.getName())){
            //超过最大递归深度退出
            level.decrementAndGet();
            return;
        }

        Class<?> clazz = field.getType();
        String parameterTypeName = clazz.getSimpleName();
        ParamDto paramDto;
        if (DocClassUtil.isAtomicType(clazz)){
            paramDto = buildParam(field,parentNode,level,parameterTypeName,className,isValid);
            params.add(paramDto);
        }
        //解析泛型
        ResolvableType resolvableType = ResolvableType.forField(field);
        if (resolvableType.hasGenerics()&&resolvableType.getGenerics().length==1){//有泛型且只有一个
            //不考虑集合嵌套集合情况
            Class<?> clazz0 = resolvableType.resolveGeneric(0);
            if (!DocClassUtil.isAtomicType(clazz0)){//可解析
                for (Field f : clazz0.getDeclaredFields()) {
                    resolveClassParam(params,f,field.getName(), level, clazz0.getName(),showFlag,false);
                }
            }

        }else if (!DocClassUtil.isAtomicType(clazz)){
            if (showFlag){//在参数列表中展示自定义对象名
                paramDto = buildParam(field,parentNode,level,parameterTypeName,className,isValid);
                params.add(paramDto);
            }
            for (Field f : clazz.getDeclaredFields()) {
                resolveClassParam(params,f,field.getName(), level, clazz.getName(),showFlag,false);
            }
        }
        level.decrementAndGet();
        //System.out.println("====count_out=="+level.toString());
    }

    /**
     * 构建一个参数对象
     * @param field
     * @param parentNode
     * @param level
     * @param simpleTypeName
     * @param className
     * @return
     */
    private static ParamDto buildParam(Field field, String parentNode, AtomicInteger level, String simpleTypeName, String className,Boolean isValid) {
        ParamDto paramDto = new ParamDto();
        //特殊处理
        String commentText;
        if (className.startsWith(GlobalConstants.CLASS_RESPONSE) ){
          //  System.out.println("-------------包名替换----------");
            className = GlobalConstants.CLASS_RESPONSE_REPLACE;
        }
        Map<String,FieldDoc> fieldMap = getParamPojoFieldDocMap(className);
        if (fieldMap==null){
            commentText =null;
        }else {
            commentText = fieldMap.get(className+"."+field.getName()).commentText();
        }
        String defaultRequire = "N";
        if (isValid){
            defaultRequire = DocFieldUtil.getFieldRequire(field);
        }

        paramDto.setCode(field.getName());
        paramDto.setName(commentText);
        paramDto.setRequire(defaultRequire);
        paramDto.setType(simpleTypeName);
        paramDto.setDescription(commentText);
        paramDto.setParentNode(parentNode);
        paramDto.setLevel(level.get());
        return paramDto;
    }

    public static List<ApiDoc> getSortedDocList(){
        List<ApiDoc> apiDocList0 = getInterfaceDocList(GlobalConstants.PACK_CONTROLLER);
        List<ApiDoc> apiDocList = apiDocList0.stream().filter(apiDoc -> apiDoc.getHasVersion()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(apiDocList)){
            return new ArrayList<>();
        }
        apiDocList.sort(Comparator.comparing(ApiDoc::getUrl));//排序
        return apiDocList;
    }



}
