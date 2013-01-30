package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.data.TransactionProcessingStrategyData;

/**
 * Immutable data object representing loan account data.
 */
@SuppressWarnings("unused")
public class LoanAccountData {

    // basic loan details
    private final Long id;
    private final String accountNo;
    private final String externalId;
    private final Long clientId;
    private final String clientName;
    private final Long groupId;
    private final String groupName;
    private final Long loanProductId;
    private final String loanProductName;
    private final String loanProductDescription;
    private final EnumOptionData status;
    private final Long fundId;
    private final String fundName;
    private final Long loanOfficerId;
    private final String loanOfficerName;
    private final CurrencyData currency;
    private final BigDecimal principal;
    private final BigDecimal inArrearsTolerance;

    private final Integer termFrequency;
    private final EnumOptionData termPeriodFrequencyType;
    private final Integer numberOfRepayments;
    private final Integer repaymentEvery;
    private final EnumOptionData repaymentFrequencyType;
    private final Integer transactionProcessingStrategyId;
    private final EnumOptionData amortizationType;
    private final BigDecimal interestRatePerPeriod;
    private final EnumOptionData interestRateFrequencyType;
    private final BigDecimal annualInterestRate;
    private final EnumOptionData interestType;
    private final EnumOptionData interestCalculationPeriodType;

    private final LocalDate submittedOnDate;
    private final LocalDate approvedOnDate;
    private final LocalDate expectedDisbursementDate;
    private final LocalDate actualDisbursementDate;
    private final LocalDate repaymentsStartingFromDate;
    private final LocalDate interestChargedFromDate;
    private final LocalDate closedOnDate;
    private final LocalDate expectedMaturityDate;
    private final LocalDate lifeCycleStatusDate;

    // associations
    private final LoanScheduleData repaymentSchedule;
    private final Collection<LoanTransactionData> transactions;
    private final Collection<LoanChargeData> charges;
    private final LoanPermissionData permissions;
    private final LoanConvenienceData convenienceData;
    private final Collection<GuarantorData> guarantors;

    // template
    private final Collection<StaffData> loanOfficerOptions;
    private final Collection<LoanProductData> productOptions;
    private final Collection<EnumOptionData> termFrequencyTypeOptions;
    private final Collection<EnumOptionData> repaymentFrequencyTypeOptions;
    private final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions;
    private final Collection<EnumOptionData> interestRateFrequencyTypeOptions;
    private final Collection<EnumOptionData> amortizationTypeOptions;
    private final Collection<EnumOptionData> interestTypeOptions;
    private final Collection<EnumOptionData> interestCalculationPeriodTypeOptions;
    private final Collection<FundData> fundOptions;
    private final Collection<ChargeData> chargeOptions;
    private final ChargeData chargeTemplate;

    public LoanAccountData(final LoanBasicDetailsData basicDetails, final boolean convenienceDataRequired,
            final LoanScheduleData repaymentSchedule, final Collection<LoanTransactionData> transactions,
            final LoanPermissionData permissions, final Collection<LoanChargeData> charges,
            final Collection<LoanProductData> productOptions, final Collection<EnumOptionData> termFrequencyTypeOptions,
            final Collection<EnumOptionData> repaymentFrequencyTypeOptions,
            final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions,
            final Collection<EnumOptionData> interestRateFrequencyTypeOptions, final Collection<EnumOptionData> amortizationTypeOptions,
            final Collection<EnumOptionData> interestTypeOptions, final Collection<EnumOptionData> interestCalculationPeriodTypeOptions,
            final Collection<FundData> fundOptions, final Collection<ChargeData> chargeOptions, final ChargeData chargeTemplate,
            final Collection<StaffData> loanOfficerOptions, final Collection<GuarantorData> guarantors) {
        this.repaymentSchedule = repaymentSchedule;
        this.transactions = transactions;
        this.permissions = permissions;
        this.charges = charges;
        this.productOptions = productOptions;
        this.termFrequencyTypeOptions = termFrequencyTypeOptions;
        this.repaymentFrequencyTypeOptions = repaymentFrequencyTypeOptions;
        this.repaymentStrategyOptions = repaymentStrategyOptions;
        this.interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions;
        this.amortizationTypeOptions = amortizationTypeOptions;
        this.interestTypeOptions = interestTypeOptions;
        this.interestCalculationPeriodTypeOptions = interestCalculationPeriodTypeOptions;
        this.fundOptions = fundOptions;
        this.chargeOptions = chargeOptions;
        this.chargeTemplate = chargeTemplate;
        this.loanOfficerOptions = loanOfficerOptions;
        this.guarantors = guarantors;

        if (convenienceDataRequired) {
            int maxSubmittedOnOffsetFromToday = basicDetails.getMaxSubmittedOnOffsetFromToday();
            int maxApprovedOnOffsetFromToday = basicDetails.getMaxApprovedOnOffsetFromToday();
            int maxDisbursedOnOffsetFromToday = basicDetails.getMaxDisbursedOnOffsetFromToday();
            int expectedLoanTermInDays = basicDetails.getLoanTermInDays();
            int expectedLoanTermInMonths = basicDetails.getLoanTermInMonths();
            int actualLoanTermInDays = basicDetails.getActualLoanTermInDays();
            int actualLoanTermInMonths = basicDetails.getActualLoanTermInMonths();

            this.convenienceData = new LoanConvenienceData(maxSubmittedOnOffsetFromToday, maxApprovedOnOffsetFromToday,
                    maxDisbursedOnOffsetFromToday, expectedLoanTermInDays, actualLoanTermInDays, expectedLoanTermInMonths,
                    actualLoanTermInMonths);
        } else {
            this.convenienceData = null;
        }

        this.id = basicDetails.getId();
        this.accountNo = basicDetails.getAccountNo();
        this.externalId = basicDetails.getExternalId();
        this.clientId = basicDetails.getClientId();
        this.clientName = basicDetails.getClientName();
        this.groupId = basicDetails.getGroupId();
        this.groupName = basicDetails.getGroupName();
        this.loanProductId = basicDetails.getLoanProductId();
        this.loanProductName = basicDetails.getLoanProductName();
        this.loanProductDescription = basicDetails.getLoanProductDescription();
        this.fundId = basicDetails.getFundId();
        this.fundName = basicDetails.getFundName();
        this.loanOfficerId = basicDetails.getLoanOfficerId();
        this.loanOfficerName = basicDetails.getLoanOfficerName();

        this.submittedOnDate = basicDetails.getSubmittedOnDate();
        this.approvedOnDate = basicDetails.getApprovedOnDate();
        this.expectedDisbursementDate = basicDetails.getExpectedDisbursementDate();
        this.actualDisbursementDate = basicDetails.getActualDisbursementDate();
        this.closedOnDate = basicDetails.getClosedOnDate();
        this.expectedMaturityDate = basicDetails.getExpectedMaturityDate();
        this.repaymentsStartingFromDate = basicDetails.getRepaymentsStartingFromDate();
        this.interestChargedFromDate = basicDetails.getInterestChargedFromDate();

        this.currency = basicDetails.getCurrency();
        this.principal = basicDetails.getPrincipal();
        this.inArrearsTolerance = basicDetails.getInArrearsTolerance();

        this.termFrequency = basicDetails.getTermFrequency();
        this.termPeriodFrequencyType = basicDetails.getTermPeriodFrequencyType();
        this.numberOfRepayments = basicDetails.getNumberOfRepayments();
        this.repaymentEvery = basicDetails.getRepaymentEvery();
        this.transactionProcessingStrategyId = basicDetails.getTransactionStrategyId();
        this.interestRatePerPeriod = basicDetails.getInterestRatePerPeriod();
        this.annualInterestRate = basicDetails.getAnnualInterestRate();
        this.repaymentFrequencyType = basicDetails.getRepaymentFrequencyType();
        this.interestRateFrequencyType = basicDetails.getInterestRateFrequencyType();
        this.amortizationType = basicDetails.getAmortizationType();
        this.interestType = basicDetails.getInterestType();
        this.interestCalculationPeriodType = basicDetails.getInterestCalculationPeriodType();

        this.status = basicDetails.getStatus();
        this.lifeCycleStatusDate = basicDetails.getLifeCycleStatusDate();
    }
}