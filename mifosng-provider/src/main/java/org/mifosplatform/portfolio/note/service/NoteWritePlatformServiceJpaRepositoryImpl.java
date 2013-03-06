/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.note.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
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
import org.mifosplatform.portfolio.note.serialization.NoteCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingAccount;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingAccountRepository;
import org.mifosplatform.portfolio.savingsaccount.exception.SavingAccountNotFoundException;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccount;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountRepository;
import org.mifosplatform.portfolio.savingsdepositaccount.exception.DepositAccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoteWritePlatformServiceJpaRepositoryImpl implements NoteWritePlatformService {

    private final NoteRepository noteRepository;
    private final ClientRepository clientRepository;
    private final GroupRepository groupRepository;
    private final DepositAccountRepository depositAccountRepository;
    private final SavingAccountRepository savingAccountRepository;
    private final LoanRepository loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final NoteCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public NoteWritePlatformServiceJpaRepositoryImpl(final NoteRepository noteRepository, final ClientRepository clientRepository,
            final GroupRepository groupRepository, final DepositAccountRepository depositAccountRepository,
            final SavingAccountRepository savingAccountRepository, final LoanRepository loanRepository,
            final LoanTransactionRepository loanTransactionRepository, final NoteCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.noteRepository = noteRepository;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.depositAccountRepository = depositAccountRepository;
        this.savingAccountRepository = savingAccountRepository;
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Override
    public CommandProcessingResult createNote(final JsonCommand command) {

        this.fromApiJsonDeserializer.validateNote(command.json());

        final Note newNote = getNewNote(command);

        this.noteRepository.save(newNote);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newNote.getId()) //
                .build();

    }

    @Override
    public CommandProcessingResult updateNote(final JsonCommand command) {

        this.fromApiJsonDeserializer.validateNote(command.json());

        final Note noteForUpdate = getNoteForUpdate(command);
        
        final Map<String, Object> changes = noteForUpdate.update(command);

        if (!changes.isEmpty()) {
            this.noteRepository.saveAndFlush(noteForUpdate);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(noteForUpdate.getId()) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult deleteNote(JsonCommand command) {
        
        final Note noteForDelete = getNoteForUpdate(command);

        this.noteRepository.delete(noteForDelete);
        return new CommandProcessingResultBuilder() //
                .withCommandId(null) //
                .withEntityId(command.entityId()) //
                .build();
    }

    private Note getNewNote(final JsonCommand command) {
        final String resourceUrl = command.getSupportedEntityType();
        final Long resourceId = command.getSupportedEntityId();
        final NoteType type = NoteType.fromApiUrl(resourceUrl);
        Note newNote = null;
        final String note = command.stringValueOfParameterNamed("note");
        switch (type) {
            case CLIENT: {
                final Client client = this.clientRepository.findOne(resourceId);
                if (client == null) { throw new ClientNotFoundException(resourceId); }
                newNote = Note.clientNoteFromJson(client, command);
            }
            break;
            case DEPOSIT: {
                final DepositAccount depositAccount = this.depositAccountRepository.findOne(resourceId);
                if (depositAccount == null) { throw new DepositAccountNotFoundException(resourceId); }
                newNote = Note.depositNote(depositAccount, note);
            }
            break;
            case GROUP: {
                final Group group = this.groupRepository.findOne(resourceId);
                if (group == null) { throw new GroupNotFoundException(resourceId); }
                newNote = Note.groupNoteFromJson(group, command);
            }
            break;
            case LOAN: {
                final Loan loan = this.loanRepository.findOne(resourceId);
                if (loan == null) { throw new LoanNotFoundException(resourceId); }
                newNote = Note.loanNote(loan, note);
            }
            break;
            case LOAN_TRANSACTION: {
                final LoanTransaction loanTransaction = this.loanTransactionRepository.findOne(resourceId);
                if (loanTransaction == null) { throw new LoanTransactionNotFoundException(resourceId); }
                final Loan loan = loanTransaction.getLoan();
                newNote = Note.loanTransactionNote(loan, loanTransaction, note);
            }
            break;
            case SAVING: {
                final SavingAccount savingAccount = this.savingAccountRepository.findOne(resourceId);
                if (savingAccount == null) { throw new SavingAccountNotFoundException(resourceId); }
                newNote = Note.savingNote(savingAccount, note);
            }
            break;
        }

        return newNote;
    }
    
    private Note getNoteForUpdate(final JsonCommand command) {
        final String resourceUrl = command.getSupportedEntityType();
        final Long resourceId = command.getSupportedEntityId();
        final Long noteId = command.entityId();
        final NoteType type = NoteType.fromApiUrl(resourceUrl);
        Note noteForUpdate = null;
        switch (type) {
            case CLIENT: {
                noteForUpdate = this.noteRepository.findByClientIdAndId(resourceId, noteId);
            }
            break;
            case DEPOSIT: {
                noteForUpdate = this.noteRepository.findByDepositAccountIdAndId(resourceId, noteId);
            }
            break;
            case GROUP: {
                noteForUpdate = this.noteRepository.findByGroupIdAndId(resourceId, noteId);
            }
            break;
            case LOAN: {
                noteForUpdate = this.noteRepository.findByLoanIdAndId(resourceId, noteId);
            }
            break;
            case LOAN_TRANSACTION: {
                noteForUpdate = this.noteRepository.findByLoanTransactionIdAndId(resourceId, noteId);
            }
            break;
            case SAVING: {
                noteForUpdate = this.noteRepository.findBySavingAccountIdAndId(resourceId, noteId);
            }
            break;
        }
        if (noteForUpdate == null) { throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase()); }
        return noteForUpdate;
    }
    
}
