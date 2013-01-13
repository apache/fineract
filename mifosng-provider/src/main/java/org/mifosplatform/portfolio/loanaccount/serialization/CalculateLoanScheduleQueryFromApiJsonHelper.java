package org.mifosplatform.portfolio.loanaccount.serialization;

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
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public final class CalculateLoanScheduleQueryFromApiJsonHelper {

    /**
     * The parameters supported for this command.
     */
    final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("clientId", "groupId", "productId", "accountNo",
            "externalId", "fundId", "transactionProcessingStrategyId", "principal", "inArrearsTolerance", "interestRatePerPeriod",
            "repaymentEvery", "numberOfRepayments", "loanTermFrequency", "loanTermFrequencyType", "charges", "repaymentFrequencyType",
            "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType", "expectedDisbursementDate",
            "repaymentsStartingFromDate", "interestChargedFromDate", "submittedOnDate", "submittedOnNote", "locale", "dateFormat",
            "loanOfficerId", "id"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CalculateLoanScheduleQueryFromApiJsonHelper(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        baseDataValidator.reset().parameter("productId").value(productId).notNull().integerGreaterThanZero();

        final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        baseDataValidator.reset().parameter("principal").value(principal).notNull().positiveAmount();

        final String loanTermFrequencyParameterName = "loanTermFrequency";
        final Integer loanTermFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyParameterName, element);
        baseDataValidator.reset().parameter(loanTermFrequencyParameterName).value(loanTermFrequency).notNull().integerGreaterThanZero();

        final String loanTermFrequencyTypeParameterName = "loanTermFrequencyType";
        final Integer loanTermFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyTypeParameterName, element);
        baseDataValidator.reset().parameter(loanTermFrequencyTypeParameterName).value(loanTermFrequencyType).notNull().inMinMaxRange(0, 3);

        final String numberOfRepaymentsParameterName = "numberOfRepayments";
        final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
        baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments).notNull().integerGreaterThanZero();

        final String repaymentEveryParameterName = "repaymentEvery";
        final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryParameterName, element);
        baseDataValidator.reset().parameter(repaymentEveryParameterName).value(repaymentEvery).notNull().integerGreaterThanZero();

        final String repaymentEveryFrequencyTypeParameterName = "repaymentFrequencyType";
        final Integer repaymentEveryType = fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryFrequencyTypeParameterName,
                element);
        baseDataValidator.reset().parameter(repaymentEveryFrequencyTypeParameterName).value(repaymentEveryType).notNull()
                .inMinMaxRange(0, 3);

        // FIXME - this constraint doesnt really need to be here. should be
        // possible to express loan term as say 12 months whilst also saying
        // - that the repayment structure is 6 repayments every bi-monthly.
        validateSelectedPeriodFrequencyTypeIsTheSame(dataValidationErrors, loanTermFrequency, loanTermFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentEveryType);

        final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
        final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName,
                element);
        baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod).notNull().positiveAmount();

        final String interestRateFrequencyTypeParameterName = "interestRateFrequencyType";
        final Integer interestRateFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed(interestRateFrequencyTypeParameterName,
                element);
        baseDataValidator.reset().parameter(interestRateFrequencyTypeParameterName).value(interestRateFrequencyType).notNull()
                .inMinMaxRange(0, 3);

        final String interestTypeParameterName = "interestType";
        final Integer interestType = fromApiJsonHelper.extractIntegerWithLocaleNamed(interestTypeParameterName, element);
        baseDataValidator.reset().parameter(interestTypeParameterName).value(interestType).notNull().inMinMaxRange(0, 1);

        final String interestCalculationPeriodTypeParameterName = "interestCalculationPeriodType";
        final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerWithLocaleNamed(
                interestCalculationPeriodTypeParameterName, element);
        baseDataValidator.reset().parameter(interestCalculationPeriodTypeParameterName).value(interestCalculationPeriodType).notNull()
                .inMinMaxRange(0, 1);

        final String amortizationTypeParameterName = "amortizationType";
        final Integer amortizationType = fromApiJsonHelper.extractIntegerWithLocaleNamed(amortizationTypeParameterName, element);
        baseDataValidator.reset().parameter(amortizationTypeParameterName).value(amortizationType).notNull().inMinMaxRange(0, 1);

        final String expectedDisbursementDateParameterName = "expectedDisbursementDate";
        final LocalDate expectedDisbursementDate = fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName, element);
        baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDate).notNull();

        LocalDate repaymentsStartingFromDate = null;
        final String repaymentsStartingFromDateParameterName = "repaymentsStartingFromDate";
        if (fromApiJsonHelper.parameterExists(repaymentsStartingFromDateParameterName, element)) {
            repaymentsStartingFromDate = fromApiJsonHelper.extractLocalDateNamed(repaymentsStartingFromDateParameterName, element);
            baseDataValidator.reset().parameter(repaymentsStartingFromDateParameterName).value(repaymentsStartingFromDate).ignoreIfNull().notNull();
        }

        LocalDate interestChargedFromDate = null;
        final String interestChargedFromDateParameterName = "interestChargedFromDate";
        if (fromApiJsonHelper.parameterExists(interestChargedFromDateParameterName, element)) {
            interestChargedFromDate = fromApiJsonHelper.extractLocalDateNamed(interestChargedFromDateParameterName, element);
            baseDataValidator.reset().parameter(interestChargedFromDateParameterName).value(interestChargedFromDate).ignoreIfNull().notNull();
        }

        validateRepaymentsStartingFromDateIsAfterDisbursementDate(dataValidationErrors, expectedDisbursementDate,
                repaymentsStartingFromDate);

        validateRepaymentsStartingFromDateAndInterestChargedFromDate(dataValidationErrors, expectedDisbursementDate,
                repaymentsStartingFromDate, interestChargedFromDate);

        final LoanChargeCommand[] charges = extractInToLoanChargeCommands(element);
        if (charges != null) {
            for (LoanChargeCommand loanChargeCommand : charges) {
                try {
                    loanChargeCommand.validateForCreate();
                } catch (PlatformApiDataValidationException e) {
                    dataValidationErrors.addAll(e.getErrors());
                }
            }
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    private void validateSelectedPeriodFrequencyTypeIsTheSame(final List<ApiParameterError> dataValidationErrors,
            final Integer loanTermFrequency, final Integer loanTermFrequencyType, final Integer numberOfRepayments,
            final Integer repaymentEvery, final Integer repaymentEveryType) {
        if (loanTermFrequencyType != null && !loanTermFrequencyType.equals(repaymentEveryType)) {
            ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.loan.loanTermFrequencyType.not.the.same.as.repaymentFrequencyType",
                    "The parameters loanTermFrequencyType and repaymentFrequencyType must be the same.", "loanTermFrequencyType",
                    loanTermFrequencyType, repaymentEveryType);
            dataValidationErrors.add(error);
        } else {
            if (loanTermFrequency != null && repaymentEvery != null && numberOfRepayments != null) {
                int suggestsedLoanTerm = repaymentEvery * numberOfRepayments;
                if (loanTermFrequency.intValue() < suggestsedLoanTerm) {
                    ApiParameterError error = ApiParameterError
                            .parameterError(
                                    "validation.msg.loan.loanTermFrequency.less.than.repayment.structure.suggests",
                                    "The parameter loanTermFrequency is less than the suggest loan term as indicated by numberOfRepayments and repaymentEvery.",
                                    "loanTermFrequency", loanTermFrequency, numberOfRepayments, repaymentEvery);
                    dataValidationErrors.add(error);
                }
            }
        }
    }

    private void validateRepaymentsStartingFromDateAndInterestChargedFromDate(final List<ApiParameterError> dataValidationErrors,
            final LocalDate expectedDisbursementDate, LocalDate repaymentsStartingFromDate, LocalDate interestChargedFromDate) {
        if (repaymentsStartingFromDate != null && interestChargedFromDate == null) {

            ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.loan.interestChargedFromDate.must.be.entered.when.using.repayments.startfrom.field",
                    "The parameter interestChargedFromDate cannot be empty when repaymentsStartingFromDate is provided.",
                    "interestChargedFromDate", repaymentsStartingFromDate);
            dataValidationErrors.add(error);
        } else if (repaymentsStartingFromDate == null && interestChargedFromDate != null) {

            if (expectedDisbursementDate != null && expectedDisbursementDate.isAfter(interestChargedFromDate)) {
                ApiParameterError error = ApiParameterError.parameterError(
                        "validation.msg.loan.interestChargedFromDate.cannot.be.before.disbursement.date",
                        "The parameter interestChargedFromDate cannot be before the date given for expectedDisbursementDate.",
                        "interestChargedFromDate", interestChargedFromDate, expectedDisbursementDate);
                dataValidationErrors.add(error);
            }
        }
    }

    private void validateRepaymentsStartingFromDateIsAfterDisbursementDate(final List<ApiParameterError> dataValidationErrors,
            final LocalDate expectedDisbursementDate, LocalDate repaymentsStartingFromDate) {
        if (expectedDisbursementDate != null) {
            if (repaymentsStartingFromDate != null && expectedDisbursementDate.isAfter(repaymentsStartingFromDate)) {
                ApiParameterError error = ApiParameterError.parameterError(
                        "validation.msg.loan.expectedDisbursementDate.cannot.be.after.first.repayment.date",
                        "The parameter expectedDisbursementDate has a date which falls after the date for repaymentsStartingFromDate.",
                        "expectedDisbursementDate", expectedDisbursementDate, repaymentsStartingFromDate);
                dataValidationErrors.add(error);
            }
        }
    }

    private LoanChargeCommand[] extractInToLoanChargeCommands(final JsonElement element) {
        LoanChargeCommand[] charges = null;
        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.has("charges") && topLevelJsonElement.get("charges").isJsonArray()) {

                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                charges = new LoanChargeCommand[array.size()];
                for (int i = 0; i < array.size(); i++) {

                    final JsonObject loanChargeElement = array.get(i).getAsJsonObject();

                    final Long chargeId = fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    final BigDecimal amount = fromApiJsonHelper.extractBigDecimalNamed("amount", loanChargeElement, locale);
                    final Integer chargeTimeType = fromApiJsonHelper.extractIntegerNamed("chargeTimeType", loanChargeElement, locale);
                    final Integer chargeCalculationType = fromApiJsonHelper.extractIntegerNamed("chargeCalculationType", loanChargeElement,
                            locale);
                    final LocalDate specifiedDueDate = fromApiJsonHelper.extractLocalDateNamed("specifiedDueDate", loanChargeElement,
                            dateFormat, locale);

                    charges[i] = new LoanChargeCommand(chargeId, amount, chargeTimeType, chargeCalculationType, specifiedDueDate);
                }
            }
        }
        return charges;
    }
}