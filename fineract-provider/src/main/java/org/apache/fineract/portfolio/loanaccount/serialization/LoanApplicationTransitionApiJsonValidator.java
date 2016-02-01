/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanaccount.api.LoanApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class LoanApplicationTransitionApiJsonValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanApplicationTransitionApiJsonValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateApproval(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> disbursementParameters = new HashSet<>(Arrays.asList(LoanApiConstants.approvedLoanAmountParameterName,
                LoanApiConstants.approvedOnDateParameterName, LoanApiConstants.noteParameterName, LoanApiConstants.localeParameterName,
                LoanApiConstants.dateFormatParameterName,LoanApiConstants.disbursementDataParameterName,LoanApiConstants.disbursementDateParameterName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanapplication");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                LoanApiConstants.approvedLoanAmountParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.approvedLoanAmountParameterName).value(principal).ignoreIfNull()
                .positiveAmount();

        final LocalDate approvedOnDate = this.fromApiJsonHelper
                .extractLocalDateNamed(LoanApiConstants.approvedOnDateParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.approvedOnDateParameterName).value(approvedOnDate).notNull();

        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper
                .extractLocalDateNamed(LoanApiConstants.disbursementDateParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.disbursementDateParameterName).value(expectedDisbursementDate).ignoreIfNull();

        final String note = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.noteParameterName).value(note).notExceedingLengthOf(1000);

       throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateRejection(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> disbursementParameters = new HashSet<>(Arrays.asList(LoanApiConstants.rejectedOnDateParameterName,
                LoanApiConstants.noteParameterName, LoanApiConstants.localeParameterName, LoanApiConstants.dateFormatParameterName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanapplication");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate rejectedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.rejectedOnDateParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.rejectedOnDateParameterName).value(rejectedOnDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.noteParameterName).value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateApplicantWithdrawal(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Set<String> disbursementParameters = new HashSet<>(Arrays.asList(LoanApiConstants.withdrawnOnDateParameterName,
                LoanApiConstants.noteParameterName, LoanApiConstants.localeParameterName, LoanApiConstants.dateFormatParameterName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanapplication");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate withdrawnOnDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.withdrawnOnDateParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.withdrawnOnDateParameterName).value(withdrawnOnDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.noteParameterName).value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
}