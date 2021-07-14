package com.deidara.dynamicds.autoconfigure;

import lombok.Data;

@Data
public class WebStatFilterProperties {
    private Boolean enabled;
    private String urlPattern;
    private String exclusions;
}
