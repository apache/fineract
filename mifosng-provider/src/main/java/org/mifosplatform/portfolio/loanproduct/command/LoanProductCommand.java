package org.mifosplatform.portfolio.loanproduct.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for creating or updating details of a loan product.
 */
public class LoanProductCommand {

    private final String name;
    private final String description;
    private final Long fundId;
    private final Long transactionProcessingStrategyId;
    private final String currencyCode;
    private final Integer digitsAfterDecimal;
    private final BigDecimal principal;
    private final BigDecimal inArrearsTolerance;
    private final Integer numberOfRepayments;
    private final Integer repaymentEvery;
    private final BigDecimal interestRatePerPeriod;
    private final Integer repaymentFrequencyType;
    private final Integer interestRateFrequencyType;
    private final Integer amortizationType;
    private final Integer interestType;
    private final Integer interestCalculationPeriodType;
    private final String[] charges;

    public LoanProductCommand(final String name, final String description, final Long fundId, final Long transactionProcessingStrategyId,
            final String currencyCode, final Integer digitsAfterDecimal, final BigDecimal principal, final BigDecimal inArrearsTolerance,
            final Integer numberOfRepayments, final Integer repaymentEvery, final BigDecimal interestRatePerPeriod,
            final Integer repaymentFrequencyType, final Integer interestRateFrequencyType, final Integer amortizationType,
            final Integer interestType, final Integer interestCalculationPeriodType, final String[] charges) {
        this.name = name;
        this.description = description;
        this.fundId = fundId;
        this.transactionProcessingStrategyId = transactionProcessingStrategyId;
        this.currencyCode = currencyCode;
        this.digitsAfterDecimal = digitsAfterDecimal;
        this.principal = principal;
        if (inArrearsTolerance != null && BigDecimal.ZERO.compareTo(inArrearsTolerance) == 0) {
            this.inArrearsTolerance = null;
        } else {
            this.inArrearsTolerance = inArrearsTolerance;
        }
        this.numberOfRepayments = numberOfRepayments;
        this.repaymentEvery = repaymentEvery;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.repaymentFrequencyType = repaymentFrequencyType;
        this.interestRateFrequencyType = interestRateFrequencyType;
        this.amortizationType = amortizationType;
        this.interestType = interestType;
        this.interestCalculationPeriodType = interestCalculationPeriodType;
        this.charges = charges;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getFundId() {
        return fundId;
    }

    public Long getTransactionProcessingStrategyId() {
        return transactionProcessingStrategyId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Integer getDigitsAfterDecimal() {
        return digitsAfterDecimal;
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

    public String[] getCharges() {
        return charges;
    }

    public void validateForCreate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("product");

        baseDataValidator.reset().parameter("name").value(this.name).notBlank();
        baseDataValidator.reset().parameter("description").value(this.description).notExceedingLengthOf(500);
        baseDataValidator.reset().parameter("currencyCode").value(this.currencyCode).notBlank();
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(this.digitsAfterDecimal).notNull().inMinMaxRange(0, 6);

        baseDataValidator.reset().parameter("principal").value(this.principal).notNull().positiveAmount();
        baseDataValidator.reset().parameter("inArrearsTolerance").value(this.inArrearsTolerance).ignoreIfNull().zeroOrPositiveAmount();

        baseDataValidator.reset().parameter("repaymentFrequencyType").value(this.repaymentFrequencyType).notNull().inMinMaxRange(0, 3);
        baseDataValidator.reset().parameter("repaymentEvery").value(this.repaymentEvery).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("numberOfRepayments").value(this.numberOfRepayments).notNull().integerGreaterThanZero();

        baseDataValidator.reset().parameter("interestRatePerPeriod").value(this.interestRatePerPeriod).notNull();
        baseDataValidator.reset().parameter("interestRateFrequencyType").value(this.interestRateFrequencyType).notNull()
                .inMinMaxRange(0, 3);

        baseDataValidator.reset().parameter("amortizationType").value(this.amortizationType).notNull().inMinMaxRange(0, 1);
        baseDataValidator.reset().parameter("interestType").value(this.interestType).notNull().inMinMaxRange(0, 1);
        baseDataValidator.reset().parameter("interestCalculationPeriodType").value(this.interestCalculationPeriodType).notNull()
                .inMinMaxRange(0, 1);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("product");

        baseDataValidator.reset().parameter("name").value(this.name).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("description").value(this.description).ignoreIfNull().notExceedingLengthOf(500);
        baseDataValidator.reset().parameter("currencyCode").value(this.currencyCode).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(this.digitsAfterDecimal).ignoreIfNull().notNull()
                .inMinMaxRange(0, 6);

        baseDataValidator.reset().parameter("principal").value(this.principal).ignoreIfNull().notNull().positiveAmount();
        baseDataValidator.reset().parameter("inArrearsTolerance").value(this.inArrearsTolerance).ignoreIfNull().notNull()
                .zeroOrPositiveAmount();

        baseDataValidator.reset().parameter("repaymentFrequencyType").value(this.repaymentFrequencyType).ignoreIfNull().notNull()
                .inMinMaxRange(0, 3);

        baseDataValidator.reset().parameter("repaymentEvery").value(this.repaymentEvery).ignoreIfNull().notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("numberOfRepayments").value(this.numberOfRepayments).ignoreIfNull().notNull()
                .integerGreaterThanZero();

        baseDataValidator.reset().parameter("interestRatePerPeriod").value(this.interestRatePerPeriod).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("interestRateFrequencyType").value(this.interestRateFrequencyType).ignoreIfNull()
                .notNull().inMinMaxRange(0, 3);

        baseDataValidator.reset().parameter("amortizationType").value(this.amortizationType).ignoreIfNull().notNull()
                .inMinMaxRange(0, 1);
        baseDataValidator.reset().parameter("interestType").value(this.interestType).ignoreIfNull().notNull().inMinMaxRange(0, 1);
        baseDataValidator.reset().parameter("interestCalculationPeriodType").value(this.interestCalculationPeriodType)
                .ignoreIfNull().notNull().inMinMaxRange(0, 1);

        baseDataValidator.reset().anyOfNotNull(this.fundId, this.transactionProcessingStrategyId, this.name,
                this.description, this.currencyCode, this.digitsAfterDecimal, this.principal,
                this.inArrearsTolerance, this.repaymentFrequencyType, this.repaymentEvery,
                this.numberOfRepayments, this.interestRatePerPeriod, this.interestRateFrequencyType,
                this.amortizationType, this.interestType, this.interestCalculationPeriodType, this.charges);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}