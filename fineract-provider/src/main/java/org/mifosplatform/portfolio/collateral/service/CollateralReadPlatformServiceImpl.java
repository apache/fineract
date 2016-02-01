/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collateral.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.collateral.data.CollateralData;
import org.mifosplatform.portfolio.collateral.exception.CollateralNotFoundException;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CollateralReadPlatformServiceImpl implements CollateralReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final LoanRepository loanRepository;

    @Autowired
    public CollateralReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final LoanRepository loanRepository) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.loanRepository = loanRepository;
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
        final Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }
        return retrieveCollaterals(loanId);
    }

}