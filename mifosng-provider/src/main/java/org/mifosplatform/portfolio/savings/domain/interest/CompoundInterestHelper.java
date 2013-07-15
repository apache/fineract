package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.util.List;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

public class CompoundInterestHelper {

    public Money calculateInterestForAllPostingPeriods(final MonetaryCurrency currency, final List<PostingPeriod> allPeriods) {

        // sum up the 'rounded' values that are posted each posting period
        Money interestEarned = Money.zero(currency);
        
        // total interest earned in previous periods but not yet recognised
        BigDecimal interestEarnedButNotPosted = BigDecimal.ZERO;
        for (PostingPeriod postingPeriod : allPeriods) {

            final BigDecimal interestEarnedThisPeriod = postingPeriod.calculateInterest(interestEarnedButNotPosted);

            Money moneyToBePostedForPeriod = Money.of(currency, interestEarnedThisPeriod);

            interestEarned = interestEarned.plus(moneyToBePostedForPeriod);

            interestEarnedButNotPosted = interestEarnedButNotPosted.add(moneyToBePostedForPeriod.getAmount());
        }

        return interestEarned;
    }
}