/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.fund.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.fund.exception.FundNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class FundReadPlatformServiceImpl implements FundReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public FundReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
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
    @Cacheable(value = "funds", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('fn')")
    public Collection<FundData> retrieveAllFunds() {

        this.context.authenticatedUser();

        final FundMapper rm = new FundMapper();
        final String sql = "select " + rm.schema() + " order by f.name";

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public FundData retrieveFund(final Long fundId) {

        try {
            this.context.authenticatedUser();

            final FundMapper rm = new FundMapper();
            final String sql = "select " + rm.schema() + " where f.id = ?";

            final FundData selectedFund = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { fundId });

            return selectedFund;
        } catch (final EmptyResultDataAccessException e) {
            throw new FundNotFoundException(fundId);
        }
    }
}