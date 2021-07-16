package com.deidara.dynamicds.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.File;
import java.io.FileOutputStream;
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
        try {
            if(keytabFile.startsWith("classpath:")){
                String path = keytabFile.split("classpath:")[1].trim();
                String fileName = path.substring(path.lastIndexOf("/") + 1);
                File file = new File("/etc/keytab/");
                if(!file.exists()){
                    file.mkdirs();
                }
                keytabFile = "/etc/keytab/" + fileName;
                IOUtils.copy(AuthDruidDataSource.class.getClassLoader().getResourceAsStream(path), new FileOutputStream(keytabFile));
            }
            this.keytabFile = keytabFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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