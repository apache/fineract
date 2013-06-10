/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.springframework.stereotype.Component;

@Component
public class DefaultLoanScheduleGeneratorFactory implements LoanScheduleGeneratorFactory {

    @Override
    public LoanScheduleGenerator create(final InterestMethod interestMethod) {

        LoanScheduleGenerator loanScheduleGenerator = null;

        switch (interestMethod) {
            case FLAT:
                loanScheduleGenerator = new FlatInterestLoanScheduleGenerator();
            break;
            case DECLINING_BALANCE:
                loanScheduleGenerator = new DecliningBalanceInterestLoanScheduleGenerator();
            break;
            case INVALID:
            break;
        }

        return loanScheduleGenerator;
    }
}