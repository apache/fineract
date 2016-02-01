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
package org.apache.fineract.portfolio.savings;

import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.joda.time.LocalDate;

public class DepositAccountUtils {

    public static final int GENERATE_MINIMUM_NUMBER_OF_FUTURE_INSTALMENTS = 5;

    public static LocalDate calculateNextDepositDate(final LocalDate lastDepositDate, final PeriodFrequencyType frequency,
            final int recurringEvery) {
        LocalDate nextDepositDate = lastDepositDate;

        switch (frequency) {
            case DAYS:
                nextDepositDate = lastDepositDate.plusDays(recurringEvery);
            break;
            case WEEKS:
                nextDepositDate = lastDepositDate.plusWeeks(recurringEvery);
            break;
            case MONTHS:
                nextDepositDate = lastDepositDate.plusMonths(recurringEvery);
            break;
            case YEARS:
                nextDepositDate = lastDepositDate.plusYears(recurringEvery);
            break;
            case INVALID:
            break;
        }
        return nextDepositDate;
    }

    public static LocalDate calculateNextDepositDate(final LocalDate lastDepositDate, final String recurrence) {
        final PeriodFrequencyType frequencyType = CalendarFrequencyType.from(CalendarUtils.getFrequency(recurrence));
        Integer frequency = CalendarUtils.getInterval(recurrence);
        frequency = frequency == -1 ? 1 : frequency;
        return calculateNextDepositDate(lastDepositDate, frequencyType, frequency);
    }

}
