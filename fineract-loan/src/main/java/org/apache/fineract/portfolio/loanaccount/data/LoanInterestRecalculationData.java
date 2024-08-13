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
package org.apache.fineract.portfolio.loanaccount.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.calendar.data.CalendarData;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LoanInterestRecalculationData {

    private Long id;
    private Long loanId;
    private EnumOptionData interestRecalculationCompoundingType;
    private EnumOptionData rescheduleStrategyType;
    private CalendarData calendarData;
    private EnumOptionData recalculationRestFrequencyType;
    private Integer recalculationRestFrequencyInterval;
    private EnumOptionData recalculationRestFrequencyNthDay;
    private EnumOptionData recalculationRestFrequencyWeekday;
    private Integer recalculationRestFrequencyOnDay;
    private EnumOptionData recalculationCompoundingFrequencyType;
    private Integer recalculationCompoundingFrequencyInterval;
    private EnumOptionData recalculationCompoundingFrequencyNthDay;
    private EnumOptionData recalculationCompoundingFrequencyWeekday;
    private Integer recalculationCompoundingFrequencyOnDay;
    private Boolean isCompoundingToBePostedAsTransaction;
    private CalendarData compoundingCalendarData;
    private Boolean allowCompoundingOnEod;

    public LoanInterestRecalculationData(final Long id, final Long loanId, final EnumOptionData interestRecalculationCompoundingType,
            final EnumOptionData rescheduleStrategyType, final CalendarData calendarData,
            final EnumOptionData recalculationRestFrequencyType, final Integer recalculationRestFrequencyInterval,
            final EnumOptionData recalculationRestFrequencyNthDay, final EnumOptionData recalculationRestFrequencyWeekday,
            final Integer recalculationRestFrequencyOnDay, final CalendarData compoundingCalendarData,
            final EnumOptionData recalculationCompoundingFrequencyType, final Integer recalculationCompoundingFrequencyInterval,
            final EnumOptionData recalculationCompoundingFrequencyNthDay, final EnumOptionData recalculationCompoundingFrequencyWeekday,
            final Integer recalculationCompoundingFrequencyOnDay, final Boolean isCompoundingToBePostedAsTransaction,
            final Boolean allowCompoundingOnEod) {
        this.id = id;
        this.loanId = loanId;
        this.interestRecalculationCompoundingType = interestRecalculationCompoundingType;
        this.rescheduleStrategyType = rescheduleStrategyType;
        this.calendarData = calendarData;
        this.recalculationRestFrequencyType = recalculationRestFrequencyType;
        this.recalculationRestFrequencyInterval = recalculationRestFrequencyInterval;
        this.recalculationRestFrequencyNthDay = recalculationRestFrequencyNthDay;
        this.recalculationRestFrequencyWeekday = recalculationRestFrequencyWeekday;
        this.recalculationRestFrequencyOnDay = recalculationRestFrequencyOnDay;
        this.recalculationCompoundingFrequencyType = recalculationCompoundingFrequencyType;
        this.recalculationCompoundingFrequencyInterval = recalculationCompoundingFrequencyInterval;
        this.recalculationCompoundingFrequencyNthDay = recalculationCompoundingFrequencyNthDay;
        this.recalculationCompoundingFrequencyWeekday = recalculationCompoundingFrequencyWeekday;
        this.recalculationCompoundingFrequencyOnDay = recalculationCompoundingFrequencyOnDay;
        this.compoundingCalendarData = compoundingCalendarData;
        this.isCompoundingToBePostedAsTransaction = isCompoundingToBePostedAsTransaction;
        this.allowCompoundingOnEod = allowCompoundingOnEod;
    }

    public LoanInterestRecalculationData withCalendarData(final CalendarData calendarData, CalendarData compoundingCalendarData) {
        return this.setCalendarData(calendarData).setCompoundingCalendarData(compoundingCalendarData);
    }

}
