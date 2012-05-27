package org.mifosng.platform.loanproduct.service;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.mifosng.data.EnumOptionData;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

/*
 * FIXME - remove hardcoded return of loan dropdown options 
 */
@SuppressWarnings("unused")
@Service
public class LoanDropdownReadPlatformServiceImpl implements LoanDropdownReadPlatformService {

	private final PlatformSecurityContext context;
	private final SimpleJdbcTemplate jdbcTemplate;

	@Autowired
	public LoanDropdownReadPlatformServiceImpl(final PlatformSecurityContext context, final DataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	@Override
	public List<EnumOptionData> retrieveLoanAmortizationMethodOptions() {
		EnumOptionData equalInstallments = new EnumOptionData(
				"Equal installments", AmortizationMethod.EQUAL_INSTALLMENTS
						.getValue().longValue());
		EnumOptionData equalPrinciplePayments = new EnumOptionData(
				"Equal principle payments", AmortizationMethod.EQUAL_PRINCIPAL
						.getValue().longValue());

		List<EnumOptionData> allowedAmortizationMethods = Arrays.asList(
				equalInstallments, equalPrinciplePayments);

		return allowedAmortizationMethods;
	}

	@Override
	public List<EnumOptionData> retrieveLoanInterestMethodOptions() {
		EnumOptionData interestRateCalculationMethod2 = new EnumOptionData(
				"Declining Balance", Long.valueOf(0));
		EnumOptionData interestRateCalculationMethod1 = new EnumOptionData(
				"Flat", Long.valueOf(1));

		List<EnumOptionData> allowedRepaymentScheduleCalculationMethods = Arrays
				.asList(interestRateCalculationMethod2,
						interestRateCalculationMethod1);

		return allowedRepaymentScheduleCalculationMethods;
	}

	@Override
	public List<EnumOptionData> retrieveLoanInterestRateCalculatedInPeriodOptions() {

		EnumOptionData option1 = new EnumOptionData("Daily",
				Long.valueOf(0));
		EnumOptionData option2 = new EnumOptionData(
				"Same as repayment period", Long.valueOf(1));

		List<EnumOptionData> allowedOptions = Arrays.asList(option1,
				option2);

		return allowedOptions;
	}

	@Override
	public List<EnumOptionData> retrieveRepaymentFrequencyOptions() {
		EnumOptionData frequency1 = new EnumOptionData("Days",
				Long.valueOf(0));
		EnumOptionData frequency2 = new EnumOptionData("Weeks",
				Long.valueOf(1));
		EnumOptionData frequency3 = new EnumOptionData("Months",
				Long.valueOf(2));
		List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(
				frequency1, frequency2, frequency3);
		return repaymentFrequencyOptions;
	}

	@Override
	public List<EnumOptionData> retrieveInterestFrequencyOptions() {
		// support for monthly and annual percentage rate (MPR) and (APR)
		EnumOptionData perMonth = new EnumOptionData("Per month",
				PeriodFrequencyType.MONTHS.getValue().longValue());
		EnumOptionData perYear = new EnumOptionData("Per year",
				PeriodFrequencyType.YEARS.getValue().longValue());
		List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(
				perMonth, perYear);
		return repaymentFrequencyOptions;
	}
}