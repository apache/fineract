package org.mifosplatform.portfolio.savingsaccount.serialization;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class SavingAccountStateTransitionCommandFromApiJsonDeserializer {

    private final Set<String> supportedParams = new HashSet<String>(Arrays.asList("locale", "dateFormat", "commencementDate", "eventDate",
            "savingsDepositAmountPerPeriod", "minimumBalanceForWithdrawal", "recurringInterestRate", "savingInterestRate", "depositDate",
            "interestType", "tenure", "tenureType", "frequency", "payEvery", "note", "interestPostEvery", "interestPostFrequency"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingAccountStateTransitionCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForApprove(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("saving.account.transition");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final LocalDate commencementDate = fromApiJsonHelper.extractLocalDateNamed("commencementDate", element);
        baseDataValidator.reset().parameter("commencementDate").value(commencementDate).notNull();

        final BigDecimal savingsDepositAmountPerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                "savingsDepositAmountPerPeriod", element);
        baseDataValidator.reset().parameter("savingsDepositAmountPerPeriod").value(savingsDepositAmountPerPeriod).ignoreIfNull()
                .zeroOrPositiveAmount();

        final BigDecimal minimumBalanceForWithdrawal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minimumBalanceForWithdrawal",
                element);
        baseDataValidator.reset().parameter("minimumBalanceForWithdrawal").value(minimumBalanceForWithdrawal).ignoreIfNull()
                .zeroOrPositiveAmount();

        final BigDecimal recurringInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("recurringInterestRate", element);
        baseDataValidator.reset().parameter("recurringInterestRate").value(recurringInterestRate).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal savingInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("savingInterestRate", element);
        baseDataValidator.reset().parameter("savingInterestRate").value(savingInterestRate).ignoreIfNull().zeroOrPositiveAmount();

        final Integer interestType = fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestType").value(interestType).ignoreIfNull().zeroOrPositiveAmount();

        final Integer tenureType = fromApiJsonHelper.extractIntegerNamed("tenureType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenureType").value(tenureType).ignoreIfNull().zeroOrPositiveAmount();

        final Integer tenure = fromApiJsonHelper.extractIntegerNamed("tenure", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenure").value(tenure).ignoreIfNull().zeroOrPositiveAmount();

        final Integer frequency = fromApiJsonHelper.extractIntegerNamed("frequency", element, Locale.getDefault());
        baseDataValidator.reset().parameter("frequency").value(frequency).ignoreIfNull().zeroOrPositiveAmount();

        final Integer depositEvery = fromApiJsonHelper.extractIntegerNamed("payEvery", element, Locale.getDefault());
        baseDataValidator.reset().parameter("payEvery").value(depositEvery).ignoreIfNull().zeroOrPositiveAmount();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForReject(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("saving.account.transition");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final LocalDate eventDate = fromApiJsonHelper.extractLocalDateNamed("eventDate", element);
        baseDataValidator.reset().parameter("eventDate").value(eventDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForWithdrawApplication(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("saving.account.transition");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final LocalDate eventDate = fromApiJsonHelper.extractLocalDateNamed("eventDate", element);
        baseDataValidator.reset().parameter("eventDate").value(eventDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForDepositAmount(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("saving.account.transaction.deposit");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final LocalDate depositDate = fromApiJsonHelper.extractLocalDateNamed("depositDate", element);
        baseDataValidator.reset().parameter("depositDate").value(depositDate).notNull();

        final BigDecimal savingsDepositAmountPerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                "savingsDepositAmountPerPeriod", element);
        baseDataValidator.reset().parameter("savingsDepositAmountPerPeriod").value(savingsDepositAmountPerPeriod).notNull()
                .zeroOrPositiveAmount();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}