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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.apache.fineract.portfolio.collectionsheet.command.SingleRepaymentCommand;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

    private final LoanTransactionValidator loanTransactionValidator;
    private final LoanAccountDomainService loanAccountDomainService;
    private final LoanAssembler loanAssembler;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final HolidayRepositoryWrapper holidayRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final LoanTransactionWritePlatformService loanTransactionWritePlatformService;
    private final LoanDisbursementWritePlatformService loanDisbursementWritePlatformService;
    private final LoanClosureWritePlatformService loanClosureWritePlatformService;
    private final LoanTransferWritePlatformService loanTransferWritePlatformService;
    private final LoanScheduleMaintenanceWritePlatformService loanScheduleMaintenanceWritePlatformService;

    @Transactional
    @Override
    public CommandProcessingResult disburseGLIMLoan(Long loanId, JsonCommand command) {
        return loanDisbursementWritePlatformService.disburseGLIMLoan(loanId, command);
    }

    @Override
    public CommandProcessingResult disburseLoan(Long loanId, JsonCommand command, Boolean isAccountTransfer) {
        return loanDisbursementWritePlatformService.disburseLoan(loanId, command, isAccountTransfer);
    }

    @Transactional
    @Override
    public CommandProcessingResult disburseLoan(Long loanId, JsonCommand command, Boolean isAccountTransfer, Boolean isWithoutAutoPayment) {
        return loanDisbursementWritePlatformService.disburseLoan(loanId, command, isAccountTransfer, isWithoutAutoPayment);
    }

    /****
     * TODO Vishwas: Pair with Ashok and re-factor collection sheet code-base
     *
     * May of the changes made to disburseLoan aren't being made here, should refactor to reuse disburseLoan ASAP
     *****/
    @Transactional
    @Override
    public Map<String, Object> bulkLoanDisbursal(JsonCommand command, CollectionSheetBulkDisbursalCommand bulkDisbursalCommand,
            Boolean isAccountTransfer) {
        return loanDisbursementWritePlatformService.bulkLoanDisbursal(command, bulkDisbursalCommand, isAccountTransfer);
    }

    @Transactional
    @Override
    public CommandProcessingResult undoGLIMLoanDisbursal(Long loanId, JsonCommand command) {
        return loanDisbursementWritePlatformService.undoGLIMLoanDisbursal(loanId, command);
    }

    @Transactional
    @Override
    public CommandProcessingResult undoLoanDisbursal(Long loanId, JsonCommand command) {
        return loanDisbursementWritePlatformService.undoLoanDisbursal(loanId, command);
    }

    @Transactional
    @Override
    public CommandProcessingResult makeGLIMLoanRepayment(final Long loanId, final JsonCommand command) {
        return this.loanTransactionWritePlatformService.makeGLIMLoanRepayment(loanId, command);
    }

    @Transactional
    @Override
    public CommandProcessingResult makeLoanRepayment(final LoanTransactionType repaymentTransactionType, final Long loanId,
            final JsonCommand command, final boolean isRecoveryRepayment) {
        return this.loanTransactionWritePlatformService.makeLoanRepayment(repaymentTransactionType, loanId, command, isRecoveryRepayment);
    }

    @Transactional
    @Override
    public CommandProcessingResult makeLoanRepaymentWithChargeRefundChargeType(final LoanTransactionType repaymentTransactionType,
            final Long loanId, final JsonCommand command, final boolean isRecoveryRepayment, final String chargeRefundChargeType) {
        return this.loanTransactionWritePlatformService.makeLoanRepaymentWithChargeRefundChargeType(repaymentTransactionType, loanId,
                command, isRecoveryRepayment, chargeRefundChargeType);
    }

    @Transactional
    @Override
    public CommandProcessingResult makeInterestPaymentWaiver(final JsonCommand command) {
        return this.loanTransactionWritePlatformService.makeInterestPaymentWaiver(command);
    }

    @Transactional
    @Override
    public Map<String, Object> makeLoanBulkRepayment(final CollectionSheetBulkRepaymentCommand bulkRepaymentCommand) {

        final SingleRepaymentCommand[] repaymentCommand = bulkRepaymentCommand.getLoanTransactions();
        final Map<String, Object> changes = new LinkedHashMap<>();
        final boolean isRecoveryRepayment = false;

        if (repaymentCommand == null) {
            return changes;
        }
        List<Long> transactionIds = new ArrayList<>();
        boolean isAccountTransfer = false;
        HolidayDetailDTO holidayDetailDTO = null;
        boolean isHolidayValidationDone = false;
        final boolean allowTransactionsOnHoliday = this.configurationDomainService.allowTransactionsOnHolidayEnabled();
        for (final SingleRepaymentCommand singleLoanRepaymentCommand : repaymentCommand) {
            if (singleLoanRepaymentCommand != null) {
                Loan loan = this.loanAssembler.assembleFrom(singleLoanRepaymentCommand.getLoanId());
                final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                        singleLoanRepaymentCommand.getTransactionDate());
                final WorkingDays workingDays = this.workingDaysRepository.findOne();
                final boolean allowTransactionsOnNonWorkingDay = this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
                boolean isHolidayEnabled;
                isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
                holidayDetailDTO = new HolidayDetailDTO(isHolidayEnabled, holidays, workingDays, allowTransactionsOnHoliday,
                        allowTransactionsOnNonWorkingDay);
                loanTransactionValidator.validateRepaymentDateIsOnHoliday(singleLoanRepaymentCommand.getTransactionDate(),
                        holidayDetailDTO.isAllowTransactionsOnHoliday(), holidayDetailDTO.getHolidays());
                loanTransactionValidator.validateRepaymentDateIsOnNonWorkingDay(singleLoanRepaymentCommand.getTransactionDate(),
                        holidayDetailDTO.getWorkingDays(), holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());
                isHolidayValidationDone = true;
                break;
            }

        }
        for (final SingleRepaymentCommand singleLoanRepaymentCommand : repaymentCommand) {
            if (singleLoanRepaymentCommand != null) {
                final Loan loan = this.loanAssembler.assembleFromInUserScope(singleLoanRepaymentCommand.getLoanId());
                final PaymentDetail paymentDetail = singleLoanRepaymentCommand.getPaymentDetail();
                ExternalId externalId = singleLoanRepaymentCommand.getExternalId();
                if (externalId.isEmpty() && configurationDomainService.isExternalIdAutoGenerationEnabled()) {
                    externalId = ExternalId.generate();
                }
                if (paymentDetail != null && paymentDetail.getId() == null) {
                    this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
                }
                final String chargeRefundChargeType = null;
                LoanTransaction loanTransaction = this.loanAccountDomainService.makeRepayment(LoanTransactionType.REPAYMENT, loan,
                        bulkRepaymentCommand.getTransactionDate(), singleLoanRepaymentCommand.getTransactionAmount(), paymentDetail,
                        bulkRepaymentCommand.getNote(), externalId, isRecoveryRepayment, chargeRefundChargeType, isAccountTransfer,
                        holidayDetailDTO, isHolidayValidationDone);
                transactionIds.add(loanTransaction.getId());
            }
        }
        changes.put("loanTransactions", transactionIds);
        return changes;
    }

    @Transactional
    @Override
    public CommandProcessingResult adjustLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {
        return this.loanTransactionWritePlatformService.adjustLoanTransaction(loanId, transactionId, command);
    }

    @Transactional
    @Override
    public CommandProcessingResult chargebackLoanTransaction(final Long loanId, final Long transactionId, final JsonCommand command) {
        return this.loanTransactionWritePlatformService.chargebackLoanTransaction(loanId, transactionId, command);
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveInterestOnLoan(final Long loanId, final JsonCommand command) {
        return this.loanTransactionWritePlatformService.waiveInterestOnLoan(loanId, command);
    }

    @Override
    public CommandProcessingResult writeOff(Long loanId, JsonCommand command) {
        return loanClosureWritePlatformService.writeOff(loanId, command);
    }

    @Override
    public CommandProcessingResult closeLoan(Long loanId, JsonCommand command) {
        return loanClosureWritePlatformService.closeLoan(loanId, command);
    }

    @Override
    public CommandProcessingResult closeAsRescheduled(Long loanId, JsonCommand command) {
        return loanClosureWritePlatformService.closeAsRescheduled(loanId, command);
    }

    @Override
    public CommandProcessingResult undoWriteOff(Long loanId) {
        return loanClosureWritePlatformService.undoWriteOff(loanId);
    }

    @Override
    public CommandProcessingResult forecloseLoan(Long loanId, JsonCommand command) {
        return loanClosureWritePlatformService.forecloseLoan(loanId, command);
    }

    @Override
    public CommandProcessingResult markLoanAsFraud(Long loanId, JsonCommand command) {
        return loanClosureWritePlatformService.markLoanAsFraud(loanId, command);
    }

    @Override
    public CommandProcessingResult chargeOff(JsonCommand command) {
        return loanClosureWritePlatformService.chargeOff(command);
    }

    @Override
    public CommandProcessingResult undoChargeOff(JsonCommand command) {
        return loanClosureWritePlatformService.undoChargeOff(command);
    }

    @Override
    public CommandProcessingResult loanReassignment(Long loanId, JsonCommand command) {
        return loanTransferWritePlatformService.loanReassignment(loanId, command);
    }

    @Override
    public CommandProcessingResult bulkLoanReassignment(JsonCommand command) {
        return loanTransferWritePlatformService.bulkLoanReassignment(command);
    }

    @Override
    public CommandProcessingResult removeLoanOfficer(Long loanId, JsonCommand command) {
        return loanTransferWritePlatformService.removeLoanOfficer(loanId, command);
    }

    @Override
    public LoanTransaction initiateLoanTransfer(Loan loan, LocalDate transferDate) {
        return loanTransferWritePlatformService.initiateLoanTransfer(loan, transferDate);
    }

    @Override
    public LoanTransaction withdrawLoanTransfer(Loan loan, LocalDate transferDate) {
        return loanTransferWritePlatformService.withdrawLoanTransfer(loan, transferDate);
    }

    @Override
    public void rejectLoanTransfer(Loan loan) {
        loanTransferWritePlatformService.rejectLoanTransfer(loan);
    }

    @Override
    public LoanTransaction acceptLoanTransfer(Loan loan, LocalDate transferDate, Office acceptedInOffice, Staff loanOfficer) {
        return loanTransferWritePlatformService.acceptLoanTransfer(loan, transferDate, acceptedInOffice, loanOfficer);
    }

    @Override
    public void applyMeetingDateChanges(Calendar calendar, Collection<CalendarInstance> loanCalendarInstances,
            Boolean reschedulebasedOnMeetingDates, LocalDate presentMeetingDate, LocalDate newMeetingDate) {
        loanScheduleMaintenanceWritePlatformService.applyMeetingDateChanges(calendar, loanCalendarInstances, reschedulebasedOnMeetingDates,
                presentMeetingDate, newMeetingDate);
    }

    @Override
    public Loan recalculateInterest(Loan loan) {
        return loanScheduleMaintenanceWritePlatformService.recalculateInterest(loan);
    }

    @Override
    public void applyMeetingDateChanges(Calendar calendar, Collection<CalendarInstance> loanCalendarInstances) {
        loanScheduleMaintenanceWritePlatformService.applyMeetingDateChanges(calendar, loanCalendarInstances);
    }

    @Override
    public void recalculateInterest(long loanId) {
        loanScheduleMaintenanceWritePlatformService.recalculateInterest(loanId);
    }

    @Override
    public void updateOriginalSchedule(Loan loan) {
        loanScheduleMaintenanceWritePlatformService.updateOriginalSchedule(loan);
    }

    @Override
    @Transactional
    public CommandProcessingResult addAndDeleteLoanDisburseDetails(Long loanId, JsonCommand command) {
        return loanDisbursementWritePlatformService.addAndDeleteLoanDisburseDetails(loanId, command);
    }

    @Override
    @Transactional
    public CommandProcessingResult updateDisbursementDateAndAmountForTranche(Long loanId, Long disbursementId, JsonCommand command) {
        return loanDisbursementWritePlatformService.updateDisbursementDateAndAmountForTranche(loanId, disbursementId, command);
    }

    @Override
    public CommandProcessingResult recoverFromGuarantor(final Long loanId) {
        return this.loanTransactionWritePlatformService.recoverFromGuarantor(loanId);
    }

    @Override
    public CommandProcessingResult creditBalanceRefund(Long loanId, JsonCommand command) {
        return this.loanTransactionWritePlatformService.creditBalanceRefund(loanId, command);
    }

    @Override
    @Transactional
    public CommandProcessingResult makeLoanRefund(Long loanId, JsonCommand command) {
        return this.loanTransactionWritePlatformService.makeLoanRefund(loanId, command);
    }

    @Override
    @Transactional
    public CommandProcessingResult undoLastLoanDisbursal(Long loanId, JsonCommand command) {
        return loanDisbursementWritePlatformService.undoLastLoanDisbursal(loanId, command);
    }

    @Override
    public CommandProcessingResult makeRefund(final Long loanId, final LoanTransactionType loanTransactionType, final JsonCommand command) {
        return this.loanTransactionWritePlatformService.makeRefund(loanId, loanTransactionType, command);
    }

    @Transactional
    @Override
    public CommandProcessingResult makeManualInterestRefund(final Long loanId, final Long transactionId, final JsonCommand command) {
        return this.loanTransactionWritePlatformService.makeManualInterestRefund(loanId, transactionId, command);
    }

}
