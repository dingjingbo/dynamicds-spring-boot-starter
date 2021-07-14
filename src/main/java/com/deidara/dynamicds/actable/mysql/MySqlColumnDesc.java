package com.deidara.dynamicds.actable.mysql;


import com.deidara.dynamicds.actable.AbstractColumnDesc;

import java.util.Objects;

public class MySqlColumnDesc extends AbstractColumnDesc {


    @Override
    public boolean isTimeType() {
        return "timestamp".equalsIgnoreCase(this.getColumnType()) || "datetime".equalsIgnoreCase(this.getColumnType());
    }

    @Override
    public String getAddColumnDDL() {
        String nullable = this.isNullable() ? " NULL" : " NOT NULL";
        String defaultValue = " DEFAULT " + this.getDefaultValue();
        String onUpdate = this.isAutoUpdate() && isTimeType() ? " ON UPDATE CURRENT_TIMESTAMP" : "";
        return "ALTER TABLE `" + this.getTableName() + "` ADD COLUMN `" + this.getColumnName() + "` " + this.getColumnType() + (this.isPrimaryKey()? "" : nullable + defaultValue + onUpdate) + " COMMENT '" + this.getComment() + "';";
    }

    @Override
    public String getAlterColumnDDL() {
        String nullable = this.isNullable() ? " NULL" : " NOT NULL";
        String defaultValue = " DEFAULT " + this.getDefaultValue();
        String onUpdate = this.isAutoUpdate() && isTimeType() ? " ON UPDATE CURRENT_TIMESTAMP" : "";
        return "ALTER TABLE `" + this.getTableName() + "` MODIFY COLUMN `" + this.getColumnName() + "` " + this.getColumnType() + (this.isPrimaryKey()? "" : nullable + defaultValue + onUpdate) + " COMMENT '" + this.getComment() + "';";
    }

    @Override
    public String getDropColumnDDL() {
        return "ALTER TABLE `" + this.getTableName() + "` DROP COLUMN `" + this.getColumnName() + "`;";
    }

    @Override
    public String getCreateTableColumnDDL() {
        String nullable = this.isNullable() ? " NULL" : " NOT NULL";
        String defaultValue = " DEFAULT " + this.getDefaultValue();
        String onUpdate = this.isAutoUpdate() && isTimeType() ? " ON UPDATE CURRENT_TIMESTAMP" : "";
        return "`" + this.getColumnName() + "` " + this.getColumnType() + (this.isPrimaryKey()? "" : nullable + defaultValue + onUpdate) + " comment '" + this.getComment() + "'";
    }

    @Override
    public boolean isSame(AbstractColumnDesc columnDesc) {
        if (this == columnDesc) return true;
        if (columnDesc == null || getClass() != columnDesc.getClass()) return false;
        MySqlColumnDesc that = (MySqlColumnDesc) columnDesc;
        return isPrimaryKey() == that.isPrimaryKey() &&
                isNullable() == that.isNullable() &&
                Objects.equals(getColumnName(), that.getColumnName()) &&
                getColumnType().equalsIgnoreCase(that.getColumnType()) &&
                Objects.equals(getComment(), that.getComment()) &&
                Objects.equals(getDefaultValue(), that.getDefaultValue());
    }

}
