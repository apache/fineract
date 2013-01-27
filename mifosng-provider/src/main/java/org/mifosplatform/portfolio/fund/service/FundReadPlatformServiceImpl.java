package org.mifosplatform.portfolio.fund.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.fund.exception.FundNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class FundReadPlatformServiceImpl implements FundReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public FundReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class FundMapper implements RowMapper<FundData> {

        public String schema() {
            return " f.id as id, f.name as name, f.external_id as externalId from m_fund f ";
        }

        @Override
        public FundData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String externalId = rs.getString("externalId");

            return FundData.instance(id, name, externalId);
        }
    }

    @Override
    public Collection<FundData> retrieveAllFunds() {

        context.authenticatedUser();

        final FundMapper rm = new FundMapper();
        final String sql = "select " + rm.schema() + " order by f.name";

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public FundData retrieveFund(final Long fundId) {

        try {
            context.authenticatedUser();

            final FundMapper rm = new FundMapper();
            final String sql = "select " + rm.schema() + " where f.id = ?";

            final FundData selectedFund = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { fundId });

            return selectedFund;
        } catch (EmptyResultDataAccessException e) {
            throw new FundNotFoundException(fundId);
        }
    }
}