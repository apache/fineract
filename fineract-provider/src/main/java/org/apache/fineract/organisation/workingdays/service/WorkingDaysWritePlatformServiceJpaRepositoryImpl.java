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
package org.apache.fineract.organisation.workingdays.service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.organisation.workingdays.data.WorkingDaysUpdateRequest;
import org.apache.fineract.organisation.workingdays.data.WorkingDaysUpdateRequestValidator;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class WorkingDaysWritePlatformServiceJpaRepositoryImpl implements WorkingDaysWritePlatformService {

    private final WorkingDaysRepositoryWrapper daysRepositoryWrapper;
    private final WorkingDaysUpdateRequestValidator validator;

    @Transactional
    @Override
    public Map<String, Object> updateWorkingDays(WorkingDaysUpdateRequest request) {
        String recurrence = "";
        RRule rrule = null;
        try {
            this.validator.validateForUpdate(request);
            final WorkingDays workingDays = this.daysRepositoryWrapper.findOne();

            recurrence = request.getRecurrence();
            rrule = new RRule(recurrence);
            rrule.validate();

            Map<String, Object> changes = update(workingDays, request);
            // include the current WorkingDays resource id in the changes for response consumption
            changes.put("resourceId", workingDays.getId());
            this.daysRepositoryWrapper.saveAndFlush(workingDays);
            return changes;

        } catch (final ValidationException e) {
            throw new PlatformDataIntegrityException("error.msg.invalid.recurring.rule",
                    "The Recurring Rule value: " + recurrence + " is not valid.", "recurrence", recurrence, e);
        } catch (final IllegalArgumentException | ParseException e) {
            throw new PlatformDataIntegrityException("error.msg.recurring.rule.parsing.error",
                    "Error in passing the Recurring Rule value: " + recurrence, "recurrence", e.getMessage(), e);
        }
    }

    public HashMap<String, Object> update(WorkingDays workingDays, WorkingDaysUpdateRequest request) {
        HashMap<String, Object> changes = new HashMap<>();

        if (!Objects.equals(request.getRecurrence(), workingDays.getRecurrence())) {
            workingDays.setRecurrence(request.getRecurrence());
            changes.put("recurrence", request.getRecurrence());
        }

        Integer repaymentRescheduleType = request.getRepaymentRescheduleType();
        if (repaymentRescheduleType != null && !Objects.equals(repaymentRescheduleType, workingDays.getRepaymentReschedulingType())) {
            workingDays.setRepaymentReschedulingType(repaymentRescheduleType);
            changes.put("repaymentRescheduleType", repaymentRescheduleType);
        }

        Boolean extendDaily = request.getExtendTermForDailyRepayments();
        if (extendDaily != null && !Objects.equals(extendDaily, workingDays.getExtendTermForDailyRepayments())) {
            workingDays.setExtendTermForDailyRepayments(extendDaily);
            changes.put("extendTermForDailyRepayments", extendDaily);
        }

        Boolean extendHolidays = request.getExtendTermForRepaymentsOnHolidays();
        if (extendHolidays != null && !Objects.equals(extendHolidays, workingDays.getExtendTermForRepaymentsOnHolidays())) {
            workingDays.setExtendTermForRepaymentsOnHolidays(extendHolidays);
            changes.put("extendTermForRepaymentsOnHolidays", extendHolidays);
        }
        return changes;
    }

}
