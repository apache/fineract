package org.mifosng.platform.loanproduct.service;

import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.LoanStatus;
import org.mifosng.platform.loan.domain.LoanTransactionType;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;

public class LoanEnumerations {

	public static EnumOptionData loanTermFrequencyType(final int id) {
		return loanTermFrequencyType(PeriodFrequencyType.fromInt(id));
	}
	
	public static EnumOptionData loanTermFrequencyType(final PeriodFrequencyType type) {
		final String codePrefix = "loanTermFrequency.";
		EnumOptionData optionData = null;
		switch (type) {
		case DAYS:
			optionData = new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(), codePrefix + PeriodFrequencyType.DAYS.getCode(), "Days");
			break;
		case WEEKS:
			optionData = new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix + PeriodFrequencyType.WEEKS.getCode(), "Weeks");
			break;
		case MONTHS:
			optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix + PeriodFrequencyType.MONTHS.getCode(), "Months");
			break;
		case YEARS:
			optionData = new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(), codePrefix + PeriodFrequencyType.YEARS.getCode(), "Years");
			break;
		default:
			optionData = new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(), PeriodFrequencyType.INVALID.getCode(), "Invalid");
			break;
		}
		return optionData;
	}
	
	public static EnumOptionData termFrequencyType(final int id) {
		return termFrequencyType(PeriodFrequencyType.fromInt(id));
	}
	
	public static EnumOptionData termFrequencyType(final PeriodFrequencyType type) {
		final String codePrefix = "termFrequency.";
		EnumOptionData optionData = null;
		switch (type) {
		case DAYS:
			optionData = new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(), codePrefix + PeriodFrequencyType.DAYS.getCode(), "Days");
			break;
		case WEEKS:
			optionData = new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix + PeriodFrequencyType.WEEKS.getCode(), "Weeks");
			break;
		case MONTHS:
			optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix + PeriodFrequencyType.MONTHS.getCode(), "Months");
			break;
		default:
			optionData = new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(), PeriodFrequencyType.INVALID.getCode(), "Invalid");
			break;
		}
		return optionData;
	}
	
	public static EnumOptionData repaymentFrequencyType(final int id) {
		return repaymentFrequencyType(PeriodFrequencyType.fromInt(id));
	}
	
	public static EnumOptionData repaymentFrequencyType(final PeriodFrequencyType type) {
		final String codePrefix = "repaymentFrequency.";
		EnumOptionData optionData = null;
		switch (type) {
		case DAYS:
			optionData = new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(), codePrefix + PeriodFrequencyType.DAYS.getCode(), "Days");
			break;
		case WEEKS:
			optionData = new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix + PeriodFrequencyType.WEEKS.getCode(), "Weeks");
			break;
		case MONTHS:
			optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix + PeriodFrequencyType.MONTHS.getCode(), "Months");
			break;
		default:
			optionData = new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(), PeriodFrequencyType.INVALID.getCode(), "Invalid");
			break;
		}
		return optionData;
	}
	
	public static EnumOptionData interestRateFrequencyType(final int id) {
		return interestRateFrequencyType(PeriodFrequencyType.fromInt(id));
	}
	
	public static EnumOptionData interestRateFrequencyType(final PeriodFrequencyType type) {
		final String codePrefix = "interestRateFrequency.";
		EnumOptionData optionData = null;
		switch (type) {
		case MONTHS:
			optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix + PeriodFrequencyType.MONTHS.getCode(), "Per month");
			break;
		case YEARS:
			optionData = new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(), codePrefix + PeriodFrequencyType.YEARS.getCode(), "Per year");
			break;
		default:
			optionData = new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(), PeriodFrequencyType.INVALID.getCode(), "Invalid");
			break;
		}
		return optionData;
	}
	
	public static EnumOptionData amortizationType(Integer id) {
		return amortizationType(AmortizationMethod.fromInt(id));
	}
	
	public static EnumOptionData amortizationType(AmortizationMethod amortizationMethod) {
		EnumOptionData optionData = null;
		switch (amortizationMethod) {
		case EQUAL_INSTALLMENTS:
			optionData = new EnumOptionData(AmortizationMethod.EQUAL_INSTALLMENTS.getValue().longValue(), AmortizationMethod.EQUAL_INSTALLMENTS.getCode(), "Equal installments");
			break;
		case EQUAL_PRINCIPAL:
			optionData = new EnumOptionData(AmortizationMethod.EQUAL_PRINCIPAL.getValue().longValue(), AmortizationMethod.EQUAL_PRINCIPAL.getCode(), "Equal principle payments");
			break;
		default:
			optionData = new EnumOptionData(AmortizationMethod.INVALID.getValue().longValue(), AmortizationMethod.INVALID.getCode(), "Invalid");
			break;
		}
		return optionData;
	}
	
	public static EnumOptionData interestType(Integer id) {
		return interestType(InterestMethod.fromInt(id));
	}
	
	public static EnumOptionData interestType(InterestMethod type) {
		EnumOptionData optionData = null;
		switch (type) {
		case FLAT:
			optionData = new EnumOptionData(InterestMethod.FLAT.getValue().longValue(), InterestMethod.FLAT.getCode(), "Flat");
			break;
		case DECLINING_BALANCE:
			optionData = new EnumOptionData(InterestMethod.DECLINING_BALANCE.getValue().longValue(), InterestMethod.DECLINING_BALANCE.getCode(), "Declining Balance");
			break;
		default:
			optionData = new EnumOptionData(InterestMethod.INVALID.getValue().longValue(), InterestMethod.INVALID.getCode(), "Invalid");
			break;
		}
		return optionData;
	}
	
	public static EnumOptionData interestCalculationPeriodType(Integer id) {
		return interestCalculationPeriodType(InterestCalculationPeriodMethod.fromInt(id));
	}
	
	public static EnumOptionData interestCalculationPeriodType(InterestCalculationPeriodMethod type) {
		EnumOptionData optionData = null;
		switch (type) {
		case DAILY:
			optionData= new EnumOptionData(InterestCalculationPeriodMethod.DAILY.getValue().longValue(), InterestCalculationPeriodMethod.DAILY.getCode(), "Daily");
			break;
		case SAME_AS_REPAYMENT_PERIOD:
			optionData = new EnumOptionData(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD.getValue().longValue(), InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD.getCode(), "Same as repayment period");
			break;
		default:
			optionData = new EnumOptionData(InterestCalculationPeriodMethod.INVALID.getValue().longValue(), InterestCalculationPeriodMethod.INVALID.getCode(), "Invalid");
			break;
		}
		return optionData;
	}
	
	public static EnumOptionData transactionType(final Integer id) {
		return transactionType(LoanTransactionType.fromInt(id));
	}
	
	public static EnumOptionData transactionType(final LoanTransactionType type) {
		EnumOptionData optionData = null;
		switch (type) {
		case DISBURSEMENT:
			optionData= new EnumOptionData(LoanTransactionType.DISBURSEMENT.getValue().longValue(), LoanTransactionType.DISBURSEMENT.getCode(), "Dibursement");
			break;
		case REPAYMENT:
			optionData = new EnumOptionData(LoanTransactionType.REPAYMENT.getValue().longValue(), LoanTransactionType.REPAYMENT.getCode(), "Repayment");
			break;
		case REVERSAL:
			optionData = new EnumOptionData(LoanTransactionType.REVERSAL.getValue().longValue(), LoanTransactionType.REVERSAL.getCode(), "Reversal");
			break;
		case WAIVED:
			optionData = new EnumOptionData(LoanTransactionType.WAIVED.getValue().longValue(), LoanTransactionType.WAIVED.getCode(), "Waiver");
			break;
		default:
			optionData = new EnumOptionData(LoanTransactionType.INVALID.getValue().longValue(), LoanTransactionType.INVALID.getCode(), "Invalid");
			break;
		}
		return optionData;
	}

	public static EnumOptionData status(final Integer statusId) {
		return status(LoanStatus.fromInt(statusId));
	}
	
	public static EnumOptionData status(final LoanStatus status) {
		EnumOptionData optionData = null;
		switch (status) {
		case SUBMITED_AND_PENDING_APPROVAL:
			optionData = new EnumOptionData(LoanStatus.SUBMITED_AND_PENDING_APPROVAL.getValue().longValue(), LoanStatus.SUBMITED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval");
			break;
		case APPROVED:
			optionData = new EnumOptionData(LoanStatus.APPROVED.getValue().longValue(), LoanStatus.APPROVED.getCode(), "Approved");
			break;
		case ACTIVE:
			optionData = new EnumOptionData(LoanStatus.ACTIVE.getValue().longValue(), LoanStatus.ACTIVE.getCode(), "Active");
			break;
		case REJECTED:
			optionData = new EnumOptionData(LoanStatus.REJECTED.getValue().longValue(), LoanStatus.REJECTED.getCode(), "Rejected");
			break;
		case WITHDRAWN_BY_CLIENT:
			optionData = new EnumOptionData(LoanStatus.WITHDRAWN_BY_CLIENT.getValue().longValue(), LoanStatus.WITHDRAWN_BY_CLIENT.getCode(), "Withdrawn by applicant");
			break;
		case CLOSED:
			optionData = new EnumOptionData(LoanStatus.CLOSED.getValue().longValue(), LoanStatus.CLOSED.getCode(), "Closed");
			break;
		case OVERPAID:
			optionData = new EnumOptionData(LoanStatus.OVERPAID.getValue().longValue(), LoanStatus.OVERPAID.getCode(), "Overpaid");
			break;
		default:
			optionData = new EnumOptionData(LoanStatus.INVALID.getValue().longValue(), LoanStatus.INVALID.getCode(), "Invalid");
			break;
		}
		return optionData;
	}
}