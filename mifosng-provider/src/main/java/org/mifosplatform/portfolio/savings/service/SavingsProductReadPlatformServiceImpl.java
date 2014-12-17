/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityType;
import org.mifosplatform.infrastructure.entityaccess.service.MifosEntityAccessUtil;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;
import org.mifosplatform.portfolio.savings.exception.SavingsProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SavingsProductReadPlatformServiceImpl implements SavingsProductReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final SavingProductMapper savingsProductRowMapper = new SavingProductMapper();
    private final SavingProductLookupMapper savingsProductLookupsRowMapper = new SavingProductLookupMapper();
    private final MifosEntityAccessUtil mifosEntityAccessUtil;

    @Autowired
    public SavingsProductReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
    		final MifosEntityAccessUtil mifosEntityAccessUtil) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.mifosEntityAccessUtil = mifosEntityAccessUtil;
    }

    @Override
    public Collection<SavingsProductData> retrieveAll() {

        this.context.authenticatedUser();

        String sql = "select " + this.savingsProductRowMapper.schema() + "where sp.deposit_type_enum = ?";
        
		// Check if branch specific products are enabled. If yes, fetch only products mapped to current user's office
		String inClause = mifosEntityAccessUtil.
				getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(
						MifosEntityType.SAVINGS_PRODUCT);
		if ( (inClause != null) && (!(inClause.trim().isEmpty())) ) {
			sql += " and sp.id in ( " + inClause + " ) ";
		}

        return this.jdbcTemplate.query(sql, this.savingsProductRowMapper, new Object[] { DepositAccountType.SAVINGS_DEPOSIT.getValue() });
    }

    @Override
    public Collection<SavingsProductData> retrieveAllForLookup() {

        String sql = "select " + this.savingsProductLookupsRowMapper.schema() + " where sp.deposit_type_enum = ? ";
        
        // Check if branch specific products are enabled. If yes, fetch only products mapped to current user's office
 		String inClause = mifosEntityAccessUtil.
 				getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(
						MifosEntityType.SAVINGS_PRODUCT);
    	if ( (inClause != null) && (!(inClause.trim().isEmpty())) ) {
    		sql += " and id in ( " + inClause + " ) ";
    	}
    

        return this.jdbcTemplate.query(sql, this.savingsProductLookupsRowMapper,
                new Object[] { DepositAccountType.SAVINGS_DEPOSIT.getValue() });
    }

    @Override
    public SavingsProductData retrieveOne(final Long savingProductId) {
        try {
            this.context.authenticatedUser();
            final String sql = "select " + this.savingsProductRowMapper.schema() + " where sp.id = ? and sp.deposit_type_enum = ?";
            return this.jdbcTemplate.queryForObject(sql, this.savingsProductRowMapper, new Object[] { savingProductId,
                    DepositAccountType.SAVINGS_DEPOSIT.getValue() });
        } catch (final EmptyResultDataAccessException e) {
            throw new SavingsProductNotFoundException(savingProductId);
        }
    }

    private static final class SavingProductMapper implements RowMapper<SavingsProductData> {

        private final String schemaSql;

        public SavingProductMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sp.id as id, sp.name as name, sp.short_name as shortName, sp.description as description, ");
            sqlBuilder
                    .append("sp.currency_code as currencyCode, sp.currency_digits as currencyDigits, sp.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("sp.nominal_annual_interest_rate as nominalAnnualInterestRate, ");
            sqlBuilder.append("sp.interest_compounding_period_enum as compoundingInterestPeriodType, ");
            sqlBuilder.append("sp.interest_posting_period_enum as interestPostingPeriodType, ");
            sqlBuilder.append("sp.interest_calculation_type_enum as interestCalculationType, ");
            sqlBuilder.append("sp.interest_calculation_days_in_year_type_enum as interestCalculationDaysInYearType, ");
            sqlBuilder.append("sp.min_required_opening_balance as minRequiredOpeningBalance, ");
            sqlBuilder.append("sp.lockin_period_frequency as lockinPeriodFrequency,");
            sqlBuilder.append("sp.lockin_period_frequency_enum as lockinPeriodFrequencyType, ");
            sqlBuilder.append("sp.withdrawal_fee_for_transfer as withdrawalFeeForTransfers, ");
            sqlBuilder.append("sp.allow_overdraft as allowOverdraft, ");
            sqlBuilder.append("sp.overdraft_limit as overdraftLimit, ");
            sqlBuilder.append("sp.min_required_balance as minRequiredBalance, ");
            sqlBuilder.append("sp.enforce_min_required_balance as enforceMinRequiredBalance, ");
            sqlBuilder.append("sp.min_balance_for_interest_calculation as minBalanceForInterestCalculation,");
            sqlBuilder.append("sp.accounting_type as accountingType ");
            sqlBuilder.append("from m_savings_product sp ");
            sqlBuilder.append("join m_currency curr on curr.code = sp.currency_code ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String shortName = rs.getString("shortName");
            final String description = rs.getString("description");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);
            final BigDecimal nominalAnnualInterestRate = rs.getBigDecimal("nominalAnnualInterestRate");

            final Integer compoundingInterestPeriodTypeValue = JdbcSupport.getInteger(rs, "compoundingInterestPeriodType");
            final EnumOptionData compoundingInterestPeriodType = SavingsEnumerations
                    .compoundingInterestPeriodType(compoundingInterestPeriodTypeValue);

            final Integer interestPostingPeriodTypeValue = JdbcSupport.getInteger(rs, "interestPostingPeriodType");
            final EnumOptionData interestPostingPeriodType = SavingsEnumerations.interestPostingPeriodType(interestPostingPeriodTypeValue);

            final Integer interestCalculationTypeValue = JdbcSupport.getInteger(rs, "interestCalculationType");
            final EnumOptionData interestCalculationType = SavingsEnumerations.interestCalculationType(interestCalculationTypeValue);

            EnumOptionData interestCalculationDaysInYearType = null;
            final Integer interestCalculationDaysInYearTypeValue = JdbcSupport.getInteger(rs, "interestCalculationDaysInYearType");
            if (interestCalculationDaysInYearTypeValue != null) {
                interestCalculationDaysInYearType = SavingsEnumerations
                        .interestCalculationDaysInYearType(interestCalculationDaysInYearTypeValue);
            }

            final Integer accountingRuleId = JdbcSupport.getInteger(rs, "accountingType");
            final EnumOptionData accountingRuleType = AccountingEnumerations.accountingRuleType(accountingRuleId);

            final BigDecimal minRequiredOpeningBalance = rs.getBigDecimal("minRequiredOpeningBalance");

            final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriodFrequency");
            EnumOptionData lockinPeriodFrequencyType = null;
            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodFrequencyType");
            if (lockinPeriodFrequencyTypeValue != null) {
                lockinPeriodFrequencyType = SavingsEnumerations.lockinPeriodFrequencyType(lockinPeriodFrequencyTypeValue);
            }

            final boolean withdrawalFeeForTransfers = rs.getBoolean("withdrawalFeeForTransfers");
            final boolean allowOverdraft = rs.getBoolean("allowOverdraft");
            final BigDecimal overdraftLimit = rs.getBigDecimal("overdraftLimit");

            final BigDecimal minRequiredBalance = rs.getBigDecimal("minRequiredBalance");
            final boolean enforceMinRequiredBalance = rs.getBoolean("enforceMinRequiredBalance");
            final BigDecimal minBalanceForInterestCalculation = rs.getBigDecimal("minBalanceForInterestCalculation");

            return SavingsProductData.instance(id, name, shortName, description, currency, nominalAnnualInterestRate,
                    compoundingInterestPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                    minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers,
                    accountingRuleType, allowOverdraft, overdraftLimit, minRequiredBalance, enforceMinRequiredBalance,
                    minBalanceForInterestCalculation);
        }
    }

    private static final class SavingProductLookupMapper implements RowMapper<SavingsProductData> {

        public String schema() {
            return " sp.id as id, sp.name as name from m_savings_product sp";
        }

        @Override
        public SavingsProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");

            return SavingsProductData.lookup(id, name);
        }
    }

    @Override
    public Collection<SavingsProductData> retrieveAllForLookupByType(Boolean isOverdraftType) {
        String sql = "select " + this.savingsProductLookupsRowMapper.schema();

        boolean inClauseAdded = false;
        
        // Check if branch specific products are enabled. If yes, fetch only products mapped to current user's office
  		String inClause = mifosEntityAccessUtil.
  				getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(
						MifosEntityType.SAVINGS_PRODUCT);
    	if ( (inClause != null) && (!(inClause.trim().isEmpty())) ) {
    		sql += " where id in ( " + inClause + " ) ";
    		inClauseAdded = true;
    	}
        
        if (isOverdraftType != null) {
        	if (inClauseAdded) {
        		sql += " and sp.allow_overdraft=?";
        	} else {
        		sql += " where sp.allow_overdraft=?";
        	}
            return this.jdbcTemplate.query(sql, this.savingsProductLookupsRowMapper, isOverdraftType);
        }

        return this.jdbcTemplate.query(sql, this.savingsProductLookupsRowMapper);

    }

    @Override
    public Collection<SavingsProductData> retrieveAllForCurrency(String currencyCode) {

        this.context.authenticatedUser();

        String sql = "select " + this.savingsProductRowMapper.schema() + " where sp.currency_code='" + currencyCode + "'";
        
        // Check if branch specific products are enabled. If yes, fetch only products mapped to current user's office
  		String inClause = mifosEntityAccessUtil.
  				getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(
						MifosEntityType.SAVINGS_PRODUCT);
    	if ( (inClause != null) && (!(inClause.trim().isEmpty())) ) {
    		sql += " and id in ( " + inClause + " ) ";
    	}

        return this.jdbcTemplate.query(sql, this.savingsProductRowMapper);
    }
}