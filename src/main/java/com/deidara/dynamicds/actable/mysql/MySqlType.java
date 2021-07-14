package com.deidara.dynamicds.actable.mysql;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class MySqlType {

    public final static String SMALLINT = "SMALLINT";
    public final static String INT = "INT";
    public final static String BIGINT = "BIGINT";
    public final static String FLOAT = "FLOAT";
    public final static String DOUBLE = "DOUBLE";
    public final static String BLOB = "BLOB";
    public final static String TIMESTAMP = "TIMESTAMP";
    public final static String BIT = "BIT";

    public final static String CHAR = CHAR(200);
    public final static String VARCHAR = VARCHAR(200);
    public final static String DECIMAL = DECIMAL(10,10);


    public static String CHAR(int length){
        return "CHAR("+length+")";
    }

    public static String VARCHAR(int length){
        return "VARCHAR("+length+")";
    }

    public static String DECIMAL(int length, int digits){
        return "DECIMAL("+length+","+digits+")";
    }


    public static String convert(Class<?> type) {
        if(type == String.class){
            return VARCHAR;
        }else if(type == BigDecimal.class){
            return DECIMAL;
        }else if(type == Integer.class || type == int.class){
            return INT;
        }else if(type == Short.class || type == short.class){
            return SMALLINT;
        }else if(type == Long.class || type == long.class){
            return BIGINT;
        }else if(type == Float.class || type == float.class){
            return FLOAT;
        }else if(type == Double.class || type == double.class){
            return DOUBLE;
        }else if(type == Boolean.class || type == boolean.class){
            return BIT;
        }else if(type == Date.class || type == Timestamp.class || type == java.sql.Date.class){
            return TIMESTAMP;
        }else if(type == Byte[].class || type == byte[].class){
            return BLOB;
        }else{
            throw new RuntimeException("没有对应的数据类型："+type.getName());
        }
    }

    public static String convert(String jdbcType) {
        switch(jdbcType.toLowerCase()) {
            case "char":
                return CHAR;
            case "varchar":
                return VARCHAR;
            case "decimal":
                return DECIMAL;
            default:
                return jdbcType.toUpperCase();
        }
    }

}
