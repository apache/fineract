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
package org.apache.fineract.portfolio.loanaccount.serialization;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.service.HolidayUtil;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.service.WorkingDaysUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class LoanDownPaymentTransactionValidator {

    private final LoanBalanceService loanBalanceService;

    public void validateRepaymentDateIsOnNonWorkingDay(final LocalDate repaymentDate, final WorkingDays workingDays,
            final boolean allowTransactionsOnNonWorkingDay) {
        if (!allowTransactionsOnNonWorkingDay && !WorkingDaysUtil.isWorkingDay(workingDays, repaymentDate)) {
            final String errorMessage = "Repayment date cannot be on a non working day";
            throw new LoanApplicationDateException("repayment.date.on.non.working.day", errorMessage, repaymentDate);
        }
    }

    public void validateRepaymentDateIsOnHoliday(final LocalDate repaymentDate, final boolean allowTransactionsOnHoliday,
            final List<Holiday> holidays) {
        if (!allowTransactionsOnHoliday && HolidayUtil.isHoliday(repaymentDate, holidays)) {
            final String errorMessage = "Repayment date cannot be on a holiday";
            throw new LoanApplicationDateException("repayment.date.on.holiday", errorMessage, repaymentDate);
        }
    }

    public void validateLoanStatusIsActiveOrFullyPaidOrOverpaid(final Loan loan) {
        if (!(loan.isOpen() || loan.isClosedObligationsMet() || loanBalanceService.isOverPaid(loan))) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final String defaultUserMessage = "Loan must be Active, Fully Paid or Overpaid";
            final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.must.be.active.fully.paid.or.overpaid",
                    defaultUserMessage);
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateRepaymentTypeAccountStatus(final Loan loan, final LoanTransaction repaymentTransaction, final LoanEvent event) {
        if (repaymentTransaction.isGoodwillCredit() || repaymentTransaction.isInterestPaymentWaiver()
                || repaymentTransaction.isMerchantIssuedRefund() || repaymentTransaction.isPayoutRefund()
                || repaymentTransaction.isChargeRefund() || repaymentTransaction.isRepayment() || repaymentTransaction.isDownPayment()
                || repaymentTransaction.isInterestRefund()) {
            validateLoanStatusIsActiveOrFullyPaidOrOverpaid(loan);
        } else {
            validateAccountStatus(loan, event);
        }
    }

    public void validateAccountStatus(final Loan loan, final LoanEvent event) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        switch (event) {
            case LOAN_APPROVED -> {
                if (!loan.isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan Account Approval is not allowed. Loan Account is not in submitted and pending approval state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.approve.account.is.not.submitted.and.pending.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_APPROVAL_UNDO -> {
                if (!loan.isApproved()) {
                    final String defaultUserMessage = "Loan Account Undo Approval is not allowed. Loan Account is not in approved state.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.undo.approval.account.is.not.approved",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_DISBURSED -> {
                if ((!(loan.isApproved() && loan.isNotDisbursed()) && !loan.getLoanProduct().isMultiDisburseLoan())
                        || (loan.getLoanProduct().isMultiDisburseLoan() && !loan.isAllTranchesNotDisbursed())) {
                    final String defaultUserMessage = "Loan Disbursal is not allowed. Loan Account is not in approved and not disbursed state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.disbursal.account.is.not.approve.not.disbursed.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_DISBURSAL_UNDO -> {
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan Undo disbursal is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.undo.disbursal.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
                if (loan.isOpen() && loan.isTopup()) {
                    final String defaultUserMessage = "Loan Undo disbursal is not allowed on Topup Loans";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.undo.disbursal.not.allowed.on.topup.loan", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_REPAYMENT_OR_WAIVER -> {
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan Repayment (or its types) or Waiver is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.repayment.or.waiver.account.is.not.active", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case WRITE_OFF_OUTSTANDING -> {
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan Written off is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.writtenoff.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case WRITE_OFF_OUTSTANDING_UNDO -> {
                if (!loan.isClosedWrittenOff()) {
                    final String defaultUserMessage = "Loan Undo Written off is not allowed. Loan Account is not Written off.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.undo.writtenoff.account.is.not.written.off", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_CHARGE_PAYMENT -> {
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Charge payment is not allowed. Loan Account is not Active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.charge.payment.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_CLOSED -> {
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Closing Loan Account is not allowed. Loan Account is not Active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.close.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_EDIT_MULTI_DISBURSE_DATE -> {
                if (loan.isClosed()) {
                    final String defaultUserMessage = "Edit disbursement is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.edit.disbursement.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_RECOVERY_PAYMENT -> {
                if (!loan.isClosedWrittenOff()) {
                    final String defaultUserMessage = "Recovery repayments may only be made on loans which are written off";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.account.is.not.written.off",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_REFUND -> {
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan Refund is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.refund.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_DISBURSAL_UNDO_LAST -> {
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan Undo last disbursal is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.undo.last.disbursal.account.is.not.active", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_FORECLOSURE -> {
                if (!loan.isOpen()) {
                    final String defaultUserMessage = "Loan foreclosure is not allowed. Loan Account is not active.";
                    final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.foreclosure.account.is.not.active",
                            defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_CREDIT_BALANCE_REFUND -> {
                if (!loan.getStatus().isOverpaid()) {
                    final String defaultUserMessage = "Loan Credit Balance Refund is not allowed. Loan Account is not Overpaid.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.credit.balance.refund.account.is.not.overpaid", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            case LOAN_CHARGE_ADJUSTMENT -> {
                if (!(loan.getStatus().isActive() || loan.getStatus().isClosedObligationsMet() || loan.getStatus().isOverpaid())) {
                    final String defaultUserMessage = "Loan Charge Adjustment is not allowed. Loan Account must be either Active, Fully repaid or Overpaid.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.charge.adjustment.account.is.not.in.valid.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            default -> {
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
