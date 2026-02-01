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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.note.data.NoteCreateRequest;
import org.apache.fineract.portfolio.note.data.NoteCreateResponse;
import org.apache.fineract.portfolio.note.data.NoteDeleteRequest;
import org.apache.fineract.portfolio.note.data.NoteDeleteResponse;
import org.apache.fineract.portfolio.note.data.NoteUpdateRequest;
import org.apache.fineract.portfolio.note.data.NoteUpdateResponse;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.note.exception.NoteNotFoundException;
import org.apache.fineract.portfolio.note.exception.NoteResourceNotSupportedException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;

@Slf4j
public class NoteWritePlatformServiceJpaRepositoryImpl implements NoteWritePlatformService {

    private final NoteRepository noteRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    private final LoanRepositoryWrapper loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final SavingsAccountRepository savingsAccountRepository;

    public NoteWritePlatformServiceJpaRepositoryImpl(final NoteRepository noteRepository, final ClientRepositoryWrapper clientRepository,
            final GroupRepository groupRepository, final LoanRepositoryWrapper loanRepository,
            final LoanTransactionRepository loanTransactionRepository, final SavingsAccountRepository savingsAccountRepository) {
        this.noteRepository = noteRepository;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.savingsAccountRepository = savingsAccountRepository;
    }

    @Override
    public NoteCreateResponse createNote(final NoteCreateRequest request) {
        Note note;
        Long officeId;

        switch (request.getType()) {
            case CLIENT: {
                final var client = this.clientRepository.findOneWithNotFoundDetection(request.getResourceId());
                note = noteRepository.saveAndFlush(Note.clientNote(client, request.getNote()));
                officeId = client.officeId();
            }
            break;
            case GROUP: {
                final var group = groupRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new GroupNotFoundException(request.getResourceId()));
                note = noteRepository.saveAndFlush(Note.groupNote(group, request.getNote()));
                officeId = group.officeId();
            }
            break;
            case LOAN: {
                final var loan = loanRepository.findOneWithNotFoundDetection(request.getResourceId());
                note = noteRepository.saveAndFlush(Note.loanNote(loan, request.getNote()));
                officeId = loan.getOfficeId();
            }
            break;
            case LOAN_TRANSACTION: {
                final var loanTransaction = this.loanTransactionRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new LoanTransactionNotFoundException(request.getResourceId()));
                note = noteRepository.saveAndFlush(Note.loanTransactionNote(loanTransaction.getLoan(), loanTransaction, request.getNote()));
                officeId = loanTransaction.getLoan().getOfficeId();
            }
            break;
            case SAVING_ACCOUNT: {
                final var savingAccount = savingsAccountRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new SavingsAccountNotFoundException(request.getResourceId()));
                note = noteRepository.saveAndFlush(Note.savingNote(savingAccount, request.getNote()));
                officeId = savingAccount.getClient().getOffice().getId();
            }
            break;
            default:
                throw new NoteResourceNotSupportedException(request.getType().getApiUrl());
        }

        return NoteCreateResponse.builder().entityId(note.getId()).resourceId(note.getId()).officeId(officeId).build();
    }

    @Override
    public NoteUpdateResponse updateNote(final NoteUpdateRequest request) {
        final var result = getNote(request.getType(), request.getResourceId(), request.getId());
        final var note = result.getLeft();
        final var response = NoteUpdateResponse.builder().officeId(result.getRight()).resourceId(request.getResourceId());

        if (!Strings.CI.equals(note.getNote(), request.getNote())) {
            response.changes(note.update(request.getNote()));
            noteRepository.saveAndFlush(note);
        }

        return response.build();
    }

    @Override
    public NoteDeleteResponse deleteNote(final NoteDeleteRequest request) {
        var note = getNote(request.getType(), request.getResourceId(), request.getId());

        noteRepository.delete(note.getLeft());

        return NoteDeleteResponse.builder().resourceId(request.getId()).build();
    }

    private Pair<Note, Long> getNote(NoteType type, Long resourceId, Long noteId) {
        Note note = null;
        Long officeId = null;

        switch (type) {
            case CLIENT: {
                final var client = clientRepository.findOneWithNotFoundDetection(resourceId);
                note = noteRepository.findByClientAndId(client, noteId);
                officeId = client.officeId();
            }
            break;
            case GROUP: {
                final var group = groupRepository.findById(resourceId).orElseThrow(() -> new GroupNotFoundException(resourceId));
                note = noteRepository.findByGroupAndId(group, noteId);
                officeId = group.officeId();
            }
            break;
            case LOAN: {
                final var loan = loanRepository.findOneWithNotFoundDetection(resourceId);
                note = noteRepository.findByLoanAndId(loan, noteId);
                officeId = loan.getOfficeId();
            }
            break;
            case LOAN_TRANSACTION: {
                final var loanTransaction = loanTransactionRepository.findById(resourceId)
                        .orElseThrow(() -> new LoanTransactionNotFoundException(resourceId));
                note = noteRepository.findByLoanTransactionAndId(loanTransaction, noteId);
                officeId = loanTransaction.getLoan().getOfficeId();
            }
            break;
            case SAVING_ACCOUNT: {
                final var savingAccount = savingsAccountRepository.findById(resourceId)
                        .orElseThrow(() -> new SavingsAccountNotFoundException(resourceId));
                note = noteRepository.findBySavingsAccountAndId(savingAccount, noteId);
                officeId = savingAccount.getClient().getOffice().getId();
            }
            break;
            case SHARE_ACCOUNT:
            case SAVINGS_TRANSACTION:
                log.error("Not yet implemented: {}", type);
            break;
        }

        if (note == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }

        return Pair.of(note, officeId);
    }
}
