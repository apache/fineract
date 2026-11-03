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
package org.apache.fineract.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;

public class CompoundInterestHelper {

    /**
     * @param currency
     * @param allPeriods
     * @param lockUntil
     *            - account locked date used with the combination of immediateWithdrawalOfInterest to avoid exclusion of
     *            interestEarned
     * @param interestTransferEnabled
     *            - boolean flag used to avoid addition of interest to next posting period as income while calculating
     *            interest
     * @return
     */
    public Money calculateInterestForAllPostingPeriods(final MonetaryCurrency currency, final List<PostingPeriod> allPeriods,
            LocalDate lockUntil, Boolean interestTransferEnabled) {

        // sum up the 'rounded' values that are posted each posting period
        Money interestEarned = Money.zero(currency);

        // total interest earned in previous periods but not yet recognised
        BigDecimal compoundedInterest = BigDecimal.ZERO;
        BigDecimal unCompoundedInterest = BigDecimal.ZERO;
        final CompoundInterestValues compoundInterestValues = new CompoundInterestValues(compoundedInterest, unCompoundedInterest);
        int index = 0;
        while (index < allPeriods.size()) {
            final PostingPeriod postingPeriod = allPeriods.get(index);
            // the overdraft split builds two periods for one posting interval, which are calculated together. It adds
            // them one right after the other, so they are next to each other in the list
            final PostingPeriod otherPeriodOfInterval = index + 1 < allPeriods.size()
                    && hasSameInterval(postingPeriod, allPeriods.get(index + 1)) ? allPeriods.get(index + 1) : null;
            final List<PostingPeriod> periodsOfInterval;
            if (otherPeriodOfInterval == null) {
                postingPeriod.calculateInterest(compoundInterestValues);
                periodsOfInterval = List.of(postingPeriod);
            } else {
                PostingPeriod.calculateInterestForSplitInterval(postingPeriod, otherPeriodOfInterval, compoundInterestValues);
                periodsOfInterval = List.of(postingPeriod, otherPeriodOfInterval);
            }

            for (final PostingPeriod period : periodsOfInterval) {
                final Money moneyToBePostedForPeriod = Money.of(currency, period.getInterestEarnedUnrounded());

                interestEarned = interestEarned.plus(moneyToBePostedForPeriod);
                // these checks are for fixed deposit account for not include
                // interest for accounts which has post interest to linked savings
                // account and if already transfered then it includes in interest
                // calculation.
                if (!(period.isInterestTransfered() || !interestTransferEnabled
                        || (lockUntil != null && !DateUtils.isAfter(period.dateOfPostingTransaction(), lockUntil)))) {
                    compoundInterestValues.setCompoundedInterest(BigDecimal.ZERO);
                }
            }
            index += periodsOfInterval.size();
        }

        return interestEarned;
    }

    private static boolean hasSameInterval(final PostingPeriod postingPeriod, final PostingPeriod other) {
        return postingPeriod.getPeriodInterval().startDate().equals(other.getPeriodInterval().startDate())
                && postingPeriod.getPeriodInterval().endDate().equals(other.getPeriodInterval().endDate());
    }
}
