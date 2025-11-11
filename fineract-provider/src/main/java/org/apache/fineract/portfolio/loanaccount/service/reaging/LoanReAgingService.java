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
package org.apache.fineract.portfolio.loanaccount.service.reaging;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.reaging.LoanReAgeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.reaging.LoanUndoReAgeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reaging.LoanReAgeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reaging.LoanUndoReAgeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoanReAgingApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.request.ReAgePreviewRequest;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeInterestHandlingType;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeParameter;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.service.InterestScheduleModelRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanRepaymentScheduleService;
import org.apache.fineract.portfolio.loanaccount.service.LoanScheduleService;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionService;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanaccount.service.ReprocessLoanTransactionsService;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanReAgingService {

    private final LoanAssembler loanAssembler;
    private final LoanReAgingValidator reAgingValidator;
    private final ExternalIdFactory externalIdFactory;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final NoteRepository noteRepository;
    private final LoanChargeValidator loanChargeValidator;
    private final LoanUtilService loanUtilService;
    private final LoanScheduleService loanScheduleService;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final CodeValueRepository codeValueRepository;
    private final LoanRepaymentScheduleService loanRepaymentScheduleService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository;
    private final InterestScheduleModelRepositoryWrapper modelRepository;
    private final LoanTransactionService loanTransactionService;

    public CommandProcessingResult reAge(final Long loanId, final JsonCommand command) {
        final Loan loan = loanAssembler.assembleFrom(loanId);
        reAgingValidator.validateReAge(loan, command);

        final LoanTransaction reAgeTransaction = createReAgeTransaction(loan, command);
        processReAgeTransaction(loan, reAgeTransaction, true);
        loanTransactionRepository.saveAndFlush(reAgeTransaction);
        loan.updateLoanScheduleDependentDerivedFields();

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(LoanReAgingApiConstants.localeParameterName, command.locale());
        changes.put(LoanReAgingApiConstants.dateFormatParameterName, command.dateFormat());
        persistNote(loan, command, changes);

        // delinquency recalculation will be triggered by the event in a decoupled way via a listener
        businessEventNotifierService.notifyPostBusinessEvent(new LoanReAgeBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanReAgeTransactionBusinessEvent(reAgeTransaction));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(reAgeTransaction.getId()) //
                .withEntityExternalId(reAgeTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(command.getLoanId()) //
                .with(changes).build();
    }

    @Transactional(readOnly = true)
    public LoanScheduleData previewReAge(final Long loanId, final String loanExternalId, final ReAgePreviewRequest reAgePreviewRequest) {
        final Loan loan = loanId != null ? loanAssembler.assembleFrom(loanId)
                : loanAssembler.assembleFrom(ExternalIdFactory.produce(loanExternalId), false);
        return previewReAge(loan, reAgePreviewRequest);
    }

    private LoanScheduleData previewReAge(final Loan loan, final ReAgePreviewRequest reAgePreviewRequest) {
        reAgingValidator.validateReAge(loan, reAgePreviewRequest);

        final LoanTransaction reAgeTransaction = createReAgeTransactionFromPreviewRequest(loan, reAgePreviewRequest);
        processReAgeTransaction(loan, reAgeTransaction, false);
        loan.updateLoanScheduleDependentDerivedFields();

        final CurrencyData currencyData = new CurrencyData(loan.getCurrencyCode(), null, loan.getCurrency().getDigitsAfterDecimal(),
                loan.getCurrency().getInMultiplesOf(), null, null);
        final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData = new RepaymentScheduleRelatedLoanData(
                loan.getDisbursementDate(), loan.getDisbursementDate(), currencyData, loan.getPrincipal().getAmount(),
                loan.getInArrearsTolerance().getAmount(), ZERO);
        final Collection<DisbursementData> disbursementData = loanReadPlatformService.retrieveLoanDisbursementDetails(loan.getId());
        final Collection<LoanTransactionRepaymentPeriodData> capitalizedIncomeData = loanCapitalizedIncomeBalanceRepository
                .findRepaymentPeriodDataByLoanId(loan.getId());
        final List<LoanRepaymentScheduleInstallment> sortedInstallments = loan.getRepaymentScheduleInstallments().stream()
                .sorted(Comparator.comparingInt(LoanRepaymentScheduleInstallment::getInstallmentNumber)).collect(Collectors.toList());

        return loanRepaymentScheduleService.extractLoanScheduleData(sortedInstallments, repaymentScheduleRelatedLoanData, disbursementData,
                capitalizedIncomeData, loan.isInterestRecalculationEnabled(), loan.getLoanProductRelatedDetail().getLoanScheduleType());
    }

    public CommandProcessingResult undoReAge(Long loanId, JsonCommand command) {
        Loan loan = loanAssembler.assembleFrom(loanId);
        reAgingValidator.validateUndoReAge(loan);

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(LoanReAgingApiConstants.localeParameterName, command.locale());
        changes.put(LoanReAgingApiConstants.dateFormatParameterName, command.dateFormat());

        LoanTransaction reAgeTransaction = findLatestNonReversedReAgeTransaction(loan);
        if (reAgeTransaction == null) {
            throw new LoanTransactionNotFoundException("Re-Age transaction for loan was not found");
        }
        if (loan.isProgressiveSchedule()) {
            final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null);
            loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
        }
        reverseReAgeTransaction(reAgeTransaction, command);
        loanTransactionRepository.saveAndFlush(reAgeTransaction);
        reprocessLoanTransactionsService.reprocessTransactions(loan);
        loan.updateLoanScheduleDependentDerivedFields();
        persistNote(loan, command, changes);

        // delinquency recalculation will be triggered by the event in a decoupled way via a listener
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoReAgeBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoReAgeTransactionBusinessEvent(reAgeTransaction));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(reAgeTransaction.getId()) //
                .withEntityExternalId(reAgeTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(command.getLoanId()) //
                .with(changes).build();
    }

    private void processReAgeTransaction(final Loan loan, final LoanTransaction reAgeTransaction, final boolean withPostTransactionChecks) {
        if (reAgeTransaction.getTransactionDate().isBefore(reAgeTransaction.getSubmittedOnDate())) {
            final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null);
            loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
            if (withPostTransactionChecks) {
                reprocessLoanTransactionsService.reprocessTransactions(loan, List.of(reAgeTransaction));
            } else {
                reprocessLoanTransactionsService.reprocessTransactionsWithoutChecks(loan, List.of(reAgeTransaction));
            }
        } else {
            reprocessLoanTransactionsService.processLatestTransaction(reAgeTransaction, loan);
        }
    }

    private void reverseReAgeTransaction(LoanTransaction reAgeTransaction, JsonCommand command) {
        ExternalId reversalExternalId = externalIdFactory.createFromCommand(command, LoanReAgingApiConstants.externalIdParameterName);
        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(reAgeTransaction.getLoan(), reAgeTransaction,
                "reversed");
        reAgeTransaction.reverse(reversalExternalId);
        reAgeTransaction.manuallyAdjustedOrReversed();
    }

    private LoanTransaction findLatestNonReversedReAgeTransaction(Loan loan) {
        return loan.getLoanTransactions().stream() //
                .filter(LoanTransaction::isNotReversed) //
                .filter(LoanTransaction::isReAge) //
                .max(Comparator.comparing(LoanTransaction::getTransactionDate)) //
                .orElse(null);
    }

    private LoanTransaction createReAgeTransaction(Loan loan, JsonCommand command) {
        ExternalId txExternalId = externalIdFactory.createFromCommand(command, LoanReAgingApiConstants.externalIdParameterName);

        // reaging transaction date is always the current business date
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        LocalDate startDate = command.dateValueOfParameterNamed(LoanReAgingApiConstants.startDate);
        if (transactionDate.isAfter(startDate)) {
            transactionDate = startDate;
        }
        // in case of a reaging transaction, only the outstanding principal amount until the business date is considered
        Money txPrincipal = loan.getTotalPrincipalOutstandingUntil(transactionDate);
        BigDecimal txPrincipalAmount = txPrincipal.getAmount();

        final LoanTransaction reAgeTransaction = new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.REAGE, transactionDate,
                txPrincipalAmount, txPrincipalAmount, ZERO, ZERO, ZERO, null, false, null, txExternalId);

        final LoanReAgeParameter reAgeParameter = createReAgeParameter(reAgeTransaction, command);
        reAgeTransaction.setLoanReAgeParameter(reAgeParameter);

        return reAgeTransaction;
    }

    private LoanReAgeParameter createReAgeParameter(LoanTransaction reAgeTransaction, JsonCommand command) {
        PeriodFrequencyType periodFrequencyType = command.enumValueOfParameterNamed(LoanReAgingApiConstants.frequencyType,
                PeriodFrequencyType.class);
        LocalDate startDate = command.dateValueOfParameterNamed(LoanReAgingApiConstants.startDate);
        Integer numberOfInstallments = command.integerValueOfParameterNamed(LoanReAgingApiConstants.numberOfInstallments);
        Integer periodFrequencyNumber = command.integerValueOfParameterNamed(LoanReAgingApiConstants.frequencyNumber);

        LoanReAgeInterestHandlingType reAgeInterestHandlingType = command
                .enumValueOfParameterNamed(LoanReAgingApiConstants.reAgeInterestHandlingParamName, LoanReAgeInterestHandlingType.class);
        if (reAgeInterestHandlingType == null) {
            reAgeInterestHandlingType = LoanReAgeInterestHandlingType.DEFAULT;
        }

        CodeValue reasonCodeValue = null;
        if (command.parameterExists(LoanReAgingApiConstants.reasonCodeValueIdParamName)) {
            reasonCodeValue = codeValueRepository.findByCodeNameAndId(LoanApiConstants.REAGE_REASONS,
                    command.longValueOfParameterNamed(LoanReAgingApiConstants.reasonCodeValueIdParamName));
        }

        return new LoanReAgeParameter(reAgeTransaction, periodFrequencyType, periodFrequencyNumber, startDate, numberOfInstallments,
                reAgeInterestHandlingType, reasonCodeValue);
    }

    private void persistNote(Loan loan, JsonCommand command, Map<String, Object> changes) {
        if (command.hasParameter("note")) {
            final String note = command.stringValueOfParameterNamed("note");
            final Note newNote = Note.loanNote(loan, note);
            changes.put("note", note);

            this.noteRepository.saveAndFlush(newNote);
        }
    }

    private LoanTransaction createReAgeTransactionFromPreviewRequest(final Loan loan, final ReAgePreviewRequest reAgePreviewRequest) {
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        final Locale locale = reAgePreviewRequest.getLocale() != null ? Locale.forLanguageTag(reAgePreviewRequest.getLocale())
                : Locale.getDefault();
        final LocalDate startDate = JsonParserHelper.convertFrom(reAgePreviewRequest.getStartDate(), LoanReAgingApiConstants.startDate,
                reAgePreviewRequest.getDateFormat(), locale);
        if (transactionDate.isAfter(startDate)) {
            transactionDate = startDate;
        }

        final Money txPrincipal = loan.getTotalPrincipalOutstandingUntil(transactionDate);
        final BigDecimal txPrincipalAmount = txPrincipal.getAmount();

        final LoanTransaction reAgeTransaction = new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.REAGE, transactionDate,
                txPrincipalAmount, txPrincipalAmount, ZERO, ZERO, ZERO, null, false, null, null);

        final LoanReAgeParameter reAgeParameter = createReAgeParameterFromPreviewRequest(reAgeTransaction, reAgePreviewRequest);
        reAgeTransaction.setLoanReAgeParameter(reAgeParameter);

        return reAgeTransaction;
    }

    private LoanReAgeParameter createReAgeParameterFromPreviewRequest(final LoanTransaction reAgeTransaction,
            final ReAgePreviewRequest reAgePreviewRequest) {
        final PeriodFrequencyType periodFrequencyType = PeriodFrequencyType.valueOf(reAgePreviewRequest.getFrequencyType());
        final Locale locale = reAgePreviewRequest.getLocale() != null ? Locale.forLanguageTag(reAgePreviewRequest.getLocale())
                : Locale.getDefault();
        final LocalDate startDate = JsonParserHelper.convertFrom(reAgePreviewRequest.getStartDate(), LoanReAgingApiConstants.startDate,
                reAgePreviewRequest.getDateFormat(), locale);
        final Integer numberOfInstallments = reAgePreviewRequest.getNumberOfInstallments();
        final Integer periodFrequencyNumber = reAgePreviewRequest.getFrequencyNumber();

        final LoanReAgeInterestHandlingType reAgeInterestHandlingType = LoanReAgeInterestHandlingType.DEFAULT;

        return new LoanReAgeParameter(reAgeTransaction, periodFrequencyType, periodFrequencyNumber, startDate, numberOfInstallments,
                reAgeInterestHandlingType, null);
    }

}
