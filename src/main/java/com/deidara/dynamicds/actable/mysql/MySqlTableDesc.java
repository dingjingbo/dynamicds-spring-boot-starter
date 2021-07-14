package com.deidara.dynamicds.actable.mysql;

import com.deidara.dynamicds.actable.AbstractColumnDesc;
import com.deidara.dynamicds.actable.AbstractTableDesc;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class MySqlTableDesc extends AbstractTableDesc {

    public MySqlTableDesc(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public String convert2SqlType(String type) {
        return MySqlType.convert(type);
    }

    @Override
    public String convert2SqlType(Class<?> type) {
        return MySqlType.convert(type);
    }

    @Override
    public AbstractColumnDesc getColumnDesc() {
        return new MySqlColumnDesc();
    }

    @Override
    public String getCreateTableDDL() {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS `");
        sb.append(this.getName()+"`(\n");
        for(AbstractColumnDesc fs : this.getColumns()){
            sb.append("    ");
            sb.append(fs.getCreateTableColumnDDL());
            sb.append(",\n");
        }
        List<String> collect = this.getColumns().stream().filter(x -> x.isPrimaryKey()).map(x -> "`" + x.getColumnName() + "`").collect(Collectors.toList());
        String pks = StringUtils.join(collect, ",");
        sb.append("    PRIMARY KEY ("+pks+")\n");
        sb.append(")");
        sb.append(" COMMENT '"+this.getComment()+"'\n");
        return  sb.toString();
    }

    @Override
    public String getDropTableDDL() {
        return "DROP TABLE IF EXISTS `" + this.getName() + "`;";
    }
}
