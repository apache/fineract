package org.apache.fineract.portfolio.clientaddress.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.clientaddress.data.FieldConfigurationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class FieldConfigurationReadPlatformServiceImpl implements FieldConfigurationReadPlatformService 
{
    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public FieldConfigurationReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    private static final class FieldMapper implements RowMapper<FieldConfigurationData>
    {
        public String schema()
        {
            return "fld.id as fieldConfigurationId,fld.entity as entity,fld.table as entitytable,fld.field as field,fld.is_enabled as is_enabled,"+
    "fld.is_mandatory as is_mandatory,fld.validation_regex as validation_regex from m_field_configuration fld";
        }
        
        @Override
        public FieldConfigurationData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException
        {
            final long fieldConfigurationId=rs.getLong("fieldConfigurationId");
            final String entity=rs.getString("entity");
            final String entitytable=rs.getString("entitytable");
            final String field=rs.getString("field");
            final boolean is_enabled=rs.getBoolean("is_enabled");
            final boolean is_mandatory=rs.getBoolean("is_mandatory");
            final String validation_regex=rs.getString("validation_regex");
           
            
           
            return FieldConfigurationData.instance(fieldConfigurationId,entity,entitytable,field,is_enabled,
                    is_mandatory,validation_regex);
            
        }
    }
    
    @Override
    public Collection<FieldConfigurationData> retrieveFieldConfiguration(String entity) 
    {
        this.context.authenticatedUser();

        final FieldMapper rm = new FieldMapper();
        final String sql = "select " + rm.schema() + " where fld.entity=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] {entity});
    }
    
    @Override
    public List<FieldConfigurationData> retrieveFieldConfigurationList(String entity) 
    {
        this.context.authenticatedUser();

        final FieldMapper rm = new FieldMapper();
        final String sql = "select " + rm.schema() + " where fld.entity=?";

   return this.jdbcTemplate.query(sql, new Object[] {entity}, rm);
       
        
    }
}
