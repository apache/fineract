/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.workingcapitalloan.validator;

import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.ACTION;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.DATE_FORMAT;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.END_DATE;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.LOCALE;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.START_DATE;

import com.google.gson.JsonElement;
import java.time.LocalDate;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WorkingCapitalLoanBreachDisableParseAndValidator extends ParseAndValidator {

    private static final String DISABLE_ACTION = "disable";
    private static final String ENABLE_ACTION = "enable";

    private final FromJsonHelper jsonHelper;
    private final WorkingCapitalLoanBreachActionRepository breachActionRepository;

    public WorkingCapitalLoanBreachAction validateAndParse(final JsonCommand command, final WorkingCapitalLoan workingCapitalLoan) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource("workingCapitalLoanBreachDisable");
        final JsonElement json = command.parsedJson();

        final String actionString = jsonHelper.extractStringNamed(ACTION, json);
        dataValidator.reset().parameter(ACTION).value(actionString).notBlank();
        if (StringUtils.isNotBlank(actionString)) {
            dataValidator.reset().parameter(ACTION).value(actionString).isOneOfTheseStringValues(DISABLE_ACTION, ENABLE_ACTION);
        }
        throwExceptionIfValidationWarningsExist(dataValidator);

        validateLoanIsActive(dataValidator, workingCapitalLoan);
        validateBreachConfigurationExists(dataValidator, workingCapitalLoan);

        final LocalDate startDate = extractDate(json, START_DATE);
        dataValidator.reset().parameter(START_DATE).value(startDate).notNull();

        final LocalDate endDate = extractDate(json, END_DATE);
        if (endDate != null) {
            dataValidator.reset().parameter(END_DATE).value(endDate).failWithCode("must.not.be.provided.for.disable.or.enable");
        }

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        if (startDate != null && !startDate.isEqual(businessDate)) {
            dataValidator.reset().parameter(START_DATE).value(startDate).failWithCode("must.be.current.business.date");
        }

        final boolean isEnable = ENABLE_ACTION.equalsIgnoreCase(actionString);
        validateDisableState(dataValidator, workingCapitalLoan.getId(), isEnable);

        throwExceptionIfValidationWarningsExist(dataValidator);

        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setAction(isEnable ? WorkingCapitalLoanBreachActionType.ENABLE : WorkingCapitalLoanBreachActionType.DISABLE);
        action.setStartDate(startDate);
        return action;
    }

    private void validateDisableState(final DataValidatorBuilder dataValidator, final Long loanId, final boolean isEnable) {
        final boolean alreadyDisabled = breachActionRepository.isBreachDisabledAsOf(loanId, DateUtils.getBusinessLocalDate());
        if (isEnable && !alreadyDisabled) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.active.breach.disable.to.enable");
        } else if (!isEnable && alreadyDisabled) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("breach.already.disabled");
        }
    }

    private LocalDate extractDate(final JsonElement json, final String paramName) {
        final String dateFormat = jsonHelper.extractStringNamed(DATE_FORMAT, json);
        final String locale = jsonHelper.extractStringNamed(LOCALE, json);
        return jsonHelper.extractLocalDateNamed(paramName, json, dateFormat, JsonParserHelper.localeFromString(locale));
    }

    private void validateLoanIsActive(final DataValidatorBuilder dataValidator, final WorkingCapitalLoan workingCapitalLoan) {
        if (!workingCapitalLoan.getLoanStatus().isActive()) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("loan.is.not.active");
        }
    }

    private void validateBreachConfigurationExists(final DataValidatorBuilder dataValidator, final WorkingCapitalLoan workingCapitalLoan) {
        final WorkingCapitalLoanProductRelatedDetails details = workingCapitalLoan.getLoanProductRelatedDetails();
        if (details == null || details.getBreach() == null) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.breach.configuration");
        }
    }
}
