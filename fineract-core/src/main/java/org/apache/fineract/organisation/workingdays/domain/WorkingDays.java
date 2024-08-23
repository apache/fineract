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
package org.apache.fineract.organisation.workingdays.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.workingdays.api.WorkingDaysApiConstants;

@Getter
@Entity
@Table(name = "m_working_days")
public class WorkingDays extends AbstractPersistableCustom<Long> {

    @Column(name = "recurrence", length = 100, nullable = true)
    private String recurrence;

    @Setter
    @Column(name = "repayment_rescheduling_enum", nullable = false)
    private Integer repaymentReschedulingType;

    @Column(name = "extend_term_daily_repayments", nullable = false)
    private Boolean extendTermForDailyRepayments;

    @Column(name = "extend_term_holiday_repayment", nullable = false)
    private Boolean extendTermForRepaymentsOnHolidays;

    protected WorkingDays() {

    }

    public WorkingDays(final String recurrence, final Integer repaymentReschedulingType, final Boolean extendTermForDailyRepayments,
            final Boolean extendTermForRepaymentsOnHolidays) {
        this.recurrence = recurrence;
        this.repaymentReschedulingType = repaymentReschedulingType;
        this.extendTermForDailyRepayments = extendTermForDailyRepayments;
        this.extendTermForRepaymentsOnHolidays = extendTermForRepaymentsOnHolidays;
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String recurrenceParamName = "recurrence";
        if (command.isChangeInStringParameterNamed(recurrenceParamName, this.recurrence)) {
            final String newValue = command.stringValueOfParameterNamed(recurrenceParamName);
            actualChanges.put(recurrenceParamName, newValue);
            this.recurrence = newValue;
        }

        final String repaymentRescheduleTypeParamName = "repaymentRescheduleType";
        if (command.isChangeInIntegerParameterNamed(repaymentRescheduleTypeParamName, this.repaymentReschedulingType)) {
            final Integer newValue = command.integerValueOfParameterNamed(repaymentRescheduleTypeParamName);
            actualChanges.put(repaymentRescheduleTypeParamName, WorkingDaysEnumerations.workingDaysStatusType(newValue));
            this.repaymentReschedulingType = RepaymentRescheduleType.fromInt(newValue).getValue();
        }

        if (command.isChangeInBooleanParameterNamed(WorkingDaysApiConstants.extendTermForDailyRepayments,
                this.extendTermForDailyRepayments)) {
            final Boolean newValue = command.booleanPrimitiveValueOfParameterNamed(WorkingDaysApiConstants.extendTermForDailyRepayments);
            actualChanges.put(WorkingDaysApiConstants.extendTermForDailyRepayments, newValue);
            this.extendTermForDailyRepayments = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(WorkingDaysApiConstants.extendTermForRepaymentsOnHolidays,
                this.extendTermForRepaymentsOnHolidays)) {
            final Boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(WorkingDaysApiConstants.extendTermForRepaymentsOnHolidays);
            actualChanges.put(WorkingDaysApiConstants.extendTermForRepaymentsOnHolidays, newValue);
            this.extendTermForRepaymentsOnHolidays = newValue;
        }

        return actualChanges;
    }

}
