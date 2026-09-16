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
package org.apache.fineract.investor.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.infrastructure.event.business.domain.journalentry.LoanJournalEntryCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.investor.config.InvestorModuleIsEnabledCondition;
import org.apache.fineract.investor.domain.ExternalAssetOwnerJournalEntryMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerJournalEntryMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMappingRepository;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionOwnerTaggingData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Conditional(InvestorModuleIsEnabledCondition.class)
public class ExternalAssetOwnerJournalEntryServiceImpl implements ExternalAssetOwnerJournalEntryService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final ExternalAssetOwnerJournalEntryMappingRepository externalAssetOwnerJournalEntryMappingRepository;
    private final ExternalAssetOwnerTransferLoanMappingRepository externalAssetOwnerTransferLoanMappingRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final ExcludedTransactionTypesService excludedTransactionTypesService;

    @PostConstruct
    public void addListeners() {
        businessEventNotifierService.addPostBusinessEventListener(LoanJournalEntryCreatedBusinessEvent.class,
                event -> handleJournalEntryCreated(event.get()));
    }

    // Package-private for testing purposes.
    void handleJournalEntryCreated(final JournalEntry journalEntry) {
        LoanTransactionOwnerTaggingData taggingData = loanTransactionRepository
                .findOwnerTaggingDataById(journalEntry.getLoanTransactionId()).orElseThrow();

        externalAssetOwnerTransferLoanMappingRepository.findByLoanId(taggingData.getLoanId())
                .ifPresent(transferLoanMapping -> attributeToOwner(journalEntry, taggingData, transferLoanMapping));
    }

    private void attributeToOwner(final JournalEntry journalEntry, final LoanTransactionOwnerTaggingData taggingData,
            final ExternalAssetOwnerTransferLoanMapping transferLoanMapping) {
        if (excludedTransactionTypesService.isExcluded(taggingData.getLoanProductId(), taggingData.getTransactionType())) {
            log.debug(
                    "Skipping external asset owner attribution of journal entry of loan transaction {}: transaction type {} is excluded for loan product {}",
                    journalEntry.getLoanTransactionId(), taggingData.getTransactionType(), taggingData.getLoanProductId());
            return;
        }

        ExternalAssetOwnerJournalEntryMapping mapping = new ExternalAssetOwnerJournalEntryMapping();
        mapping.setJournalEntry(journalEntry);
        mapping.setOwner(transferLoanMapping.getOwnerTransfer().getOwner());
        externalAssetOwnerJournalEntryMappingRepository.saveAndFlush(mapping);
    }
}
