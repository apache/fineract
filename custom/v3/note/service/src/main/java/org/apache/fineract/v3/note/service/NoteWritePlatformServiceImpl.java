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

package org.apache.fineract.v3.note.service;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.note.exception.NoteNotFoundException;
import org.apache.fineract.portfolio.note.exception.NoteResourceNotSupportedException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.v3.note.data.NoteCreateRequest;
import org.apache.fineract.v3.note.data.NoteCreateResponse;
import org.apache.fineract.v3.note.data.NoteDeleteRequest;
import org.apache.fineract.v3.note.data.NoteDeleteResponse;
import org.apache.fineract.v3.note.data.NoteResponseData;
import org.apache.fineract.v3.note.data.NoteUpdateRequest;
import org.apache.fineract.v3.note.data.NoteUpdateResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteWritePlatformServiceImpl implements NoteWritePlatformService {

    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepositoryWrapper groupRepository;
    private final LoanRepositoryWrapper loanRepository;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final NoteRepository noteRepository;

    @Override
    public NoteCreateResponse createNote(NoteCreateRequest createRequest) {

        Note newNoteEntity;
        Long officeId = null;
        Long clientId = null;
        Long groupId = null;
        Long loanId = null;
        Long savingsId = null;

        switch (createRequest.getNoteType()) {
            case CLIENT -> {
                clientId = createRequest.getResourceId();
                Client client = clientRepository.findOneWithNotFoundDetection(clientId);
                newNoteEntity = new Note(client, createRequest.getBody().getNote());
                officeId = client.officeId();
            }
            case GROUP -> {
                groupId = createRequest.getResourceId();
                Group group = groupRepository.findOneWithNotFoundDetection(groupId);
                newNoteEntity = new Note(group, createRequest.getBody().getNote());
                officeId = group.officeId();
            }
            case LOAN -> {
                loanId = createRequest.getResourceId();
                Loan loan = loanRepository.findOneWithNotFoundDetection(loanId);
                newNoteEntity = Note.loanNote(loan, createRequest.getBody().getNote());
                officeId = loan.getOfficeId();
            }
            case SAVING_ACCOUNT -> {
                savingsId = createRequest.getResourceId();
                SavingsAccount savingAccount = savingsAccountRepository.findOneWithNotFoundDetection(savingsId);
                officeId = savingAccount.getClient().getOffice().getId();
                newNoteEntity = Note.savingNote(savingAccount, createRequest.getBody().getNote());
            }
            case LOAN_TRANSACTION -> {
                Long loanTransactionId = createRequest.getResourceId();
                LoanTransaction loanTransaction = loanTransactionRepository.findById(loanTransactionId)
                        .orElseThrow(() -> new LoanTransactionNotFoundException(createRequest.getResourceId()));
                Loan loan = loanTransaction.getLoan();
                newNoteEntity = Note.loanTransactionNote(loan, loanTransaction, createRequest.getBody().getNote());
                officeId = loan.getOfficeId();
                loanId = loan.getId();
            }
            default -> throw new NoteResourceNotSupportedException(createRequest.getNoteType().name());
        }

        noteRepository.saveAndFlush(newNoteEntity);
        return buildNoteCreateResponse(newNoteEntity.getId(), clientId, officeId, groupId, loanId, savingsId);
    }

    @Override
    public NoteUpdateResponse updateNote(NoteUpdateRequest updateRequest) {

        Long officeId = null;
        Long clientId = null;
        Long groupId = null;
        Long loanId = null;
        Long savingsId = null;
        Long noteId = updateRequest.getNoteId();
        Note persistedNote = noteRepository.findById(noteId).orElseThrow(() -> new NoteNotFoundException(noteId));

        switch (updateRequest.getNoteType()) {
            case CLIENT -> {
                clientId = updateRequest.getResourceId();
                Client client = clientRepository.findOneWithNotFoundDetection(clientId);
                officeId = client.officeId();
            }
            case GROUP -> {
                groupId = updateRequest.getResourceId();
                Group group = groupRepository.findOneWithNotFoundDetection(groupId);
                officeId = group.officeId();
            }
            case LOAN -> {
                loanId = updateRequest.getResourceId();
                Loan loan = loanRepository.findOneWithNotFoundDetection(loanId);
                officeId = loan.getOfficeId();
            }
            case LOAN_TRANSACTION -> {
                Long loanTransactionId = updateRequest.getResourceId();
                LoanTransaction loanTransaction = loanTransactionRepository.findById(loanTransactionId)
                        .orElseThrow(() -> new LoanTransactionNotFoundException(loanTransactionId));
                Loan loan = loanTransaction.getLoan();
                officeId = loan.getOfficeId();
                loanId = loan.getId();
            }
            case SAVING_ACCOUNT -> {
                savingsId = updateRequest.getResourceId();
                SavingsAccount savingAccount = savingsAccountRepository.findOneWithNotFoundDetection(savingsId);
                officeId = savingAccount.getClient().getOffice().getId();
            }
            default -> throw new NoteResourceNotSupportedException(updateRequest.getNoteType().name());
        }

        String newNote = updateRequest.getBody().getNote();
        Map<String, Object> changes = new HashMap<>();

        if (!persistedNote.getNote().equals(newNote)) {
            persistedNote.setNote(newNote);
            noteRepository.saveAndFlush(persistedNote);
            changes.put("note", newNote);
        }

        return buildNoteUpdateResponse(persistedNote.getId(), clientId, officeId, groupId, loanId, savingsId, changes);
    }

    @Override
    public NoteDeleteResponse deleteNote(NoteDeleteRequest deleteRequest) {
        Long noteId = deleteRequest.getNoteId();

        if (!noteRepository.existsById(noteId)) {
            throw new NoteNotFoundException(noteId);
        }

        noteRepository.deleteById(noteId);
        var noteResponseData = NoteResponseData.builder().resourceId(noteId).build();
        return NoteDeleteResponse.builder().data(noteResponseData).build();
    }

    private NoteCreateResponse buildNoteCreateResponse(Long noteId, Long clientId, Long officeId, Long groupId, Long loanId,
            Long savingsId) {

        var noteResponseData = NoteResponseData.builder() //
                .resourceId(noteId) //
                .clientId(clientId) //
                .officeId(officeId) //
                .groupId(groupId) //
                .loanId(loanId) //
                .savingsId(savingsId) //
                .build();

        return NoteCreateResponse.builder().data(noteResponseData).build();
    }

    private NoteUpdateResponse buildNoteUpdateResponse(Long noteId, Long clientId, Long officeId, Long groupId, Long loanId, Long savingsId,
            Map<String, Object> changes) {

        var noteResponseData = NoteResponseData.builder() //
                .resourceId(noteId) //
                .clientId(clientId) //
                .officeId(officeId) //
                .groupId(groupId) //
                .loanId(loanId) //
                .savingsId(savingsId) //
                .changes(changes) //
                .build();

        return NoteUpdateResponse.builder().data(noteResponseData).build();
    }
}
