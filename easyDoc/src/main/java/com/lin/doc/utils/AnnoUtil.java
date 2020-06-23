package com.lin.doc.utils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AnnoUtil {

    private static Set<String> requireSet = new HashSet<>(Arrays.asList(new String[]{"NotEmpty","NotNull"}));
    private static Set<String> validSet = new HashSet<>(Arrays.asList(new String[]{"Valid"}));


    public static boolean isRequired(Annotation[] annotations){
        for (int i=0;i<annotations.length ; i++){
            Class<? extends Annotation> clazz = annotations[i].annotationType();
            if (requireSet.contains(clazz.getSimpleName()))return true;
        }
        return false;
    }

    public static boolean hasValid(Annotation[] annotations){
        for (int i=0;i<annotations.length ; i++){
            Class<? extends Annotation> clazz = annotations[i].annotationType();
            if (validSet.contains(clazz.getSimpleName()))return true;
        }
        return false;
    }

}
