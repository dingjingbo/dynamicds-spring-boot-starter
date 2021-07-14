package com.deidara.dynamicds.aop;

import com.deidara.dynamicds.annotation.DataSource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class DynamicDataSourceAspect {

    @Before("@within(com.deidara.dynamicds.annotation.DataSource) || @annotation(com.deidara.dynamicds.annotation.DataSource)")
    public void beforeSwitchDS(JoinPoint point){
        //获得当前访问的class
        Class<?> clazz = point.getSignature().getDeclaringType();
        String dataSource = null;
        if(clazz.isAnnotationPresent(DataSource.class)){
            DataSource annotation = clazz.getAnnotation(DataSource.class);
            //取出类注解中的数据源名
            dataSource = annotation.value();
        }
        //获得访问的方法名
        String methodName = point.getSignature().getName();
        //得到方法的参数的类型
        Class<?>[] argClasses = ((MethodSignature)point.getSignature()).getParameterTypes();
        try {
            //得到访问的方法对象
            Method method = clazz.getMethod(methodName, argClasses);
            //判断是否存在注解
            if (method.isAnnotationPresent(DataSource.class)) {
                DataSource annotation = method.getAnnotation(DataSource.class);
                //取出注解中的数据源名
                dataSource = annotation.value();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 切换数据源
        DataSourceContextHolder.setCurrentDataSource(dataSource);
    }

    @After("@within(com.deidara.dynamicds.annotation.DataSource) || @annotation(com.deidara.dynamicds.annotation.DataSource)")
    public void afterSwitchDS(JoinPoint point){
        DataSourceContextHolder.clear();
    }

}
