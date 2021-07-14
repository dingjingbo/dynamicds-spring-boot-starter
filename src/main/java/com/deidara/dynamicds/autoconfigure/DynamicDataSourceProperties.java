package com.deidara.dynamicds.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "spring.datasource.dynamic")
public class DynamicDataSourceProperties {

    private String primary;

    private Map<String, DataSourceProperties> datasource;

    private ACTableProperties actable;

}
