/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.note.service;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.mifosplatform.portfolio.note.domain.Note;
import org.mifosplatform.portfolio.note.domain.NoteRepository;
import org.mifosplatform.portfolio.note.domain.NoteType;
import org.mifosplatform.portfolio.note.exception.NoteNotFoundException;
import org.mifosplatform.portfolio.note.exception.NoteResourceNotSupportedException;
import org.mifosplatform.portfolio.note.serialization.NoteCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoteWritePlatformServiceJpaRepositoryImpl implements NoteWritePlatformService {

    private final NoteRepository noteRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    // private final SavingAccountRepository savingAccountRepository;
    private final LoanRepository loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final NoteCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public NoteWritePlatformServiceJpaRepositoryImpl(final NoteRepository noteRepository, final ClientRepositoryWrapper clientRepository,
            final GroupRepository groupRepository, final LoanRepository loanRepository,
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
        if (client == null) { throw new ClientNotFoundException(resourceId); }
        final Note newNote = Note.clientNoteFromJson(client, command);

        this.noteRepository.save(newNote);

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

        final Group group = this.groupRepository.findOne(resourceId);
        if (group == null) { throw new GroupNotFoundException(resourceId); }
        final Note newNote = Note.groupNoteFromJson(group, command);

        this.noteRepository.save(newNote);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newNote.getId()) //
                .withGroupId(group.getId()) //
                .withOfficeId(group.officeId()) //
                .build();
    }

    private CommandProcessingResult createLoanNote(final JsonCommand command) {

        final Long resourceId = command.getLoanId();

        final Loan loan = this.loanRepository.findOne(resourceId);
        if (loan == null) { throw new LoanNotFoundException(resourceId); }

        final String note = command.stringValueOfParameterNamed("note");
        final Note newNote = Note.loanNote(loan, note);

        this.noteRepository.save(newNote);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newNote.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withLoanId(loan.getId()) //
                .build();
    }

    private CommandProcessingResult createLoanTransactionNote(final JsonCommand command) {

        final Long resourceId = command.subentityId();

        final LoanTransaction loanTransaction = this.loanTransactionRepository.findOne(resourceId);
        if (loanTransaction == null) { throw new LoanTransactionNotFoundException(resourceId); }

        final Loan loan = loanTransaction.getLoan();

        final String note = command.stringValueOfParameterNamed("note");
        final Note newNote = Note.loanTransactionNote(loan, loanTransaction, note);

        this.noteRepository.save(newNote);

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

        final String resourceUrl = getResourceUrlFromCommand(command); //command.getSupportedEntityType();
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
            //TODO: SAVING_TRANSACTION type need to be add.
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

        final Note noteForUpdate = this.noteRepository.findByClientIdAndId(resourceId, noteId);
        if (noteForUpdate == null) { throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase()); }

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

        final Group group = this.groupRepository.findOne(resourceId);
        if (group == null) { throw new GroupNotFoundException(resourceId); }
        final Note noteForUpdate = this.noteRepository.findByGroupIdAndId(resourceId, noteId);

        if (noteForUpdate == null) { throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase()); }

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

        final Loan loan = this.loanRepository.findOne(resourceId);
        if (loan == null) { throw new LoanNotFoundException(resourceId); }
        final Note noteForUpdate = this.noteRepository.findByLoanIdAndId(resourceId, noteId);

        if (noteForUpdate == null) { throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase()); }

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

        final LoanTransaction loanTransaction = this.loanTransactionRepository.findOne(resourceId);
        if (loanTransaction == null) { throw new LoanTransactionNotFoundException(resourceId); }
        final Loan loan = loanTransaction.getLoan();

        final Note noteForUpdate = this.noteRepository.findByLoanTransactionIdAndId(resourceId, noteId);

        if (noteForUpdate == null) { throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase()); }

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

        final String resourceUrl = getResourceUrlFromCommand(command); //command.getSupportedEntityType();
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
                noteForUpdate = this.noteRepository.findByClientIdAndId(resourceId, noteId);
            }
            break;
            case GROUP: {
                resourceId = command.getGroupId();
                noteForUpdate = this.noteRepository.findByGroupIdAndId(resourceId, noteId);
            }
            break;
            case LOAN: {
                resourceId = command.getLoanId();
                noteForUpdate = this.noteRepository.findByLoanIdAndId(resourceId, noteId);
            }
            break;
            case LOAN_TRANSACTION: {
                resourceId = command.subentityId();
                noteForUpdate = this.noteRepository.findByLoanTransactionIdAndId(resourceId, noteId);
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
        }
        if (noteForUpdate == null) { throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase()); }
        return noteForUpdate;
    }

}
