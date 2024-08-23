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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanApprovedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanRejectedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanUndoApprovalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.domain.AccountAssociationType;
import org.apache.fineract.portfolio.account.domain.AccountAssociations;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.exception.CalendarNotFoundException;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.group.exception.GroupMemberNotFoundInGSIMException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.GroupLoanIndividualMonitoringAccount;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeDeleted;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationTransitionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationValidator;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.data.GroupSavingsIndividualMonitoringAccountData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.GSIMReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class LoanApplicationWritePlatformServiceJpaRepositoryImpl implements LoanApplicationWritePlatformService {

    private final PlatformSecurityContext context;
    private final LoanApplicationTransitionValidator loanApplicationTransitionValidator;
    private final LoanApplicationValidator loanApplicationValidator;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final NoteRepository noteRepository;
    private final LoanAssembler loanAssembler;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final CalendarRepository calendarRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final AccountAssociationsRepository accountAssociationsRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final LoanUtilService loanUtilService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final GLIMAccountInfoRepository glimRepository;
    private final LoanRepository loanRepository;
    private final GSIMReadPlatformService gsimReadPlatformService;
    private final LoanLifecycleStateMachine defaultLoanLifecycleStateMachine;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;

    @Transactional
    @Override
    public CommandProcessingResult submitApplication(final JsonCommand command) {

        try {
            // Validations (prior assembling)
            this.loanApplicationValidator.validateForCreate(command);
            // Assembling loan
            final Loan loan = this.loanAssembler.assembleFrom(command);
            // Validations (further validations which requires the assembling first)
            this.loanApplicationValidator.validateForCreate(loan);
            // Need to flush to gather loan id
            this.loanRepositoryWrapper.saveAndFlush(loan);
            // Account number regeneration (need loan id...)
            this.loanAssembler.accountNumberGeneration(command, loan);
            // Save interest recalculation calendar
            if (loan.getLoanProduct().isInterestRecalculationEnabled()) {
                createAndPersistCalendarInstanceForInterestRecalculation(loan);
            }
            // Save note
            final String submittedOnNote = command.stringValueOfParameterNamed("submittedOnNote");
            createNote(submittedOnNote, loan);
            // Save calendar instance
            createCalendar(command, loan);
            // Save linked account information
            final Long savingsAccountId = command.longValueOfParameterNamed("linkAccountId");
            createSavingsAccountAssociation(savingsAccountId, loan);
            // Save related datatable entries
            if (command.parameterExists(LoanApiConstants.datatables)) {
                this.entityDatatableChecksWritePlatformService.saveDatatables(StatusEnum.CREATE.getValue(), EntityTables.LOAN.getName(),
                        loan.getId(), loan.productId(), command.arrayOfParameterNamed(LoanApiConstants.datatables));
            }
            // TODO: review whether we really need this
            loanRepositoryWrapper.flush();
            // Check mandatory datatable entries were created
            this.entityDatatableChecksWritePlatformService.runTheCheckForProduct(loan.getId(), EntityTables.LOAN.getName(),
                    StatusEnum.CREATE.getValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), loan.productId());
            // Trigger business event
            businessEventNotifierService.notifyPostBusinessEvent(new LoanCreatedBusinessEvent(loan));
            // Building response
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(loan.getId()) //
                    .withEntityExternalId(loan.getExternalId()) //
                    .withOfficeId(loan.getOfficeId()) //
                    .withClientId(loan.getClientId()) //
                    .withGroupId(loan.getGroupId()) //
                    .withLoanId(loan.getId()).withGlimId(loan.getGlimId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    private void createAndPersistCalendarInstanceForInterestRecalculation(final Loan loan) {

        LocalDate calendarStartDate = loan.getExpectedDisbursedOnLocalDate();
        Integer repeatsOnDay = null;
        final RecalculationFrequencyType recalculationFrequencyType = loan.loanInterestRecalculationDetails().getRestFrequencyType();
        Integer recalculationFrequencyNthDay = loan.loanInterestRecalculationDetails().getRestFrequencyOnDay();
        if (recalculationFrequencyNthDay == null) {
            recalculationFrequencyNthDay = loan.loanInterestRecalculationDetails().getRestFrequencyNthDay();
            repeatsOnDay = loan.loanInterestRecalculationDetails().getRestFrequencyWeekday();
        }

        Integer frequency = loan.loanInterestRecalculationDetails().getRestInterval();
        CalendarEntityType calendarEntityType = CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL;
        final String title = "loan_recalculation_detail_" + loan.loanInterestRecalculationDetails().getId();

        createCalendar(loan, calendarStartDate, recalculationFrequencyNthDay, repeatsOnDay, recalculationFrequencyType, frequency,
                calendarEntityType, title);

        if (loan.loanInterestRecalculationDetails().getInterestRecalculationCompoundingMethod().isCompoundingEnabled()) {
            LocalDate compoundingStartDate = loan.getExpectedDisbursedOnLocalDate();
            Integer compoundingRepeatsOnDay = null;
            final RecalculationFrequencyType recalculationCompoundingFrequencyType = loan.loanInterestRecalculationDetails()
                    .getCompoundingFrequencyType();
            Integer recalculationCompoundingFrequencyNthDay = loan.loanInterestRecalculationDetails().getCompoundingFrequencyOnDay();
            if (recalculationCompoundingFrequencyNthDay == null) {
                recalculationCompoundingFrequencyNthDay = loan.loanInterestRecalculationDetails().getCompoundingFrequencyNthDay();
                compoundingRepeatsOnDay = loan.loanInterestRecalculationDetails().getCompoundingFrequencyWeekday();
            }

            Integer compoundingFrequency = loan.loanInterestRecalculationDetails().getCompoundingInterval();
            CalendarEntityType compoundingCalendarEntityType = CalendarEntityType.LOAN_RECALCULATION_COMPOUNDING_DETAIL;
            final String compoundingCalendarTitle = "loan_recalculation_detail_compounding_frequency"
                    + loan.loanInterestRecalculationDetails().getId();

            createCalendar(loan, compoundingStartDate, recalculationCompoundingFrequencyNthDay, compoundingRepeatsOnDay,
                    recalculationCompoundingFrequencyType, compoundingFrequency, compoundingCalendarEntityType, compoundingCalendarTitle);
        }

    }

    private void createCalendar(final Loan loan, LocalDate calendarStartDate, Integer recalculationFrequencyNthDay,
            final Integer repeatsOnDay, final RecalculationFrequencyType recalculationFrequencyType, Integer frequency,
            CalendarEntityType calendarEntityType, final String title) {
        Integer updatedRepeatsOnDay = repeatsOnDay;
        final CalendarFrequencyType calendarFrequencyType = switch (recalculationFrequencyType) {
            case DAILY -> CalendarFrequencyType.DAILY;
            case WEEKLY -> CalendarFrequencyType.WEEKLY;
            case MONTHLY -> CalendarFrequencyType.MONTHLY;
            case SAME_AS_REPAYMENT_PERIOD -> CalendarFrequencyType.from(loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType());
            case INVALID -> CalendarFrequencyType.INVALID;
        };

        if (recalculationFrequencyType == SAME_AS_REPAYMENT_PERIOD) {
            frequency = loan.repaymentScheduleDetail().getRepayEvery();
            calendarStartDate = loan.getExpectedDisbursedOnLocalDate();
            if (updatedRepeatsOnDay == null) {
                updatedRepeatsOnDay = calendarStartDate.get(ChronoField.DAY_OF_WEEK);
            }
        }

        final Calendar calendar = Calendar.createRepeatingCalendar(title, calendarStartDate, CalendarType.COLLECTION.getValue(),
                calendarFrequencyType, frequency, updatedRepeatsOnDay, recalculationFrequencyNthDay);
        final CalendarInstance calendarInstance = CalendarInstance.from(calendar, loan.loanInterestRecalculationDetails().getId(),
                calendarEntityType.getValue());
        this.calendarInstanceRepository.save(calendarInstance);
    }

    @Transactional
    @Override
    public CommandProcessingResult modifyApplication(final Long loanId, final JsonCommand command) {

        try {
            Loan loan = retrieveLoanBy(loanId);
            // Validations (prior assembling)
            this.loanApplicationValidator.validateForModify(command, loan);
            // Assembling loan
            Map<String, Object> changes = this.loanAssembler.updateFrom(command, loan);
            // Validations (further validations which requires the assembling first)
            this.loanApplicationValidator.validateForModify(loan);
            // TODO: check whether this is needed!
            loan = loanRepository.saveAndFlush(loan);
            // Save note
            final String submittedOnNote = command.stringValueOfParameterNamed("submittedOnNote");
            createNote(submittedOnNote, loan);
            // Modify calendar instance
            final Long calendarId = command.longValueOfParameterNamed("calendarId");
            modifyCalendar(loanId, calendarId, loan, changes);
            // Save linked account information
            modifyLinkedAccount(command, changes, loan);

            // updating loan interest recalculation details throwing null
            // pointer exception after saveAndFlush
            // http://stackoverflow.com/questions/17151757/hibernate-cascade-update-gives-null-pointer/17334374#17334374
            // TODO: check whether this is needed!
            this.loanRepositoryWrapper.saveAndFlush(loan);
            // Save interest recalculation calendar
            if (loan.getLoanProductRelatedDetail().isInterestRecalculationEnabled()
                    && changes.containsKey(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME)) {
                createAndPersistCalendarInstanceForInterestRecalculation(loan);
            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(loanId) //
                    .withEntityExternalId(loan.getExternalId()) //
                    .withOfficeId(loan.getOfficeId()) //
                    .withClientId(loan.getClientId()) //
                    .withGroupId(loan.getGroupId()) //
                    .withLoanId(loan.getId()) //
                    .with(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    private void modifyLinkedAccount(JsonCommand command, Map<String, Object> changes, Loan loan) {
        final Long savingsAccountId = command.longValueOfParameterNamed(LoanApiConstants.linkAccountIdParameterName);
        final boolean linkedAccountWasProvided = command.parameterExists(LoanApiConstants.linkAccountIdParameterName);
        // Only process if something was provided
        if (linkedAccountWasProvided) {
            AccountAssociations accountAssociations = this.accountAssociationsRepository.findByLoanIdAndType(loan.getId(),
                    AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue());
            // Explicit null value was provided as linked account id, so we shall remove the association
            if (savingsAccountId == null) {
                removeLinkedAccountAssociation(accountAssociations, changes);
            } else {
                final SavingsAccount savingsAccount = this.savingsAccountRepository.findOneWithNotFoundDetection(savingsAccountId);
                // If there was no previous
                if (accountAssociations == null) {
                    createLinkedAccountAssociation(loan, savingsAccount, changes);
                    // When the previous one was linking to a different account
                } else if (!accountAssociations.linkedSavingsAccount().getId().equals(savingsAccountId)) {
                    updateLinkedAccountAssociation(accountAssociations, savingsAccount, changes);
                }
            }
        }
    }

    private void updateLinkedAccountAssociation(AccountAssociations accountAssociations, SavingsAccount savingsAccount,
            Map<String, Object> changes) {
        accountAssociations.updateLinkedSavingsAccount(savingsAccount);
        this.accountAssociationsRepository.save(accountAssociations);
        changes.put(LoanApiConstants.linkAccountIdParameterName, savingsAccount.getId());
    }

    private void removeLinkedAccountAssociation(final AccountAssociations accountAssociations, final Map<String, Object> changes) {
        if (accountAssociations != null) {
            this.accountAssociationsRepository.delete(accountAssociations);
            changes.put(LoanApiConstants.linkAccountIdParameterName, null);
        }
    }

    private void createLinkedAccountAssociation(final Loan loan, final SavingsAccount savingsAccount, final Map<String, Object> changes) {
        boolean isActive = true;
        this.accountAssociationsRepository.save(AccountAssociations.associateSavingsAccount(loan, savingsAccount,
                AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue(), isActive));
        changes.put(LoanApiConstants.linkAccountIdParameterName, savingsAccount.getId());
    }

    private void modifyCalendar(Long loanId, Long calendarId, Loan loan, Map<String, Object> changes) {
        Calendar calendar = null;
        if (calendarId != null && calendarId != 0) {
            calendar = this.calendarRepository.findById(calendarId).orElseThrow(() -> new CalendarNotFoundException(calendarId));
        }

        final List<CalendarInstance> ciList = (List<CalendarInstance>) this.calendarInstanceRepository.findByEntityIdAndEntityTypeId(loanId,
                CalendarEntityType.LOANS.getValue());
        if (calendar != null) {
            // For loans, allow to attach only one calendar instance per
            // loan
            if (ciList != null && !ciList.isEmpty()) {
                final CalendarInstance calendarInstance = ciList.get(0);
                final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(
                        calendarInstance.getEntityId(), calendarInstance.getCalendar().getId(),
                        CalendarEntityType.LOANS.getValue().longValue());
                if (isCalendarAssociatedWithEntity && calendarId == null) {
                    this.calendarRepository.delete(calendarInstance.getCalendar());
                }
                if (!calendarInstance.getCalendar().getId().equals(calendar.getId())) {
                    calendarInstance.updateCalendar(calendar);
                    this.calendarInstanceRepository.saveAndFlush(calendarInstance);
                }
            } else {
                // attaching new calendar
                final CalendarInstance calendarInstance = new CalendarInstance(calendar, loan.getId(), CalendarEntityType.LOANS.getValue());
                this.calendarInstanceRepository.save(calendarInstance);
            }

        } else {
            if (ciList != null && !ciList.isEmpty()) {
                final CalendarInstance existingCalendarInstance = ciList.get(0);
                final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(
                        existingCalendarInstance.getEntityId(), existingCalendarInstance.getCalendar().getId(),
                        CalendarEntityType.GROUPS.getValue().longValue());
                if (isCalendarAssociatedWithEntity) {
                    this.calendarInstanceRepository.delete(existingCalendarInstance);
                }
            }
            if (changes.containsKey("repaymentFrequencyNthDayType") || changes.containsKey("repaymentFrequencyDayOfWeekType")) {
                if (changes.get("repaymentFrequencyNthDayType") == null) {
                    if (ciList != null && !ciList.isEmpty()) {
                        final CalendarInstance calendarInstance = ciList.get(0);
                        final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(
                                calendarInstance.getEntityId(), calendarInstance.getCalendar().getId(),
                                CalendarEntityType.LOANS.getValue().longValue());
                        if (isCalendarAssociatedWithEntity) {
                            this.calendarInstanceRepository.delete(calendarInstance);
                            this.calendarRepository.delete(calendarInstance.getCalendar());
                        }
                    }
                } else {
                    PeriodFrequencyType repaymentFrequencyType = loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType();
                    if (repaymentFrequencyType == PeriodFrequencyType.MONTHS) {
                        final String title = "loan_schedule_" + loan.getId();
                        final Integer typeId = CalendarType.COLLECTION.getValue();
                        final CalendarFrequencyType calendarFrequencyType = CalendarFrequencyType.MONTHLY;
                        final Integer interval = loan.repaymentScheduleDetail().getRepayEvery();
                        LocalDate startDate = loan.getExpectedFirstRepaymentOnDate();
                        if (startDate == null) {
                            startDate = loan.getExpectedDisbursedOnLocalDate();
                        }
                        final Calendar newCalendar = Calendar.createRepeatingCalendar(title, startDate, typeId, calendarFrequencyType,
                                interval, (Integer) changes.get("repaymentFrequencyDayOfWeekType"),
                                (Integer) changes.get("repaymentFrequencyNthDayType"));
                        if (ciList != null && !ciList.isEmpty()) {
                            final CalendarInstance calendarInstance = ciList.get(0);
                            final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(
                                    calendarInstance.getEntityId(), calendarInstance.getCalendar().getId(),
                                    CalendarEntityType.LOANS.getValue().longValue());
                            if (isCalendarAssociatedWithEntity) {
                                final Calendar existingCalendar = calendarInstance.getCalendar();
                                if (existingCalendar != null) {
                                    String existingRecurrence = existingCalendar.getRecurrence();
                                    if (!existingRecurrence.equals(newCalendar.getRecurrence())) {
                                        existingCalendar.setRecurrence(newCalendar.getRecurrence());
                                        this.calendarRepository.save(existingCalendar);
                                    }
                                }
                            }
                        } else {
                            this.calendarRepository.save(newCalendar);
                            final Integer calendarEntityType = CalendarEntityType.LOANS.getValue();
                            final CalendarInstance calendarInstance = new CalendarInstance(newCalendar, loan.getId(), calendarEntityType);
                            this.calendarInstanceRepository.save(calendarInstance);
                        }
                    }
                }
            }
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("loan_account_no_UNIQUE")
                || (realCause.getCause() != null && realCause.getCause().getMessage().contains("loan_account_no_UNIQUE"))) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.loan.duplicate.accountNo",
                    "Loan with accountNo `" + accountNo + "` already exists", "accountNo", accountNo);
        } else if (realCause.getMessage().contains("loan_externalid_UNIQUE")
                || (realCause.getCause() != null && realCause.getCause().getMessage().contains("loan_externalid_UNIQUE"))
                || realCause.getMessage().toLowerCase().contains("external_id_unique")) {
            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.loan.duplicate.externalId",
                    "Loan with externalId `" + externalId + "` already exists", "externalId", externalId);
        }

        log.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteApplication(final Long loanId) {

        final Loan loan = retrieveLoanBy(loanId);
        loanApplicationTransitionValidator.checkClientOrGroupActive(loan);

        if (loan.isNotSubmittedAndPendingApproval()) {
            throw new LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeDeleted(loanId);
        }

        final List<Note> relatedNotes = this.noteRepository.findByLoanId(loan.getId());
        this.noteRepository.deleteAllInBatch(relatedNotes);

        final AccountAssociations accountAssociations = this.accountAssociationsRepository.findByLoanIdAndType(loanId,
                AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue());
        if (accountAssociations != null) {
            this.accountAssociationsRepository.delete(accountAssociations);
        }

        // Note: check if releaseAttachedCollaterals method can be used here
        Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();
        for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
            BigDecimal quantity = loanCollateralManagement.getQuantity();
            ClientCollateralManagement clientCollateralManagement = loanCollateralManagement.getClientCollateralManagement();
            clientCollateralManagement.updateQuantityAfterLoanClosed(quantity);
            loanCollateralManagement.setIsReleased(true);
            loanCollateralManagement.setClientCollateralManagement(clientCollateralManagement);
        }

        this.loanRepositoryWrapper.delete(loanId);

        return new CommandProcessingResultBuilder() //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loan.getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult approveGLIMLoanAppication(final Long loanId, final JsonCommand command) {

        final Long parentLoanId = loanId;
        GroupLoanIndividualMonitoringAccount parentLoan = glimRepository.findById(parentLoanId).orElseThrow();
        JsonArray approvalFormData = command.arrayOfParameterNamed("approvalFormData");

        JsonObject jsonObject = null;
        JsonCommand childCommand = null;
        Long[] childLoanId = new Long[approvalFormData.size()];
        BigDecimal parentPrincipalAmount = command.bigDecimalValueOfParameterNamed("glimPrincipal");

        for (int i = 0; i < approvalFormData.size(); i++) {

            jsonObject = approvalFormData.get(i).getAsJsonObject();

            childLoanId[i] = jsonObject.get("loanId").getAsLong();
        }

        CommandProcessingResult result = null;
        int count = 0;
        int j = 0;
        for (JsonElement approvals : approvalFormData) {

            childCommand = JsonCommand.fromExistingCommand(command, approvals);

            result = approveApplication(childLoanId[j++], childCommand);

            if (result.getLoanId() != null) {
                count++;
                // if all the child loans are approved, mark the parent loan as
                // approved
                if (count == parentLoan.getChildAccountsCount()) {
                    parentLoan.setPrincipalAmount(parentPrincipalAmount);
                    parentLoan.setLoanStatus(LoanStatus.APPROVED.getValue());
                    glimRepository.save(parentLoan);
                }

            }

        }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult approveApplication(final Long loanId, final JsonCommand command) {
        final AppUser currentUser = getAppUserIfPresent();
        loanApplicationValidator.validateApproval(command, loanId);

        Pair<Loan, Map<String, Object>> loanAndChanges = loanScheduleAssembler.assembleLoanApproval(currentUser, command, loanId);
        final Loan loan = loanAndChanges.getLeft();
        final Map<String, Object> changes = loanAndChanges.getRight();

        if (!changes.isEmpty()) {
            final String noteText = command.stringValueOfParameterNamed("note");
            createNote(noteText, loan).ifPresent(note -> changes.put("note", noteText));
            businessEventNotifierService.notifyPostBusinessEvent(new LoanApprovedBusinessEvent(loan));
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult undoGLIMLoanApplicationApproval(final Long loanId, final JsonCommand command) {

        // GroupLoanIndividualMonitoringAccount
        // glimAccount=glimRepository.findOne(loanId);
        final Long parentLoanId = loanId;
        GroupLoanIndividualMonitoringAccount parentLoan = glimRepository.findById(parentLoanId).orElseThrow();
        List<Loan> childLoans = this.loanRepository.findByGlimId(loanId);

        CommandProcessingResult result = null;
        int count = 0;
        for (Loan loan : childLoans) {
            result = undoApplicationApproval(loan.getId(), command);

            if (result.getLoanId() != null) {
                count++;
                // if all the child loans are approved, mark the parent loan as
                // approved
                if (count == parentLoan.getChildAccountsCount()) {
                    parentLoan.setLoanStatus(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue());
                    glimRepository.save(parentLoan);
                }

            }

        }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult undoApplicationApproval(final Long loanId, final JsonCommand command) {

        this.loanApplicationValidator.validateForUndo(command.json());

        Loan loan = retrieveLoanBy(loanId);
        loanApplicationTransitionValidator.checkClientOrGroupActive(loan);

        final Map<String, Object> changes = loan.undoApproval(defaultLoanLifecycleStateMachine);
        if (!changes.isEmpty()) {

            // If loan approved amount is not same as loan amount demanded, then
            // during undo, restore the demand amount to principal amount.

            if (changes.containsKey(LoanApiConstants.approvedLoanAmountParameterName)
                    || changes.containsKey(LoanApiConstants.disbursementPrincipalParameterName)) {
                LocalDate recalculateFrom = null;
                ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
                loan.regenerateRepaymentSchedule(scheduleGeneratorDTO);
                loanAccrualsProcessingService.reprocessExistingAccruals(loan);
            }

            loan.adjustNetDisbursalAmount(loan.getProposedPrincipal());

            loan = loanRepository.saveAndFlush(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            createNote(noteText, loan);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoApprovalBusinessEvent(loan));
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult rejectGLIMApplicationApproval(final Long glimId, final JsonCommand command) {

        // GroupLoanIndividualMonitoringAccount
        // glimAccount=glimRepository.findOne(loanId);
        final Long parentLoanId = glimId;
        GroupLoanIndividualMonitoringAccount parentLoan = glimRepository.findById(parentLoanId).orElseThrow();
        List<Loan> childLoans = this.loanRepository.findByGlimId(glimId);

        CommandProcessingResult result = null;
        int count = 0;
        for (Loan loan : childLoans) {
            result = rejectApplication(loan.getId(), command);

            if (result.getLoanId() != null) {
                count++;
                // if all the child loans are Rejected, mark the parent loan as
                // rejected
                if (count == parentLoan.getChildAccountsCount()) {
                    parentLoan.setLoanStatus(LoanStatus.REJECTED.getValue());
                    glimRepository.save(parentLoan);
                }

            }

        }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult rejectApplication(final Long loanId, final JsonCommand command) {

        // retrieve loan
        Loan loan = retrieveLoanBy(loanId);

        // validate loan rejection
        loanApplicationTransitionValidator.validateRejection(command, loan);

        // check for mandatory entities
        entityDatatableChecksWritePlatformService.runTheCheckForProduct(loanId, EntityTables.LOAN.getName(), StatusEnum.REJECTED.getValue(),
                EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), loan.productId());

        // loan application rejection
        final AppUser currentUser = getAppUserIfPresent();
        defaultLoanLifecycleStateMachine.transition(LoanEvent.LOAN_REJECTED, loan);
        final Map<String, Object> changes = loanAssembler.updateLoanApplicationAttributesForRejection(loan, command, currentUser);

        if (!changes.isEmpty()) {
            loanRepositoryWrapper.saveAndFlush(loan);
            final String noteText = command.stringValueOfParameterNamed("note");
            createNote(noteText, loan);
        }

        businessEventNotifierService.notifyPostBusinessEvent(new LoanRejectedBusinessEvent(loan));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult applicantWithdrawsFromApplication(final Long loanId, final JsonCommand command) {

        // retrieve loan
        final Loan loan = retrieveLoanBy(loanId);

        // validate withdrawal
        loanApplicationTransitionValidator.validateApplicantWithdrawal(command, loan);

        // check for mandatory entities
        entityDatatableChecksWritePlatformService.runTheCheckForProduct(loanId, EntityTables.LOAN.getName(),
                StatusEnum.WITHDRAWN.getValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(), loan.productId());

        // loan application withdrawal
        final AppUser currentUser = getAppUserIfPresent();
        defaultLoanLifecycleStateMachine.transition(LoanEvent.LOAN_WITHDRAWN, loan);
        final Map<String, Object> changes = loanAssembler.updateLoanApplicationAttributesForWithdrawal(loan, command, currentUser);
        // Release attached collaterals
        if (loan.getLoanType().isIndividualAccount()) {
            releaseAttachedCollaterals(loan);
        }

        if (!changes.isEmpty()) {
            loanRepositoryWrapper.saveAndFlush(loan);

            final String noteText = command.stringValueOfParameterNamed("note");
            createNote(noteText, loan);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private Loan retrieveLoanBy(final Long loanId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        loan.setHelpers(defaultLoanLifecycleStateMachine, this.loanSummaryWrapper, this.loanRepaymentScheduleTransactionProcessorFactory);
        return loan;
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

    private void createSavingsAccountAssociation(Long savingsAccountId, Loan loan) {
        if (savingsAccountId != null) {
            SavingsAccount savingsAccount;
            AccountAssociations accountAssociations;
            if (loan.getLoanType().isGLIMAccount()) {
                List<GroupSavingsIndividualMonitoringAccountData> childSavings = (List<GroupSavingsIndividualMonitoringAccountData>) gsimReadPlatformService
                        .findGSIMAccountsByGSIMId(savingsAccountId);
                List<BigDecimal> gsimClientMembers = new ArrayList<>();
                Map<BigDecimal, BigDecimal> clientAccountMappings = new HashMap<>();
                for (GroupSavingsIndividualMonitoringAccountData childSaving : childSavings) {
                    gsimClientMembers.add(childSaving.getClientId());
                    clientAccountMappings.put(childSaving.getClientId(), childSaving.getChildAccountId());

                }

                if (gsimClientMembers.contains(BigDecimal.valueOf(loan.getClientId()))) {
                    savingsAccount = this.savingsAccountRepository
                            .findOneWithNotFoundDetection(clientAccountMappings.get(BigDecimal.valueOf(loan.getClientId())).longValue());
                } else {
                    throw new GroupMemberNotFoundInGSIMException(loan.getClientId());
                }
            } else {
                savingsAccount = this.savingsAccountRepository.findOneWithNotFoundDetection(savingsAccountId);
            }

            boolean isActive = true;
            accountAssociations = AccountAssociations.associateSavingsAccount(loan, savingsAccount,
                    AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue(), isActive);
            this.accountAssociationsRepository.save(accountAssociations);
        }
    }

    private void createCalendar(JsonCommand command, Loan loan) {
        final Long calendarId = command.longValueOfParameterNamed("calendarId");
        Calendar calendar;

        if (calendarId != null && calendarId != 0) {
            calendar = this.calendarRepository.findById(calendarId).orElseThrow(() -> new CalendarNotFoundException(calendarId));
            final CalendarInstance calendarInstance = new CalendarInstance(calendar, loan.getId(), CalendarEntityType.LOANS.getValue());
            this.calendarInstanceRepository.save(calendarInstance);
        } else {
            // TODO: Would be nice to avoid recreating the loan application terms once again
            final LoanApplicationTerms loanApplicationTerms = this.loanScheduleAssembler.assembleLoanTerms(command.parsedJson());
            if (loanApplicationTerms.getRepaymentPeriodFrequencyType() == PeriodFrequencyType.MONTHS
                    && loanApplicationTerms.getNthDay() != null) {
                final String title = "loan_schedule_" + loan.getId();
                LocalDate calendarStartDate = loanApplicationTerms.getRepaymentsStartingFromLocalDate();
                if (calendarStartDate == null) {
                    calendarStartDate = loanApplicationTerms.getExpectedDisbursementDate();
                }
                final CalendarFrequencyType calendarFrequencyType = CalendarFrequencyType.MONTHLY;
                final Integer frequency = loanApplicationTerms.getRepaymentEvery();
                final Integer repeatsOnDay = loanApplicationTerms.getWeekDayType().getValue();
                final Integer repeatsOnNthDayOfMonth = loanApplicationTerms.getNthDay();
                final Integer calendarEntityType = CalendarEntityType.LOANS.getValue();
                final Calendar loanCalendar = Calendar.createRepeatingCalendar(title, calendarStartDate, CalendarType.COLLECTION.getValue(),
                        calendarFrequencyType, frequency, repeatsOnDay, repeatsOnNthDayOfMonth);
                this.calendarRepository.save(loanCalendar);
                final CalendarInstance calendarInstance = CalendarInstance.from(loanCalendar, loan.getId(), calendarEntityType);
                this.calendarInstanceRepository.save(calendarInstance);
            }
        }
    }

    private Optional<Note> createNote(String submittedOnNote, Loan newLoanApplication) {
        if (StringUtils.isNotBlank(submittedOnNote)) {
            final Note note = Note.loanNote(newLoanApplication, submittedOnNote);
            this.noteRepository.save(note);
            return Optional.of(note);
        } else {
            return Optional.empty();
        }
    }

    private void releaseAttachedCollaterals(Loan loan) {
        Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();
        for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
            ClientCollateralManagement clientCollateralManagement = loanCollateralManagement.getClientCollateralManagement();
            clientCollateralManagement.updateQuantity(clientCollateralManagement.getQuantity().add(loanCollateralManagement.getQuantity()));
            loanCollateralManagement.setClientCollateralManagement(clientCollateralManagement);
            loanCollateralManagement.setIsReleased(true);
        }
        loan.updateLoanCollateral(loanCollateralManagements);
    }

}
