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

import java.util.Collection;
import java.util.Map;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.joda.time.LocalDate;

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

    LoanTransaction initiateLoanTransfer(Loan loan, LocalDate transferDate);

    LoanTransaction withdrawLoanTransfer(final Loan loan, LocalDate transferDate);

    void rejectLoanTransfer(final Loan loan);

    LoanTransaction acceptLoanTransfer(Loan loan, LocalDate transferDate, Office acceptedInOffice, Staff loanOfficer);

    CommandProcessingResult payLoanCharge(Long loanId, Long loanChargeId, JsonCommand command, boolean isChargeIdIncludedInJson);

    void transferFeeCharges() throws JobExecutionException;

    CommandProcessingResult undoWriteOff(Long loanId);

    CommandProcessingResult updateDisbursementDateAndAmountForTranche(Long loanId, Long disbursementId, JsonCommand command);

    CommandProcessingResult recoverFromGuarantor(Long loanId);

    void applyMeetingDateChanges(Calendar calendar, Collection<CalendarInstance> loanCalendarInstances);

    CommandProcessingResult makeLoanRefund(Long loanId, JsonCommand command);

    CommandProcessingResult addAndDeleteLoanDisburseDetails(Long loanId, JsonCommand command);

    void applyOverdueChargesForLoan(Long loanId, Collection<OverdueLoanScheduleData> overdueLoanScheduleDatas);

    void recalculateInterest(long loanId);

    CommandProcessingResult undoLastLoanDisbursal(Long loanId, JsonCommand command);

    CommandProcessingResult forecloseLoan(final Long loanId, JsonCommand command);

}