package org.apache.fineract.portfolio.clientaddress.data;


public class FieldConfigurationData 
{
private final long fieldConfigurationId;

private final String entity;

private final String table;

private final String field;

private final boolean is_enabled;

private final boolean is_mandatory;

private final String validation_regex;

private FieldConfigurationData(final long fieldConfigurationId,final String entity,final String table,final String field,
        final boolean is_enabled,final boolean is_mandatory,final String validation_regex)
{
    this.fieldConfigurationId=fieldConfigurationId;
    this.entity=entity;
    this.table=table;
    this.field=field;
    this.is_enabled=is_enabled;
    this.is_mandatory=is_mandatory;
    this.validation_regex=validation_regex;
}

public static FieldConfigurationData instance(final long fieldConfigurationId,final String entity,final String table,final String field,
        final boolean is_enabled,final boolean is_mandatory,final String validation_regex)
{
    return new FieldConfigurationData(fieldConfigurationId,entity,table,field,is_enabled,
            is_mandatory,validation_regex);
}


public long getFieldConfigurationId() {
    return this.fieldConfigurationId;
}


public String getEntity() {
    return this.entity;
}


public String getTable() {
    return this.table;
}


public String getField() {
    return this.field;
}


public boolean isIs_enabled() {
    return this.is_enabled;
}


public boolean isIs_mandatory() {
    return this.is_mandatory;
}


public String getValidation_regex() {
    return this.validation_regex;
}



}
