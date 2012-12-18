package org.mifosplatform.portfolio.loanaccount.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.query.CalculateLoanScheduleQuery;

/**
 * Immutable command for submitting new loan application.
 */
public class LoanApplicationCommand {

    private final Long loanId;
    private final Long clientId;
    private final Long groupId;
    private final Long productId;
    private final String externalId;

    private final Long fundId;
    private final Long loanOfficerId;
    private final Long transactionProcessingStrategyId;

    private final BigDecimal principal;
    private final BigDecimal inArrearsTolerance;

    private final Integer loanTermFrequency;
    private final Integer loanTermFrequencyType;

    private final Integer numberOfRepayments;
    private final Integer repaymentEvery;

    private final BigDecimal interestRatePerPeriod;
    private final Integer repaymentFrequencyType;
    private final Integer interestRateFrequencyType;
    private final Integer amortizationType;
    private final Integer interestType;
    private final Integer interestCalculationPeriodType;

    private final LocalDate expectedDisbursementDate;
    private final LocalDate repaymentsStartingFromDate;
    private final LocalDate interestChargedFromDate;
    private final LocalDate submittedOnDate;
    private final String submittedOnNote;

    private final LoanChargeCommand[] charges;

    private final Set<String> modifiedParameters;

    public LoanApplicationCommand(final Set<String> modifiedParameters, final Long loanId, final Long clientId, final Long groupId,
            final Long productId, final String externalId, final Long fundId, final Long transactionProcessingStrategyId,
            final LocalDate submittedOnDate, final String submittedOnNote, final LocalDate expectedDisbursementDate,
            final LocalDate repaymentsStartingFromDate, final LocalDate interestChargedFromLocalDate, final BigDecimal principal,
            final BigDecimal interestRatePerPeriod, final Integer interestRateFrequencyMethod, final Integer interestMethod,
            final Integer interestCalculationPeriodMethod, final Integer repaymentEvery, final Integer repaymentFrequency,
            final Integer numberOfRepayments, Integer amortizationMethod, final Integer loanTermFrequency,
            final Integer loanTermFrequencyType, final BigDecimal toleranceAmount, final LoanChargeCommand[] charges,
            final Long loanOfficerId) {
        this.modifiedParameters = modifiedParameters;
        this.loanId = loanId;
        this.clientId = clientId;
        this.groupId = groupId;
        this.productId = productId;
        this.externalId = externalId;
        this.fundId = fundId;
        this.loanOfficerId = loanOfficerId;
        this.transactionProcessingStrategyId = transactionProcessingStrategyId;

        this.submittedOnDate = submittedOnDate;
        this.submittedOnNote = submittedOnNote;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.repaymentsStartingFromDate = repaymentsStartingFromDate;
        this.interestChargedFromDate = interestChargedFromLocalDate;

        this.principal = principal;
        this.loanTermFrequency = loanTermFrequency;
        this.loanTermFrequencyType = loanTermFrequencyType;
        this.inArrearsTolerance = toleranceAmount;

        this.interestRatePerPeriod = interestRatePerPeriod;
        this.interestRateFrequencyType = interestRateFrequencyMethod;
        this.interestType = interestMethod;
        this.interestCalculationPeriodType = interestCalculationPeriodMethod;
        this.repaymentEvery = repaymentEvery;
        this.repaymentFrequencyType = repaymentFrequency;
        this.numberOfRepayments = numberOfRepayments;
        this.amortizationType = amortizationMethod;

        this.charges = charges;
    }

    public CalculateLoanScheduleQuery toCalculateLoanScheduleCommand() {
        return new CalculateLoanScheduleQuery(this.productId, this.principal, this.interestRatePerPeriod, this.interestRateFrequencyType,
                this.interestType, this.interestCalculationPeriodType, this.repaymentEvery, this.repaymentFrequencyType,
                this.numberOfRepayments, this.amortizationType, this.loanTermFrequency, this.loanTermFrequencyType,
                this.expectedDisbursementDate, this.repaymentsStartingFromDate, this.interestChargedFromDate, this.charges);
    }

    public Long getLoanId() {
        return loanId;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getExternalId() {
        return externalId;
    }

    public Long getFundId() {
        return fundId;
    }

    public Long getTransactionProcessingStrategyId() {
        return transactionProcessingStrategyId;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public BigDecimal getInArrearsTolerance() {
        return inArrearsTolerance;
    }

    public Integer getNumberOfRepayments() {
        return numberOfRepayments;
    }

    public Integer getRepaymentEvery() {
        return repaymentEvery;
    }

    public Integer getLoanTermFrequency() {
        return loanTermFrequency;
    }

    public Integer getLoanTermFrequencyType() {
        return loanTermFrequencyType;
    }

    public BigDecimal getInterestRatePerPeriod() {
        return interestRatePerPeriod;
    }

    public Integer getRepaymentFrequencyType() {
        return repaymentFrequencyType;
    }

    public Integer getInterestRateFrequencyType() {
        return interestRateFrequencyType;
    }

    public Integer getAmortizationType() {
        return amortizationType;
    }

    public Integer getInterestType() {
        return interestType;
    }

    public Integer getInterestCalculationPeriodType() {
        return interestCalculationPeriodType;
    }

    public LocalDate getExpectedDisbursementDate() {
        return expectedDisbursementDate;
    }

    public LocalDate getRepaymentsStartingFromDate() {
        return repaymentsStartingFromDate;
    }

    public LocalDate getInterestChargedFromDate() {
        return interestChargedFromDate;
    }

    public LocalDate getSubmittedOnDate() {
        return submittedOnDate;
    }

    public String getSubmittedOnNote() {
        return submittedOnNote;
    }

    public LoanChargeCommand[] getCharges() {
        return charges;
    }

    public boolean isClientChanged() {
        return this.modifiedParameters.contains("clientId");
    }

    public boolean isProductChanged() {
        return this.modifiedParameters.contains("productId");
    }

    public boolean isFundChanged() {
        return this.modifiedParameters.contains("fundId");
    }

    public boolean isLoanOfficerChanged() {
        return this.modifiedParameters.contains("loanOfficerId");
    }

    public boolean isTransactionStrategyChanged() {
        return this.modifiedParameters.contains("transactionProcessingStrategyId");
    }

    public boolean isTermFrequencyChanged() {
        return this.modifiedParameters.contains("loanTermFrequency");
    }

    public boolean isTermFrequencyTypeChanged() {
        return this.modifiedParameters.contains("loanTermFrequencyType");
    }

    public boolean isSubmittedOnDateChanged() {
        return this.modifiedParameters.contains("submittedOnDate");
    }

    public boolean isExpectedDisbursementDatePassed() {
        return this.modifiedParameters.contains("expectedDisbursementDate");
    }

    public boolean isRepaymentsStartingFromDateChanged() {
        return this.modifiedParameters.contains("repaymentsStartingFromDate");
    }

    public boolean isInterestChargedFromDateChanged() {
        return this.modifiedParameters.contains("interestChargedFromDate");
    }

    public boolean isChargesChanged() {
        return this.modifiedParameters.contains("charges");
    }

    public Long getLoanOfficerId() {
        return loanOfficerId;
    }

    public void validate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        if (this.groupId != null) {
            baseDataValidator.reset().parameter("clientId").value(this.clientId).mustBeBlankWhenParameterProvided("groupId", this.groupId)
                    .longGreaterThanZero();
        }
        if (this.clientId != null) {
            baseDataValidator.reset().parameter("groupId").value(this.groupId).mustBeBlankWhenParameterProvided("clientId", this.clientId)
                    .longGreaterThanZero();
        }

        if (this.submittedOnDate == null) {
            ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.submitted.on.date.cannot.be.blank",
                    "The parameter submittedOnDate cannot be empty.", "submittedOnDate");
            dataValidationErrors.add(error);
        } else {
            if (this.submittedOnDate.isAfter(this.expectedDisbursementDate)) {
                ApiParameterError error = ApiParameterError.parameterError(
                        "validation.msg.loan.submitted.on.date.cannot.be.after.expectedDisbursementDate",
                        "The date of parameter submittedOnDate cannot fall after the date given for expectedDisbursementDate.",
                        "submittedOnDate", this.submittedOnDate, this.expectedDisbursementDate);
                dataValidationErrors.add(error);
            }
        }

        if (this.transactionProcessingStrategyId == null) {
            baseDataValidator.reset().parameter("transactionProcessingStrategyId").value(this.transactionProcessingStrategyId).notNull()
                    .inMinMaxRange(2, 2);
        }

        if (this.charges != null) {
            for (LoanChargeCommand loanChargeCommand : this.charges) {
                try {
                    LoanChargeCommandValidator validator = new LoanChargeCommandValidator(loanChargeCommand);
                    validator.validateForCreate();
                } catch (PlatformApiDataValidationException e) {
                    dataValidationErrors.addAll(e.getErrors());
                }
            }
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}