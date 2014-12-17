/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;

public interface LoanAccountDomainService {

    LoanTransaction makeRepayment(Loan loan, CommandProcessingResultBuilder builderResult, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, String noteText, String txnExternalId,
            final boolean isRecoveryRepayment, boolean isAccountTransfer);

    LoanTransaction makeRefund(Long accountId, CommandProcessingResultBuilder builderResult, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, String noteText, String txnExternalId);

    void reverseTransfer(LoanTransaction loanTransaction);

    LoanTransaction makeChargePayment(Loan loan, Long chargeId, LocalDate transactionDate, BigDecimal transactionAmount,
            PaymentDetail paymentDetail, String noteText, String txnExternalId, Integer transactionType, Integer installmentNumber);

    LoanTransaction makeDisburseTransaction(Long loanId, LocalDate transactionDate, BigDecimal transactionAmount,
            PaymentDetail paymentDetail, String noteText, String txnExternalId);

    LocalDate getCalculatedRepaymentsStartingFromDate(LocalDate actualDisbursementDate, Loan loan, CalendarInstance calendarInstance);
    
    LoanTransaction makeRefundForActiveLoan(Long accountId, CommandProcessingResultBuilder builderResult,
			LocalDate transactionDate, BigDecimal transactionAmount,
			PaymentDetail paymentDetail, String noteText, String txnExternalId);

    /**
     * This method is to recalculate and accrue the income till the last accrued
     * date. this method is used when the schedule changes due to interest
     * recalculation
     * 
     * @param loan
     */
    void recalculateAccruals(Loan loan);

    /**
     * This method is used for building ScheduleGeneratorDTO from loan, usages
     * are as, if have loan and some of the terms are modified which may impact
     * the schedule, in that case need to recompute the schedule, so this method
     * helps to build the ScheduleGeneratorDTO from modified loan.
     * 
     * @param loan
     */

    public ScheduleGeneratorDTO buildScheduleGeneratorDTO(final Loan loan);

}
