package org.mifosng.platform.charge.service;

import org.mifosng.platform.api.data.ChargeData;
import org.mifosng.platform.exceptions.ChargeNotFoundException;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Service
public class ChargeReadPlatformServiceImpl implements ChargeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public ChargeReadPlatformServiceImpl(PlatformSecurityContext context,
                                         final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<ChargeData> retrieveAllCharges() {
        this.context.authenticatedUser();

        ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=0 order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public ChargeData retrieveCharge(Long chargeId) {
        try {
            this.context.authenticatedUser();

            ChargeMapper rm = new ChargeMapper();

            String sql = "select " + rm.chargeSchema() + " where c.id = ? and c.is_deleted=0 ";

            ChargeData chargeData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] {chargeId});
            return chargeData;
        } catch (EmptyResultDataAccessException e){
            throw new ChargeNotFoundException(chargeId);
        }
    }

    private static final class ChargeMapper implements RowMapper<ChargeData> {

        public String chargeSchema(){
            return "c.id as id, c.name as name, c.amount as amount from o_charge c ";
        }

        @Override
        public ChargeData mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String name = rs.getString("name");
            BigDecimal amount = rs.getBigDecimal("amount");

            return new ChargeData(id, name, amount);
        }
    }
}
