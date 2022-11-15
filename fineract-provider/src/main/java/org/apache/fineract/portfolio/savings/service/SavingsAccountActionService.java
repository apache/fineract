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

package org.apache.fineract.portfolio.savings.service;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.request.FixedDepositApprovalReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SavingsAccountActionService {

    @Autowired
    private PlatformSecurityContext context;
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private SavingsAccountRepository savingAccountRepository;

    public Map<String, Object> approveAccount(FixedDepositApprovalReq fixedDepositApprovalReq, SavingsAccount savingsAccount) {

        Map<String, Object> changes = savingsAccount.approveApplication(this.context.authenticatedUser(), fixedDepositApprovalReq);

        if (!changes.isEmpty()) {
            this.savingAccountRepository.save(savingsAccount);
            String noteText = fixedDepositApprovalReq.getNote();
            if (StringUtils.isNotBlank(noteText)) {
                Note note = Note.savingNote(savingsAccount, noteText);
                changes.put("note", noteText);
                this.noteRepository.save(note);
            }
        }
        return changes;
    }

    /**
     * Populate savings account with transactions
     *
     * @param account
     * @param transactions
     */
    public static void populateTransactions(SavingsAccount account, List<SavingsAccountTransaction> transactions) {
        // We do this in case the passed transaction list is read-only
        List<SavingsAccountTransaction> trans = account.getTransactions();
        // Always clear the list first to avoid dups
        trans.clear();
        trans.addAll(transactions);
    }
}
