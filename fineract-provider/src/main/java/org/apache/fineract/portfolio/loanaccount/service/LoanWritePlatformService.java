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
import java.util.Collection;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.transaction.annotation.Transactional;

public interface LoanWritePlatformService {

    CommandProcessingResult disburseLoan(Long loanId, JsonCommand command, Boolean isAccountTransfer);

    CommandProcessingResult disburseLoan(Long loanId, JsonCommand command, Boolean isAccountTransfer, Boolean isWithoutAutoPayment);

    Map<String, Object> bulkLoanDisbursal(JsonCommand command, CollectionSheetBulkDisbursalCommand bulkDisbursalCommand,
            Boolean isAccountTransfer);

    CommandProcessingResult undoLoanDisbursal(Long loanId, JsonCommand command);

    CommandProcessingResult makeLoanRepayment(LoanTransactionType repaymentTransactionType, Long loanId, JsonCommand command,
            boolean isRecoveryRepayment);

    @Transactional
    CommandProcessingResult makeLoanRepaymentWithChargeRefundChargeType(LoanTransactionType repaymentTransactionType, Long loanId,
            JsonCommand command, boolean isRecoveryRepayment, String chargeRefundChargeType);

    Map<String, Object> makeLoanBulkRepayment(CollectionSheetBulkRepaymentCommand bulkRepaymentCommand);

    CommandProcessingResult adjustLoanTransaction(Long loanId, Long transactionId, JsonCommand command);

    CommandProcessingResult chargebackLoanTransaction(Long loanId, Long transactionId, JsonCommand command);

    CommandProcessingResult waiveInterestOnLoan(Long loanId, JsonCommand command);

    CommandProcessingResult writeOff(Long loanId, JsonCommand command);

    CommandProcessingResult closeLoan(Long loanId, JsonCommand command);

    CommandProcessingResult closeAsRescheduled(Long loanId, JsonCommand command);

    CommandProcessingResult loanReassignment(Long loanId, JsonCommand command);

    CommandProcessingResult bulkLoanReassignment(JsonCommand command);

    CommandProcessingResult removeLoanOfficer(Long loanId, JsonCommand command);

    void applyMeetingDateChanges(Calendar calendar, Collection<CalendarInstance> loanCalendarInstances,
            Boolean reschedulebasedOnMeetingDates, LocalDate presentMeetingDate, LocalDate newMeetingDate);

    LoanTransaction initiateLoanTransfer(Loan loan, LocalDate transferDate);

    LoanTransaction withdrawLoanTransfer(Loan loan, LocalDate transferDate);

    void rejectLoanTransfer(Loan loan);

    LoanTransaction acceptLoanTransfer(Loan loan, LocalDate transferDate, Office acceptedInOffice, Staff loanOfficer);

    CommandProcessingResult undoWriteOff(Long loanId);

    CommandProcessingResult updateDisbursementDateAndAmountForTranche(Long loanId, Long disbursementId, JsonCommand command);

    CommandProcessingResult recoverFromGuarantor(Long loanId);

    void applyMeetingDateChanges(Calendar calendar, Collection<CalendarInstance> loanCalendarInstances);

    CommandProcessingResult makeLoanRefund(Long loanId, JsonCommand command);

    CommandProcessingResult addAndDeleteLoanDisburseDetails(Long loanId, JsonCommand command);

    void recalculateInterest(long loanId);

    CommandProcessingResult undoLastLoanDisbursal(Long loanId, JsonCommand command);

    CommandProcessingResult forecloseLoan(Long loanId, JsonCommand command);

    CommandProcessingResult disburseGLIMLoan(Long loanId, JsonCommand command);

    CommandProcessingResult undoGLIMLoanDisbursal(Long loanId, JsonCommand command);

    CommandProcessingResult makeGLIMLoanRepayment(Long loanId, JsonCommand command);

    void updateOriginalSchedule(Loan loan);

    CommandProcessingResult creditBalanceRefund(Long loanId, JsonCommand command);

    CommandProcessingResult markLoanAsFraud(Long loanId, JsonCommand command);

    CommandProcessingResult chargeOff(JsonCommand command);

    @Transactional
    CommandProcessingResult undoChargeOff(JsonCommand command);
}
