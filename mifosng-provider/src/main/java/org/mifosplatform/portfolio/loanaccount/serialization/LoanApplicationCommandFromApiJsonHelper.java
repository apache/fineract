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
public final class LoanApplicationCommandFromApiJsonHelper {

    /**
     * The parameters supported for this command.
     */
    final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "clientId", "groupId", "productId", "accountNo",
            "externalId", "fundId", "loanOfficerId", "transactionProcessingStrategyId", "principal", "inArrearsTolerance",
            "repaymentEvery", "numberOfRepayments", "loanTermFrequency", "loanTermFrequencyType", "charges", "repaymentFrequencyType",
            "interestRatePerPeriod", "interestRateFrequencyType", "interestType", "interestCalculationPeriodType", "amortizationType",
            "expectedDisbursementDate", "repaymentsStartingFromDate", "interestChargedFromDate", "submittedOnDate", "submittedOnNote",
            "locale", "dateFormat"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanApplicationCommandFromApiJsonHelper(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final JsonElement element = fromApiJsonHelper.parse(json);
        final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
        if (clientId != null) {
            baseDataValidator.reset().parameter("clientId").value(clientId).longGreaterThanZero();
        }

        final Long groupId = fromApiJsonHelper.extractLongNamed("groupId", element);
        if (groupId != null) {
            baseDataValidator.reset().parameter("groupId").value(groupId).longGreaterThanZero();
        }

        if (clientId == null && groupId == null) {
            baseDataValidator.reset().parameter("clientId").value(clientId).notNull().integerGreaterThanZero();
        }

        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        baseDataValidator.reset().parameter("productId").value(productId).notNull().integerGreaterThanZero();

        final String accountNoParameterName = "accountNo";
        if (fromApiJsonHelper.parameterExists(accountNoParameterName, element)) {
            final String accountNo = fromApiJsonHelper.extractStringNamed(accountNoParameterName, element);
            baseDataValidator.reset().parameter(accountNoParameterName).value(accountNo).ignoreIfNull().notExceedingLengthOf(20);
        }

        final String externalIdParameterName = "externalId";
        if (fromApiJsonHelper.parameterExists(externalIdParameterName, element)) {
            final String externalId = fromApiJsonHelper.extractStringNamed(externalIdParameterName, element);
            baseDataValidator.reset().parameter(externalIdParameterName).value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        }

        final String fundIdParameterName = "fundId";
        if (fromApiJsonHelper.parameterExists(fundIdParameterName, element)) {
            final Long fundId = fromApiJsonHelper.extractLongNamed(fundIdParameterName, element);
            baseDataValidator.reset().parameter(fundIdParameterName).value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        final String loanOfficerIdParameterName = "loanOfficerId";
        if (fromApiJsonHelper.parameterExists(loanOfficerIdParameterName, element)) {
            final Long loanOfficerId = fromApiJsonHelper.extractLongNamed(loanOfficerIdParameterName, element);
            baseDataValidator.reset().parameter(loanOfficerIdParameterName).value(loanOfficerId).ignoreIfNull().integerGreaterThanZero();
        }

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

        final String repaymentsStartingFromDateParameterName = "repaymentsStartingFromDate";
        if (fromApiJsonHelper.parameterExists(repaymentsStartingFromDateParameterName, element)) {
            final LocalDate repaymentsStartingFromDate = fromApiJsonHelper.extractLocalDateNamed(repaymentsStartingFromDateParameterName,
                    element);
            baseDataValidator.reset().parameter(repaymentsStartingFromDateParameterName).value(repaymentsStartingFromDate).ignoreIfNull().notNull();
        }

        final String interestChargedFromDateParameterName = "interestChargedFromDate";
        if (fromApiJsonHelper.parameterExists(interestChargedFromDateParameterName, element)) {
            final LocalDate interestChargedFromDate = fromApiJsonHelper
                    .extractLocalDateNamed(interestChargedFromDateParameterName, element);
            baseDataValidator.reset().parameter(interestChargedFromDateParameterName).value(interestChargedFromDate).ignoreIfNull().notNull();
        }

        final String inArrearsToleranceParameterName = "inArrearsTolerance";
        if (fromApiJsonHelper.parameterExists(inArrearsToleranceParameterName, element)) {
            final BigDecimal inArrearsTolerance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(inArrearsToleranceParameterName,
                    element);
            baseDataValidator.reset().parameter(inArrearsToleranceParameterName).value(inArrearsTolerance).ignoreIfNull().zeroOrPositiveAmount();
        }

        final String submittedOnDateParameterName = "submittedOnDate";
        final LocalDate submittedOnDate = fromApiJsonHelper.extractLocalDateNamed(submittedOnDateParameterName, element);
        if (submittedOnDate == null) {
            baseDataValidator.reset().parameter(submittedOnDateParameterName).value(submittedOnDate).notNull();
        }

        final String submittedOnNoteParameterName = "submittedOnNote";
        if (fromApiJsonHelper.parameterExists(submittedOnNoteParameterName, element)) {
            final String submittedOnNote = fromApiJsonHelper.extractStringNamed(submittedOnNoteParameterName, element);
            baseDataValidator.reset().parameter(submittedOnNoteParameterName).value(submittedOnNote).ignoreIfNull().notExceedingLengthOf(500);
        }

        // not optional at present but might make it so to allow it to pick up
        // on only available strategy
        // by default which would probably be the norm.
        final String transactionProcessingStrategyIdParameterName = "transactionProcessingStrategyId";
        final Long transactionProcessingStrategyId = fromApiJsonHelper.extractLongNamed(transactionProcessingStrategyIdParameterName,
                element);
        baseDataValidator.reset().parameter(transactionProcessingStrategyIdParameterName).value(transactionProcessingStrategyId).notNull()
                .integerGreaterThanZero();

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

    public void validateForModify(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        final JsonElement element = fromApiJsonHelper.parse(json);
        boolean atLeastOneParameterPassedForUpdate = false;

        final String clientIdParameterName = "clientId";
        if (fromApiJsonHelper.parameterExists(clientIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long clientId = fromApiJsonHelper.extractLongNamed(clientIdParameterName, element);
            baseDataValidator.reset().parameter(clientIdParameterName).value(clientId).notNull().integerGreaterThanZero();
        }

        final String groupIdParameterName = "groupId";
        if (fromApiJsonHelper.parameterExists(groupIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long groupId = fromApiJsonHelper.extractLongNamed(groupIdParameterName, element);
            baseDataValidator.reset().parameter(groupIdParameterName).value(groupId).notNull().integerGreaterThanZero();
        }

        final String productIdParameterName = "productId";
        if (fromApiJsonHelper.parameterExists(productIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long productId = fromApiJsonHelper.extractLongNamed(productIdParameterName, element);
            baseDataValidator.reset().parameter(productIdParameterName).value(productId).notNull().integerGreaterThanZero();
        }

        final String accountNoParameterName = "accountNo";
        if (fromApiJsonHelper.parameterExists(accountNoParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String accountNo = fromApiJsonHelper.extractStringNamed(accountNoParameterName, element);
            baseDataValidator.reset().parameter(accountNoParameterName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        final String externalIdParameterName = "externalId";
        if (fromApiJsonHelper.parameterExists(externalIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String externalId = fromApiJsonHelper.extractStringNamed(externalIdParameterName, element);
            baseDataValidator.reset().parameter(externalIdParameterName).value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        }

        final String fundIdParameterName = "fundId";
        if (fromApiJsonHelper.parameterExists(fundIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long fundId = fromApiJsonHelper.extractLongNamed(fundIdParameterName, element);
            baseDataValidator.reset().parameter(fundIdParameterName).value(fundId).ignoreIfNull().integerGreaterThanZero();
        }

        final String loanOfficerIdParameterName = "loanOfficerId";
        if (fromApiJsonHelper.parameterExists(loanOfficerIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long loanOfficerId = fromApiJsonHelper.extractLongNamed(loanOfficerIdParameterName, element);
            baseDataValidator.reset().parameter(loanOfficerIdParameterName).value(loanOfficerId).ignoreIfNull().integerGreaterThanZero();
        }

        final String transactionProcessingStrategyIdParameterName = "transactionProcessingStrategyId";
        if (fromApiJsonHelper.parameterExists(transactionProcessingStrategyIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long transactionProcessingStrategyId = fromApiJsonHelper.extractLongNamed(transactionProcessingStrategyIdParameterName,
                    element);
            baseDataValidator.reset().parameter(transactionProcessingStrategyIdParameterName).value(transactionProcessingStrategyId)
                    .notNull().integerGreaterThanZero();
        }

        final String principalParameterName = "principal";
        if (fromApiJsonHelper.parameterExists(principalParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalParameterName, element);
            baseDataValidator.reset().parameter(principalParameterName).value(principal).notNull().positiveAmount();
        }

        final String inArrearsToleranceParameterName = "inArrearsTolerance";
        if (fromApiJsonHelper.parameterExists(inArrearsToleranceParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(inArrearsToleranceParameterName, element);
            baseDataValidator.reset().parameter(inArrearsToleranceParameterName).value(principal).ignoreIfNull().positiveAmount();
        }

        final String loanTermFrequencyParameterName = "loanTermFrequency";
        if (fromApiJsonHelper.parameterExists(loanTermFrequencyParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer loanTermFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyParameterName, element);
            baseDataValidator.reset().parameter(loanTermFrequencyParameterName).value(loanTermFrequency).notNull().integerGreaterThanZero();
        }

        final String loanTermFrequencyTypeParameterName = "loanTermFrequencyType";
        if (fromApiJsonHelper.parameterExists(loanTermFrequencyTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer loanTermFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed(loanTermFrequencyTypeParameterName,
                    element);
            baseDataValidator.reset().parameter(loanTermFrequencyTypeParameterName).value(loanTermFrequencyType).notNull()
                    .inMinMaxRange(0, 3);
        }

        final String numberOfRepaymentsParameterName = "numberOfRepayments";
        if (fromApiJsonHelper.parameterExists(numberOfRepaymentsParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer numberOfRepayments = fromApiJsonHelper.extractIntegerWithLocaleNamed(numberOfRepaymentsParameterName, element);
            baseDataValidator.reset().parameter(numberOfRepaymentsParameterName).value(numberOfRepayments).notNull()
                    .integerGreaterThanZero();
        }

        final String repaymentEveryParameterName = "repaymentEvery";
        if (fromApiJsonHelper.parameterExists(repaymentEveryParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer repaymentEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryParameterName, element);
            baseDataValidator.reset().parameter(repaymentEveryParameterName).value(repaymentEvery).notNull().integerGreaterThanZero();
        }

        final String repaymentEveryTypeParameterName = "repaymentFrequencyType";
        if (fromApiJsonHelper.parameterExists(repaymentEveryTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer repaymentEveryType = fromApiJsonHelper.extractIntegerWithLocaleNamed(repaymentEveryTypeParameterName, element);
            baseDataValidator.reset().parameter(repaymentEveryTypeParameterName).value(repaymentEveryType).notNull().inMinMaxRange(0, 3);
        }
        
        final String interestRatePerPeriodParameterName = "interestRatePerPeriod";
        if (fromApiJsonHelper.parameterExists(interestRatePerPeriodParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final BigDecimal interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(interestRatePerPeriodParameterName,
                    element);
            baseDataValidator.reset().parameter(interestRatePerPeriodParameterName).value(interestRatePerPeriod).notNull().positiveAmount();
        }

        final String interestRateFrequencyTypeParameterName = "interestRateFrequencyType";
        if (fromApiJsonHelper.parameterExists(interestRateFrequencyTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer interestRateFrequencyType = fromApiJsonHelper.extractIntegerWithLocaleNamed(
                    interestRateFrequencyTypeParameterName, element);
            baseDataValidator.reset().parameter(interestRateFrequencyTypeParameterName).value(interestRateFrequencyType).notNull()
                    .inMinMaxRange(0, 3);
        }

        final String interestTypeParameterName = "interestType";
        if (fromApiJsonHelper.parameterExists(interestTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer interestType = fromApiJsonHelper.extractIntegerWithLocaleNamed(interestTypeParameterName, element);
            baseDataValidator.reset().parameter(interestTypeParameterName).value(interestType).notNull().inMinMaxRange(0, 1);
        }

        final String interestCalculationPeriodTypeParameterName = "interestCalculationPeriodType";
        if (fromApiJsonHelper.parameterExists(interestCalculationPeriodTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer interestCalculationPeriodType = fromApiJsonHelper.extractIntegerWithLocaleNamed(
                    interestCalculationPeriodTypeParameterName, element);
            baseDataValidator.reset().parameter(interestCalculationPeriodTypeParameterName).value(interestCalculationPeriodType).notNull()
                    .inMinMaxRange(0, 1);
        }

        final String amortizationTypeParameterName = "amortizationType";
        if (fromApiJsonHelper.parameterExists(amortizationTypeParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer amortizationType = fromApiJsonHelper.extractIntegerWithLocaleNamed(amortizationTypeParameterName, element);
            baseDataValidator.reset().parameter(amortizationTypeParameterName).value(amortizationType).notNull().inMinMaxRange(0, 1);
        }

        final String expectedDisbursementDateParameterName = "expectedDisbursementDate";
        if (fromApiJsonHelper.parameterExists(expectedDisbursementDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;

            final String expectedDisbursementDateStr = fromApiJsonHelper.extractStringNamed(expectedDisbursementDateParameterName, element);
            baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDateStr).notBlank();

            final LocalDate expectedDisbursementDate = fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName,
                    element);
            baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDate).notNull();
        }

        final String repaymentsStartingFromDateParameterName = "repaymentsStartingFromDate";
        if (fromApiJsonHelper.parameterExists(repaymentsStartingFromDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate repaymentsStartingFromDate = fromApiJsonHelper.extractLocalDateNamed(repaymentsStartingFromDateParameterName,
                    element);
            baseDataValidator.reset().parameter(repaymentsStartingFromDateParameterName).value(repaymentsStartingFromDate).ignoreIfNull();
        }

        final String interestChargedFromDateParameterName = "interestChargedFromDate";
        if (fromApiJsonHelper.parameterExists(interestChargedFromDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate interestChargedFromDate = fromApiJsonHelper
                    .extractLocalDateNamed(interestChargedFromDateParameterName, element);
            baseDataValidator.reset().parameter(interestChargedFromDateParameterName).value(interestChargedFromDate).ignoreIfNull();
        }

        final String submittedOnDateParameterName = "submittedOnDate";
        if (fromApiJsonHelper.parameterExists(submittedOnDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate submittedOnDate = fromApiJsonHelper.extractLocalDateNamed(submittedOnDateParameterName, element);
            baseDataValidator.reset().parameter(submittedOnDateParameterName).value(submittedOnDate).notNull();
        }

        final String submittedOnNoteParameterName = "submittedOnNote";
        if (fromApiJsonHelper.parameterExists(submittedOnNoteParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String submittedOnNote = fromApiJsonHelper.extractStringNamed(submittedOnNoteParameterName, element);
            baseDataValidator.reset().parameter(submittedOnNoteParameterName).value(submittedOnNote).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }

        if (!atLeastOneParameterPassedForUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public LoanChargeCommand[] extractLoanCharges(final String json) {
        final JsonElement element = fromApiJsonHelper.parse(json);
        return extractInToLoanChargeCommands(element);
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