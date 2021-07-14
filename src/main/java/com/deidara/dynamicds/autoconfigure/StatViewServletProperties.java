package com.deidara.dynamicds.autoconfigure;

import lombok.Data;

@Data
public class StatViewServletProperties {
    private String urlPattern;
    private String allow;
    private String deny;
    private String resetEnable;
    private String loginUsername;
    private String loginPassword;
}
