package org.mifosplatform.portfolio.loanaccount.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.query.CalculateLoanScheduleQuery;

/**
 * Immutable command for submitting new loan application.
 */
public class LoanApplicationCommand {

    private final Long clientId;
    private final Long groupId;
    private final Long productId;
    @SuppressWarnings("unused")
    private final String externalId;

    @SuppressWarnings("unused")
    private final Long fundId;
    @SuppressWarnings("unused")
    private final Long loanOfficerId;
    private final Long transactionProcessingStrategyId;

    private final BigDecimal principal;
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    private final String submittedOnNote;

    private final LoanChargeCommand[] charges;

    public LoanApplicationCommand(final Long clientId, final Long groupId, final Long productId, final String externalId,
            final Long fundId, final Long transactionProcessingStrategyId, final LocalDate submittedOnDate, final String submittedOnNote,
            final LocalDate expectedDisbursementDate, final LocalDate repaymentsStartingFromDate,
            final LocalDate interestChargedFromLocalDate, final BigDecimal principal, final BigDecimal interestRatePerPeriod,
            final Integer interestRateFrequencyMethod, final Integer interestMethod, final Integer interestCalculationPeriodMethod,
            final Integer repaymentEvery, final Integer repaymentFrequency, final Integer numberOfRepayments, Integer amortizationMethod,
            final Integer loanTermFrequency, final Integer loanTermFrequencyType, final BigDecimal toleranceAmount,
            final LoanChargeCommand[] charges, final Long loanOfficerId) {
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

    public LoanChargeCommand[] getCharges() {
        return charges;
    }

    public List<ApiParameterError> validate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        if (this.groupId != null) {
            baseDataValidator.reset().parameter("clientId").value(this.clientId).mustBeBlankWhenParameterProvided("groupId", this.groupId)
                    .longGreaterThanZero();
        }
        if (this.clientId != null) {
            baseDataValidator.reset().parameter("groupId").value(this.groupId).mustBeBlankWhenParameterProvided("clientId", this.clientId)
                    .longGreaterThanZero();
        }
        
        if (this.clientId == null && this.groupId == null) {
            baseDataValidator.reset().parameter("clientId").value(this.clientId).notNull().integerGreaterThanZero();
        }
        
        baseDataValidator.reset().parameter("productId").value(this.productId).notNull().integerGreaterThanZero();

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
                    loanChargeCommand.validateForCreate();
                } catch (PlatformApiDataValidationException e) {
                    dataValidationErrors.addAll(e.getErrors());
                }
            }
        }

        return dataValidationErrors;
    }
}