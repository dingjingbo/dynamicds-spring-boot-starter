package com.deidara.dynamicds.actable;

import cn.hutool.core.util.ClassUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deidara.dynamicds.actable.annotation.Table;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TableClassUtil {

    private static boolean isImportMybatisPlus;

    static {
        try {
            Class.forName("com.baomidou.mybatisplus.annotation.TableName");
            isImportMybatisPlus = true;
        } catch (Exception e) {
            isImportMybatisPlus = false;
        }
    }

    /**
     * 获取实体类直接骂列表
     * @param basePackages
     * @return
     */
    public static List<Class<?>> loadTableClasses(String ... basePackages){
        return Arrays.stream(basePackages).map(basePackage -> ClassUtil.scanPackage(basePackage, x -> x.isAnnotationPresent(Table.class) || (isImportMybatisPlus && x.isAnnotationPresent(TableName.class))))
                .flatMap(Collection::stream).distinct().collect(Collectors.toList());
    }

}
