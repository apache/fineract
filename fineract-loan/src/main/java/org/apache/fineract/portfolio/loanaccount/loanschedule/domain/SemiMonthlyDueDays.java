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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.time.YearMonth;

/**
 * The two configured due days of a {@code SEMI_MONTHLY} repayment frequency.
 * <p>
 * The validators guarantee that {@code firstDayOfMonth} exists in every month and that {@code secondDayOfMonth} is
 * greater than it. A second day beyond the length of a month falls on that month's last day, so {@code 31} reads as
 * "last day of the month".
 */
public record SemiMonthlyDueDays(int firstDayOfMonth, int secondDayOfMonth) {

    /**
     * The second due day in the given month, capped at the length of the month.
     */
    public int secondDayOf(final YearMonth month) {
        return Math.min(secondDayOfMonth, month.lengthOfMonth());
    }
}
