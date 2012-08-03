package org.mifosng.platform.loanproduct.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.data.FundData;
import org.mifosng.platform.api.data.LoanProductData;
import org.mifosng.platform.api.data.LoanProductLookup;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.api.data.TransactionProcessingStrategyData;
import org.mifosng.platform.currency.service.CurrencyReadPlatformService;
import org.mifosng.platform.exceptions.LoanProductNotFoundException;
import org.mifosng.platform.fund.service.FundReadPlatformService;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class LoanProductReadPlatformServiceImpl implements LoanProductReadPlatformService {

	private final PlatformSecurityContext context;
	private final CurrencyReadPlatformService currencyReadPlatformService;
	private final JdbcTemplate jdbcTemplate;
	private final LoanDropdownReadPlatformService dropdownReadPlatformService;
	private final FundReadPlatformService fundReadPlatformService;

	@Autowired
	public LoanProductReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final CurrencyReadPlatformService currencyReadPlatformService,
			final FundReadPlatformService fundReadPlatformService,
			final LoanDropdownReadPlatformService dropdownReadPlatformService,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.currencyReadPlatformService = currencyReadPlatformService;
		this.fundReadPlatformService = fundReadPlatformService;
		this.dropdownReadPlatformService = dropdownReadPlatformService;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public LoanProductData retrieveLoanProduct(final Long loanProductId) {

		try {
			LoanProductMapper rm = new LoanProductMapper();
			String sql = "select " + rm.loanProductSchema()
					+ " where lp.id = ?";

			LoanProductData productData = this.jdbcTemplate.queryForObject(sql,
					rm, new Object[] { loanProductId });

			populateProductDataWithDropdownOptions(productData);

			return productData;
		} catch (EmptyResultDataAccessException e) {
			throw new LoanProductNotFoundException(loanProductId);
		}
	}

	@Override
	public Collection<LoanProductData> retrieveAllLoanProducts() {

		this.context.authenticatedUser();

		LoanProductMapper rm = new LoanProductMapper();

		String sql = "select " + rm.loanProductSchema();

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	@Override
	public Collection<LoanProductLookup> retrieveAllLoanProductsForLookup() {

		this.context.authenticatedUser();

		LoanProductLookupMapper rm = new LoanProductLookupMapper();

		String sql = "select " + rm.loanProductLookupSchema();

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	@Override
	public LoanProductData retrieveNewLoanProductDetails() {

		LoanProductData productData = new LoanProductData();

		productData.setAmortizationType(LoanEnumerations
				.amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS));
		productData.setInterestType(LoanEnumerations
				.interestType(InterestMethod.DECLINING_BALANCE));
		productData.setRepaymentFrequencyType(LoanEnumerations
				.repaymentFrequencyType(PeriodFrequencyType.MONTHS));
		productData.setInterestRateFrequencyType(LoanEnumerations
				.interestRateFrequencyType(PeriodFrequencyType.MONTHS));
		productData
				.setInterestCalculationPeriodType(LoanEnumerations
						.interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD));

		populateProductDataWithDropdownOptions(productData);

		if (productData.getCurrencyOptions().size() >= 1) {
			CurrencyData currency = productData.getCurrencyOptions().get(0);
			MoneyData zero = MoneyData.zero(currency);
			productData.setPrincipal(zero);
			productData.setInArrearsTolerance(zero);
		}

		return productData;
	}

	private static final class LoanProductMapper implements
			RowMapper<LoanProductData> {

		public LoanProductMapper() {
		}

		public String loanProductSchema() {
			return "lp.id as id, lp.fund_id as fundId, f.name as fundName, lp.loan_transaction_strategy_id as transactionStrategyId, ltps.name as transactionStrategyName, "
					+ "lp.name as name, lp.description as description, lp.flexible_repayment_schedule as isFlexible, lp.interest_rebate as isInterestRebateAllowed, "
					+ "lp.principal_amount as principal, lp.currency_code as currencyCode, lp.currency_digits as currencyDigits, "
					+ "lp.nominal_interest_rate_per_period as interestRatePerPeriod, lp.interest_period_frequency_enum as interestRatePerPeriodFreq, "
					+ "lp.annual_nominal_interest_rate as annualInterestRate, lp.interest_method_enum as interestMethod, lp.interest_calculated_in_period_enum as interestCalculationInPeriodMethod,"
					+ "lp.repay_every as repaidEvery, lp.repayment_period_frequency_enum as repaymentPeriodFrequency, lp.number_of_repayments as numberOfRepayments, "
					+ "lp.amortization_method_enum as amortizationMethod, lp.arrearstolerance_amount as tolerance, "
					+ "lp.created_date as createdon, lp.lastmodified_date as modifiedon, "
					+ "curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, curr.display_symbol as currencyDisplaySymbol "
					+ " from portfolio_product_loan lp "
					+ " left join org_fund f on f.id = lp.fund_id"
					+ " left join ref_loan_transaction_processing_strategy ltps on ltps.id = lp.loan_transaction_strategy_id"
					+ " join ref_currency curr on curr.code = lp.currency_code";
		}

		@Override
		public LoanProductData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");
			String description = rs.getString("description");

			Long fundId = JdbcSupport.getLong(rs, "fundId");
			String fundName = rs.getString("fundName");
			
			Long transactionStrategyId = JdbcSupport.getLong(rs, "transactionStrategyId");
			String transactionStrategyName = rs.getString("transactionStrategyName");

			String currencyCode = rs.getString("currencyCode");
			String currencyName = rs.getString("currencyName");
			String currencyNameCode = rs.getString("currencyNameCode");
			String currencyDisplaySymbol = rs
					.getString("currencyDisplaySymbol");
			Integer currencyDigits = JdbcSupport.getInteger(rs,
					"currencyDigits");

			CurrencyData currencyData = new CurrencyData(currencyCode,
					currencyName, currencyDigits, currencyDisplaySymbol,
					currencyNameCode);

			BigDecimal principal = rs.getBigDecimal("principal");
			BigDecimal tolerance = rs.getBigDecimal("tolerance");

			MoneyData principalMoney = MoneyData.of(currencyData, principal);
			MoneyData toleranceMoney = MoneyData.of(currencyData, tolerance);

			Integer numberOfRepayments = JdbcSupport.getInteger(rs,
					"numberOfRepayments");
			Integer repaymentEvery = JdbcSupport.getInteger(rs, "repaidEvery");
			BigDecimal interestRatePerPeriod = rs
					.getBigDecimal("interestRatePerPeriod");
			BigDecimal annualInterestRate = rs
					.getBigDecimal("annualInterestRate");

			int repaymentFrequencyTypeId = JdbcSupport.getInteger(rs, "repaymentPeriodFrequency");
			EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(repaymentFrequencyTypeId);
			
			EnumOptionData loanTermFrequencyType = LoanEnumerations.loanTermFrequencyType(repaymentFrequencyTypeId);

			int amortizationTypeId = JdbcSupport.getInteger(rs,
					"amortizationMethod");
			EnumOptionData amortizationType = LoanEnumerations
					.amortizationType(amortizationTypeId);

			int interestRateFrequencyTypeId = JdbcSupport.getInteger(rs,"interestRatePerPeriodFreq");
			EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(interestRateFrequencyTypeId);

			int interestTypeId = JdbcSupport.getInteger(rs, "interestMethod");
			EnumOptionData interestType = LoanEnumerations.interestType(interestTypeId);

			int interestCalculationPeriodTypeId = JdbcSupport.getInteger(rs,
					"interestCalculationInPeriodMethod");
			EnumOptionData interestCalculationPeriodType = LoanEnumerations
					.interestCalculationPeriodType(interestCalculationPeriodTypeId);

			DateTime createdOn = JdbcSupport.getDateTime(rs, "createdon");
			DateTime lastModifedOn = JdbcSupport.getDateTime(rs, "modifiedon");

			return new LoanProductData(createdOn, lastModifedOn, id, name,
					description, principalMoney, toleranceMoney,
					numberOfRepayments, repaymentEvery, interestRatePerPeriod,
					annualInterestRate, loanTermFrequencyType, repaymentFrequencyType,
					interestRateFrequencyType, amortizationType, interestType,
					interestCalculationPeriodType, fundId, fundName, transactionStrategyId, transactionStrategyName);
		}

	}

	private static final class LoanProductLookupMapper implements
			RowMapper<LoanProductLookup> {

		public String loanProductLookupSchema() {
			return "lp.id as id, lp.name as name from portfolio_product_loan lp";
		}

		@Override
		public LoanProductLookup mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");

			return new LoanProductLookup(id, name);
		}

	}

	private void populateProductDataWithDropdownOptions(final LoanProductData productData) {

		List<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();
		List<EnumOptionData> amortizationTypeOptions = dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
		List<EnumOptionData> interestTypeOptions = dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
		List<EnumOptionData> interestCalculationPeriodTypeOptions = dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();
		List<EnumOptionData> repaymentFrequencyTypeOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
		List<EnumOptionData> interestRateFrequencyTypeOptions = dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();

		Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
		Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions = this.dropdownReadPlatformService.retreiveTransactionProcessingStrategies();

		productData.setCurrencyOptions(currencyOptions);
		productData.setAmortizationTypeOptions(amortizationTypeOptions);
		productData.setInterestTypeOptions(interestTypeOptions);
		productData.setInterestCalculationPeriodTypeOptions(interestCalculationPeriodTypeOptions);
		productData.setRepaymentFrequencyTypeOptions(repaymentFrequencyTypeOptions);
		productData.setInterestRateFrequencyTypeOptions(interestRateFrequencyTypeOptions);
		productData.setFundOptions(fundOptions);
		productData.setTransactionProcessingStrategyOptions(transactionProcessingStrategyOptions);
	}

}