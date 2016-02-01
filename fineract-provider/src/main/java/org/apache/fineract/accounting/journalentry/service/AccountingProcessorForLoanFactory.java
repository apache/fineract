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
package org.apache.fineract.accounting.journalentry.service;

import org.apache.fineract.accounting.journalentry.data.LoanDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AccountingProcessorForLoanFactory {

    private final ApplicationContext applicationContext;

    @Autowired
    public AccountingProcessorForLoanFactory(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public AccountingProcessorForLoan determineProcessor(final LoanDTO loanDTO) {

        AccountingProcessorForLoan accountingProcessorForLoan = null;

        if (loanDTO.isCashBasedAccountingEnabled()) {
            accountingProcessorForLoan = this.applicationContext.getBean("cashBasedAccountingProcessorForLoan",
                    AccountingProcessorForLoan.class);
        } else if (loanDTO.isUpfrontAccrualBasedAccountingEnabled()) {
            accountingProcessorForLoan = this.applicationContext.getBean("accrualBasedAccountingProcessorForLoan",
                    AccountingProcessorForLoan.class);
        } else if (loanDTO.isPeriodicAccrualBasedAccountingEnabled()) {
            accountingProcessorForLoan = this.applicationContext.getBean("accrualBasedAccountingProcessorForLoan",
                    AccountingProcessorForLoan.class);
        }

        return accountingProcessorForLoan;
    }

}
