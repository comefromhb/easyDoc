package com.lin.doc.utils;

import com.lin.doc.cloader.MyClassLoader;
import com.lin.doc.constants.GlobalConstants;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 获取包下的类
 */
public class ClassScanner implements ResourceLoaderAware {

    //保存过滤规则要排除的注解
    private final List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();
    private final List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();
    private static final String CLASS_PRE = "/target/classes/";
    private static MyClassLoader myClassLoader = new MyClassLoader(GlobalConstants.USER_DIR_PATH);

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    public static Set<Class> scan(String[] basePackages,
                                  Class<? extends Annotation>... annotations) {
        ClassScanner cs = new ClassScanner();

        if(annotations != null) {
            for (Class anno : annotations) {
                cs.addIncludeFilter(new AnnotationTypeFilter(anno));
            }
        }

        Set<Class> classes = new HashSet<Class>();
        for (String s : basePackages)
            classes.addAll(cs.doScan(s));
        return classes;
    }

    public static Set<Class> scan(String basePackages, Class<? extends Annotation>... annotations) {
        return ClassScanner.scan(StringUtils.tokenizeToStringArray(basePackages, ",; \t\n"), annotations);
    }


    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils
                .getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory = new CachingMetadataReaderFactory(
                resourceLoader);
    }

    public void addIncludeFilter(TypeFilter includeFilter) {
        this.includeFilters.add(includeFilter);
    }

    public void addExcludeFilter(TypeFilter excludeFilter) {
        this.excludeFilters.add(excludeFilter);
    }

    public void resetFilters(boolean useDefaultFilters) {
        this.includeFilters.clear();
        this.excludeFilters.clear();
    }
    private static boolean isLocalPro(){
        return StringUtils.isEmpty(GlobalConstants.PROJ_DIR);
    }

    public static Class loadClass(String name) throws ClassNotFoundException {
        if (isLocalPro()){
            return Class.forName(name);
        }else {
            //自定义的类加载器
            return myClassLoader.loadClass(name);
        }
    }

    public Set<Class> doScan(String basePackage) {
        Set<Class> classes = new HashSet<Class>();
        if (StringUtils.isEmpty(GlobalConstants.PROJ_DIR)&&StringUtils.isEmpty(GlobalConstants.OPERATION_CONTROLLER_SRC)){
            throw new NullPointerException("OPERATION_CONTROLLER_SRC not found");
        }
        String moduleDir = isLocalPro()?null :GlobalConstants.OPERATION_CONTROLLER_SRC;
        try {
            Set<String> fullNames = getClassFullNameSet(moduleDir, new String[]{basePackage});
            try {
                for (String fullName : fullNames){
                    classes.add(loadClass(fullName));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException(
                    "I/O failure during classpath scanning", ex);
        }
        return classes;
    }

    private static String getPackageSearchPath(String moduleDir ,String basePackage) {
        String prefix=StringUtils.isEmpty(moduleDir)? ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                :("file:"+moduleDir.replace(GlobalConstants.SRC_PRE,CLASS_PRE));
        //空，表示当前项目
        String packageSearchPath = prefix
                + ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils
                .resolvePlaceholders(basePackage))
                + "/**/*".concat(".class");
        System.out.println("packPath="+packageSearchPath);
        return packageSearchPath;
    }

    public static Set<String> getClassFullNameSet(String moduleDir,String[] packageSearchPaths) throws IOException {
        Set<String> nameSet = new HashSet<>();
        ClassScanner cs = new ClassScanner();
        for (String basePackage: packageSearchPaths){
            String packageSearchPath = getPackageSearchPath(moduleDir,basePackage);
            Resource[] resources = cs.resourcePatternResolver.getResources(packageSearchPath);
            for (int i = 0; i < resources.length; i++) {
                Resource resource = resources[i];
                if (resource.isReadable()) {
                    MetadataReader metadataReader = cs.metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();
                    nameSet.add(className);
                }
            }
        }
        return nameSet;
    }

    protected boolean matches(MetadataReader metadataReader) throws IOException {
        for (TypeFilter tf : this.excludeFilters) {
            if (tf.match(metadataReader, this.metadataReaderFactory)) {
                return false;
            }
        }
        for (TypeFilter tf : this.includeFilters) {
            if (tf.match(metadataReader, this.metadataReaderFactory)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        //System.setProperty("user.dir","D:\\mall3\\eslink_operation_mall");
        //String packageSearchPath = "";
        //String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX+"/cc/eslink/mall/domain/**/*.class";
        //String packageSearchPath = "file:D:/mall3/eslink_operation_mall/operation-dao/target/classes/cc/eslink/mall/domain/**/*.class";

        //Set<String> sset = getClassFullNameSet(GlobalConstants.OPERATION_DAO_SRC,StringUtils.tokenizeToStringArray(GlobalConstants.PACK_DOMAINS, ",; \t\n"));
        Set<Class> aClasses = scan(GlobalConstants.PACK_CONTROLLER, RestController.class);
//        for (String str :sset){
//            System.out.println(str);
//        }
        for (Class clazz:aClasses){
            System.out.println(clazz.getName());
        }

    }

}

