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
package org.apache.fineract.portfolio.workingcapitalloannearbreach.validator;

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
import org.apache.fineract.infrastructure.core.service.FrequencyTypeUtil;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachData;
import org.apache.fineract.portfolio.workingcapitalloanbreach.service.WorkingCapitalBreachReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachRequest;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.service.WorkingCapitalNearBreachReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WorkingCapitalNearBreachParseAndValidator extends ParseAndValidator {

    private final FromJsonHelper jsonHelper;
    private final WorkingCapitalBreachReadPlatformService workingCapitalBreachReadPlatformService;
    private final WorkingCapitalNearBreachReadPlatformService workingCapitalNearBreachReadPlatformService;

    public WorkingCapitalNearBreachRequest validateAndParse(@NotNull final JsonCommand command) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource("workingCapitalNearBreach.create");
        final JsonObject element = extractJsonObject(jsonHelper, command);
        final WorkingCapitalNearBreachRequest result = validateAndParse(dataValidator, element, jsonHelper);
        throwExceptionIfValidationWarningsExist(dataValidator);
        return result;
    }

    private WorkingCapitalNearBreachRequest validateAndParse(final DataValidatorBuilder dataValidator, final JsonObject element,
            final FromJsonHelper jsonHelper) {
        if (element == null) {
            return null;
        }
        jsonHelper.checkForUnsupportedParameters(element, List.of(WorkingCapitalLoanProductConstants.nearBreachNameParamName, //
                WorkingCapitalLoanProductConstants.nearBreachFrequencyParamName, //
                WorkingCapitalLoanProductConstants.nearBreachFrequencyTypeParamName, //
                WorkingCapitalLoanProductConstants.nearBreachThresholdParamName));

        final String nearBreachName = jsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.nearBreachNameParamName, element);
        dataValidator.reset().parameter(WorkingCapitalLoanProductConstants.nearBreachNameParamName).value(nearBreachName).notNull();

        final Integer nearBreachFrequency = jsonHelper.extractIntegerNamed(WorkingCapitalLoanProductConstants.nearBreachFrequencyParamName,
                element);
        dataValidator.reset().parameter(WorkingCapitalLoanProductConstants.nearBreachFrequencyParamName).value(nearBreachFrequency)
                .notNull().integerGreaterThanZero();

        final String nearBreachFrequencyTypeValue = jsonHelper
                .extractStringNamed(WorkingCapitalLoanProductConstants.nearBreachFrequencyTypeParamName, element);
        dataValidator.reset().parameter(WorkingCapitalLoanProductConstants.nearBreachFrequencyTypeParamName)
                .value(nearBreachFrequencyTypeValue).notNull().isOneOfEnumValues(WorkingCapitalLoanPeriodFrequencyType.class);

        final BigDecimal nearBreachThreshold = jsonHelper
                .extractBigDecimalNamed(WorkingCapitalLoanProductConstants.nearBreachThresholdParamName, element, new HashSet<>());
        dataValidator.reset().parameter(WorkingCapitalLoanProductConstants.nearBreachThresholdParamName).value(nearBreachThreshold)
                .notNull().percentage();

        return dataValidator.hasError() ? null
                : new WorkingCapitalNearBreachRequest(nearBreachName, nearBreachFrequency, nearBreachFrequencyTypeValue,
                        nearBreachThreshold);
    }

    public void validateNearBreachAgainstBreach(final DataValidatorBuilder baseDataValidator, final Long breachId,
            final Long nearBreachId) {
        if (nearBreachId != null) {
            final WorkingCapitalBreachData breachData = workingCapitalBreachReadPlatformService.retrieveOne(breachId);
            final WorkingCapitalNearBreachData nearBreachData = workingCapitalNearBreachReadPlatformService.retrieveOne(nearBreachId);
            if (FrequencyTypeUtil.compareFrequencies(nearBreachData.getFrequency(), nearBreachData.getFrequencyType().getCode(),
                    breachData.getBreachFrequency(), breachData.getBreachFrequencyType().getCode()) >= 0) {
                baseDataValidator.reset().parameter(WorkingCapitalLoanProductConstants.nearBreachIdParamName)
                        .failWithCode("near.breach.frequency.must.be.lower.than.breach.frequency");
                return;
            }
        }
    }
}
