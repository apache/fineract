/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.service;

import java.util.Map;

import org.mifosplatform.accounting.api.commands.GLJournalEntryCommand;
import org.mifosplatform.accounting.api.data.LoanDTO;
import org.springframework.security.access.prepost.PreAuthorize;

public interface GLJournalEntryWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_JOURNAL_ENTRY')")
    String createJournalEntry(GLJournalEntryCommand journalEntryCommand);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'REVERT_JOURNAL_ENTRY')")
    String revertJournalEntry(String transactionId);

    void createJournalEntriesForLoan(LoanDTO loanDTO);

    /**
     * Using this interface over createJournalEntriesForLoan(LoanDTO loanDTO) to
     * remove the Object contract and have a 'Data' contract between portfolio
     * and accounting subsystems. (portfolio had to know about DTO objects that
     * belong in downstream system 'accounting')
     * 
     * If the only thing portfolio knows about how to talk to account is a
     * service with a data contract it will be easier move to other means of
     * making that communication happen in the future
     */
    void createJournalEntriesForLoan(Map<String, Object> accountingBridgeData);
}