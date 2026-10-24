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

import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;

/**
 * Validates the two configured {@link SemiMonthlyDueDays}, shared by the loan product and the loan application
 * validators so that both apply the same ranges.
 */
public final class SemiMonthlyDueDaysValidator {

    private SemiMonthlyDueDaysValidator() {}

    /**
     * The first day has to exist in every month and the second one has to come after it; a second day the month lacks
     * falls on its last day, so 31 is the last day of every month.
     */
    public static void validate(final DataValidatorBuilder baseDataValidator, final Integer firstRepaymentDayOfMonth,
            final Integer secondRepaymentDayOfMonth) {
        baseDataValidator.reset().parameter(LoanApiConstants.FIRST_REPAYMENT_DAY_OF_MONTH).value(firstRepaymentDayOfMonth).notNull()
                .inMinMaxRange(SemiMonthlyScheduleDates.MIN_FIRST_DAY_OF_MONTH, SemiMonthlyScheduleDates.MAX_FIRST_DAY_OF_MONTH);
        baseDataValidator.reset().parameter(LoanApiConstants.SECOND_REPAYMENT_DAY_OF_MONTH).value(secondRepaymentDayOfMonth).notNull()
                .inMinMaxRange(SemiMonthlyScheduleDates.MIN_SECOND_DAY_OF_MONTH, SemiMonthlyScheduleDates.MAX_SECOND_DAY_OF_MONTH);
        if (firstRepaymentDayOfMonth != null && secondRepaymentDayOfMonth != null
                && secondRepaymentDayOfMonth <= firstRepaymentDayOfMonth) {
            baseDataValidator.reset().parameter(LoanApiConstants.SECOND_REPAYMENT_DAY_OF_MONTH).value(secondRepaymentDayOfMonth)
                    .failWithCode("must.be.greater.than.first.repayment.day.of.month");
        }
    }
}
