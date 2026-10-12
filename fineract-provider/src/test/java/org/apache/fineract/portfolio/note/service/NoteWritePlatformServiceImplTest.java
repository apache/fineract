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
package org.apache.fineract.portfolio.note.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.note.data.NoteCreateRequest;
import org.apache.fineract.portfolio.note.data.NoteCreateResponse;
import org.apache.fineract.portfolio.note.data.NoteDeleteRequest;
import org.apache.fineract.portfolio.note.data.NoteDeleteResponse;
import org.apache.fineract.portfolio.note.data.NoteUpdateRequest;
import org.apache.fineract.portfolio.note.data.NoteUpdateResponse;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccount;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoteWritePlatformServiceImplTest {

    @Mock
    private NoteRepository noteRepository;
    @Mock
    private ClientRepositoryWrapper clientRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private LoanRepositoryWrapper loanRepository;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private SavingsAccountRepository savingsAccountRepository;
    @Mock
    private SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    @Mock
    private ShareAccountRepositoryWrapper shareAccountRepository;
    @Mock
    private ShareAccount shareAccount;
    @Mock
    private SavingsAccountTransaction savingsAccountTransaction;
    @Mock
    private SavingsAccount savingsAccount;
    @Mock
    private Client client;
    @Mock
    private Office office;
    @Mock
    private Note note;

    private NoteWritePlatformServiceImpl subject;

    @BeforeEach
    void setUp() {
        subject = new NoteWritePlatformServiceImpl(noteRepository, clientRepository, groupRepository, loanRepository,
                loanTransactionRepository, savingsAccountRepository, savingsAccountTransactionRepository, shareAccountRepository);
    }

    @Test
    void createNoteShouldSupportShareAccount() {
        NoteCreateRequest request = NoteCreateRequest.builder().resourceId(10L).type(NoteType.SHARE_ACCOUNT).note("share note").build();
        when(shareAccountRepository.findOneWithNotFoundDetection(10L)).thenReturn(shareAccount);
        when(shareAccount.getOfficeId()).thenReturn(7L);
        when(noteRepository.saveAndFlush(any(Note.class))).thenReturn(note);
        when(note.getId()).thenReturn(101L);

        NoteCreateResponse response = subject.createNote(request);

        assertEquals(101L, response.getResourceId());
        assertEquals(7L, response.getOfficeId());
        verify(noteRepository).saveAndFlush(any(Note.class));
    }

    @Test
    void createNoteShouldSupportSavingsTransaction() {
        NoteCreateRequest request = NoteCreateRequest.builder().resourceId(22L).type(NoteType.SAVINGS_TRANSACTION)
                .note("savings transaction note").build();
        when(savingsAccountTransactionRepository.findById(22L)).thenReturn(Optional.of(savingsAccountTransaction));
        when(savingsAccountTransaction.getSavingsAccount()).thenReturn(savingsAccount);
        when(savingsAccount.getClient()).thenReturn(client);
        when(client.getOffice()).thenReturn(office);
        when(office.getId()).thenReturn(8L);
        when(noteRepository.saveAndFlush(any(Note.class))).thenReturn(note);
        when(note.getId()).thenReturn(202L);

        NoteCreateResponse response = subject.createNote(request);

        assertEquals(202L, response.getResourceId());
        assertEquals(8L, response.getOfficeId());
        verify(noteRepository).saveAndFlush(any(Note.class));
    }

    @Test
    void updateNoteShouldSupportShareAccount() {
        NoteUpdateRequest request = NoteUpdateRequest.builder().id(9L).resourceId(10L).type(NoteType.SHARE_ACCOUNT).note("updated").build();
        when(shareAccountRepository.findOneWithNotFoundDetection(10L)).thenReturn(shareAccount);
        when(shareAccount.getOfficeId()).thenReturn(7L);
        when(noteRepository.findByShareAccountAndId(shareAccount, 9L)).thenReturn(note);
        when(note.getNote()).thenReturn("old");
        when(note.update("updated")).thenReturn(Map.of("note", "updated"));

        NoteUpdateResponse response = subject.updateNote(request);

        assertEquals(10L, response.getResourceId());
        assertEquals(7L, response.getOfficeId());
        assertEquals(Map.of("note", "updated"), response.getChanges());
        verify(noteRepository).saveAndFlush(note);
    }

    @Test
    void deleteNoteShouldSupportSavingsTransaction() {
        NoteDeleteRequest request = NoteDeleteRequest.builder().id(3L).resourceId(22L).type(NoteType.SAVINGS_TRANSACTION).build();
        when(savingsAccountTransactionRepository.findById(22L)).thenReturn(Optional.of(savingsAccountTransaction));
        when(noteRepository.findBySavingsTransactionAndId(savingsAccountTransaction, 3L)).thenReturn(note);
        when(savingsAccountTransaction.getSavingsAccount()).thenReturn(savingsAccount);
        when(savingsAccount.getClient()).thenReturn(client);
        when(client.getOffice()).thenReturn(office);
        when(office.getId()).thenReturn(8L);

        NoteDeleteResponse response = subject.deleteNote(request);

        assertEquals(3L, response.getResourceId());
        verify(noteRepository).delete(note);
    }
}
