/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.collateral.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.collateral.data.CollateralData;
import org.apache.fineract.portfolio.collateral.exception.CollateralNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CollateralReadPlatformServiceImpl implements CollateralReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;

    @Autowired
    public CollateralReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final LoanRepositoryWrapper loanRepositoryWrapper) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.loanRepositoryWrapper = loanRepositoryWrapper;
    }

    private static final class CollateralMapper implements RowMapper<CollateralData> {

        private final StringBuilder sqlBuilder = new StringBuilder(
                "lc.id as id, lc.description as description, lc.value as value, cv.id as typeId, cv.code_value as typeName, oc.code as currencyCode, ")
                .append(" oc.name as currencyName,oc.decimal_places as currencyDecimalPlaces, oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol, oc.internationalized_name_code as currencyNameCode")
                .append(" FROM m_loan_collateral lc") //
                .append(" JOIN m_code_value cv on lc.type_cv_id = cv.id")//
                .append(" JOIN m_loan loan on lc.loan_id = loan.id")//
                .append(" JOIN m_organisation_currency oc on loan.currency_code = oc.code");

        public String schema() {
            return this.sqlBuilder.toString();
        }

        @Override
        public CollateralData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String description = rs.getString("description");
            final Long typeId = rs.getLong("typeId");
            final BigDecimal value = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "value");
            final String typeName = rs.getString("typeName");

            final CodeValueData type = CodeValueData.instance(typeId, typeName);

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            return CollateralData.instance(id, type, value, description, currencyData);
        }
    }

    @Override
    public List<CollateralData> retrieveCollaterals(final Long loanId) {
        this.context.authenticatedUser();

        final CollateralMapper rm = new CollateralMapper();

        final String sql = "select " + rm.schema() + " where lc.loan_id=? order by id ASC";

        return this.jdbcTemplate.query(sql, rm, new Object[] { loanId });
    }

    @Override
    public CollateralData retrieveCollateral(final Long loanId, final Long collateralId) {
        try {
            final CollateralMapper rm = new CollateralMapper();
            String sql = "select " + rm.schema();
            sql += " where lc.loan_id=? and lc.id = ?";
            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanId, collateralId });
        } catch (final EmptyResultDataAccessException e) {
            throw new CollateralNotFoundException(loanId, collateralId);
        }

    }

    @Override
    public List<CollateralData> retrieveCollateralsForValidLoan(final Long loanId) {
        this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        return retrieveCollaterals(loanId);
    }

}