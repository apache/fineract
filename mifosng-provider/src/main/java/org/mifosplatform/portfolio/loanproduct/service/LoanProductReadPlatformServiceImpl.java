package org.mifosplatform.portfolio.loanproduct.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.service.ChargeReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class LoanProductReadPlatformServiceImpl implements LoanProductReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final ChargeReadPlatformService chargeReadPlatformService;

    @Autowired
    public LoanProductReadPlatformServiceImpl(final PlatformSecurityContext context,
            final ChargeReadPlatformService chargeReadPlatformService, final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public LoanProductData retrieveLoanProduct(final Long loanProductId) {

        try {
            final Collection<ChargeData> charges = this.chargeReadPlatformService.retrieveLoanProductCharges(loanProductId);

            final LoanProductMapper rm = new LoanProductMapper(charges);
            final String sql = "select " + rm.loanProductSchema() + " where lp.id = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanProductId });

        } catch (EmptyResultDataAccessException e) {
            throw new LoanProductNotFoundException(loanProductId);
        }
    }

    @Override
    public Collection<LoanProductData> retrieveAllLoanProducts() {

        this.context.authenticatedUser();

        final LoanProductMapper rm = new LoanProductMapper(null);

        final String sql = "select " + rm.loanProductSchema();

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public Collection<LoanProductData> retrieveAllLoanProductsForLookup() {

        this.context.authenticatedUser();

        final LoanProductLookupMapper rm = new LoanProductLookupMapper();

        final String sql = "select " + rm.schema();

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public LoanProductData retrieveNewLoanProductDetails() {
        return LoanProductData.sensibleDefaultsForNewLoanProductCreation();
    }

    private static final class LoanProductMapper implements RowMapper<LoanProductData> {

        private final Collection<ChargeData> charges;

        public LoanProductMapper(final Collection<ChargeData> charges) {
            this.charges = charges;
        }

        public String loanProductSchema() {
            return "lp.id as id, lp.fund_id as fundId, f.name as fundName, lp.loan_transaction_strategy_id as transactionStrategyId, ltps.name as transactionStrategyName, "
                    + "lp.name as name, lp.description as description, "
                    + "lp.principal_amount as principal, lp.currency_code as currencyCode, lp.currency_digits as currencyDigits, "
                    + "lp.nominal_interest_rate_per_period as interestRatePerPeriod, lp.interest_period_frequency_enum as interestRatePerPeriodFreq, "
                    + "lp.annual_nominal_interest_rate as annualInterestRate, lp.interest_method_enum as interestMethod, lp.interest_calculated_in_period_enum as interestCalculationInPeriodMethod,"
                    + "lp.repay_every as repaidEvery, lp.repayment_period_frequency_enum as repaymentPeriodFrequency, lp.number_of_repayments as numberOfRepayments, "
                    + "lp.amortization_method_enum as amortizationMethod, lp.arrearstolerance_amount as tolerance, "
                    + "lp.accounting_type as accountingType, "
                    + "curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, curr.display_symbol as currencyDisplaySymbol "
                    + " from m_product_loan lp "
                    + " left join m_fund f on f.id = lp.fund_id"
                    + " left join ref_loan_transaction_processing_strategy ltps on ltps.id = lp.loan_transaction_strategy_id"
                    + " join m_currency curr on curr.code = lp.currency_code";
        }

        @Override
        public LoanProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final Long fundId = JdbcSupport.getLong(rs, "fundId");
            final String fundName = rs.getString("fundName");
            final Long transactionStrategyId = JdbcSupport.getLong(rs, "transactionStrategyId");
            final String transactionStrategyName = rs.getString("transactionStrategyName");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");

            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, currencyDisplaySymbol,
                    currencyNameCode);

            final BigDecimal principal = rs.getBigDecimal("principal");
            final BigDecimal tolerance = rs.getBigDecimal("tolerance");

            final Integer numberOfRepayments = JdbcSupport.getInteger(rs, "numberOfRepayments");
            final Integer repaymentEvery = JdbcSupport.getInteger(rs, "repaidEvery");
            
            final Integer accountingTypeId = JdbcSupport.getInteger(rs, "accountingType");
            final EnumOptionData accountingRuleType = LoanEnumerations.accountingRuleType(accountingTypeId);
            
            final Integer loanTermFrequency = repaymentEvery * numberOfRepayments;
            final BigDecimal interestRatePerPeriod = rs.getBigDecimal("interestRatePerPeriod");
            final BigDecimal annualInterestRate = rs.getBigDecimal("annualInterestRate");

            final int repaymentFrequencyTypeId = JdbcSupport.getInteger(rs, "repaymentPeriodFrequency");
            final EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(repaymentFrequencyTypeId);

            final EnumOptionData loanTermFrequencyType = LoanEnumerations.loanTermFrequencyType(repaymentFrequencyTypeId);

            final int amortizationTypeId = JdbcSupport.getInteger(rs, "amortizationMethod");
            final EnumOptionData amortizationType = LoanEnumerations.amortizationType(amortizationTypeId);

            final int interestRateFrequencyTypeId = JdbcSupport.getInteger(rs, "interestRatePerPeriodFreq");
            final EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(interestRateFrequencyTypeId);

            final int interestTypeId = JdbcSupport.getInteger(rs, "interestMethod");
            final EnumOptionData interestType = LoanEnumerations.interestType(interestTypeId);

            final int interestCalculationPeriodTypeId = JdbcSupport.getInteger(rs, "interestCalculationInPeriodMethod");
            final EnumOptionData interestCalculationPeriodType = LoanEnumerations
                    .interestCalculationPeriodType(interestCalculationPeriodTypeId);

            return new LoanProductData(id, name, description, currency, principal, tolerance, numberOfRepayments, loanTermFrequency,
                    repaymentEvery, interestRatePerPeriod, annualInterestRate, loanTermFrequencyType, repaymentFrequencyType,
                    interestRateFrequencyType, amortizationType, interestType, interestCalculationPeriodType, fundId, fundName,
                    transactionStrategyId, transactionStrategyName, this.charges, accountingRuleType);
        }

    }

    private static final class LoanProductLookupMapper implements RowMapper<LoanProductData> {

        public String schema() {
            return "lp.id as id, lp.name as name from m_product_loan lp";
        }

        @Override
        public LoanProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");

            return LoanProductData.lookup(id, name);
        }
    }
}