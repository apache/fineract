package org.mifosplatform.portfolio.loanproduct.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanproduct.domain.AccountingRuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class LoanProductCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("name", "description", "fundId",
            "transactionProcessingStrategyId", "currencyCode", "digitsAfterDecimal", "principal", "inArrearsTolerance",
            "interestRatePerPeriod", "repaymentEvery", "numberOfRepayments", "repaymentFrequencyType", "interestRateFrequencyType",
            "amortizationType", "interestType", "interestCalculationPeriodType", "charges", "locale", "accountingRule",
            LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
            LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue()));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanProductCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanproduct");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String name = fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final String description = fromApiJsonHelper.extractStringNamed("description", element);
        baseDataValidator.reset().parameter("description").value(description).notExceedingLengthOf(500);

        if (fromApiJsonHelper.parameterExists("fundId", element)) {
            final Long fundId = fromApiJsonHelper.extractLongNamed("fundId", element);
            baseDataValidator.reset().parameter("fundId").value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element);
        baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notBlank().notExceedingLengthOf(3);

        final Integer digitsAfterDecimal = fromApiJsonHelper.extractIntegerNamed("digitsAfterDecimal", element, Locale.getDefault());
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);

        final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        baseDataValidator.reset().parameter("principal").value(principal).notNull().positiveAmount();

        final BigDecimal inArrearsTolerance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
        baseDataValidator.reset().parameter("inArrearsTolerance").value(inArrearsTolerance).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
        baseDataValidator.reset().parameter("interestRatePerPeriod").value(interestRatePerPeriod).notNull().zeroOrPositiveAmount();

        final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        baseDataValidator.reset().parameter("numberOfRepayments").value(numberOfRepayments).notNull().integerGreaterThanZero();

        final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
        baseDataValidator.reset().parameter("repaymentEvery").value(repaymentEvery).notNull().integerGreaterThanZero();

        final Integer repaymentFrequencyType = fromApiJsonHelper
                .extractIntegerNamed("repaymentFrequencyType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("repaymentFrequencyType").value(repaymentFrequencyType).notNull().inMinMaxRange(0, 3);

        final Long transactionProcessingStrategyId = fromApiJsonHelper.extractLongNamed("transactionProcessingStrategyId", element);
        baseDataValidator.reset().parameter("transactionProcessingStrategyId").value(transactionProcessingStrategyId).notNull()
                .integerGreaterThanZero();

        final Integer interestRateFrequencyType = fromApiJsonHelper.extractIntegerNamed("interestRateFrequencyType", element,
                Locale.getDefault());
        baseDataValidator.reset().parameter("interestRateFrequencyType").value(interestRateFrequencyType).notNull().inMinMaxRange(0, 3);

        final Integer amortizationType = fromApiJsonHelper.extractIntegerNamed("amortizationType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("amortizationType").value(amortizationType).notNull().inMinMaxRange(0, 1);

        final Integer interestType = fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestType").value(interestType).notNull().inMinMaxRange(0, 1);

        final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerNamed("interestCalculationPeriodType", element,
                Locale.getDefault());
        baseDataValidator.reset().parameter("interestCalculationPeriodType").value(interestCalculationPeriodType).notNull()
                .inMinMaxRange(0, 1);

        // accounting related data validation
        final Integer accountingRuleType = fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        baseDataValidator.reset().parameter("accountingRule").value(accountingRuleType).notNull().inMinMaxRange(1, 3);

        if (isCashBasedAccounting(accountingRuleType) || isAccrualBasedAccounting(accountingRuleType)) {

            final Long fundAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).value(fundAccountId).notNull()
                    .integerGreaterThanZero();

            final Long loanPortfolioAccountId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue()).value(loanPortfolioAccountId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromInterestId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue()).value(incomeFromInterestId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromFeeId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(),
                    element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromPenaltyId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                    .notNull().integerGreaterThanZero();

            final Long writeOffAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
                    element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue()).value(writeOffAccountId)
                    .notNull().integerGreaterThanZero();

        }

        if (isAccrualBasedAccounting(accountingRuleType)) {

            final Long receivableInterestAccountId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue())
                    .value(receivableInterestAccountId).notNull().integerGreaterThanZero();

            final Long receivableFeeAccountId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue()).value(receivableFeeAccountId)
                    .notNull().integerGreaterThanZero();

            final Long receivablePenaltyAccountId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue())
                    .value(receivablePenaltyAccountId).notNull().integerGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanproduct");

        final JsonElement element = fromApiJsonHelper.parse(json);
        if (fromApiJsonHelper.parameterExists("name", element)) {
            final String name = fromApiJsonHelper.extractStringNamed("name", element);
            baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);
        }

        if (fromApiJsonHelper.parameterExists("description", element)) {
            final String description = fromApiJsonHelper.extractStringNamed("description", element);
            baseDataValidator.reset().parameter("description").value(description).notExceedingLengthOf(500);
        }

        if (fromApiJsonHelper.parameterExists("fundId", element)) {
            final Long fundId = fromApiJsonHelper.extractLongNamed("fundId", element);
            baseDataValidator.reset().parameter("fundId").value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        if (fromApiJsonHelper.parameterExists("currencyCode", element)) {
            final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element);
            baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notBlank().notExceedingLengthOf(3);
        }

        if (fromApiJsonHelper.parameterExists("digitsAfterDecimal", element)) {
            final Integer digitsAfterDecimal = fromApiJsonHelper.extractIntegerNamed("digitsAfterDecimal", element, Locale.getDefault());
            baseDataValidator.reset().parameter("digitsAfterDecimal").value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);
        }

        if (fromApiJsonHelper.parameterExists("principal", element)) {
            final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
            baseDataValidator.reset().parameter("principal").value(principal).notNull().positiveAmount();
        }

        if (fromApiJsonHelper.parameterExists("inArrearsTolerance", element)) {
            final BigDecimal inArrearsTolerance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("inArrearsTolerance", element);
            baseDataValidator.reset().parameter("inArrearsTolerance").value(inArrearsTolerance).ignoreIfNull().zeroOrPositiveAmount();
        }

        if (fromApiJsonHelper.parameterExists("interestRatePerPeriod", element)) {
            final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRatePerPeriod", element);
            baseDataValidator.reset().parameter("interestRatePerPeriod").value(interestRatePerPeriod).notNull().zeroOrPositiveAmount();
        }

        if (fromApiJsonHelper.parameterExists("numberOfRepayments", element)) {
            final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
            baseDataValidator.reset().parameter("numberOfRepayments").value(numberOfRepayments).notNull().integerGreaterThanZero();
        }

        if (fromApiJsonHelper.parameterExists("repaymentEvery", element)) {
            final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed("repaymentEvery", element);
            baseDataValidator.reset().parameter("repaymentEvery").value(repaymentEvery).notNull().integerGreaterThanZero();
        }

        if (fromApiJsonHelper.parameterExists("repaymentFrequencyType", element)) {
            final Integer repaymentFrequencyType = fromApiJsonHelper.extractIntegerNamed("repaymentFrequencyType", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("repaymentFrequencyType").value(repaymentFrequencyType).notNull().inMinMaxRange(0, 3);
        }

        if (fromApiJsonHelper.parameterExists("transactionProcessingStrategyId", element)) {
            final Long transactionProcessingStrategyId = fromApiJsonHelper.extractLongNamed("transactionProcessingStrategyId", element);
            baseDataValidator.reset().parameter("transactionProcessingStrategyId").value(transactionProcessingStrategyId).notNull()
                    .integerGreaterThanZero();
        }

        if (fromApiJsonHelper.parameterExists("interestRateFrequencyType", element)) {
            final Integer interestRateFrequencyType = fromApiJsonHelper.extractIntegerNamed("interestRateFrequencyType", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("interestRateFrequencyType").value(interestRateFrequencyType).notNull().inMinMaxRange(0, 3);
        }

        if (fromApiJsonHelper.parameterExists("amortizationType", element)) {
            final Integer amortizationType = fromApiJsonHelper.extractIntegerNamed("amortizationType", element, Locale.getDefault());
            baseDataValidator.reset().parameter("amortizationType").value(amortizationType).notNull().inMinMaxRange(0, 1);
        }

        if (fromApiJsonHelper.parameterExists("interestType", element)) {
            final Integer interestType = fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
            baseDataValidator.reset().parameter("interestType").value(interestType).notNull().inMinMaxRange(0, 1);
        }

        if (fromApiJsonHelper.parameterExists("interestCalculationPeriodType", element)) {
            final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerNamed("interestCalculationPeriodType", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("interestCalculationPeriodType").value(interestCalculationPeriodType).notNull()
                    .inMinMaxRange(0, 1);
        }

        final Integer accountingRuleType = fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        baseDataValidator.reset().parameter("accountingRule").value(accountingRuleType).ignoreIfNull().inMinMaxRange(1, 3);

        final Long fundAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).value(fundAccountId).ignoreIfNull()
                .integerGreaterThanZero();

        final Long loanPortfolioAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(),
                element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue()).value(loanPortfolioAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromInterestId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(),
                element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue()).value(incomeFromInterestId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromFeeId = fromApiJsonHelper
                .extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long incomeFromPenaltyId = fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long writeOffAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
                element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue()).value(writeOffAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long receivableInterestAccountId = fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue())
                .value(receivableInterestAccountId).ignoreIfNull().integerGreaterThanZero();

        final Long receivableFeeAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(),
                element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue()).value(receivableFeeAccountId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long receivablePenaltyAccountId = fromApiJsonHelper.extractLongNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), element);
        baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue())
                .value(receivablePenaltyAccountId).ignoreIfNull().integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private boolean isCashBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.CASH_BASED.getValue().equals(accountingRuleType);
    }

    private boolean isAccrualBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.ACCRUAL_BASED.getValue().equals(accountingRuleType);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}