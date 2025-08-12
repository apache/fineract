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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import org.apache.fineract.accounting.provisioning.domain.ProvisioningEntry;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.portfolio.loanaccount.data.AccountingBridgeDataDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;

public interface JournalEntryWritePlatformService {

    CommandProcessingResult createJournalEntry(JsonCommand command);

    CommandProcessingResult revertJournalEntry(JsonCommand command);

    void createJournalEntriesForLoan(AccountingBridgeDataDTO accountingBridgeData);

    void createJournalEntriesForSavings(Map<String, Object> accountingBridgeData);

    void createJournalEntriesForClientTransactions(Map<String, Object> accountingBridgeData);

    CommandProcessingResult defineOpeningBalance(JsonCommand command);

    void createJournalEntryForReversedLoanTransaction(LocalDate transactionDate, String loanTransactionId, Long officeId);

    String revertProvisioningJournalEntries(LocalDate reversalTransactionDate, Long entityId, Integer entityType);

    String createProvisioningJournalEntries(ProvisioningEntry entry);

    void createJournalEntriesForShares(Map<String, Object> accountingBridgeData);

    void revertShareAccountJournalEntries(ArrayList<Long> transactionId, LocalDate transactionDate);

    /**
     * Create journal entries immediately for a single loan transaction
     *
     * @param loanTransaction
     *            the loan transaction to create journal entries for
     * @param isAccountTransfer
     *            whether this is an account transfer transaction
     * @param isLoanToLoanTransfer
     *            whether this is a loan-to-loan transfer transaction
     */
    void createJournalEntriesForLoanTransaction(LoanTransaction loanTransaction, boolean isAccountTransfer, boolean isLoanToLoanTransfer);

    /**
     * Create journal entries immediately for an external owner transfer
     *
     * @param loan
     *            the loan being transferred
     * @param externalAssetOwnerTransfer
     *            the external owner transfer details
     * @param previousOwner
     *            the previous owner (can be null for initial transfers)
     */
    void createJournalEntriesForExternalOwnerTransfer(Loan loan, ExternalAssetOwnerTransfer externalAssetOwnerTransfer,
            ExternalAssetOwner previousOwner);

}
