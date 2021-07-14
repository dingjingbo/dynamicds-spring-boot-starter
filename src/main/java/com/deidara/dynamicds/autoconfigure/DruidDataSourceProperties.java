package com.deidara.dynamicds.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.datasource.druid")
public class DruidDataSourceProperties {

    private String filters;

    private WebStatFilterProperties webStatFilter;

    private StatViewServletProperties statViewServlet;

}
