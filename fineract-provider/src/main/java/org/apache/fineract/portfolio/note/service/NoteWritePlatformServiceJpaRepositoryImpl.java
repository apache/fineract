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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.note.data.NoteCreateRequest;
import org.apache.fineract.portfolio.note.data.NoteCreateResponse;
import org.apache.fineract.portfolio.note.data.NoteDeleteByResourceIdRequest;
import org.apache.fineract.portfolio.note.data.NoteDeleteRequest;
import org.apache.fineract.portfolio.note.data.NoteDeleteResponse;
import org.apache.fineract.portfolio.note.data.NoteUpdateRequest;
import org.apache.fineract.portfolio.note.data.NoteUpdateResponse;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.note.exception.NoteNotFoundException;
import org.apache.fineract.portfolio.note.exception.NoteResourceNotSupportedException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;

@Slf4j
@RequiredArgsConstructor
public class NoteWritePlatformServiceJpaRepositoryImpl implements NoteWritePlatformService {

    private final NoteRepository noteRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    private final LoanRepositoryWrapper loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;

    @Override
    public NoteCreateResponse createNote(final NoteCreateRequest request) {
        Note note;
        var response = NoteCreateResponse.builder().resourceId(request.getResourceId());

        switch (request.getNoteType()) {
            case CLIENT -> {
                var resource = clientRepository.findOneWithNotFoundDetection(request.getResourceId());

                note = Note.builder().client(resource).note(request.getNote()).noteTypeId(request.getNoteType().getValue()).build();

                response = response.officeId(resource.getOffice() != null ? resource.getOffice().getId() : null);
            }
            case GROUP -> {
                var resource = groupRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new GroupNotFoundException(request.getResourceId()));

                note = Note.builder().group(resource).note(request.getNote()).noteTypeId(request.getNoteType().getValue()).build();

                response = response.officeId(resource.getOffice() != null ? resource.getOffice().getId() : null);
            }
            case LOAN -> {
                var resource = loanRepository.findOneWithNotFoundDetection(request.getResourceId());

                note = Note.builder().loan(resource).client(resource.getClient()).note(request.getNote())
                        .noteTypeId(request.getNoteType().getValue()).build();

                response = response.officeId(resource.getOffice() != null ? resource.getOffice().getId() : null);
            }
            case LOAN_TRANSACTION -> {
                var resource = loanTransactionRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new LoanTransactionNotFoundException(request.getResourceId()));

                note = Note.builder().loanTransaction(resource).loan(resource.getLoan()).client(resource.getLoan().getClient())
                        .note(request.getNote()).noteTypeId(request.getNoteType().getValue()).build();

                response = response.officeId(resource.getOffice() != null ? resource.getOffice().getId() : null);
            }
            case SAVING_ACCOUNT -> {
                var resource = savingsAccountRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new SavingsAccountNotFoundException(request.getResourceId()));
                // TODO: fix this
                // var transaction = savingsAccountTransactionRepository.findBySavingsAccount(resource);

                note = Note.builder().savingsAccount(resource)
                        // .savingsTransaction(transaction)
                        .client(resource.getClient()).note(request.getNote()).noteTypeId(request.getNoteType().getValue()).build();

                if (resource.getClient() != null && resource.getClient().getOffice() != null) {
                    response = response.officeId(resource.getClient().getOffice().getId());
                }
            }
            default -> throw new NoteResourceNotSupportedException(request.getNoteType().getApiUrl());

            // TODO Implement getNoteForDelete for SHARE_ACCOUNT
            // TODO Implement getNoteForDelete for SAVINGS_TRANSACTION
        }

        note = noteRepository.saveAndFlush(note);

        return response.entityId(note.getId()).build();
    }

    @Override
    public NoteUpdateResponse updateNote(final NoteUpdateRequest request) {
        Note note = null;
        var response = NoteUpdateResponse.builder().resourceId(request.getResourceId());

        switch (request.getNoteType()) {
            case CLIENT -> {
                var resource = clientRepository.findOneWithNotFoundDetection(request.getResourceId());

                note = noteRepository.findByClientAndId(resource, request.getNoteId());
            }
            case GROUP -> {
                var resource = groupRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new GroupNotFoundException(request.getResourceId()));

                note = noteRepository.findByGroupAndId(resource, request.getNoteId());
            }
            case LOAN -> {
                var resource = loanRepository.findOneWithNotFoundDetection(request.getResourceId());

                note = noteRepository.findByLoanAndId(resource, request.getNoteId());
            }
            case LOAN_TRANSACTION -> {
                var resource = loanTransactionRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new LoanTransactionNotFoundException(request.getResourceId()));

                note = noteRepository.findByLoanTransactionAndId(resource, request.getNoteId());
            }
            case SAVING_ACCOUNT -> {
                var resource = savingsAccountRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new SavingsAccountNotFoundException(request.getResourceId()));

                note = noteRepository.findBySavingsAccountAndId(resource, request.getNoteId());
            }
            default -> throw new NoteResourceNotSupportedException(request.getNoteType().getApiUrl());

            // TODO Implement getNoteForDelete for SHARE_ACCOUNT
            // TODO Implement getNoteForDelete for SAVINGS_TRANSACTION
        }

        if (note == null) {
            throw new NoteNotFoundException(request.getNoteId(), request.getResourceId(), request.getNoteType().name().toLowerCase());
        }

        if (Objects.equals(request.getNote(), note.getNote())) {
            note = noteRepository.saveAndFlush(note);
            response.entityId(note.getId()).changes(Map.of("note", request.getNote()));
        }

        return response.build();
    }

    @Override
    public NoteDeleteResponse deleteNote(final NoteDeleteRequest request) {
        Note note;

        switch (request.getNoteType()) {
            case CLIENT -> {
                var resource = clientRepository.findOneWithNotFoundDetection(request.getResourceId());
                note = noteRepository.findByClientAndId(resource, request.getNoteId());
            }
            case GROUP -> {
                var resource = groupRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new GroupNotFoundException(request.getResourceId()));
                note = noteRepository.findByGroupAndId(resource, request.getNoteId());
            }
            case LOAN -> {
                var resource = loanRepository.findOneWithNotFoundDetection(request.getResourceId());
                note = noteRepository.findByLoanAndId(resource, request.getNoteId());
            }
            case LOAN_TRANSACTION -> {
                var resource = loanTransactionRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new LoanTransactionNotFoundException(request.getResourceId()));
                note = noteRepository.findByLoanTransactionAndId(resource, request.getNoteId());
            }
            case SAVING_ACCOUNT -> {
                var resource = savingsAccountRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new SavingsAccountNotFoundException(request.getResourceId()));

                note = noteRepository.findBySavingsAccountAndId(resource, request.getNoteId());
            }
            default ->
                throw new NoteNotFoundException(request.getNoteId(), request.getResourceId(), request.getNoteType().name().toLowerCase());

            // TODO Implement getNoteForDelete for SHARE_ACCOUNT
            // TODO Implement getNoteForDelete for SAVINGS_TRANSACTION
        }

        noteRepository.delete(note);

        return NoteDeleteResponse.builder().noteId(request.getNoteId()).resourceId(request.getResourceId()).build();
    }

    @Override
    public void deleteByResource(NoteDeleteByResourceIdRequest request) {
        List<Note> notes = null;

        switch (request.getNoteType()) {
            case CLIENT -> {
                notes = this.noteRepository.findByClientId(request.getResourceId());
            }
            case GROUP -> {
                notes = this.noteRepository.findByGroupId(request.getResourceId());
            }
            case LOAN -> {
                notes = this.noteRepository.findByLoanId(request.getResourceId());
            }
            case LOAN_TRANSACTION -> {
                notes = this.noteRepository.findByLoanTransactionId(request.getResourceId());
            }
            case SAVING_ACCOUNT -> {
                notes = this.noteRepository.findBySavingsAccountId(request.getResourceId());
            }
            default -> throw new NoteNotFoundException(null, request.getResourceId(), request.getNoteType().name().toLowerCase());

            // TODO Implement getNoteForDelete for SHARE_ACCOUNT
            // TODO Implement getNoteForDelete for SAVINGS_TRANSACTION
        }

        if (notes != null && !notes.isEmpty()) {
            noteRepository.deleteAllInBatch(notes);
        }
    }
}
