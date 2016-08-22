package org.apache.fineract.portfolio.clientaddress.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name="m_field_configuration")
public class FieldConfiguration extends AbstractPersistable<Long>{
    
   private String entity;
   
   private String table;
   
   private String field;
   
   private boolean is_enabled;
   
   public FieldConfiguration()
   {
       
   }
   
   private FieldConfiguration(String entity,String table,String field,boolean is_enabled)
   {
       this.entity=entity;
       this.table=table;
       this.field=field;
       this.is_enabled=is_enabled;
               
   }

   private static FieldConfiguration fromJson(final JsonCommand command)
   {
       final String entity = command.stringValueOfParameterNamed("entity");
       final String table = command.stringValueOfParameterNamed("table");
       final String field=command.stringValueOfParameterNamed("field");
       final boolean is_enabled=command.booleanPrimitiveValueOfParameterNamed("implementationKey");
       
       return new FieldConfiguration(entity,table,field,is_enabled);
   }
}
