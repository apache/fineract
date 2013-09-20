/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.monetary.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CurrencyReadPlatformServiceImpl implements CurrencyReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final CurrencyMapper currencyRowMapper;

    @Autowired
    public CurrencyReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.currencyRowMapper = new CurrencyMapper();
    }

    @Override
    public Collection<CurrencyData> retrieveAllowedCurrencies() {

        this.context.authenticatedUser();

        final String sql = "select " + this.currencyRowMapper.schema() + " from m_organisation_currency c order by c.name";

        return this.jdbcTemplate.query(sql, this.currencyRowMapper, new Object[] {});
    }

    @Override
    public Collection<CurrencyData> retrieveAllPlatformCurrencies() {

        final String sql = "select " + this.currencyRowMapper.schema() + " from m_currency c order by c.name";

        return this.jdbcTemplate.query(sql, this.currencyRowMapper, new Object[] {});
    }

    private static final class CurrencyMapper implements RowMapper<CurrencyData> {

        @Override
        public CurrencyData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String code = rs.getString("code");
            final String name = rs.getString("name");
            final int decimalPlaces = JdbcSupport.getInteger(rs, "decimalPlaces");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final String displaySymbol = rs.getString("displaySymbol");
            final String nameCode = rs.getString("nameCode");

            return new CurrencyData(code, name, decimalPlaces, inMultiplesOf, displaySymbol, nameCode);
        }

        public String schema() {
            return " c.code as code, c.name as name, c.decimal_places as decimalPlaces,c.currency_multiplesof as inMultiplesOf, c.display_symbol as displaySymbol, c.internationalized_name_code as nameCode ";
        }
    }
}