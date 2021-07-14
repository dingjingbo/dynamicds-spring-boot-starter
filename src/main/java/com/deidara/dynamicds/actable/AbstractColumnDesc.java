package com.deidara.dynamicds.actable;

import lombok.Data;

@Data
public abstract class AbstractColumnDesc {
    private String tableName;
    private String columnName;
    private String columnType;
    private boolean primaryKey = false;
    private boolean nullable = true;
    private String defaultValue = "NULL";
    private boolean autoUpdate = false;
    private String comment = "";

    public abstract boolean isTimeType();

    public abstract String getAddColumnDDL();

    public abstract String getAlterColumnDDL();

    public abstract String getDropColumnDDL();

    public abstract String getCreateTableColumnDDL();

    public abstract boolean isSame(AbstractColumnDesc columnDesc);
}
