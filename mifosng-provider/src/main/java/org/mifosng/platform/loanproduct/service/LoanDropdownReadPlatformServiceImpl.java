package org.mifosng.platform.loanproduct.service;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.mifosng.data.EnumOptionReadModel;
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
	public List<EnumOptionReadModel> retrieveLoanAmortizationMethodOptions() {
		EnumOptionReadModel equalInstallments = new EnumOptionReadModel(
				"Equal installments", AmortizationMethod.EQUAL_INSTALLMENTS
						.getValue().longValue());
		EnumOptionReadModel equalPrinciplePayments = new EnumOptionReadModel(
				"Equal principle payments", AmortizationMethod.EQUAL_PRINCIPAL
						.getValue().longValue());

		List<EnumOptionReadModel> allowedAmortizationMethods = Arrays.asList(
				equalInstallments, equalPrinciplePayments);

		return allowedAmortizationMethods;
	}

	@Override
	public List<EnumOptionReadModel> retrieveLoanInterestMethodOptions() {
		EnumOptionReadModel interestRateCalculationMethod2 = new EnumOptionReadModel(
				"Declining Balance", Long.valueOf(0));
		EnumOptionReadModel interestRateCalculationMethod1 = new EnumOptionReadModel(
				"Flat", Long.valueOf(1));

		List<EnumOptionReadModel> allowedRepaymentScheduleCalculationMethods = Arrays
				.asList(interestRateCalculationMethod2,
						interestRateCalculationMethod1);

		return allowedRepaymentScheduleCalculationMethods;
	}

	@Override
	public List<EnumOptionReadModel> retrieveLoanInterestRateCalculatedInPeriodOptions() {

		EnumOptionReadModel option1 = new EnumOptionReadModel("Daily",
				Long.valueOf(0));
		EnumOptionReadModel option2 = new EnumOptionReadModel(
				"Same as repayment period", Long.valueOf(1));

		List<EnumOptionReadModel> allowedOptions = Arrays.asList(option1,
				option2);

		return allowedOptions;
	}

	@Override
	public List<EnumOptionReadModel> retrieveRepaymentFrequencyOptions() {
		EnumOptionReadModel frequency1 = new EnumOptionReadModel("Days",
				Long.valueOf(0));
		EnumOptionReadModel frequency2 = new EnumOptionReadModel("Weeks",
				Long.valueOf(1));
		EnumOptionReadModel frequency3 = new EnumOptionReadModel("Months",
				Long.valueOf(2));
		List<EnumOptionReadModel> repaymentFrequencyOptions = Arrays.asList(
				frequency1, frequency2, frequency3);
		return repaymentFrequencyOptions;
	}

	@Override
	public List<EnumOptionReadModel> retrieveInterestFrequencyOptions() {
		// support for monthly and annual percentage rate (MPR) and (APR)
		EnumOptionReadModel perMonth = new EnumOptionReadModel("Per month",
				PeriodFrequencyType.MONTHS.getValue().longValue());
		EnumOptionReadModel perYear = new EnumOptionReadModel("Per year",
				PeriodFrequencyType.YEARS.getValue().longValue());
		List<EnumOptionReadModel> repaymentFrequencyOptions = Arrays.asList(
				perMonth, perYear);
		return repaymentFrequencyOptions;
	}
}