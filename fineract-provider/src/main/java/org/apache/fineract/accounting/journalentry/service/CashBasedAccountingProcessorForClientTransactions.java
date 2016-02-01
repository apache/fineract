/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.service;

import java.math.BigDecimal;
import java.util.Date;

import org.mifosplatform.accounting.closure.domain.GLClosure;
import org.mifosplatform.accounting.journalentry.data.ClientTransactionDTO;
import org.mifosplatform.organisation.office.domain.Office;
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
