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
package org.apache.fineract.portfolio.savings.domain;

import java.time.LocalDate;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the lock-in expiration date for a savings account.
 */
public class SavingsAccountLockPeriodCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SavingsAccountLockPeriodCalculator.class);

    public LocalDate calculateDateAccountIsLockedUntil(final LocalDate activationLocalDate, final Integer lockInPeriodFrequency,
            final PeriodFrequencyType lockinPeriodFrequencyType) {
        if (activationLocalDate == null || lockInPeriodFrequency == null || lockinPeriodFrequencyType == null) {
            return null;
        }

        LocalDate lockedInUntilLocalDate = null;
        switch (lockinPeriodFrequencyType) {
            case INVALID:
            case SEMI_MONTHLY:
            // SEMI_MONTHLY is a loan repayment frequency and is never offered as a savings lock-in period,
            // so it leaves the account unlocked just like an unset frequency does.
            break;
            case DAYS:
                lockedInUntilLocalDate = activationLocalDate.plusDays(lockInPeriodFrequency);
            break;
            case WEEKS:
                lockedInUntilLocalDate = activationLocalDate.plusWeeks(lockInPeriodFrequency);
            break;
            case MONTHS:
                lockedInUntilLocalDate = activationLocalDate.plusMonths(lockInPeriodFrequency);
            break;
            case YEARS:
                lockedInUntilLocalDate = activationLocalDate.plusYears(lockInPeriodFrequency);
            break;
            case WHOLE_TERM:
                LOGGER.error("TODO Implement calculateDateAccountIsLockedUntil for WHOLE_TERM");
            break;
        }

        return lockedInUntilLocalDate;
    }
}
