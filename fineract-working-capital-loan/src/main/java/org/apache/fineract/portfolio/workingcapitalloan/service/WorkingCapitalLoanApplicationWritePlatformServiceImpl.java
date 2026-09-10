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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanApplicationModifiedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.service.LoanOriginatorLinkingService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNote;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanApplicationNotInSubmittedStateCannotBeDeletedException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanChargeNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.ProjectedAmortizationLoanModelRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanApplicationDataValidator;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanChargeConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkingCapitalLoanApplicationWritePlatformServiceImpl implements WorkingCapitalLoanApplicationWritePlatformService {

    private final WorkingCapitalLoanApplicationDataValidator validator;
    private final WorkingCapitalLoanRepository repository;
    private final WorkingCapitalLoanAssembler assembler;
    private final WorkingCapitalLoanNoteRepository noteRepository;
    private final ProjectedAmortizationLoanModelRepository projectedAmortizationLoanModelRepository;
    private final Optional<LoanOriginatorLinkingService> loanOriginatorLinkingService;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final WorkingCapitalLoanChargeAssembler chargeAssembler;
    private final WorkingCapitalLoanChargeRepository chargeRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final ExternalIdFactory externalIdFactory;

    public WorkingCapitalLoanApplicationWritePlatformServiceImpl(WorkingCapitalLoanApplicationDataValidator validator,
            WorkingCapitalLoanRepository repository, WorkingCapitalLoanAssembler assembler, WorkingCapitalLoanNoteRepository noteRepository,
            ProjectedAmortizationLoanModelRepository projectedAmortizationLoanModelRepository,
            @Qualifier("workingCapitalLoanOriginatorLinkingServiceImpl") Optional<LoanOriginatorLinkingService> loanOriginatorLinkingService,
            BusinessEventNotifierService businessEventNotifierService, WorkingCapitalLoanChargeAssembler chargeAssembler,
            WorkingCapitalLoanChargeRepository chargeRepository, FromJsonHelper fromApiJsonHelper, ExternalIdFactory externalIdFactory) {
        this.validator = validator;
        this.repository = repository;
        this.assembler = assembler;
        this.noteRepository = noteRepository;
        this.projectedAmortizationLoanModelRepository = projectedAmortizationLoanModelRepository;
        this.loanOriginatorLinkingService = loanOriginatorLinkingService;
        this.businessEventNotifierService = businessEventNotifierService;
        this.chargeAssembler = chargeAssembler;
        this.chargeRepository = chargeRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.externalIdFactory = externalIdFactory;
    }

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
            attachOriginatorsIfProvided(command, saved);
            attachChargesIfProvided(command, saved);

            this.businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanCreatedBusinessEvent(saved));

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
            final Map<String, Object> changes = new LinkedHashMap<>(this.assembler.updateFrom(command, loan));
            // Validations (further validations which require the assembled entity)
            this.validator.validateForModify(loan);
            final WorkingCapitalLoan saved = this.repository.saveAndFlush(loan);
            replaceChargesIfProvided(command, saved, changes);
            final String submittedOnNote = command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.submittedOnNoteParameterName);
            createNote(submittedOnNote, saved);

            this.businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanApplicationModifiedBusinessEvent(saved));

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
        if (loan.isNotSubmittedAndPendingApproval()) {
            throw new WorkingCapitalLoanApplicationNotInSubmittedStateCannotBeDeletedException(loanId);
        }
        final List<WorkingCapitalLoanNote> relatedNotes = this.noteRepository.findByWcLoanId(loan.getId());
        this.noteRepository.deleteAllInBatch(relatedNotes);
        projectedAmortizationLoanModelRepository.findByLoanId(loan.getId()).ifPresent(projectedAmortizationLoanModelRepository::delete);
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

    private void attachOriginatorsIfProvided(final JsonCommand command, final WorkingCapitalLoan loan) {
        if (this.loanOriginatorLinkingService.isPresent()
                && command.parameterExists(WorkingCapitalLoanConstants.originatorsParameterName)) {
            final JsonArray originatorsArray = command.arrayOfParameterNamed(WorkingCapitalLoanConstants.originatorsParameterName);
            if (originatorsArray != null && !originatorsArray.isEmpty()) {
                this.loanOriginatorLinkingService.get().processOriginatorsForLoanApplication(loan.getId(), originatorsArray);
            }
        }
    }

    /** One entry of the request's {@code charges} array, already parsed. */
    private record ChargeRequestItem(Long id, Long chargeId, BigDecimal amount, LocalDate dueDate, ExternalId externalId) {
    }

    /** @return the parsed entries, or null when the request carries no {@code charges} array at all */
    private List<ChargeRequestItem> parseChargeItems(final JsonCommand command) {
        if (!command.parameterExists(WorkingCapitalLoanConstants.chargesParameterName)) {
            return null;
        }
        final JsonArray array = command.arrayOfParameterNamed(WorkingCapitalLoanConstants.chargesParameterName);
        if (array == null) {
            return null;
        }
        final JsonObject topLevel = command.parsedJson().getAsJsonObject();
        final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevel);
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevel);

        final List<ChargeRequestItem> items = new ArrayList<>();
        for (final JsonElement element : array) {
            final JsonObject chargeElement = element.getAsJsonObject();
            final Long id = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.idParameterName, chargeElement);
            final Long chargeId = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanChargeConstants.chargeIdParamName,
                    chargeElement);
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanChargeConstants.amountParamName,
                    chargeElement, locale);
            final LocalDate dueDate = this.fromApiJsonHelper.parameterExists(WorkingCapitalLoanChargeConstants.dueDateParamName,
                    chargeElement)
                            ? this.fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanChargeConstants.dueDateParamName,
                                    chargeElement, dateFormat, locale)
                            : null;
            final ExternalId externalId = externalIdFactory.create(
                    this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanChargeConstants.externalIdParamName, chargeElement));
            items.add(new ChargeRequestItem(id, chargeId, amount, dueDate, externalId));
        }
        return items;
    }

    /**
     * Charges sent with the application. Each one goes through the same assembly and rules as {@code POST
     * /working-capital-loans/{loanId}/charges} would apply to a loan pending approval, so in practice only disbursement
     * charges pass. Nothing else happens on creation: no balance, no lifecycle, no accrual - a disbursement charge is
     * settled when the loan is disbursed.
     */
    private void attachChargesIfProvided(final JsonCommand command, final WorkingCapitalLoan loan) {
        final List<ChargeRequestItem> items = parseChargeItems(command);
        if (items == null || items.isEmpty()) {
            return;
        }
        final Set<Long> seenChargeIds = new HashSet<>();
        final List<WorkingCapitalLoanCharge> charges = new ArrayList<>();
        for (final ChargeRequestItem item : items) {
            rejectDuplicateChargeId(seenChargeIds, item.chargeId());
            charges.add(chargeAssembler.assemble(loan, item.chargeId(), item.amount(), item.dueDate(), item.externalId()));
        }
        chargeRepository.saveAllAndFlush(charges);
    }

    /**
     * Replaces the loan's active charges with the request's list: an entry with {@code id} updates that charge's
     * amount, an entry without {@code id} adds a new charge, and active charges not referenced are retired
     * ({@code active =
     * false}). The disbursement settles exactly the charges left on the loan.
     */
    private void replaceChargesIfProvided(final JsonCommand command, final WorkingCapitalLoan loan, final Map<String, Object> changes) {
        final List<ChargeRequestItem> items = parseChargeItems(command);
        if (items == null) {
            return;
        }
        final List<WorkingCapitalLoanCharge> activeCharges = chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loan.getId());
        final Map<Long, WorkingCapitalLoanCharge> activeById = activeCharges.stream()
                .collect(Collectors.toMap(WorkingCapitalLoanCharge::getId, Function.identity()));
        final Set<Long> keptIds = new HashSet<>();

        for (final ChargeRequestItem item : items) {
            if (item.id() == null) {
                continue;
            }
            final WorkingCapitalLoanCharge existing = activeById.get(item.id());
            if (existing == null) {
                throw new WorkingCapitalLoanChargeNotFoundException(item.id());
            }
            if (item.chargeId() != null && !existing.getCharge().getId().equals(item.chargeId())) {
                throw new PlatformApiDataValidationException(
                        "charge.id.does.not.match.existing.charge", "chargeId " + item.chargeId() + " does not match charge "
                                + existing.getCharge().getId() + " of loan charge " + item.id(),
                        WorkingCapitalLoanChargeConstants.chargeIdParamName);
            }
            if (item.amount() != null) {
                chargeAssembler.updateAmount(loan, existing, item.amount());
            }
            keptIds.add(item.id());
        }

        // The product catalogue is a default, not a contract: the loan carries exactly the charges the application
        // decided on, so any of them may be retired here.
        final Set<Long> seenChargeIds = new HashSet<>();
        for (final WorkingCapitalLoanCharge existing : activeCharges) {
            if (keptIds.contains(existing.getId())) {
                seenChargeIds.add(existing.getCharge().getId());
                continue;
            }
            existing.setActive(false);
        }

        final List<WorkingCapitalLoanCharge> toSave = new ArrayList<>(activeCharges);
        for (final ChargeRequestItem item : items) {
            if (item.id() != null) {
                continue;
            }
            rejectDuplicateChargeId(seenChargeIds, item.chargeId());
            toSave.add(chargeAssembler.assemble(loan, item.chargeId(), item.amount(), item.dueDate(), item.externalId()));
        }
        chargeRepository.saveAllAndFlush(toSave);
        changes.put(WorkingCapitalLoanConstants.chargesParameterName,
                command.jsonFragment(WorkingCapitalLoanConstants.chargesParameterName));
    }

    private static void rejectDuplicateChargeId(final Set<Long> seenChargeIds, final Long chargeId) {
        if (!seenChargeIds.add(chargeId)) {
            throw new PlatformApiDataValidationException("validation.msg.workingCapitalLoan.charges.duplicate.chargeId",
                    "Charge " + chargeId + " is listed more than once", WorkingCapitalLoanConstants.chargesParameterName, chargeId);
        }
    }
}
