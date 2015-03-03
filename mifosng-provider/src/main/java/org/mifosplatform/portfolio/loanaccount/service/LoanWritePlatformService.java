/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import java.util.Collection;
import java.util.Map;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;

public interface LoanWritePlatformService {

    CommandProcessingResult disburseLoan(Long loanId, JsonCommand command, Boolean isAccountTransfer);

    Map<String, Object> bulkLoanDisbursal(JsonCommand command, CollectionSheetBulkDisbursalCommand bulkDisbursalCommand,
            Boolean isAccountTransfer);

    CommandProcessingResult undoLoanDisbursal(Long loanId, JsonCommand command);

    CommandProcessingResult makeLoanRepayment(Long loanId, JsonCommand command, boolean isRecoveryRepayment);

    Map<String, Object> makeLoanBulkRepayment(CollectionSheetBulkRepaymentCommand bulkRepaymentCommand);

    CommandProcessingResult adjustLoanTransaction(Long loanId, Long transactionId, JsonCommand command);

    CommandProcessingResult waiveInterestOnLoan(Long loanId, JsonCommand command);

    CommandProcessingResult writeOff(Long loanId, JsonCommand command);

    CommandProcessingResult closeLoan(Long loanId, JsonCommand command);

    CommandProcessingResult closeAsRescheduled(Long loanId, JsonCommand command);

    CommandProcessingResult addLoanCharge(Long loanId, JsonCommand command);

    CommandProcessingResult updateLoanCharge(Long loanId, Long loanChargeId, JsonCommand command);

    CommandProcessingResult deleteLoanCharge(Long loanId, Long loanChargeId, JsonCommand command);

    CommandProcessingResult waiveLoanCharge(Long loanId, Long loanChargeId, JsonCommand command);

    CommandProcessingResult loanReassignment(Long loanId, JsonCommand command);

    CommandProcessingResult bulkLoanReassignment(JsonCommand command);

    CommandProcessingResult removeLoanOfficer(Long loanId, JsonCommand command);

    void applyMeetingDateChanges(Calendar calendar, Collection<CalendarInstance> loanCalendarInstances,
            Boolean reschedulebasedOnMeetingDates, LocalDate presentMeetingDate, LocalDate newMeetingDate);

    void applyHolidaysToLoans();

    LoanTransaction initiateLoanTransfer(Long accountId, LocalDate transferDate);

    LoanTransaction withdrawLoanTransfer(Long accountId, LocalDate transferDate);

    void rejectLoanTransfer(Long accountId);

    LoanTransaction acceptLoanTransfer(Long accountId, LocalDate transferDate, Office acceptedInOffice, Staff loanOfficer);

    CommandProcessingResult payLoanCharge(Long loanId, Long loanChargeId, JsonCommand command, boolean isChargeIdIncludedInJson);

    void transferFeeCharges() throws JobExecutionException;

    void applyChargeForOverdueLoans() throws JobExecutionException;

    CommandProcessingResult undoWriteOff(Long loanId);

    CommandProcessingResult updateDisbursementDateAndAmountForTranche(Long loanId, Long disbursementId, JsonCommand command);

    void recalculateInterest();

    CommandProcessingResult recoverFromGuarantor(Long loanId);

    void applyMeetingDateChanges(Calendar calendar, Collection<CalendarInstance> loanCalendarInstances);

    CommandProcessingResult makeLoanRefund(Long loanId, JsonCommand command);

	CommandProcessingResult addAndDeleteLoanDisburseDetails(Long loanId, JsonCommand command);

}