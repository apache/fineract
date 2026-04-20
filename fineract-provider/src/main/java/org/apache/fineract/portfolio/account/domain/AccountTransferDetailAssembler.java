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
package org.apache.fineract.portfolio.account.domain;

import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.springframework.stereotype.Service;

@Service
public class AccountTransferDetailAssembler {

    public AccountTransferDetails assembleSavingsToLoanTransfer(final SavingsAccount fromSavingsAccount, final Loan toLoanAccount,
            Integer transferType) {
        final Office fromOffice = fromSavingsAccount.office();
        final Client fromClient = fromSavingsAccount.getClient();
        final Office toOffice = toLoanAccount.getOffice();
        final Client toClient = toLoanAccount.client();

        return AccountTransferDetails.savingsToLoanTransfer(fromOffice, fromClient, fromSavingsAccount, toOffice, toClient, toLoanAccount,
                transferType);

    }

    public AccountTransferDetails assembleSavingsToSavingsTransfer(final SavingsAccount fromSavingsAccount,
            final SavingsAccount toSavingsAccount, Integer transferType) {
        final Office fromOffice = fromSavingsAccount.office();
        final Client fromClient = fromSavingsAccount.getClient();
        final Office toOffice = toSavingsAccount.office();
        final Client toClient = toSavingsAccount.getClient();

        return AccountTransferDetails.savingsToSavingsTransfer(fromOffice, fromClient, fromSavingsAccount, toOffice, toClient,
                toSavingsAccount, transferType);
    }

    public AccountTransferDetails assembleLoanToSavingsTransfer(final Loan fromLoanAccount, final SavingsAccount toSavingsAccount,
            Integer transferType) {
        final Office fromOffice = fromLoanAccount.getOffice();
        final Client fromClient = fromLoanAccount.client();
        final Office toOffice = toSavingsAccount.office();
        final Client toClient = toSavingsAccount.getClient();

        return AccountTransferDetails.loanTosavingsTransfer(fromOffice, fromClient, fromLoanAccount, toOffice, toClient, toSavingsAccount,
                transferType);
    }

    public AccountTransferDetails assembleLoanToLoanTransfer(Loan fromLoanAccount, Loan toLoanAccount, Integer transferType) {
        final Office fromOffice = fromLoanAccount.getOffice();
        final Client fromClient = fromLoanAccount.client();
        final Office toOffice = toLoanAccount.getOffice();
        final Client toClient = toLoanAccount.client();

        return AccountTransferDetails.loanToLoanTransfer(fromOffice, fromClient, fromLoanAccount, toOffice, toClient, toLoanAccount,
                transferType);
    }
}
