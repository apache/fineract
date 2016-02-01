/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.service;

import org.mifosplatform.accounting.journalentry.data.SavingsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AccountingProcessorForSavingsFactory {

    private final ApplicationContext applicationContext;

    @Autowired
    public AccountingProcessorForSavingsFactory(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /***
     * Looks like overkill for now, but wanted to keep the savings side of
     * accounting identical to that of Loans (would we need an Accrual based
     * accounting in the future?)
     ***/
    public AccountingProcessorForSavings determineProcessor(final SavingsDTO savingsDTO) {

        AccountingProcessorForSavings accountingProcessorForSavings = null;

        if (savingsDTO.isCashBasedAccountingEnabled()) {
            accountingProcessorForSavings = this.applicationContext.getBean("cashBasedAccountingProcessorForSavings",
                    AccountingProcessorForSavings.class);
        }

        return accountingProcessorForSavings;
    }

}
