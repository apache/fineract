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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNote;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanApplicationNotInSubmittedStateCannotBeDeletedException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanApplicationDataValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanApplicationWritePlatformServiceImpl implements WorkingCapitalLoanApplicationWritePlatformService {

    private final WorkingCapitalLoanApplicationDataValidator validator;
    private final WorkingCapitalLoanRepository repository;
    private final WorkingCapitalLoanAssembler assembler;
    private final WorkingCapitalLoanNoteRepository noteRepository;

    @Transactional
    @Override
    public CommandProcessingResult submitApplication(final JsonCommand command) {
        try {
            this.validator.validateForCreate(command);
            final WorkingCapitalLoan loan = this.assembler.assembleFrom(command);
            final WorkingCapitalLoan saved = this.repository.saveAndFlush(loan);
            this.assembler.accountNumberGeneration(command, saved);
            this.repository.saveAndFlush(saved);
            final String submittedOnNote = command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.submittedOnNoteParameterName);
            createNote(submittedOnNote, saved);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(saved.getId()) //
                    .withEntityExternalId(saved.getExternalId()) //
                    .withOfficeId(saved.getOfficeId()) //
                    .withClientId(saved.getClientId()) //
                    .withLoanId(saved.getId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            this.validator.handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            this.validator.handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult modifyApplication(final Long loanId, final JsonCommand command) {
        try {
            final WorkingCapitalLoan loan = retrieveLoanBy(loanId);
            // Validations (prior assembling)
            this.validator.validateForUpdate(command, loan);
            // Assembling
            final Map<String, Object> changes = this.assembler.updateFrom(command, loan);
            // Validations (further validations which require the assembled entity)
            this.validator.validateForModify(loan);
            final WorkingCapitalLoan saved = this.repository.saveAndFlush(loan);
            final String submittedOnNote = command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.submittedOnNoteParameterName);
            createNote(submittedOnNote, saved);

            return new CommandProcessingResultBuilder() //
                    .withEntityId(loanId) //
                    .withEntityExternalId(saved.getExternalId()) //
                    .withOfficeId(saved.getOfficeId()) //
                    .withClientId(saved.getClientId()) //
                    .withLoanId(saved.getId()) //
                    .with(changes) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            this.validator.handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            this.validator.handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteApplication(final Long loanId) {
        final WorkingCapitalLoan loan = retrieveLoanBy(loanId);
        if (loan.getLoanStatus() != LoanStatus.SUBMITTED_AND_PENDING_APPROVAL) {
            throw new WorkingCapitalLoanApplicationNotInSubmittedStateCannotBeDeletedException(loanId);
        }
        final List<WorkingCapitalLoanNote> relatedNotes = this.noteRepository.findByWcLoanId(loan.getId());
        this.noteRepository.deleteAllInBatch(relatedNotes);
        this.repository.delete(loan);

        return new CommandProcessingResultBuilder() //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loan.getId()) //
                .build();
    }

    private WorkingCapitalLoan retrieveLoanBy(final Long loanId) {
        return this.repository.findByIdWithFullDetails(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
    }

    private void createNote(final String submittedOnNote, final WorkingCapitalLoan loan) {
        if (StringUtils.isNotBlank(submittedOnNote)) {
            final WorkingCapitalLoanNote note = WorkingCapitalLoanNote.create(loan, submittedOnNote);
            this.noteRepository.save(note);
        }
    }
}
