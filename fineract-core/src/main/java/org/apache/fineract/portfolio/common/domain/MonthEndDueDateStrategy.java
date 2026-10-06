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
package org.apache.fineract.portfolio.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiFacingEnum;

/**
 * Decides where a monthly due date goes when its anchor day (the day of month of the schedule's seed date) does not
 * exist in the target month, e.g. the 30th of February.
 * <p>
 * Either way the anchor day is kept for the periods after it: a loan anchored on the 31st is due on the 31st again as
 * soon as a month has one.
 * </p>
 * <ul>
 * <li>{@link #LAST_DAY_OF_MONTH} - the due date is clamped back to the last day of the target month (31 January + 1
 * month is 28 February). This is the default, and what an unset value means.</li>
 * <li>{@link #FIRST_DAY_OF_NEXT_MONTH} - the due date rolls forward to the first day of the month after the target
 * month (31 January + 1 month is 1 March).</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum MonthEndDueDateStrategy implements ApiFacingEnum<MonthEndDueDateStrategy> {

    /** Clamps a missing day back to the last day of the target month. */
    LAST_DAY_OF_MONTH("MonthEndDueDateStrategy.lastDayOfMonth", "Last day of month"), //

    /** Rolls a missing day forward to the first day of the following month. */
    FIRST_DAY_OF_NEXT_MONTH("MonthEndDueDateStrategy.firstDayOfNextMonth", "First day of next month"); //

    private final String code;
    private final String humanReadableName;

    public static boolean rollsForward(final MonthEndDueDateStrategy strategy) {
        return FIRST_DAY_OF_NEXT_MONTH.equals(strategy);
    }
}
