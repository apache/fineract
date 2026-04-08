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
package org.apache.fineract.portfolio.workingcapitalloanbreach.validator;

import com.google.gson.JsonObject;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachRequest;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalBreachAmountCalculationType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WorkingCapitalBreachParseAndValidator extends ParseAndValidator {

    private final FromJsonHelper jsonHelper;

    private static final String BREACH_FREQUENCY_PARAM = "breachFrequency";
    private static final String BREACH_FREQUENCY_TYPE_PARAM = "breachFrequencyType";
    private static final String BREACH_AMOUNT_CALCULATION_TYPE_PARAM = "breachAmountCalculationType";
    private static final String BREACH_AMOUNT_PARAM = "breachAmount";

    public WorkingCapitalBreachRequest validateAndParse(@NotNull final JsonCommand command) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource("workingCapitalBreach.create");
        final JsonObject element = extractJsonObject(jsonHelper, command);
        final WorkingCapitalBreachRequest result = validateAndParse(dataValidator, element, jsonHelper);
        throwExceptionIfValidationWarningsExist(dataValidator);
        return result;
    }

    private WorkingCapitalBreachRequest validateAndParse(final DataValidatorBuilder dataValidator, final JsonObject element,
            final FromJsonHelper jsonHelper) {
        if (element == null) {
            return null;
        }

        jsonHelper.checkForUnsupportedParameters(element,
                List.of(BREACH_FREQUENCY_PARAM, BREACH_FREQUENCY_TYPE_PARAM, BREACH_AMOUNT_CALCULATION_TYPE_PARAM, BREACH_AMOUNT_PARAM));

        final Integer breachFrequency = jsonHelper.extractIntegerNamed(BREACH_FREQUENCY_PARAM, element);
        dataValidator.reset().parameter(BREACH_FREQUENCY_PARAM).value(breachFrequency).notNull().integerGreaterThanZero();

        final String breachFrequencyTypeValue = jsonHelper.extractStringNamed(BREACH_FREQUENCY_TYPE_PARAM, element);
        dataValidator.reset().parameter(BREACH_FREQUENCY_TYPE_PARAM).value(breachFrequencyTypeValue).notNull()
                .isOneOfEnumValues(WorkingCapitalLoanPeriodFrequencyType.class);

        final String breachAmountCalculationTypeValue = jsonHelper.extractStringNamed(BREACH_AMOUNT_CALCULATION_TYPE_PARAM, element);
        dataValidator.reset().parameter(BREACH_AMOUNT_CALCULATION_TYPE_PARAM).value(breachAmountCalculationTypeValue).notNull()
                .isOneOfEnumValues(WorkingCapitalBreachAmountCalculationType.class);

        final BigDecimal breachAmount = jsonHelper.extractBigDecimalNamed(BREACH_AMOUNT_PARAM, element, new HashSet<>());
        dataValidator.reset().parameter(BREACH_AMOUNT_PARAM).value(breachAmount).notNull().zeroOrPositiveAmount();

        return dataValidator.hasError() ? null
                : new WorkingCapitalBreachRequest(breachFrequency, breachFrequencyTypeValue, breachAmountCalculationTypeValue,
                        breachAmount);
    }
}
