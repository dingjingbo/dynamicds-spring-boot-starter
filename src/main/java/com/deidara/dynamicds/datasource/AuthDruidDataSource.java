package com.deidara.dynamicds.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.deidara.hutool.AppUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.security.PrivilegedExceptionAction;
import java.sql.SQLException;

@Slf4j
public class AuthDruidDataSource extends DruidDataSource {

    private String authType;
    private String authUser;
    private String keytabFile;

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    public String getKeytabFile() {
        return keytabFile;
    }

    public void setKeytabFile(String keytabFile) {
        this.keytabFile = AppUtil.getConfFile(keytabFile);
    }

    public DruidPooledConnection superGetConnection(long maxWaitMillis) throws SQLException {
        return super.getConnection(maxWaitMillis);
    }

    @Override
    public DruidPooledConnection getConnection(long maxWaitMillis) throws SQLException {
        try {
            log.info("keytabFile------------------------------:"+keytabFile);
            if ("kerberos".equalsIgnoreCase(authType)){
                Configuration conf = new Configuration();
                conf.set("hadoop.security.authentication", "Kerberos");
                UserGroupInformation.setConfiguration(conf);
                UserGroupInformation ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(authUser, keytabFile);
                AuthDruidDataSource _this = this;
                DruidPooledConnection conn = ugi.doAs(new PrivilegedExceptionAction<DruidPooledConnection>() {
                    public DruidPooledConnection run() {
                        DruidPooledConnection tcon = null;
                        try {
                            // 父类的getConnection(long)方法
                            tcon = _this.superGetConnection(maxWaitMillis);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return tcon;
                    }
                });
                return conn;
            }else{
                return superGetConnection(maxWaitMillis);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
