package com.deidara.dynamicds.autoconfigure;

import lombok.Data;

import java.util.Properties;

@Data
public class DataSourceProperties {

    private String username;
    private String password;
    private String url;
    private String driverClassName;
    private String type;
    private String authType;
    private String authUser;
    private String keytabFile;
    private Integer initialSize;
    private Integer minIdle;
    private Integer maxActive;
    private Integer maxWait;
    private String filters;
    private Properties connectProperties;

}
