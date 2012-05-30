package org.mifosng.platform.loanproduct.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.EnumOptionData;
import org.mifosng.data.MoneyData;
import org.mifosng.platform.api.data.LoanProductData;
import org.mifosng.platform.currency.service.CurrencyReadPlatformService;
import org.mifosng.platform.exceptions.LoanProductNotFoundException;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoanProductReadPlatformServiceImpl implements LoanProductReadPlatformService {

	private final PlatformSecurityContext context;
	private final CurrencyReadPlatformService currencyReadPlatformService;
	private final SimpleJdbcTemplate jdbcTemplate;
	private final LoanDropdownReadPlatformService dropdownReadPlatformService;
	
	@Autowired
	public LoanProductReadPlatformServiceImpl(final PlatformSecurityContext context, final CurrencyReadPlatformService currencyReadPlatformService,
			final LoanDropdownReadPlatformService dropdownReadPlatformService,
			final DataSource dataSource) {
		this.context = context;
		this.currencyReadPlatformService = currencyReadPlatformService;
		this.dropdownReadPlatformService = dropdownReadPlatformService;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	@Override
	public LoanProductData retrieveLoanProduct(final Long loanProductId) {

		try {
			List<CurrencyData> allowedCurrencies = currencyReadPlatformService.retrieveAllPlatformCurrencies();
			
			LoanProductMapper rm = new LoanProductMapper(allowedCurrencies);
			String sql = "select " + rm.loanProductSchema() + " where lp.id = ?";
	
			LoanProductData productData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanProductId });
	
			populateProductDataWithDropdownOptions(productData);
	
			return productData;
		} catch (EmptyResultDataAccessException e) {
			throw new LoanProductNotFoundException(loanProductId);
		}
	}
	
	@Override
	public Collection<LoanProductData> retrieveAllLoanProducts() {

		AppUser currentUser = this.context.authenticatedUser();

		List<CurrencyData> allowedCurrencies = currencyReadPlatformService.retrieveAllPlatformCurrencies();
		
		LoanProductMapper rm = new LoanProductMapper(allowedCurrencies);

		String sql = "select " + rm.loanProductSchema() + " where lp.org_id = ?";

		return this.jdbcTemplate.query(sql, rm, new Object[] { currentUser.getOrganisation().getId() });
	}

	@Override
	public LoanProductData retrieveNewLoanProductDetails() {

		LoanProductData productData = new LoanProductData();

		productData.setAmortizationType(LoanEnumerations.amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS));
		productData.setInterestType(LoanEnumerations.interestType(InterestMethod.DECLINING_BALANCE));
		productData.setRepaymentFrequencyType(LoanEnumerations.repaymentFrequencyType(PeriodFrequencyType.MONTHS));
		productData.setInterestRateFrequencyType(LoanEnumerations.interestRateFrequencyType(PeriodFrequencyType.MONTHS));
		productData.setInterestCalculationPeriodType(LoanEnumerations.interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD));

		populateProductDataWithDropdownOptions(productData);

		if (productData.getCurrencyOptions().size() >= 1) {
			CurrencyData currency = productData.getCurrencyOptions().get(0);
			MoneyData zero = MoneyData.zero(currency);
			productData.setPrincipal(zero);
			productData.setInArrearsTolerance(zero);
		}
		
		return productData;
	}

	private static final class LoanProductMapper implements RowMapper<LoanProductData> {

		private final List<CurrencyData> allowedCurrencies;

		public LoanProductMapper(List<CurrencyData> allowedCurrencies) {
			this.allowedCurrencies = allowedCurrencies;
		}

		public String loanProductSchema() {
			return "lp.id as id, lp.name as name, lp.description as description, lp.flexible_repayment_schedule as isFlexible, lp.interest_rebate as isInterestRebateAllowed, "
					+ "lp.principal_amount as principal, lp.currency_code as currencyCode, lp.currency_digits as currencyDigits, "
					+ "lp.nominal_interest_rate_per_period as interestRatePerPeriod, lp.interest_period_frequency_enum as interestRatePerPeriodFreq, "
					+ "lp.annual_nominal_interest_rate as annualInterestRate, lp.interest_method_enum as interestMethod, lp.interest_calculated_in_period_enum as interestCalculationInPeriodMethod,"
					+ "lp.repay_every as repaidEvery, lp.repayment_period_frequency_enum as repaymentPeriodFrequency, lp.number_of_repayments as numberOfRepayments, "
					+ "lp.amortization_method_enum as amortizationMethod, lp.arrearstolerance_amount as tolerance, "
					+ "lp.created_date as createdon, lp.lastmodified_date as modifiedon "
					+ " from portfolio_product_loan lp";
		}

		@Override
		public LoanProductData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");
			String description = rs.getString("description");

			String currencyCode = rs.getString("currencyCode");
			Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
			
			CurrencyData currencyData = findCurrencyByCode(currencyCode, allowedCurrencies);
			if (currencyData != null) {
				currencyData.setDecimalPlaces(currencyDigits);
			}

			BigDecimal principal = rs.getBigDecimal("principal");
			BigDecimal tolerance = rs.getBigDecimal("tolerance");
			
			MoneyData principalMoney = MoneyData.of(currencyData, principal);
			MoneyData toleranceMoney = MoneyData.of(currencyData, tolerance);

			Integer numberOfRepayments = JdbcSupport.getInteger(rs, "numberOfRepayments");
			Integer repaymentEvery = JdbcSupport.getInteger(rs, "repaidEvery");
			BigDecimal interestRatePerPeriod = rs.getBigDecimal("interestRatePerPeriod");
			BigDecimal annualInterestRate = rs.getBigDecimal("annualInterestRate");

			int repaymentFrequencyTypeId = JdbcSupport.getInteger(rs, "repaymentPeriodFrequency");
			EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(repaymentFrequencyTypeId);
			
			int amortizationTypeId = JdbcSupport.getInteger(rs, "amortizationMethod");
			EnumOptionData amortizationType = LoanEnumerations.amortizationType(amortizationTypeId);
			
			int interestRateFrequencyTypeId = JdbcSupport.getInteger(rs, "interestRatePerPeriodFreq");
			EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(interestRateFrequencyTypeId);
			
			int interestTypeId = JdbcSupport.getInteger(rs, "interestMethod");
			EnumOptionData interestType = LoanEnumerations.interestType(interestTypeId);
			
			int interestCalculationPeriodTypeId = JdbcSupport.getInteger(rs, "interestCalculationInPeriodMethod");
			EnumOptionData interestCalculationPeriodType = LoanEnumerations.interestCalculationPeriodType(interestCalculationPeriodTypeId);
			
			DateTime createdOn = JdbcSupport.getDateTime(rs, "createdon");
			DateTime lastModifedOn = JdbcSupport.getDateTime(rs, "modifiedon");

			return new LoanProductData(createdOn, lastModifedOn, id, name, description, principalMoney, toleranceMoney, 
					numberOfRepayments, repaymentEvery, interestRatePerPeriod, annualInterestRate, 
					repaymentFrequencyType, interestRateFrequencyType, amortizationType, interestType, interestCalculationPeriodType);
		}

		private CurrencyData findCurrencyByCode(String currencyCode, List<CurrencyData> allowedCurrencies) {
			CurrencyData match = null;
			for (CurrencyData currencyData : allowedCurrencies) {
				if (currencyData.getCode().equalsIgnoreCase(currencyCode)) {
					match = currencyData;
					break;
				}
			}			
			return match;
		}
	}

	private void populateProductDataWithDropdownOptions(LoanProductData productData) {

		List<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();
		List<EnumOptionData> amortizationTypeOptions = dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
		List<EnumOptionData> interestTypeOptions = dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
		List<EnumOptionData> interestCalculationPeriodTypeOptions = dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();
		List<EnumOptionData> repaymentFrequencyTypeOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
		List<EnumOptionData> interestRateFrequencyTypeOptions = dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();

		productData.setCurrencyOptions(currencyOptions);
		productData.setAmortizationTypeOptions(amortizationTypeOptions);
		productData.setInterestTypeOptions(interestTypeOptions);
		productData.setInterestCalculationPeriodTypeOptions(interestCalculationPeriodTypeOptions);
		productData.setRepaymentFrequencyTypeOptions(repaymentFrequencyTypeOptions);
		productData.setInterestRateFrequencyTypeOptions(interestRateFrequencyTypeOptions);
	}
	
}