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
package org.apache.fineract.organisation.workingdays.data;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.organisation.workingdays.api.WorkingDaysApiConstants;
import org.springframework.stereotype.Component;

@Component
public class WorkingDaysUpdateRequestValidator {

    public void validateForUpdate(final WorkingDaysUpdateRequest request) {

        List<ApiParameterError> validationErrors = new ArrayList<>();
        DataValidatorBuilder validator = new DataValidatorBuilder(validationErrors)
                .resource(WorkingDaysApiConstants.WORKING_DAYS_RESOURCE_NAME);

        // recurrence (mandatory)
        String recurrence = request.getRecurrence();
        validator.reset().parameter(WorkingDaysApiConstants.recurrence).value(recurrence).notNull();

        // repaymentRescheduleType (optional, but must be 1–4 if present)
        validator.reset().parameter(WorkingDaysApiConstants.repayment_rescheduling_enum).value(request.getRepaymentRescheduleType())
                .ignoreIfNull().inMinMaxRange(1, 4);

        // extendTermForDailyRepayments (optional but must be boolean if provided)
        validator.reset().parameter(WorkingDaysApiConstants.extendTermForDailyRepayments).value(request.getExtendTermForDailyRepayments())
                .ignoreIfNull().validateForBooleanValue();

        // extendTermForRepaymentsOnHolidays (optional but must be boolean if provided)
        validator.reset().parameter(WorkingDaysApiConstants.extendTermForRepaymentsOnHolidays)
                .value(request.getExtendTermForRepaymentsOnHolidays()).ignoreIfNull().validateForBooleanValue();

        throwExceptionIfValidationWarningsExist(validationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> validationErrors) {
        if (!validationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(validationErrors);
        }
    }
}
