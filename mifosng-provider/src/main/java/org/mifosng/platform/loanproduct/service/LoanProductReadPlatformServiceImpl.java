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
import org.mifosng.data.LoanProductData;
import org.mifosng.data.MoneyData;
import org.mifosng.platform.currency.service.CurrencyReadPlatformService;
import org.mifosng.platform.exceptions.PlatformResourceNotFoundException;
import org.mifosng.platform.infrastructure.JdbcSupport;
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
			throw new PlatformResourceNotFoundException("error.msg.loanproduct.id.invalid", "Loan product with identifier {0} does not exist.", loanProductId);
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

		populateProductDataWithDropdownOptions(productData);

		if (productData.getPossibleCurrencies().size() >= 1) {
			CurrencyData currency = productData.getPossibleCurrencies().get(0);
			MoneyData zero = MoneyData.zero(currency);
			productData.setPrincipalMoney(zero);
			productData.setInArrearsTolerance(zero);
		}

		productData.setAmortizationMethod(Integer.valueOf(1));
		productData.setInterestMethod(Integer.valueOf(0));
		productData.setRepaymentPeriodFrequency(2);
		productData.setInterestRatePeriod(2);
		productData.setInterestRateCalculatedInPeriod(1);

		productData.setRepaidEvery(1);
		productData.setNumberOfRepayments(0);

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

			BigDecimal interestRatePerPeriod = rs.getBigDecimal("interestRatePerPeriod");
			Integer interestRatePeriod = JdbcSupport.getInteger(rs, "interestRatePerPeriodFreq");
			BigDecimal annualInterestRate = rs.getBigDecimal("annualInterestRate");
			Integer interestMethod = JdbcSupport.getInteger(rs, "interestMethod");
			Integer interestCalculationInPeriodMethod = JdbcSupport.getInteger(rs, "interestCalculationInPeriodMethod");

			Integer repaidEvery = JdbcSupport.getInteger(rs, "repaidEvery");
			Integer repaymentFrequency = JdbcSupport.getInteger(rs, "repaymentPeriodFrequency");
			Integer numberOfRepayments = JdbcSupport.getInteger(rs, "numberOfRepayments");
			Integer amortizationMethod = JdbcSupport.getInteger(rs, "amortizationMethod");

			DateTime createdOn = JdbcSupport.getDateTime(rs, "createdon");
			DateTime lastModifedOn = JdbcSupport.getDateTime(rs, "modifiedon");

			return new LoanProductData(id, name, description, principalMoney,
					interestRatePerPeriod, interestRatePeriod,
					annualInterestRate, interestMethod, interestCalculationInPeriodMethod,
					repaidEvery,
					repaymentFrequency, numberOfRepayments, amortizationMethod,
					toleranceMoney, createdOn, lastModifedOn);
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

		List<CurrencyData> possibleCurrencies = currencyReadPlatformService.retrieveAllowedCurrencies();

		List<EnumOptionData> possibleAmortizationOptions = dropdownReadPlatformService.retrieveLoanAmortizationMethodOptions();
		List<EnumOptionData> possibleInterestOptions = dropdownReadPlatformService.retrieveLoanInterestMethodOptions();
		List<EnumOptionData> possibleInterestRateCalculatedInPeriodOptions = dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();
		List<EnumOptionData> repaymentFrequencyOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyOptions();
		List<EnumOptionData> interestFrequencyOptions = dropdownReadPlatformService.retrieveInterestFrequencyOptions();

		productData.setPossibleCurrencies(possibleCurrencies);
		productData.setPossibleAmortizationOptions(possibleAmortizationOptions);
		productData.setPossibleInterestOptions(possibleInterestOptions);
		productData.setPossibleInterestRateCalculatedInPeriodOptions(possibleInterestRateCalculatedInPeriodOptions);
		productData.setRepaymentFrequencyOptions(repaymentFrequencyOptions);
		productData.setInterestFrequencyOptions(interestFrequencyOptions);
	}
}