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
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.organisation.workingdays.api.WorkingDaysApiConstants;
import org.apache.fineract.organisation.workingdays.data.WorkingDaysUpdateRequest;
import org.apache.fineract.organisation.workingdays.data.WorkingDaysUpdateResponse;
import org.apache.fineract.organisation.workingdays.domain.RepaymentRescheduleType;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysEnumerations;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class WorkingDaysWritePlatformServiceJpaRepositoryImpl implements WorkingDaysWritePlatformService {

    private final WorkingDaysRepositoryWrapper daysRepositoryWrapper;

    @Transactional
    @Override
    public WorkingDaysUpdateResponse updateWorkingDays(WorkingDaysUpdateRequest request) {
        String recurrence = "";
        RRule rrule = null;
        try {
            final WorkingDays workingDays = this.daysRepositoryWrapper.findOne();

            rrule = new RRule(recurrence);
            rrule.validate();

            Map<String, Object> changes = updateEntity(workingDays, request);
            this.daysRepositoryWrapper.saveAndFlush(workingDays);

            return WorkingDaysUpdateResponse.builder().resourceId(workingDays.getId()) // entityId
                    .changes(changes).build();
        } catch (final ValidationException e) {
            throw new PlatformDataIntegrityException("error.msg.invalid.recurring.rule",
                    "The Recurring Rule value: " + recurrence + " is not valid.", "recurrence", recurrence, e);
        } catch (final IllegalArgumentException | ParseException e) {
            throw new PlatformDataIntegrityException("error.msg.recurring.rule.parsing.error",
                    "Error in passing the Recurring Rule value: " + recurrence, "recurrence", e.getMessage(), e);
        }
    }

    public Map<String, Object> updateEntity(final WorkingDays workingDays, final WorkingDaysUpdateRequest request) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String recurrenceParamName = "recurrence";
        if (!request.getRecurrence().equals(workingDays.getRecurrence())) {
            final String newValue = request.getRecurrence();
            actualChanges.put(recurrenceParamName, newValue);
            workingDays.setRecurrence(newValue);
        }

        final String repaymentRescheduleTypeParamName = "repaymentRescheduleType";
        if (request.getRepaymentRescheduleType() != null) {
            Integer newValue = Integer.valueOf(request.getRepaymentRescheduleType().getValue());
            if ((newValue.compareTo(workingDays.getRepaymentReschedulingType())) != 0) {
                actualChanges.put(repaymentRescheduleTypeParamName, WorkingDaysEnumerations.workingDaysStatusType(newValue));
                workingDays.setRepaymentReschedulingType(RepaymentRescheduleType.fromInt(newValue).getValue());
            }
        }

        final Boolean newValue = request.getExtendTermForDailyRepayments();
        if (newValue != null && newValue.compareTo(workingDays.getExtendTermForDailyRepayments()) != 0) {
            actualChanges.put(WorkingDaysApiConstants.extendTermForDailyRepayments, newValue);
            workingDays.setExtendTermForDailyRepayments(newValue);
        }

        final Boolean newValueHolidays = request.getExtendTermForRepaymentsOnHolidays();
        if (newValueHolidays != null && newValueHolidays.compareTo(workingDays.getExtendTermForRepaymentsOnHolidays()) != 0) {
            actualChanges.put(WorkingDaysApiConstants.extendTermForRepaymentsOnHolidays, newValueHolidays);
            workingDays.setExtendTermForRepaymentsOnHolidays(newValueHolidays);
        }

        return actualChanges;
    }

}
