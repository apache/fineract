package org.mifosplatform.portfolio.savings.data;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.accountNoParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.activationDateParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.activeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.annualFeeAmountParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.annualFeeOnMonthDayParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.clientIdParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.externalIdParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.groupIdParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.productIdParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.withdrawalFeeAmountParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.withdrawalFeeTypeParamName;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.savings.api.SavingsApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class SavingsAccountDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingsAccountDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SavingsApiConstants.SAVINGS_ACCOUNT_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final JsonElement element = fromApiJsonHelper.parse(json);
        final Long clientId = fromApiJsonHelper.extractLongNamed(clientIdParamName, element);
        if (clientId != null) {
            baseDataValidator.reset().parameter(clientIdParamName).value(clientId).longGreaterThanZero();
        }

        final Long groupId = fromApiJsonHelper.extractLongNamed(groupIdParamName, element);
        if (groupId != null) {
            baseDataValidator.reset().parameter(groupIdParamName).value(groupId).longGreaterThanZero();
        }

        if (clientId == null && groupId == null) {
            baseDataValidator.reset().parameter(clientIdParamName).value(clientId).notNull().integerGreaterThanZero();
        }

        final Long productId = fromApiJsonHelper.extractLongNamed(productIdParamName, element);
        baseDataValidator.reset().parameter(productIdParamName).value(productId).notNull().integerGreaterThanZero();

        final Boolean active = fromApiJsonHelper.extractBooleanNamed(activeParamName, element);
        baseDataValidator.reset().parameter(activeParamName).value(active).notNull();

        LocalDate activationDate = null;
        if (active != null && active.booleanValue()) {
            activationDate = fromApiJsonHelper.extractLocalDateNamed(activationDateParamName, element);
            baseDataValidator.reset().parameter(activationDateParamName).value(activationDate).notNull();
        }

        if (fromApiJsonHelper.parameterExists(accountNoParamName, element)) {
            final String accountNo = fromApiJsonHelper.extractStringNamed(accountNoParamName, element);
            baseDataValidator.reset().parameter(accountNoParamName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        if (fromApiJsonHelper.parameterExists(externalIdParamName, element)) {
            final String externalId = fromApiJsonHelper.extractStringNamed(externalIdParamName, element);
            baseDataValidator.reset().parameter(externalIdParamName).value(externalId).notExceedingLengthOf(100);
        }

        if (fromApiJsonHelper.parameterExists(nominalAnnualInterestRateParamName, element)) {
            final BigDecimal interestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(nominalAnnualInterestRateParamName, element);
            baseDataValidator.reset().parameter(nominalAnnualInterestRateParamName).value(interestRate).notNull().zeroOrPositiveAmount();
        }

        if (fromApiJsonHelper.parameterExists(interestCompoundingPeriodTypeParamName, element)) {
            final Integer interestCompoundingPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    interestCompoundingPeriodTypeParamName, element);
            baseDataValidator.reset().parameter(interestCompoundingPeriodTypeParamName).value(interestCompoundingPeriodType).notNull()
                    .isOneOfTheseValues(new Object[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        }

        if (fromApiJsonHelper.parameterExists(interestPostingPeriodTypeParamName, element)) {
            final Integer interestPostingPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(interestPostingPeriodTypeParamName,
                    element);
            baseDataValidator.reset().parameter(interestPostingPeriodTypeParamName).value(interestPostingPeriodType).notNull()
                    .isOneOfTheseValues(new Object[] { 4, 5, 6, 7 });
        }

        if (fromApiJsonHelper.parameterExists(interestCalculationTypeParamName, element)) {
            final Integer interestCalculationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(interestCalculationTypeParamName,
                    element);
            baseDataValidator.reset().parameter(interestCalculationTypeParamName).value(interestCalculationType).notNull()
                    .isOneOfTheseValues(new Object[] { 1, 2 });
        }

        if (fromApiJsonHelper.parameterExists(interestCalculationDaysInYearTypeParamName, element)) {
            final Integer interestCalculationDaysInYearType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    interestCalculationDaysInYearTypeParamName, element);
            baseDataValidator.reset().parameter(interestCalculationDaysInYearTypeParamName).value(interestCalculationDaysInYearType)
                    .notNull().isOneOfTheseValues(360, 365);
        }

        if (this.fromApiJsonHelper.parameterExists(minRequiredOpeningBalanceParamName, element)) {
            final BigDecimal minOpeningBalance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minRequiredOpeningBalanceParamName,
                    element);
            baseDataValidator.reset().parameter(minRequiredOpeningBalanceParamName).value(minOpeningBalance).positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(lockinPeriodFrequencyParamName, element)) {

            final Integer lockinPeriodFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName, element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).ignoreIfNull()
                    .positiveAmount();

            if (lockinPeriodFrequency != null) {
                final Integer lockinPeriodFrequencyType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                        lockinPeriodFrequencyTypeParamName, element);
                baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).notNull()
                        .inMinMaxRange(1, 3);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(lockinPeriodFrequencyTypeParamName, element)) {
            final Integer lockinPeriodFrequencyType = fromApiJsonHelper.extractIntegerSansLocaleNamed(lockinPeriodFrequencyTypeParamName,
                    element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).inMinMaxRange(1, 3);

            if (lockinPeriodFrequencyType != null) {
                final Integer lockinPeriodFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName,
                        element);
                baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).notNull().positiveAmount();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(withdrawalFeeAmountParamName, element)) {

            final BigDecimal withdrawalFeeAmount = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(withdrawalFeeAmountParamName, element);
            baseDataValidator.reset().parameter(withdrawalFeeAmountParamName).value(withdrawalFeeAmount).positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(withdrawalFeeTypeParamName, element)) {
            final Integer withdrawalFeeType = fromApiJsonHelper.extractIntegerSansLocaleNamed(withdrawalFeeTypeParamName, element);
            baseDataValidator.reset().parameter(withdrawalFeeTypeParamName).value(withdrawalFeeType).isOneOfTheseValues(1, 2);
        }

        if (this.fromApiJsonHelper.parameterExists(annualFeeAmountParamName, element)) {
            final BigDecimal annualFeeAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(annualFeeAmountParamName, element);
            baseDataValidator.reset().parameter(annualFeeAmountParamName).value(annualFeeAmount).notNull().positiveAmount();

            if (annualFeeAmount != null) {
                MonthDay monthDayOfAnnualFee = fromApiJsonHelper.extractMonthDayNamed(annualFeeOnMonthDayParamName, element);
                baseDataValidator.reset().parameter(annualFeeOnMonthDayParamName).value(monthDayOfAnnualFee).notNull();
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SavingsApiConstants.SAVINGS_ACCOUNT_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final JsonElement element = fromApiJsonHelper.parse(json);

        Long clientId = null;
        if (fromApiJsonHelper.parameterExists(clientIdParamName, element)) {
            clientId = fromApiJsonHelper.extractLongNamed(clientIdParamName, element);
            baseDataValidator.reset().parameter(clientIdParamName).value(clientId).ignoreIfNull().longGreaterThanZero();

            Long groupId = null;
            if (fromApiJsonHelper.parameterExists(productIdParamName, element)) {
                groupId = fromApiJsonHelper.extractLongNamed(groupIdParamName, element);
                baseDataValidator.reset().parameter(groupIdParamName).value(groupId).ignoreIfNull().longGreaterThanZero();
            }

            if (clientId == null && groupId == null) {
                // either clientId or groupId must exists if param passed for
                // update.
                baseDataValidator.reset().parameter(clientIdParamName).value(clientId).notNull().integerGreaterThanZero();
            }
        }

        Long groupId = null;
        if (fromApiJsonHelper.parameterExists(groupIdParamName, element)) {
            groupId = fromApiJsonHelper.extractLongNamed(groupIdParamName, element);
            baseDataValidator.reset().parameter(groupIdParamName).value(groupId).ignoreIfNull().longGreaterThanZero();

            if (fromApiJsonHelper.parameterExists(clientIdParamName, element)) {
                clientId = fromApiJsonHelper.extractLongNamed(clientIdParamName, element);
                baseDataValidator.reset().parameter(clientIdParamName).value(clientId).ignoreIfNull().longGreaterThanZero();
            }

            if (clientId == null && groupId == null) {
                // either clientId or groupId must exists if param passed for
                // update.
                baseDataValidator.reset().parameter(clientIdParamName).value(clientId).notNull().integerGreaterThanZero();
            }
        }

        if (fromApiJsonHelper.parameterExists(productIdParamName, element)) {
            final Long productId = fromApiJsonHelper.extractLongNamed(productIdParamName, element);
            baseDataValidator.reset().parameter(productIdParamName).value(productId).notNull().integerGreaterThanZero();
        }

        if (fromApiJsonHelper.parameterExists(accountNoParamName, element)) {
            final String accountNo = fromApiJsonHelper.extractStringNamed(accountNoParamName, element);
            baseDataValidator.reset().parameter(accountNoParamName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        if (fromApiJsonHelper.parameterExists(externalIdParamName, element)) {
            final String externalId = fromApiJsonHelper.extractStringNamed(externalIdParamName, element);
            baseDataValidator.reset().parameter(externalIdParamName).value(externalId).notExceedingLengthOf(100);
        }

        if (fromApiJsonHelper.parameterExists(nominalAnnualInterestRateParamName, element)) {
            final BigDecimal interestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(nominalAnnualInterestRateParamName, element);
            baseDataValidator.reset().parameter(nominalAnnualInterestRateParamName).value(interestRate).notNull().zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(interestCompoundingPeriodTypeParamName, element)) {
            final Integer interestCompoundingPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    interestCompoundingPeriodTypeParamName, element);
            baseDataValidator.reset().parameter(interestCompoundingPeriodTypeParamName).value(interestCompoundingPeriodType).notNull()
                    .isOneOfTheseValues(new Object[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        }

        if (this.fromApiJsonHelper.parameterExists(interestPostingPeriodTypeParamName, element)) {
            final Integer interestPostingPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(interestPostingPeriodTypeParamName,
                    element);
            baseDataValidator.reset().parameter(interestPostingPeriodTypeParamName).value(interestPostingPeriodType).notNull()
                    .isOneOfTheseValues(new Object[] { 4, 5, 6, 7 });
        }

        if (this.fromApiJsonHelper.parameterExists(interestCalculationTypeParamName, element)) {
            final Integer interestCalculationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(interestCalculationTypeParamName,
                    element);
            baseDataValidator.reset().parameter(interestCalculationTypeParamName).value(interestCalculationType).notNull()
                    .isOneOfTheseValues(new Object[] { 1, 2 });
        }

        if (this.fromApiJsonHelper.parameterExists(interestCalculationDaysInYearTypeParamName, element)) {
            final Integer interestCalculationDaysInYearType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    interestCalculationDaysInYearTypeParamName, element);
            baseDataValidator.reset().parameter(interestCalculationDaysInYearTypeParamName).value(interestCalculationDaysInYearType)
                    .notNull().isOneOfTheseValues(360, 365);
        }

        if (this.fromApiJsonHelper.parameterExists(minRequiredOpeningBalanceParamName, element)) {
            final BigDecimal minOpeningBalance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(minRequiredOpeningBalanceParamName,
                    element);
            baseDataValidator.reset().parameter(minRequiredOpeningBalanceParamName).value(minOpeningBalance).positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(lockinPeriodFrequencyParamName, element)) {

            final Integer lockinPeriodFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName, element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).ignoreIfNull()
                    .positiveAmount();

            if (lockinPeriodFrequency != null) {
                final Integer lockinPeriodFrequencyType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                        lockinPeriodFrequencyTypeParamName, element);
                baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).notNull()
                        .inMinMaxRange(1, 3);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(lockinPeriodFrequencyTypeParamName, element)) {
            final Integer lockinPeriodFrequencyType = fromApiJsonHelper.extractIntegerSansLocaleNamed(lockinPeriodFrequencyTypeParamName,
                    element);
            baseDataValidator.reset().parameter(lockinPeriodFrequencyTypeParamName).value(lockinPeriodFrequencyType).inMinMaxRange(1, 3);

            if (lockinPeriodFrequencyType != null) {
                final Integer lockinPeriodFrequency = fromApiJsonHelper.extractIntegerWithLocaleNamed(lockinPeriodFrequencyParamName,
                        element);
                baseDataValidator.reset().parameter(lockinPeriodFrequencyParamName).value(lockinPeriodFrequency).notNull().positiveAmount();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(withdrawalFeeAmountParamName, element)) {

            final BigDecimal withdrawalFeeAmount = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(withdrawalFeeAmountParamName, element);
            baseDataValidator.reset().parameter(withdrawalFeeAmountParamName).value(withdrawalFeeAmount).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(withdrawalFeeTypeParamName, element)) {
            final Integer withdrawalFeeType = fromApiJsonHelper.extractIntegerSansLocaleNamed(withdrawalFeeTypeParamName, element);
            baseDataValidator.reset().parameter(withdrawalFeeTypeParamName).value(withdrawalFeeType).isOneOfTheseValues(1, 2);
        }

        if (this.fromApiJsonHelper.parameterExists(annualFeeAmountParamName, element)) {
            final BigDecimal annualFeeAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(annualFeeAmountParamName, element);
            baseDataValidator.reset().parameter(annualFeeAmountParamName).value(annualFeeAmount).ignoreIfNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(annualFeeOnMonthDayParamName, element)) {
            MonthDay monthDayOfAnnualFee = fromApiJsonHelper.extractMonthDayNamed(annualFeeOnMonthDayParamName, element);
            baseDataValidator.reset().parameter(annualFeeOnMonthDayParamName).value(monthDayOfAnnualFee).ignoreIfNull();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}