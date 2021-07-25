package com.deidara.dynamicds.autoconfigure;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.deidara.dynamicds.actable.annotation.Table;
import com.deidara.dynamicds.actable.TableClassUtil;
import com.deidara.dynamicds.actable.mysql.MySqlTableDesc;
import com.deidara.dynamicds.aop.DataSourceContextHolder;
import com.deidara.dynamicds.aop.DynamicDataSourceAspect;
import com.deidara.dynamicds.datasource.DynamicDataSource;
import com.deidara.dynamicds.datasource.CustomDataHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Configuration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Import(DynamicDataSourceAspect.class)
@EnableConfigurationProperties({DynamicDataSourceProperties.class, DruidDataSourceProperties.class})
public class DynamicDataSourceAutoConfiguration {

    @Autowired
    private DynamicDataSourceProperties dynamicDataSourceProperties;

    @Autowired
    private DruidDataSourceProperties druidDataSourceProperties;

    @Bean
    public DataSource dataSource() {
        Map<Object, Object> dsMap = new HashMap<>();
        for(Map.Entry<String, DataSourceProperties> entry: dynamicDataSourceProperties.getDatasource().entrySet()){
            String dataSourceName = entry.getKey();
            DataSourceProperties dataSourceProperties = entry.getValue();
            try {
                Class<?> clazz = Class.forName(dataSourceProperties.getType());
                Object ds = clazz.newInstance();
                BeanUtils.copyProperties(dataSourceProperties, ds);
                dsMap.put(dataSourceName, ds);
            } catch (Exception e) {
                throw new RuntimeException("数据源配置错误：" + dataSourceName, e);
            }
        }
        Object defaultDataSource = dsMap.get(dynamicDataSourceProperties.getPrimary());
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        // 默认数据源
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        // 配置多数据源
        dynamicDataSource.setTargetDataSources(dsMap);
        return dynamicDataSource;
    }


    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.dynamic.actable", name = "enabled", havingValue = "true")
    public CustomDataHolder autoCreateTable(){
        DataSource dataSource = dataSource();
        String[] modelPacks = dynamicDataSourceProperties.getActable().getModelPacks();
        List<Class<?>> classes = TableClassUtil.loadTableClasses(modelPacks);

        Map<String, List<Class<?>>> groupedMap = classes.stream().collect(Collectors.groupingBy(clazz -> {
            String ds = "";
            if (clazz.isAnnotationPresent(Table.class)) {
                Table table = clazz.getAnnotation(Table.class);
                ds = table.ds().trim();
            }
            return ds;
        }));

        groupedMap.forEach((key, value) -> {
            Connection connection = null;
            try {
                if(!"".equals(key)){
                    DataSourceContextHolder.setCurrentDataSource(key);
                }
                for(Class<?> clazz: value){
                    connection = dataSource.getConnection();
                    String url =connection.getMetaData().getURL();
                    String dbType = url.split(":")[1];
                    if("mysql".equalsIgnoreCase(dbType)){
                        MySqlTableDesc mySqlTableDesc = new MySqlTableDesc(clazz);
                        mySqlTableDesc.map2Table(connection, dynamicDataSourceProperties.getActable().getAutoType());
                    }else{
                        throw new RuntimeException("不支持的自动建表数据源：" + key);
                    }
                }
                DataSourceContextHolder.clear();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if(connection != null){
                    try {
                        connection.close();
                    } catch (Exception e) {
                        connection = null;
                    }
                }
            }
        });

        return new CustomDataHolder(groupedMap);
    }


    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.druid.stat-view-servlet", name = "enabled", havingValue = "true")
    public ServletRegistrationBean<StatViewServlet> statViewServlet() {
        String urlPattern = druidDataSourceProperties.getStatViewServlet().getUrlPattern();
        String allow = druidDataSourceProperties.getStatViewServlet().getAllow();
        String deny = druidDataSourceProperties.getStatViewServlet().getDeny();
        String resetEnable = druidDataSourceProperties.getStatViewServlet().getResetEnable();
        String loginUsername = druidDataSourceProperties.getStatViewServlet().getLoginUsername();
        String loginPassword = druidDataSourceProperties.getStatViewServlet().getLoginPassword();
        ServletRegistrationBean<StatViewServlet> servletRegistrationBean = new ServletRegistrationBean<>(new StatViewServlet(), urlPattern);
        if(StringUtils.isNoneBlank(allow)){
            servletRegistrationBean.addInitParameter("allow", allow);
        }
        if(StringUtils.isNoneBlank(deny)){
            servletRegistrationBean.addInitParameter("deny", deny);
        }
        if(StringUtils.isNoneBlank(resetEnable)){
            servletRegistrationBean.addInitParameter("resetEnable", resetEnable);
        }
        if(StringUtils.isNoneBlank(loginUsername) && StringUtils.isNoneBlank(loginPassword)){
            servletRegistrationBean.addInitParameter("loginUsername", loginUsername);
            servletRegistrationBean.addInitParameter("loginPassword", loginPassword);
        }
        return servletRegistrationBean;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.druid.web-stat-filter", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<WebStatFilter> statFilter() {
        String urlPattern = druidDataSourceProperties.getWebStatFilter().getUrlPattern();
        String exclusions = druidDataSourceProperties.getWebStatFilter().getExclusions();
        FilterRegistrationBean<WebStatFilter> filterRegistrationBean = new FilterRegistrationBean<>(new WebStatFilter());
        if(StringUtils.isNoneBlank(urlPattern)){
            filterRegistrationBean.addUrlPatterns(urlPattern);
        }
        if(StringUtils.isNoneBlank(exclusions)){
            filterRegistrationBean.addInitParameter("exclusions", exclusions);
        }
        return filterRegistrationBean;
    }

}
