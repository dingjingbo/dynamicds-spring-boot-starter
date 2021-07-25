package com.deidara.dynamicds.actable;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deidara.dynamicds.actable.annotation.Column;
import com.deidara.dynamicds.actable.annotation.Table;
import com.deidara.dynamicds.actable.annotation.Transient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Slf4j
public abstract class AbstractTableDesc {

    private String name;
    private String comment = "";
    private List<AbstractColumnDesc> columns = new ArrayList<>();


    private static boolean isImportMybatisPlus;

    static {
        try {
            Class.forName("com.baomidou.mybatisplus.annotation.TableName");
            isImportMybatisPlus = true;
        } catch (Exception e) {
            isImportMybatisPlus = false;
        }
    }


    public AbstractTableDesc(Class<?> clazz){

        if(!clazz.isAnnotationPresent(Table.class) && (!isImportMybatisPlus || !clazz.isAnnotationPresent(TableName.class))){
            throw new RuntimeException("初始化错误，" + clazz.getName() + "没有被Table或TableName注解标注");
        }

        if(clazz.isAnnotationPresent(Table.class)){
            Table tableAnno = clazz.getAnnotation(Table.class);
            if ("".equals(tableAnno.value().trim())){
                this.setName(StrUtil.toUnderlineCase(clazz.getSimpleName()));
            }else {
                this.setName(tableAnno.value().trim());
            }
            this.setComment(tableAnno.comment().trim());
        }

        if(isImportMybatisPlus && clazz.isAnnotationPresent(TableName.class)){
            TableName tableAnno = clazz.getAnnotation(TableName.class);
            if ("".equals(tableAnno.value().trim())){
                this.setName(StrUtil.toUnderlineCase(clazz.getSimpleName()));
            }else {
                this.setName(tableAnno.value().trim());
            }
        }

        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superclass = clazz.getSuperclass();
        if(superclass != Object.class){
            fields.addAll(Arrays.asList(superclass.getDeclaredFields()));
        }
        for(Field field: fields){
            if(field.isAnnotationPresent(Transient.class)) continue;
            if(isImportMybatisPlus && field.isAnnotationPresent(TableField.class) && !field.getAnnotation(TableField.class).exist()) continue;
            AbstractColumnDesc tableColumn = getColumnDesc();
            tableColumn.setTableName(this.getName());
            tableColumn.setColumnName(StrUtil.toUnderlineCase(field.getName()));
            tableColumn.setColumnType(convert2SqlType(field.getType()));
            if(field.isAnnotationPresent(Column.class)){
                Column columnAnno = field.getAnnotation(Column.class);
                if (!"".equals(columnAnno.name().trim())){
                    tableColumn.setColumnName(columnAnno.name().trim());
                }
                if (!"".equals(columnAnno.type().trim())){
                    tableColumn.setColumnType(columnAnno.type().trim().replace(" ", "").toUpperCase());
                }
                if(columnAnno.autoUpdate() && (field.getType() == Timestamp.class || field.getType() == Date.class || field.getType() == Date.class)){
                    tableColumn.setAutoUpdate(columnAnno.autoUpdate());
                }
                tableColumn.setComment(columnAnno.comment().trim());
                tableColumn.setNullable(columnAnno.nullable());
                if(columnAnno.primaryKey()){
                    tableColumn.setPrimaryKey(true);
                    tableColumn.setNullable(false);
                }
                if(!"null".equalsIgnoreCase(columnAnno.defaultValue()) && field.getType() == String.class){
                    tableColumn.setDefaultValue("'"+columnAnno.defaultValue()+"'");
                }else{
                    tableColumn.setDefaultValue(columnAnno.defaultValue());
                }
            }
            if(isImportMybatisPlus && field.isAnnotationPresent(TableField.class)){
                TableField columnAnno = field.getAnnotation(TableField.class);
                if (!"".equals(columnAnno.value().trim())){
                    tableColumn.setColumnName(columnAnno.value().trim());
                }
                if (!"UNDEFINED".equals(columnAnno.jdbcType().name())){
                    tableColumn.setColumnType(convert2SqlType(columnAnno.jdbcType().name()));
                }
            }
            if(isImportMybatisPlus && field.isAnnotationPresent(TableId.class)){
                TableId columnAnno = field.getAnnotation(TableId.class);
                if (!"".equals(columnAnno.value().trim())){
                    tableColumn.setColumnName(columnAnno.value().trim());
                }
                tableColumn.setPrimaryKey(true);
                tableColumn.setNullable(false);
            }

            if(tableColumn.isAutoUpdate() && !tableColumn.isTimeType()){
                tableColumn.setAutoUpdate(false);
            }

            this.getColumns().add(tableColumn);
        }

        if(!this.getColumns().stream().anyMatch(x -> x.isPrimaryKey())){
            throw new RuntimeException("初始化错误" + clazz.getName() + "没有标识主键");
        }
    }


    public abstract String convert2SqlType(String type);

    public abstract String convert2SqlType(Class<?> type);

    public abstract AbstractColumnDesc getColumnDesc();

    public abstract String getCreateTableDDL();

    public abstract String getDropTableDDL();

    /**
     * 将table对象映射为表
     * @param connection
     * @param autoType
     */
    public void map2Table(Connection connection, String autoType) {
        Statement st = null;
        ResultSet tablesRS = null;
        ResultSet primaryKeysRS = null;
        ResultSet columnsRS = null;
        try {
            if("none".equalsIgnoreCase(autoType)) return;
            List<String> updateDDLs = new ArrayList<>();
            if("create".equalsIgnoreCase(autoType)){
                updateDDLs.add(this.getDropTableDDL());
                updateDDLs.add(this.getCreateTableDDL());
            }else{//update
                DatabaseMetaData metaData = connection.getMetaData();
                tablesRS = metaData.getTables(null, null, this.getName(), null);
                if(tablesRS.next()){ //表存在
                    primaryKeysRS = metaData.getPrimaryKeys(null, null, this.getName());
                    List<String> pks = new ArrayList<>();
                    while(primaryKeysRS.next()){
                        String columnName = primaryKeysRS.getString("COLUMN_NAME");
                        pks.add(columnName);
                    }
                    columnsRS = metaData.getColumns(connection.getCatalog(), "%", this.getName(), null);
                    List<AbstractColumnDesc> columnDescs = new ArrayList<>();
                    while(columnsRS.next()){

                        /*int columnCount = columnsRS.getMetaData().getColumnCount();
                        System.out.println("------------------------------------------------------------------------");
                        for(int i = 1; i<=columnCount; i++) {
                            String columnName = columnsRS.getMetaData().getColumnName(i);
                            String string = columnsRS.getString(columnName);
                            System.out.println(columnName+": "+string);
                        }*/

                        String name = columnsRS.getString("COLUMN_NAME");
                        String type = columnsRS.getString("TYPE_NAME");
                        int columnSize = columnsRS.getInt("COLUMN_SIZE");
                        int decimalDigits = columnsRS.getInt("DECIMAL_DIGITS");
                        if("CHAR".equalsIgnoreCase(type) || "VARCHAR".equalsIgnoreCase(type)){
                            type = type + "(" + columnSize + ")";
                        }
                        if("DECIMAL".equalsIgnoreCase(type)){
                            type = type + "(" + columnSize + "," + decimalDigits + ")";
                        }
                        String comment = columnsRS.getString("REMARKS");
                        if(comment == null){
                            comment = "";
                        }

                        String defaultValue = Optional.ofNullable(columnsRS.getString("COLUMN_DEF")).orElse("NULL");
                        boolean nullable = "YES".equals(columnsRS.getString("IS_NULLABLE"));
                        boolean isPrimaryKey = pks.contains(name);
                        AbstractColumnDesc columnDesc = getColumnDesc();
                        columnDesc.setTableName(this.getName());
                        columnDesc.setColumnName(name);
                        columnDesc.setColumnType(type);
                        columnDesc.setPrimaryKey(isPrimaryKey);
                        columnDesc.setNullable(nullable);
                        columnDesc.setDefaultValue(defaultValue);
                        columnDesc.setComment(comment);
                        columnDescs.add(columnDesc);
                    }
                    this.getColumns().forEach(x->{
                        List<AbstractColumnDesc> collect = columnDescs.stream().filter(y -> y.getColumnName().equals(x.getColumnName())).collect(Collectors.toList());
                        if(collect.size() > 0){
                            AbstractColumnDesc columnDesc = collect.get(0);
                            if(!columnDesc.isSame(x) || x.isTimeType()){
                                //字段被修改
                                updateDDLs.add(x.getAlterColumnDDL());
                            }
                        }else{//新增字段
                            updateDDLs.add(x.getAddColumnDDL());
                        }
                    });
                    List<String> collect = this.getColumns().stream().map(y -> y.getColumnName()).collect(Collectors.toList());
                    columnDescs.stream().filter(x -> !collect.contains(x.getColumnName())).forEach(x -> {
                        updateDDLs.add(x.getDropColumnDDL());
                    });
                }else{
                    updateDDLs.add(this.getCreateTableDDL());
                }
            }
            //创建或修改表
            st = connection.createStatement();
            for (String ddl : updateDDLs) {
                log.info(ddl);
                st.addBatch(ddl);
            }
            st.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            if(primaryKeysRS != null){
                try {
                    primaryKeysRS.close();
                } catch (Exception e) {
                    primaryKeysRS = null;
                }
            }
            if(columnsRS != null){
                try {
                    columnsRS.close();
                } catch (Exception e) {
                    columnsRS = null;
                }
            }
            if(tablesRS != null){
                try {
                    tablesRS.close();
                } catch (Exception e) {
                    tablesRS = null;
                }
            }
            if(st != null){
                try {
                    st.close();
                } catch (Exception e) {
                    st = null;
                }
            }
        }

    }

}
