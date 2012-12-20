package org.mifosplatform.portfolio.loanaccount.loanschedule.query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;

/**
 * Immutable query used when auto-calculating loan schedules based on loan
 * terms.
 */
public class CalculateLoanScheduleQuery {

    private final Long productId;
    private final BigDecimal principal;
    private final Integer loanTermFrequency;
    private final Integer loanTermFrequencyType;
    private final Integer repaymentEvery;
    private final Integer repaymentFrequencyType;
    private final Integer numberOfRepayments;
    private final BigDecimal interestRatePerPeriod;
    private final Integer interestRateFrequencyType;
    private final Integer amortizationType;
    private final Integer interestType;
    private final Integer interestCalculationPeriodType;
    private final LocalDate expectedDisbursementDate;
    private final LocalDate repaymentsStartingFromDate;
    private final LocalDate interestChargedFromDate;
    private final LoanChargeCommand[] charges;

    public CalculateLoanScheduleQuery(final Long productId, final BigDecimal principal, final BigDecimal interestRatePerPeriod,
            Integer interestRateFrequencyMethod, final Integer interestMethod, final Integer interestCalculationPeriodMethod,
            final Integer repaymentEvery, final Integer repaymentFrequency, final Integer numberOfRepayments, Integer amortizationMethod,
            final Integer loanTermFrequency, final Integer loanTermFrequencyType, final LocalDate expectedDisbursementDate,
            final LocalDate repaymentsStartingFromDate, final LocalDate interestCalculatedFromDate, final LoanChargeCommand[] charges) {

        this.productId = productId;
        this.principal = principal;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.interestRateFrequencyType = interestRateFrequencyMethod;
        this.interestType = interestMethod;
        this.interestCalculationPeriodType = interestCalculationPeriodMethod;
        this.repaymentEvery = repaymentEvery;
        this.repaymentFrequencyType = repaymentFrequency;
        this.numberOfRepayments = numberOfRepayments;
        this.amortizationType = amortizationMethod;
        this.loanTermFrequency = loanTermFrequency;
        this.loanTermFrequencyType = loanTermFrequencyType;

        this.expectedDisbursementDate = expectedDisbursementDate;
        this.repaymentsStartingFromDate = repaymentsStartingFromDate;
        this.interestChargedFromDate = interestCalculatedFromDate;
        this.charges = charges;
    }

    public Long getProductId() {
        return productId;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public Integer getLoanTermFrequency() {
        return loanTermFrequency;
    }

    public Integer getLoanTermFrequencyType() {
        return loanTermFrequencyType;
    }

    public Integer getRepaymentEvery() {
        return repaymentEvery;
    }

    public Integer getRepaymentFrequencyType() {
        return repaymentFrequencyType;
    }

    public Integer getNumberOfRepayments() {
        return numberOfRepayments;
    }

    public BigDecimal getInterestRatePerPeriod() {
        return interestRatePerPeriod;
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

    public LoanChargeCommand[] getCharges() {
        return charges;
    }

    public List<ApiParameterError> validate() {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        baseDataValidator.reset().parameter("principal").value(this.principal).notNull().positiveAmount();

        baseDataValidator.reset().parameter("loanTermFrequency").value(this.loanTermFrequency).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("loanTermFrequencyType").value(this.loanTermFrequencyType).notNull().inMinMaxRange(0, 4);

        baseDataValidator.reset().parameter("numberOfRepayments").value(this.numberOfRepayments).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("repaymentEvery").value(this.repaymentEvery).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("repaymentFrequencyType").value(this.repaymentFrequencyType).notNull().inMinMaxRange(0, 3);

        // FIXME - this constraint doesnt really need to be here. should be
        // possible to express loan term as say 12 months whilst also saying
        // - that the repayment structure is 6 repayments every bi-monthly.
        if (this.loanTermFrequencyType != null && !this.loanTermFrequencyType.equals(this.repaymentFrequencyType)) {
            ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.loan.loanTermFrequencyType.not.the.same.as.repaymentFrequencyType",
                    "The parameters loanTermFrequencyType and repaymentFrequencyType must be the same.", "loanTermFrequencyType",
                    this.loanTermFrequencyType, this.repaymentFrequencyType);
            dataValidationErrors.add(error);
        } else {
            if (this.loanTermFrequency != null && this.repaymentEvery != null && this.numberOfRepayments != null) {
                int suggestsedLoanTerm = this.repaymentEvery * this.numberOfRepayments;
                if (this.loanTermFrequency.intValue() < suggestsedLoanTerm) {
                    ApiParameterError error = ApiParameterError
                            .parameterError(
                                    "validation.msg.loan.loanTermFrequency.less.than.repayment.structure.suggests",
                                    "The parameter loanTermFrequency is less than the suggest loan term as indicated by numberOfRepayments and repaymentEvery.",
                                    "loanTermFrequency", this.loanTermFrequency, this.numberOfRepayments, this.repaymentEvery);
                    dataValidationErrors.add(error);
                }
            }
        }

        baseDataValidator.reset().parameter("interestRatePerPeriod").value(this.interestRatePerPeriod).notNull();
        baseDataValidator.reset().parameter("interestRateFrequencyType").value(this.interestRateFrequencyType).notNull()
                .inMinMaxRange(0, 3);
        baseDataValidator.reset().parameter("amortizationType").value(this.amortizationType).notNull().inMinMaxRange(0, 1);
        baseDataValidator.reset().parameter("interestType").value(this.interestType).notNull().inMinMaxRange(0, 1);
        baseDataValidator.reset().parameter("interestCalculationPeriodType").value(this.interestCalculationPeriodType).notNull()
                .inMinMaxRange(0, 1);

        baseDataValidator.reset().parameter("expectedDisbursementDate").value(this.expectedDisbursementDate).notNull();

        if (this.expectedDisbursementDate != null) {
            if (this.repaymentsStartingFromDate != null && this.expectedDisbursementDate.isAfter(this.repaymentsStartingFromDate)) {
                ApiParameterError error = ApiParameterError.parameterError(
                        "validation.msg.loan.expectedDisbursementDate.cannot.be.after.first.repayment.date",
                        "The parameter expectedDisbursementDate has a date which falls after the given first repayment date.",
                        "expectedDisbursementDate", this.expectedDisbursementDate, this.repaymentsStartingFromDate);
                dataValidationErrors.add(error);
            }
        }

        if (this.repaymentsStartingFromDate != null && this.interestChargedFromDate == null) {

            ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.loan.interestCalculatedFromDate.must.be.entered.when.using.repayments.startfrom.field",
                    "The parameter interestCalculatedFromDate cannot be empty when first repayment date is provided.",
                    "interestChargedFromDate", this.repaymentsStartingFromDate);
            dataValidationErrors.add(error);
        } else if (this.repaymentsStartingFromDate == null && this.interestChargedFromDate != null) {

            // validate interestCalculatedFromDate is after or on
            // repaymentsStartingFromDate
            if (this.expectedDisbursementDate.isAfter(this.interestChargedFromDate)) {
                ApiParameterError error = ApiParameterError.parameterError(
                        "validation.msg.loan.interestChargedFromDate.cannot.be.before.disbursement.date",
                        "The parameter interestCalculatedFromDate cannot be before the date given for expected disbursement.",
                        "interestChargedFromDate", this.interestChargedFromDate, this.expectedDisbursementDate);
                dataValidationErrors.add(error);
            }
        }

        return dataValidationErrors;

    }
}