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
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.note.data.CreateNoteResponse;
import org.apache.fineract.portfolio.note.data.DeleteNoteResponse;
import org.apache.fineract.portfolio.note.data.NoteCreateRequest;
import org.apache.fineract.portfolio.note.data.NoteDeleteRequest;
import org.apache.fineract.portfolio.note.data.NoteUpdateRequest;
import org.apache.fineract.portfolio.note.data.UpdateNoteResponse;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.note.exception.NoteNotFoundException;
import org.apache.fineract.portfolio.note.exception.NoteResourceNotSupportedException;
import org.apache.fineract.portfolio.note.model.ClientNoteDto;
import org.apache.fineract.portfolio.note.model.GroupNoteDto;
import org.apache.fineract.portfolio.note.model.LoanNoteDto;
import org.apache.fineract.portfolio.note.model.LoanTransactionsNoteDto;
import org.apache.fineract.portfolio.note.model.NoteTypeBaseDto;
import org.apache.fineract.portfolio.note.model.SavingAccountNoteDto;
import org.apache.fineract.portfolio.note.serialization.NoteCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
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
            final LoanTransactionRepository loanTransactionRepository, final NoteCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final SavingsAccountRepository savingsAccountRepository) {
        this.noteRepository = noteRepository;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.savingsAccountRepository = savingsAccountRepository;
    }

    private CreateNoteResponse createClientNote(ClientNoteDto dto) {
        final Long resourceId = dto.getResourceId(); // getClientId();
        final String note = dto.getNote();

        final Client client = this.clientRepository.findOneWithNotFoundDetection(resourceId);
        if (client == null) {
            throw new ClientNotFoundException(resourceId);
        }

        final Note newNote = new Note(client, note);
        this.noteRepository.saveAndFlush(newNote);

        return CreateNoteResponse.builder().resourceId(newNote.getId()).entityId(newNote.getId()).clientId(client.getId())
                .officeId(client.officeId()).build();
    }

    @Override
    public void createAndPersistClientNote(final Client client, final JsonCommand command) {
        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note newNote = new Note(client, noteText);
            this.noteRepository.save(newNote);
        }
    }

    private CreateNoteResponse createGroupNote(GroupNoteDto dto) {
        final Long resourceId = dto.getResourceId();
        final String note = dto.getNote();

        final Group group = this.groupRepository.findById(resourceId).orElseThrow(() -> new GroupNotFoundException(resourceId));

        final Note newNote = new Note(group, note);
        this.noteRepository.saveAndFlush(newNote);

        return CreateNoteResponse.builder().resourceId(newNote.getId()).entityId(newNote.getId()).groupId(group.getId())
                .officeId(group.officeId()).build();
    }

    private CreateNoteResponse createLoanNote(LoanNoteDto dto) {
        final Long resourceId = dto.getResourceId(); // getLoanId();
        final String note = dto.getNote();

        final Loan loan = this.loanRepository.findOneWithNotFoundDetection(resourceId);

        final Note newNote = Note.loanNote(loan, note);
        this.noteRepository.saveAndFlush(newNote);

        return CreateNoteResponse.builder().resourceId(newNote.getId()).entityId(newNote.getId()).officeId(loan.getOfficeId())
                .loanId(loan.getId()).build();
    }

    private CreateNoteResponse createLoanTransactionNote(LoanTransactionsNoteDto dto) {
        final Long resourceId = dto.getResourceId(); // subentityId
        final String note = dto.getNote();

        final LoanTransaction loanTransaction = this.loanTransactionRepository.findById(resourceId)
                .orElseThrow(() -> new LoanTransactionNotFoundException(resourceId));
        final Loan loan = loanTransaction.getLoan();

        final Note newNote = Note.loanTransactionNote(loan, loanTransaction, note);
        this.noteRepository.saveAndFlush(newNote);

        return CreateNoteResponse.builder().resourceId(newNote.getId()).entityId(newNote.getId()).officeId(loan.getOfficeId())
                .loanId(loan.getId()) // Loan can be associated
                .build();
    }

    private CreateNoteResponse createSavingAccountNote(SavingAccountNoteDto dto) {
        final Long resourceId = dto.getResourceId(); // .getSavingsId();
        final String note = dto.getNote();

        final SavingsAccount savingAccount = this.savingsAccountRepository.findById(resourceId)
                .orElseThrow(() -> new SavingsAccountNotFoundException(resourceId));

        final Note newNote = Note.savingNote(savingAccount, note);
        this.noteRepository.saveAndFlush(newNote);

        return CreateNoteResponse.builder().resourceId(newNote.getId()).entityId(newNote.getId())
                .officeId(savingAccount.getClient().getOffice().getId()).savingsId(savingAccount.getId()).build();
    }

    @Override
    public CreateNoteResponse createNote(NoteCreateRequest request) {
        NoteTypeBaseDto noteTypeBaseDto = request.getNoteTypeBaseDto();
        NoteType type = noteTypeBaseDto.getNoteType();

        switch (type) {
            case CLIENT: {
                var dto = ((ClientNoteDto) noteTypeBaseDto);
                dto.setNote(request.getNote());
                return createClientNote(dto);
            }
            case GROUP: {
                var dto = ((GroupNoteDto) noteTypeBaseDto);
                dto.setNote(request.getNote());
                return createGroupNote(dto);
            }
            case LOAN: {
                var dto = ((LoanNoteDto) noteTypeBaseDto);
                dto.setNote(request.getNote());
                return createLoanNote(dto);
            }
            case LOAN_TRANSACTION: {
                var dto = ((LoanTransactionsNoteDto) noteTypeBaseDto);
                dto.setNote(request.getNote());
                return createLoanTransactionNote(dto);
            }
            case SAVING_ACCOUNT: {
                var dto = ((SavingAccountNoteDto) noteTypeBaseDto);
                dto.setNote(request.getNote());
                return createSavingAccountNote(dto);
            }
            default:
                throw new NoteResourceNotSupportedException(type.getApiUrl());
        }

    }

    @Override
    public void createLoanNote(final Long loanId, final String note) {
        final Loan loan = this.loanRepository.findOneWithNotFoundDetection(loanId);
        final Note newNote = Note.loanNote(loan, note);

        this.noteRepository.save(newNote);
    }

    private UpdateNoteResponse updateClientNote(ClientNoteDto clientNoteDto) {
        final Long resourceId = clientNoteDto.getResourceId(); // getClientId();
        final Long noteId = clientNoteDto.getNoteId();
        final String newNote = clientNoteDto.getNote();

        final NoteType type = NoteType.CLIENT;

        final Client client = this.clientRepository.findOneWithNotFoundDetection(resourceId);

        final Note noteForUpdate = this.noteRepository.findByClientAndId(client, noteId);
        if (noteForUpdate == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }

        NoteChangesDto noteChangesDto = updateNoteEntity(newNote, noteForUpdate);

        return UpdateNoteResponse.builder().resourceId(noteForUpdate.getId()).entityId(noteForUpdate.getId()).clientId(client.getId())
                .officeId(client.officeId()).changes(noteChangesDto).build();
    }

    private NoteChangesDto updateNoteEntity(String newNote, Note noteForUpdate) {
        NoteChangesDto noteChangesDto = new NoteChangesDto();
        if (!newNote.equals(noteForUpdate.getNote())) {
            noteChangesDto.setNote(newNote);
            noteForUpdate.setNote(newNote);
            this.noteRepository.saveAndFlush(noteForUpdate);
        }
        return noteChangesDto;
    }

    private UpdateNoteResponse updateGroupNote(GroupNoteDto groupNoteDto) {
        final Long resourceId = groupNoteDto.getResourceId(); // getGroupId();
        final Long noteId = groupNoteDto.getNoteId();
        final String newNote = groupNoteDto.getNote();

        final NoteType type = NoteType.GROUP;

        final Group group = this.groupRepository.findById(resourceId).orElseThrow(() -> new GroupNotFoundException(resourceId));

        final Note noteForUpdate = this.noteRepository.findByGroupAndId(group, noteId);
        if (noteForUpdate == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }

        NoteChangesDto noteChangesDto = updateNoteEntity(newNote, noteForUpdate);

        return UpdateNoteResponse.builder().resourceId(noteForUpdate.getId()).entityId(noteForUpdate.getId()).groupId(group.getId())
                .officeId(group.officeId()).changes(noteChangesDto).build();
    }

    private UpdateNoteResponse updateLoanNote(LoanNoteDto loanNoteDto) {
        final Long resourceId = loanNoteDto.getResourceId(); // getLoanId()
        final Long noteId = loanNoteDto.getNoteId();
        final String newNote = loanNoteDto.getNote();

        final NoteType type = NoteType.LOAN;

        final Loan loan = this.loanRepository.findOneWithNotFoundDetection(resourceId);
        final Note noteForUpdate = this.noteRepository.findByLoanAndId(loan, noteId);
        if (noteForUpdate == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }

        NoteChangesDto noteChangesDto = updateNoteEntity(newNote, noteForUpdate);

        return UpdateNoteResponse.builder().resourceId(noteForUpdate.getId()).entityId(noteForUpdate.getId()).loanId(loan.getId())
                .officeId(loan.getOfficeId()).changes(noteChangesDto).build();
    }

    private UpdateNoteResponse updateLoanTransactionNote(LoanTransactionsNoteDto dto) {
        final Long resourceId = dto.getResourceId();
        final Long noteId = dto.getNoteId();
        final String newNote = dto.getNote();

        final NoteType type = NoteType.LOAN_TRANSACTION;

        final LoanTransaction loanTransaction = this.loanTransactionRepository.findById(resourceId)
                .orElseThrow(() -> new LoanTransactionNotFoundException(resourceId));
        final Loan loan = loanTransaction.getLoan();

        final Note noteForUpdate = this.noteRepository.findByLoanTransactionAndId(loanTransaction, noteId);

        if (noteForUpdate == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }

        NoteChangesDto noteChangesDto = updateNoteEntity(newNote, noteForUpdate);

        return UpdateNoteResponse.builder().resourceId(noteForUpdate.getId()).entityId(noteForUpdate.getId()).loanId(loan.getId())
                .officeId(loan.getOfficeId()).changes(noteChangesDto).build();
    }

    private UpdateNoteResponse updateSavingAccountNote(SavingAccountNoteDto savingAccountNoteDto) {
        final Long resourceId = savingAccountNoteDto.getResourceId(); // getSavingsId();
        final Long noteId = savingAccountNoteDto.getNoteId();
        final String newNote = savingAccountNoteDto.getNote();

        final NoteType type = NoteType.SAVING_ACCOUNT;

        final SavingsAccount savingAccount = this.savingsAccountRepository.findById(resourceId)
                .orElseThrow(() -> new SavingsAccountNotFoundException(resourceId));

        final Note noteForUpdate = this.noteRepository.findBySavingsAccountAndId(savingAccount, noteId);
        if (noteForUpdate == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }

        NoteChangesDto noteChangesDto = updateNoteEntity(newNote, noteForUpdate);

        return UpdateNoteResponse.builder().resourceId(noteForUpdate.getId()).entityId(noteForUpdate.getId())
                .officeId(savingAccount.getClient().getOffice().getId()).savingsId(savingAccount.getId()).changes(noteChangesDto).build();
    }

    @Override
    public UpdateNoteResponse updateNote(NoteUpdateRequest request) {
        NoteTypeBaseDto noteTypeBaseDto = request.getNoteTypeBaseDto();
        NoteType type = noteTypeBaseDto.getNoteType();

        switch (type) {
            case CLIENT: {
                var dto = ((ClientNoteDto) noteTypeBaseDto);
                dto.setNoteId(request.getNoteId());
                dto.setNote(request.getNote());
                return updateClientNote(dto);
            }
            case GROUP: {
                var dto = ((GroupNoteDto) noteTypeBaseDto);
                dto.setNoteId(request.getNoteId());
                dto.setNote(request.getNote());
                return updateGroupNote(dto);
            }
            case LOAN: {
                var dto = ((LoanNoteDto) noteTypeBaseDto);
                dto.setNoteId(request.getNoteId());
                dto.setNote(request.getNote());
                return updateLoanNote((LoanNoteDto) noteTypeBaseDto);
            }
            case LOAN_TRANSACTION: {
                var dto = ((LoanTransactionsNoteDto) noteTypeBaseDto);
                dto.setNoteId(request.getNoteId());
                dto.setNote(request.getNote());
                return updateLoanTransactionNote(dto);
            }
            case SAVING_ACCOUNT: {
                var dto = ((SavingAccountNoteDto) noteTypeBaseDto);
                dto.setNoteId(request.getNoteId());
                dto.setNote(request.getNote());
                return updateSavingAccountNote(dto);
            }
            default:
                throw new NoteResourceNotSupportedException(type.getApiUrl());
        }
    }

    @Override
    public DeleteNoteResponse deleteNote(NoteDeleteRequest noteDeleteRequest) {

        final Note noteForDelete = getNoteForDelete(noteDeleteRequest);

        this.noteRepository.delete(noteForDelete);
        return DeleteNoteResponse.builder().resourceId(noteForDelete.getId()).build();
    }

    private Note getNoteForDelete(NoteDeleteRequest noteDeleteRequest) {
        NoteTypeBaseDto noteTypeBaseDto = noteDeleteRequest.getNoteTypeBaseDto();
        Long noteId = noteDeleteRequest.getNoteId(); // entityId();

        final NoteType type = noteTypeBaseDto.getNoteType();
        Long resourceId = noteTypeBaseDto.getResourceId();
        Note noteForUpdate = null;
        switch (type) {
            case CLIENT: {
                final Client client = this.clientRepository.findOneWithNotFoundDetection(resourceId);
                noteForUpdate = this.noteRepository.findByClientAndId(client, noteId);
            }
            break;
            case GROUP: {
                Group group = this.groupRepository.findById(resourceId).orElseThrow(() -> new GroupNotFoundException(resourceId));
                noteForUpdate = this.noteRepository.findByGroupAndId(group, noteId);
            }
            break;
            case LOAN: {
                final Loan loan = this.loanRepository.findOneWithNotFoundDetection(resourceId);
                noteForUpdate = this.noteRepository.findByLoanAndId(loan, noteId);
            }
            break;
            case LOAN_TRANSACTION: {
                final Long loanTransactionId = resourceId;
                final LoanTransaction loanTransaction = this.loanTransactionRepository.findById(loanTransactionId)
                        .orElseThrow(() -> new LoanTransactionNotFoundException(loanTransactionId));
                noteForUpdate = this.noteRepository.findByLoanTransactionAndId(loanTransaction, noteId);
            }
            break;
            case SAVING_ACCOUNT: {
                final SavingsAccount savingAccount = this.savingsAccountRepository.findById(resourceId)
                        .orElseThrow(() -> new SavingsAccountNotFoundException(resourceId));

                noteForUpdate = this.noteRepository.findBySavingsAccountAndId(savingAccount, noteId);
            }
            break;
            case SHARE_ACCOUNT:
                log.error("TODO Implement getNoteForDelete for SHARE_ACCOUNT");
            break;
            case SAVINGS_TRANSACTION:
                log.error("TODO Implement getNoteForDelete for SAVINGS_TRANSACTION");
            break;
        }
        if (noteForUpdate == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }
        return noteForUpdate;
    }

}
