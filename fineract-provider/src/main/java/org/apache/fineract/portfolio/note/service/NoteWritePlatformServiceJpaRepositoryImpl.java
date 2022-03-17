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

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
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
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.note.exception.NoteNotFoundException;
import org.apache.fineract.portfolio.note.exception.NoteResourceNotSupportedException;
import org.apache.fineract.portfolio.note.serialization.NoteCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoteWritePlatformServiceJpaRepositoryImpl implements NoteWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(NoteWritePlatformServiceJpaRepositoryImpl.class);
    private final NoteRepository noteRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    private final LoanRepositoryWrapper loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final NoteCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    public NoteWritePlatformServiceJpaRepositoryImpl(final NoteRepository noteRepository, final ClientRepositoryWrapper clientRepository,
            final GroupRepository groupRepository, final LoanRepositoryWrapper loanRepository,
            final LoanTransactionRepository loanTransactionRepository, final NoteCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.noteRepository = noteRepository;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    private CommandProcessingResult createClientNote(final JsonCommand command) {

        final Long resourceId = command.getClientId();

        final Client client = this.clientRepository.findOneWithNotFoundDetection(resourceId);
        if (client == null) {
            throw new ClientNotFoundException(resourceId);
        }
        final Note newNote = Note.clientNoteFromJson(client, command);

        this.noteRepository.saveAndFlush(newNote);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newNote.getId()) //
                .withClientId(client.getId()) //
                .withOfficeId(client.officeId()) //
                .build();

    }

    @Override
    public void createAndPersistClientNote(final Client client, final JsonCommand command) {
        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note newNote = new Note(client, noteText);
            this.noteRepository.save(newNote);
        }
    }

    private CommandProcessingResult createGroupNote(final JsonCommand command) {

        final Long resourceId = command.getGroupId();

        final Group group = this.groupRepository.findById(resourceId).orElseThrow(() -> new GroupNotFoundException(resourceId));
        final Note newNote = Note.groupNoteFromJson(group, command);

        this.noteRepository.saveAndFlush(newNote);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newNote.getId()) //
                .withGroupId(group.getId()) //
                .withOfficeId(group.officeId()) //
                .build();
    }

    private CommandProcessingResult createLoanNote(final JsonCommand command) {

        final Long resourceId = command.getLoanId();

        final Loan loan = this.loanRepository.findOneWithNotFoundDetection(resourceId);
        final String note = command.stringValueOfParameterNamed("note");
        final Note newNote = Note.loanNote(loan, note);

        this.noteRepository.saveAndFlush(newNote);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newNote.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withLoanId(loan.getId()) //
                .build();
    }

    private CommandProcessingResult createLoanTransactionNote(final JsonCommand command) {

        final Long resourceId = command.subentityId();

        final LoanTransaction loanTransaction = this.loanTransactionRepository.findById(resourceId)
                .orElseThrow(() -> new LoanTransactionNotFoundException(resourceId));

        final Loan loan = loanTransaction.getLoan();

        final String note = command.stringValueOfParameterNamed("note");
        final Note newNote = Note.loanTransactionNote(loan, loanTransaction, note);

        this.noteRepository.saveAndFlush(newNote);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newNote.getId()) //
                .withOfficeId(loan.getOfficeId())//
                .withLoanId(loan.getId())// Loan can be associated
                .build();
    }

    // private CommandProcessingResult createSavingAccountNote(final JsonCommand
    // command) {
    //
    // final Long resourceId = command.getSupportedEntityId();
    //
    // final SavingAccount savingAccount =
    // this.savingAccountRepository.findOne(resourceId);
    // if (savingAccount == null) { throw new
    // SavingAccountNotFoundException(resourceId); }
    //
    // final String note = command.stringValueOfParameterNamed("note");
    // final Note newNote = Note.savingNote(savingAccount, note);
    //
    // this.noteRepository.save(newNote);
    //
    // return new CommandProcessingResultBuilder() //
    // .withCommandId(command.commandId()) //
    // .withEntityId(newNote.getId()) //
    // .withClientId(savingAccount.getClient().getId()).withOfficeId(savingAccount.getClient().getOffice().getId()).build();
    // }

    @Override
    public CommandProcessingResult createNote(final JsonCommand command) {

        this.fromApiJsonDeserializer.validateNote(command.json());

        final String resourceUrl = getResourceUrlFromCommand(command); // command.getSupportedEntityType();
        final NoteType type = NoteType.fromApiUrl(resourceUrl);
        switch (type) {
            case CLIENT: {
                return createClientNote(command);
            }
            case GROUP: {
                return createGroupNote(command);
            }
            case LOAN: {
                return createLoanNote(command);
            }
            case LOAN_TRANSACTION: {
                return createLoanTransactionNote(command);
            }
            // case SAVING_ACCOUNT: {
            // return createSavingAccountNote(command);
            // }
            default:
                throw new NoteResourceNotSupportedException(resourceUrl);
        }

    }

    private String getResourceUrlFromCommand(JsonCommand command) {

        final String resourceUrl;

        if (command.getClientId() != null) {
            resourceUrl = NoteType.CLIENT.getApiUrl();
        } else if (command.getGroupId() != null) {
            resourceUrl = NoteType.GROUP.getApiUrl();
        } else if (command.getLoanId() != null) {
            if (command.subentityId() != null) {
                resourceUrl = NoteType.LOAN_TRANSACTION.getApiUrl();
            } else {
                resourceUrl = NoteType.LOAN.getApiUrl();
            }
        } else if (command.getSavingsId() != null) {
            // TODO: SAVING_TRANSACTION type need to be add.
            resourceUrl = NoteType.SAVING_ACCOUNT.getApiUrl();
        } else {
            resourceUrl = "";
        }

        return resourceUrl;
    }

    private CommandProcessingResult updateClientNote(final JsonCommand command) {

        final Long resourceId = command.getClientId();
        final Long noteId = command.entityId();

        final NoteType type = NoteType.CLIENT;

        final Client client = this.clientRepository.findOneWithNotFoundDetection(resourceId);

        final Note noteForUpdate = this.noteRepository.findByClientAndId(client, noteId);
        if (noteForUpdate == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }

        final Map<String, Object> changes = noteForUpdate.update(command);

        if (!changes.isEmpty()) {
            this.noteRepository.saveAndFlush(noteForUpdate);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(noteForUpdate.getId()) //
                .withClientId(client.getId()) //
                .withOfficeId(client.officeId()) //
                .with(changes) //
                .build();
    }

    private CommandProcessingResult updateGroupNote(final JsonCommand command) {

        final Long resourceId = command.getGroupId();
        final Long noteId = command.entityId();

        final NoteType type = NoteType.GROUP;

        final Group group = this.groupRepository.findById(resourceId).orElseThrow(() -> new GroupNotFoundException(resourceId));

        final Note noteForUpdate = this.noteRepository.findByGroupAndId(group, noteId);

        if (noteForUpdate == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }

        final Map<String, Object> changes = noteForUpdate.update(command);

        if (!changes.isEmpty()) {
            this.noteRepository.saveAndFlush(noteForUpdate);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(noteForUpdate.getId()) //
                .withGroupId(group.getId()) //
                .withOfficeId(group.officeId()) //
                .with(changes).build();
    }

    private CommandProcessingResult updateLoanNote(final JsonCommand command) {

        final Long resourceId = command.getLoanId();
        final Long noteId = command.entityId();

        final NoteType type = NoteType.LOAN;

        final Loan loan = this.loanRepository.findOneWithNotFoundDetection(resourceId);
        final Note noteForUpdate = this.noteRepository.findByLoanAndId(loan, noteId);
        if (noteForUpdate == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }

        final Map<String, Object> changes = noteForUpdate.update(command);

        if (!changes.isEmpty()) {
            this.noteRepository.saveAndFlush(noteForUpdate);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(noteForUpdate.getId())
                .withLoanId(loan.getId()).withOfficeId(loan.getOfficeId()).with(changes).build();
    }

    private CommandProcessingResult updateLoanTransactionNote(final JsonCommand command) {

        final Long resourceId = command.subentityId();
        final Long noteId = command.entityId();

        final NoteType type = NoteType.LOAN_TRANSACTION;

        final LoanTransaction loanTransaction = this.loanTransactionRepository.findById(resourceId)
                .orElseThrow(() -> new LoanTransactionNotFoundException(resourceId));
        final Loan loan = loanTransaction.getLoan();

        final Note noteForUpdate = this.noteRepository.findByLoanTransactionAndId(loanTransaction, noteId);

        if (noteForUpdate == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }

        final Map<String, Object> changes = noteForUpdate.update(command);

        if (!changes.isEmpty()) {
            this.noteRepository.saveAndFlush(noteForUpdate);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(noteForUpdate.getId())
                .withLoanId(loan.getId()).withOfficeId(loan.getOfficeId()).with(changes).build();
    }

    // private CommandProcessingResult updateSavingAccountNote(final JsonCommand
    // command) {
    //
    // final Long resourceId = command.getSupportedEntityId();
    // final Long noteId = command.entityId();
    // final String resourceUrl = command.getSupportedEntityType();
    //
    // final NoteType type = NoteType.fromApiUrl(resourceUrl);
    //
    // final SavingAccount savingAccount =
    // this.savingAccountRepository.findOne(resourceId);
    // if (savingAccount == null) { throw new
    // SavingAccountNotFoundException(resourceId); }
    //
    // final Note noteForUpdate =
    // this.noteRepository.findBySavingAccountIdAndId(resourceId, noteId);
    //
    // if (noteForUpdate == null) { throw new NoteNotFoundException(noteId,
    // resourceId, type.name().toLowerCase()); }
    //
    // final Map<String, Object> changes = noteForUpdate.update(command);
    //
    // if (!changes.isEmpty()) {
    // this.noteRepository.saveAndFlush(noteForUpdate);
    // }
    //
    // return new CommandProcessingResultBuilder()
    // //
    // .withCommandId(command.commandId())
    // //
    // .withEntityId(noteForUpdate.getId())
    // //
    // .withClientId(savingAccount.getClient().getId()).withOfficeId(savingAccount.getClient().getOffice().getId()).with(changes)
    // .build();
    // }

    @Override
    public CommandProcessingResult updateNote(final JsonCommand command) {

        this.fromApiJsonDeserializer.validateNote(command.json());

        final String resourceUrl = getResourceUrlFromCommand(command); // command.getSupportedEntityType();
        final NoteType type = NoteType.fromApiUrl(resourceUrl);

        switch (type) {
            case CLIENT: {
                return updateClientNote(command);
            }
            case GROUP: {
                return updateGroupNote(command);
            }
            case LOAN: {
                return updateLoanNote(command);
            }
            case LOAN_TRANSACTION: {
                return updateLoanTransactionNote(command);
            }
            // case SAVING_ACCOUNT: {
            // return updateSavingAccountNote(command);
            // }
            default:
                throw new NoteResourceNotSupportedException(resourceUrl);
        }
    }

    @Override
    public CommandProcessingResult deleteNote(final JsonCommand command) {

        final Note noteForDelete = getNoteForDelete(command);

        this.noteRepository.delete(noteForDelete);
        return new CommandProcessingResultBuilder() //
                .withCommandId(null) //
                .withEntityId(command.entityId()) //
                .build();
    }

    private Note getNoteForDelete(final JsonCommand command) {
        final String resourceUrl = getResourceUrlFromCommand(command);// command.getSupportedEntityType();
        final Long noteId = command.entityId();
        final NoteType type = NoteType.fromApiUrl(resourceUrl);
        Long resourceId = null;
        Note noteForUpdate = null;
        switch (type) {
            case CLIENT: {
                resourceId = command.getClientId();
                final Client client = this.clientRepository.findOneWithNotFoundDetection(resourceId);
                noteForUpdate = this.noteRepository.findByClientAndId(client, noteId);
            }
            break;
            case GROUP: {
                final Long groupId = command.getGroupId();
                resourceId = groupId;
                Group group = this.groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
                noteForUpdate = this.noteRepository.findByGroupAndId(group, noteId);
            }
            break;
            case LOAN: {
                resourceId = command.getLoanId();
                final Loan loan = this.loanRepository.findOneWithNotFoundDetection(resourceId);
                noteForUpdate = this.noteRepository.findByLoanAndId(loan, noteId);
            }
            break;
            case LOAN_TRANSACTION: {
                resourceId = command.subentityId();
                final Long loanTransactionId = resourceId;
                final LoanTransaction loanTransaction = this.loanTransactionRepository.findById(loanTransactionId)
                        .orElseThrow(() -> new LoanTransactionNotFoundException(loanTransactionId));
                noteForUpdate = this.noteRepository.findByLoanTransactionAndId(loanTransaction, noteId);
            }
            break;
            // case SAVING_ACCOUNT: {
            // noteForUpdate =
            // this.noteRepository.findBySavingAccountIdAndId(resourceId,
            // noteId);
            // }
            // break;
            case SAVING_ACCOUNT:
            break;
            case SHARE_ACCOUNT:
                LOG.error("TODO Implement getNoteForDelete for SHARE_ACCOUNT");
            break;
            case SAVINGS_TRANSACTION:
                LOG.error("TODO Implement getNoteForDelete for SAVINGS_TRANSACTION");
            break;
        }
        if (noteForUpdate == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }
        return noteForUpdate;
    }

}
