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

import java.math.BigDecimal;
import java.util.Date;

import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.journalentry.data.ClientTransactionDTO;
import org.apache.fineract.organisation.office.domain.Office;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CashBasedAccountingProcessorForClientTransactions implements AccountingProcessorForClientTransactions {

    AccountingProcessorHelper helper;

    @Autowired
    public CashBasedAccountingProcessorForClientTransactions(final AccountingProcessorHelper accountingProcessorHelper) {
        this.helper = accountingProcessorHelper;
    }

    @Override
    public void createJournalEntriesForClientTransaction(ClientTransactionDTO clientTransactionDTO) {
        if (clientTransactionDTO.getAccountingEnabled()) {
            final GLClosure latestGLClosure = this.helper.getLatestClosureByBranch(clientTransactionDTO.getOfficeId());
            final Date transactionDate = clientTransactionDTO.getTransactionDate();
            final Office office = this.helper.getOfficeById(clientTransactionDTO.getOfficeId());
            this.helper.checkForBranchClosures(latestGLClosure, transactionDate);

            /** Handle client payments **/
            if (clientTransactionDTO.isChargePayment()) {
                createJournalEntriesForChargePayments(clientTransactionDTO, office);
            }
        }
    }

    /**
     * Create a single debit to fund source and multiple credits for the income
     * account mapped with each charge this payment pays off
     * 
     * In case the loan transaction is a reversal, all debits are turned into
     * credits and vice versa
     */
    private void createJournalEntriesForChargePayments(final ClientTransactionDTO clientTransactionDTO, final Office office) {
        // client properties
        final Long clientId = clientTransactionDTO.getClientId();

        // transaction properties
        final String currencyCode = clientTransactionDTO.getCurrencyCode();
        final Long transactionId = clientTransactionDTO.getTransactionId();
        final Date transactionDate = clientTransactionDTO.getTransactionDate();
        final BigDecimal amount = clientTransactionDTO.getAmount();
        final boolean isReversal = clientTransactionDTO.isReversed();

        if (amount != null && !(amount.compareTo(BigDecimal.ZERO) == 0)) {
            BigDecimal totalCreditedAmount = this.helper.createCreditJournalEntryOrReversalForClientPayments(office, currencyCode, clientId,
                    transactionId, transactionDate, isReversal, clientTransactionDTO.getChargePayments());

            /***
             * create a single Debit entry (or reversal) for the entire amount
             * that was credited (accounting is turned on at the level of for
             * each charge that has been paid by this transaction)
             **/
            this.helper.createDebitJournalEntryOrReversalForClientChargePayments(office, currencyCode, clientId, transactionId,
                    transactionDate, totalCreditedAmount, isReversal);
        }

    }

}
