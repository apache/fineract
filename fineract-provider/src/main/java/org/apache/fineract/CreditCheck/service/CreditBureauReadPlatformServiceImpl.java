package org.apache.fineract.CreditCheck.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

import org.apache.fineract.CreditCheck.data.CreditBureauData;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CreditBureauReadPlatformServiceImpl implements CreditBureauReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public CreditBureauReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /*private static final class CBMapper implements RowMapper<CreditBureauData> {

        public String schema() {
            return " cb.id as id, cb.cb_master_id as cbm_id,cbm.cb_name as cb_name,cb.alias as alias"
                    + ",cb.country as country,cb.cb_product_id as cb_product_id"
                    + ",cbp.cb_product_name as cbp_name,cb.start_date as start_date,cb.end_date as end_date,"
                    + "cb.is_active as is_active from m_creditbureau cb,m_creditbureau_master cbm,m_creditbureau_product cbp"
                    + " where cb.cb_master_id=cbm.id and cb.cb_master_id=cbp.cb_master_id";
        }

        @Override
        public CreditBureauData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long cbID = rs.getLong("id");
            final Long cbm_id = rs.getLong("cbm_id");
            final String cb_name=rs.getString("cb_name");
            final String alias=rs.getString("alias");
            final String country=rs.getString("country");
            final Long cb_product_id=rs.getLong("cb_product_id");
            final String cbp_name=rs.getString("cbp_name"); 
            final Date start_date=rs.getDate("start_date");
            final Date end_date=rs.getDate("end_date");
            final boolean is_active=rs.getBoolean("is_active");

            return CreditBureauData.instance(cbID, cbm_id,cb_name,alias,country,cb_product_id,cbp_name,start_date,
                    end_date,is_active);
        }
    }*/

   
    private static final class CBMapper implements RowMapper<CreditBureauData>
    {
        public String schema()
        {
            return "cb.id as creditBureauID,cb.name as creditBureauName,cb.product as creditBureauProduct,"
                    + "cb.country as country,concat(cb.product,' - ',cb.name,' - ',cb.country) as cbSummary,cb.implementationKey as implementationKey from m_creditbureau cb";
        }
        
        @Override
        public CreditBureauData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException
        {
            final long id=rs.getLong("creditBureauID");
            final String name=rs.getString("creditBureauName");
            final String product=rs.getString("creditBureauProduct");
            final String country=rs.getString("country");
            final String cbSummary=rs.getString("cbSummary");
            final long implementationKey=rs.getLong("implementationKey");
           
            return CreditBureauData.instance(id, name, product, country, cbSummary,implementationKey);
            
        }
    }
    
    @Override
    public Collection<CreditBureauData> retrieveCreditBureau() {
        this.context.authenticatedUser();

        final CBMapper rm = new CBMapper();
        final String sql = "select " + rm.schema() + " order by id";

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }
    
  
    
 /*   @Override
    public CreditBureauData findCreditBureau(final Long cbid)
    {
        this.context.authenticatedUser();
        final CBMapper rm = new CBMapper();
        final String sql = "select " + rm.schema() + " where c.id = ?";

        return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { cbid });
    }*/

}
