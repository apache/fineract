package org.mifosplatform.portfolio.savingsaccountproduct.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.DateTime;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.service.CurrencyReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductData;
import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductLookup;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SavingProductReadPlatformServiceImpl implements SavingProductReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final CurrencyReadPlatformService currencyReadPlatformService;

    @Autowired
    public SavingProductReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final CurrencyReadPlatformService currencyReadPlatformService) {

        this.context = context;
        jdbcTemplate = new JdbcTemplate(dataSource);
        this.currencyReadPlatformService = currencyReadPlatformService;
    }

    @Override
    public Collection<SavingProductData> retrieveAllSavingProducts() {

        this.context.authenticatedUser();

        SavingProductMapper savingProductMapper = new SavingProductMapper();

        String sql = "select " + savingProductMapper.savingProductSchema() + " where sp.is_deleted=0";

        return this.jdbcTemplate.query(sql, savingProductMapper, new Object[] {});

    }

    @Override
    public Collection<SavingProductLookup> retrieveAllSavingProductsForLookup() {

        this.context.authenticatedUser();

        SavingProductLookupMapper savingProductLookupMapper = new SavingProductLookupMapper();

        String sql = "select " + savingProductLookupMapper.savingProductLookupSchema() + " where sp.is_deleted=0";

        return this.jdbcTemplate.query(sql, savingProductLookupMapper, new Object[] {});
    }

    @Override
    public SavingProductData retrieveSavingProduct(Long savingProductId) {
        try {
            this.context.authenticatedUser();
            SavingProductMapper savingProductMapper = new SavingProductMapper();
            String sql = "select " + savingProductMapper.savingProductSchema() + " where sp.id = ? and sp.is_deleted=0";
            SavingProductData productData = this.jdbcTemplate.queryForObject(sql, savingProductMapper, new Object[] { savingProductId });

            populateProductDataWithDropdownOptions(productData);

            return productData;
        } catch (EmptyResultDataAccessException e) {
            throw new LoanProductNotFoundException(savingProductId);
        }
    }

    @Override
    public SavingProductData retrieveNewSavingProductDetails() {

        SavingProductData productData = new SavingProductData();

        populateProductDataWithDropdownOptions(productData);

        return productData;
    }

    private static final class SavingProductMapper implements RowMapper<SavingProductData> {

        public String savingProductSchema() {
            return "sp.id as id,sp.name as name, sp.description as description,sp.currency_code as currencyCode, sp.currency_digits as currencyDigits,sp.interest_rate as interestRate, "
                    + "sp.min_interest_rate as minInterestRate, sp.max_interest_rate as maxInterstRate, sp.deposit_every as depositEvery, "
                    + " sp.savings_deposit_amount as savingsDepositAmount, sp.savings_product_type as savingProductType, sp.tenure_type as tenureType, sp.tenure as tenure, sp.frequency as frequency, "
                    + " sp.interest_type as interestType, sp.interest_calculation_method as interestCalculationMethod, sp.min_bal_for_withdrawal as minimumBalanceForWithdrawal, "
                    + " sp.is_partial_deposit_allowed as isPartialDepositAllowed, sp.is_lock_in_period_allowed as isLockinPeriodAllowed, sp.lock_in_period as lockinPeriod, sp.lock_in_period_type as lockinPeriodType, "
                    + " sp.created_date as createdon, sp.lastmodified_date as modifiedon, "
                    + "curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, curr.display_symbol as currencyDisplaySymbol"
                    + "  from m_product_savings sp join m_currency curr on curr.code = sp.currency_code";
        }

        @Override
        public SavingProductData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String name = rs.getString("name");
            String description = rs.getString("description");

            String currencyCode = rs.getString("currencyCode");
            String currencyName = rs.getString("currencyName");
            String currencyNameCode = rs.getString("currencyNameCode");
            String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");

            CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, currencyDisplaySymbol,
                    currencyNameCode);
            BigDecimal interestRate = rs.getBigDecimal("interestRate");
            BigDecimal minInterestRate = rs.getBigDecimal("minInterestRate");
            BigDecimal maxInterestRate = rs.getBigDecimal("maxInterstRate");

            DateTime createdOn = JdbcSupport.getDateTime(rs, "createdon");
            DateTime lastModifedOn = JdbcSupport.getDateTime(rs, "modifiedon");

            Integer depositEvery = JdbcSupport.getInteger(rs, "depositEvery");
            BigDecimal savingsDepositAmount = rs.getBigDecimal("savingsDepositAmount");
            EnumOptionData savingProductTypeEnum = SavingProductEnumerations.savingProductType(SavingProductType.fromInt(JdbcSupport
                    .getInteger(rs, "savingProductType")));
            EnumOptionData tenureTypeEnum = SavingProductEnumerations.tenureTypeEnum(JdbcSupport.getInteger(rs, "tenureType"));
            Integer tenure = JdbcSupport.getInteger(rs, "tenure");
            EnumOptionData savingFrequencyType = SavingProductEnumerations.interestFrequencyType(JdbcSupport.getInteger(rs, "frequency"));
            EnumOptionData savingInterestType = SavingProductEnumerations.savingInterestType(JdbcSupport.getInteger(rs, "interestType"));
            EnumOptionData interestCalculationMethodEnum = SavingProductEnumerations.savingInterestCalculationMethod(JdbcSupport
                    .getInteger(rs, "interestCalculationMethod"));
            BigDecimal minimumBalanceForWithdrawal = rs.getBigDecimal("minimumBalanceForWithdrawal");
            boolean isPartialDepositAllowed = rs.getBoolean("isPartialDepositAllowed");
            boolean isLockinPeriodAllowed = rs.getBoolean("isLockinPeriodAllowed");
            Integer lockinPeriod = JdbcSupport.getInteger(rs, "lockinPeriod");
            EnumOptionData lockinPeriodType = SavingsDepositEnumerations.interestCompoundingPeriodType(PeriodFrequencyType
                    .fromInt(JdbcSupport.getInteger(rs, "lockinPeriodType")));

            return new SavingProductData(createdOn, lastModifedOn, id, name, description, interestRate, minInterestRate, maxInterestRate,
                    currencyData, currencyDigits, savingsDepositAmount, savingProductTypeEnum, tenureTypeEnum, tenure, savingFrequencyType,
                    savingInterestType, interestCalculationMethodEnum, minimumBalanceForWithdrawal, isPartialDepositAllowed,
                    isLockinPeriodAllowed, lockinPeriod, lockinPeriodType, depositEvery);
        }
    }

    private static final class SavingProductLookupMapper implements RowMapper<SavingProductLookup> {

        public String savingProductLookupSchema() {
            return " sp.id as id, sp.name as name from m_product_savings sp";
        }

        @Override
        public SavingProductLookup mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String name = rs.getString("name");

            return new SavingProductLookup(id, name);
        }

    }

    private void populateProductDataWithDropdownOptions(final SavingProductData productData) {

        Collection<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();
        productData.setCurrencyOptions(currencyOptions);
    }

}
