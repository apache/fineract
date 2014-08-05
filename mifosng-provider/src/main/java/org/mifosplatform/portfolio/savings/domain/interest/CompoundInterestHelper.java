/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

public class CompoundInterestHelper {

    /**
     * @param currency
     * @param allPeriods
     * @param lockUntil
     *            - account locked date used with the combination of
     *            immediateWithdrawalOfInterest to avoid exclusion of
     *            interestEarned
     * @param interestTransferEnabled
     *            - boolean flag used to avoid addition of interest to next
     *            posting period as income while calculating interest
     * @return
     */
    public Money calculateInterestForAllPostingPeriods(final MonetaryCurrency currency, final List<PostingPeriod> allPeriods,
            LocalDate lockUntil, Boolean interestTransferEnabled) {

        // sum up the 'rounded' values that are posted each posting period
        Money interestEarned = Money.zero(currency);

        // total interest earned in previous periods but not yet recognised
        BigDecimal interestEarnedButNotPosted = BigDecimal.ZERO;
        for (final PostingPeriod postingPeriod : allPeriods) {

            final BigDecimal interestEarnedThisPeriod = postingPeriod.calculateInterest(interestEarnedButNotPosted);

            final Money moneyToBePostedForPeriod = Money.of(currency, interestEarnedThisPeriod);

            interestEarned = interestEarned.plus(moneyToBePostedForPeriod);
            // these checks are for fixed deposit account for not include
            // interest for accounts which has post interest to linked savings
            // account and if already transfered then it includes in interest
            // calculation.
            if (postingPeriod.isInterestTransfered() || !interestTransferEnabled
                    || (lockUntil != null && !postingPeriod.dateOfPostingTransaction().isAfter(lockUntil))) {
                interestEarnedButNotPosted = interestEarnedButNotPosted.add(moneyToBePostedForPeriod.getAmount());
            }
        }

        return interestEarned;
    }
}