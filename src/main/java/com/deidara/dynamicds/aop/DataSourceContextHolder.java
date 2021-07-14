package com.deidara.dynamicds.aop;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataSourceContextHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    /**
     * 设置当前数据源名称
     * @param dsName
     */
    public static void setCurrentDataSource(String dsName) {
        log.info("切换到{}数据源", dsName);
        contextHolder.set(dsName);
    }

    /**
     * 获取当前数据源名称
     * @return
     */
    public static String getCurrentDataSource() {
        return contextHolder.get();
    }

    /**
     * 清空当前线程上下文
     */
    public static void clear() {
        contextHolder.remove();
    }

}
