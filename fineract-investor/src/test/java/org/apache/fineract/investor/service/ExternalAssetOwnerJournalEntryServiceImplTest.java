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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerJournalEntryMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerJournalEntryMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMappingRepository;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionOwnerTaggingData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExternalAssetOwnerJournalEntryServiceImplTest {

    private static final Long LOAN_TRANSACTION_ID = 11L;
    private static final Long LOAN_ID = 22L;
    private static final Long LOAN_PRODUCT_ID = 33L;

    @Mock
    private org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private ExternalAssetOwnerJournalEntryMappingRepository externalAssetOwnerJournalEntryMappingRepository;
    @Mock
    private ExternalAssetOwnerTransferLoanMappingRepository externalAssetOwnerTransferLoanMappingRepository;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private ExcludedTransactionTypesService excludedTransactionTypesService;

    @InjectMocks
    private ExternalAssetOwnerJournalEntryServiceImpl underTest;

    @Test
    public void testJournalEntryOfNotExternallyOwnedLoanIsNotAttributedAndTypeIsNotEvaluated() {
        JournalEntry journalEntry = journalEntry();
        stubTaggingData(LoanTransactionType.BUY_DOWN_FEE);
        when(externalAssetOwnerTransferLoanMappingRepository.findByLoanId(LOAN_ID)).thenReturn(Optional.empty());

        underTest.handleJournalEntryCreated(journalEntry);

        verifyNoInteractions(externalAssetOwnerJournalEntryMappingRepository);
        verifyNoInteractions(excludedTransactionTypesService);
    }

    @Test
    public void testJournalEntryOfNonExcludedTypeIsAttributedToTheOwner() {
        JournalEntry journalEntry = journalEntry();
        ExternalAssetOwner owner = stubActiveOwner();
        stubTaggingData(LoanTransactionType.REPAYMENT);
        when(excludedTransactionTypesService.isExcluded(LOAN_PRODUCT_ID, LoanTransactionType.REPAYMENT)).thenReturn(false);

        underTest.handleJournalEntryCreated(journalEntry);

        ArgumentCaptor<ExternalAssetOwnerJournalEntryMapping> captor = ArgumentCaptor.forClass(ExternalAssetOwnerJournalEntryMapping.class);
        verify(externalAssetOwnerJournalEntryMappingRepository, times(1)).saveAndFlush(captor.capture());
        assertEquals(journalEntry, captor.getValue().getJournalEntry());
        assertEquals(owner, captor.getValue().getOwner());
    }

    @Test
    public void testJournalEntryOfExcludedTypeIsNotAttributedToTheOwner() {
        JournalEntry journalEntry = journalEntry();
        stubActiveOwner();
        stubTaggingData(LoanTransactionType.BUY_DOWN_FEE);
        when(excludedTransactionTypesService.isExcluded(LOAN_PRODUCT_ID, LoanTransactionType.BUY_DOWN_FEE)).thenReturn(true);

        underTest.handleJournalEntryCreated(journalEntry);

        verifyNoInteractions(externalAssetOwnerJournalEntryMappingRepository);
    }

    @Test
    public void testMissingLoanTransactionStillFailsFast() {
        JournalEntry journalEntry = journalEntry();
        when(loanTransactionRepository.findOwnerTaggingDataById(LOAN_TRANSACTION_ID)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> underTest.handleJournalEntryCreated(journalEntry));

        verify(externalAssetOwnerJournalEntryMappingRepository, times(0)).saveAndFlush(any());
    }

    private JournalEntry journalEntry() {
        JournalEntry journalEntry = mock(JournalEntry.class);
        when(journalEntry.getLoanTransactionId()).thenReturn(LOAN_TRANSACTION_ID);
        return journalEntry;
    }

    private void stubTaggingData(final LoanTransactionType transactionType) {
        when(loanTransactionRepository.findOwnerTaggingDataById(LOAN_TRANSACTION_ID))
                .thenReturn(Optional.of(new LoanTransactionOwnerTaggingData(LOAN_ID, LOAN_PRODUCT_ID, transactionType)));
    }

    private ExternalAssetOwner stubActiveOwner() {
        ExternalAssetOwner owner = mock(ExternalAssetOwner.class);
        ExternalAssetOwnerTransfer transfer = mock(ExternalAssetOwnerTransfer.class);
        ExternalAssetOwnerTransferLoanMapping loanMapping = mock(ExternalAssetOwnerTransferLoanMapping.class);
        // Not reached when the transaction type is excluded, which is exactly what the exclusion test asserts.
        org.mockito.Mockito.lenient().when(transfer.getOwner()).thenReturn(owner);
        org.mockito.Mockito.lenient().when(loanMapping.getOwnerTransfer()).thenReturn(transfer);
        when(externalAssetOwnerTransferLoanMappingRepository.findByLoanId(LOAN_ID)).thenReturn(Optional.of(loanMapping));
        return owner;
    }
}
