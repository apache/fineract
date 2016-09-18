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
package org.apache.fineract.accounting.journalentry.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.apache.fineract.accounting.common.AccountingConstants.FINANCIAL_ACTIVITY;
import org.apache.fineract.accounting.journalentry.data.ChargePaymentDTO;
import org.apache.fineract.accounting.journalentry.data.LoanDTO;
import org.apache.fineract.accounting.journalentry.data.LoanTransactionDTO;
import org.apache.fineract.organisation.office.domain.Office;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CashBasedAccountingProcessorForLoan implements AccountingProcessorForLoan {

    private final AccountingProcessorHelper helper;

    @Autowired
    public CashBasedAccountingProcessorForLoan(final AccountingProcessorHelper accountingProcessorHelper) {
        this.helper = accountingProcessorHelper;
    }

    @Override
    public void createJournalEntriesForLoan(final LoanDTO loanDTO) {
        final GLClosure latestGLClosure = this.helper.getLatestClosureByBranch(loanDTO.getOfficeId());
        // final Office office =
        // this.helper.getOfficeById(loanDTO.getOfficeId());
        final Long loanProductId = loanDTO.getLoanProductId();
        final String currencyCode = loanDTO.getCurrencyCode();
        for (final LoanTransactionDTO loanTransactionDTO : loanDTO.getNewLoanTransactions()) {
            final Date transactionDate = loanTransactionDTO.getTransactionDate();
            final String transactionId = loanTransactionDTO.getTransactionId();
            final Office office = this.helper.getOfficeById(loanTransactionDTO.getOfficeId());
            final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
            final Long loanId = loanDTO.getLoanId();

            this.helper.checkForBranchClosures(latestGLClosure, transactionDate);

            /** Handle Disbursements and reversals of disbursements **/
            if (loanTransactionDTO.getTransactionType().isDisbursement()) {
                createJournalEntriesForDisbursements(loanDTO, loanTransactionDTO, office);
            }
            /***
             * Logic for repayments, repayments at disbursement and reversal of
             * Repayments and Repayments at disbursement
             ***/
            else if (loanTransactionDTO.getTransactionType().isRepayment()
                    || loanTransactionDTO.getTransactionType().isRepaymentAtDisbursement()
                    || loanTransactionDTO.getTransactionType().isChargePayment()) {
                createJournalEntriesForRepayments(loanDTO, loanTransactionDTO, office);
            }

            /** Logic for handling recovery payments **/
            else if (loanTransactionDTO.getTransactionType().isRecoveryRepayment()) {
                createJournalEntriesForRecoveryRepayments(loanDTO, loanTransactionDTO, office);
            }

            /** Logic for Refunds of Overpayments **/
            else if (loanTransactionDTO.getTransactionType().isRefund()) {
                createJournalEntriesForRefund(loanDTO, loanTransactionDTO, office);
            }
            /***
             * Only principal write off affects cash based accounting (interest
             * and fee write off need not be considered). Debit losses written
             * off and credit Loan Portfolio
             **/
            else if (loanTransactionDTO.getTransactionType().isWriteOff()) {
                final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
                if (principalAmount != null && !(principalAmount.compareTo(BigDecimal.ZERO) == 0)) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForLoan(office, currencyCode,
                            CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue(), CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(),
                            loanProductId, paymentTypeId, loanId, transactionId, transactionDate, principalAmount,
                            loanTransactionDTO.isReversed());

                }
            } else if (loanTransactionDTO.getTransactionType().isInitiateTransfer()
                    || loanTransactionDTO.getTransactionType().isApproveTransfer()
                    || loanTransactionDTO.getTransactionType().isWithdrawTransfer()) {
                createJournalEntriesForTransfers(loanDTO, loanTransactionDTO, office);
            }
            /** Logic for Refunds of Active Loans **/
            else if (loanTransactionDTO.getTransactionType().isRefundForActiveLoans()) {
                createJournalEntriesForRefundForActiveLoan(loanDTO, loanTransactionDTO, office);
            }
        }
    }

    /**
     * Debit loan Portfolio and credit Fund source for a Disbursement <br/>
     * 
     * All debits are turned into credits and vice versa in case of disbursement
     * reversals
     * 
     * 
     * @param loanDTO
     * @param loanTransactionDTO
     * @param office
     */
    private void createJournalEntriesForDisbursements(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final Date transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal disbursalAmount = loanTransactionDTO.getAmount();
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        if(loanTransactionDTO.isLoanToLoanTransfer()){
            this.helper.createCashBasedJournalEntriesAndReversalsForLoan(office, currencyCode,
                    CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(), FINANCIAL_ACTIVITY.ASSET_TRANSFER.getValue(), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, disbursalAmount, isReversal);
        } else if (loanTransactionDTO.isAccountTransfer()) {
            this.helper.createCashBasedJournalEntriesAndReversalsForLoan(office, currencyCode,
                    CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(), FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue(), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, disbursalAmount, isReversal);
        } else {
            this.helper.createCashBasedJournalEntriesAndReversalsForLoan(office, currencyCode,
                    CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(), CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, disbursalAmount, isReversal);
        }

    }

    /**
     * Debit loan Portfolio and credit Fund source for a Disbursement <br/>
     * 
     * All debits are turned into credits and vice versa in case of disbursement
     * reversals
     * 
     * 
     * @param loanDTO
     * @param loanTransactionDTO
     * @param office
     */
    private void createJournalEntriesForRefund(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO, final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final Date transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal refundAmount = loanTransactionDTO.getAmount();
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        if (loanTransactionDTO.isAccountTransfer()) {
            this.helper.createCashBasedJournalEntriesAndReversalsForLoan(office, currencyCode,
                    CASH_ACCOUNTS_FOR_LOAN.OVERPAYMENT.getValue(), FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue(), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, refundAmount, isReversal);
        } else {
            this.helper.createCashBasedJournalEntriesAndReversalsForLoan(office, currencyCode,
                    CASH_ACCOUNTS_FOR_LOAN.OVERPAYMENT.getValue(), CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, refundAmount, isReversal);
        }
    }

    /**
     * Create a single Debit to fund source and multiple credits if applicable
     * (loan portfolio for principal repayments, Interest on loans for interest
     * repayments, Income from fees for fees payment and Income from penalties
     * for penalty payment)
     * 
     * In case the loan transaction is a reversal, all debits are turned into
     * credits and vice versa
     */
    private void createJournalEntriesForRepayments(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO, final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final Date transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final BigDecimal overPaymentAmount = loanTransactionDTO.getOverPayment();
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        if (principalAmount != null && !(principalAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO,
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, principalAmount, isReversal);
        }

        if (interestAmount != null && !(interestAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS,
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, interestAmount, isReversal);
        }

        if (feesAmount != null && !(feesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);
            this.helper.createCreditJournalEntryOrReversalForLoanCharges(office, currencyCode,
                    CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue(), loanProductId, loanId, transactionId, transactionDate, feesAmount,
                    isReversal, loanTransactionDTO.getFeePayments());
        }

        if (penaltiesAmount != null && !(penaltiesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            this.helper.createCreditJournalEntryOrReversalForLoanCharges(office, currencyCode,
                    CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue(), loanProductId, loanId, transactionId, transactionDate,
                    penaltiesAmount, isReversal, loanTransactionDTO.getPenaltyPayments());
        }

        if (overPaymentAmount != null && !(overPaymentAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CASH_ACCOUNTS_FOR_LOAN.OVERPAYMENT, loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, overPaymentAmount, isReversal);
        }

        /*** create a single debit entry (or reversal) for the entire amount **/
        if(loanTransactionDTO.isLoanToLoanTransfer()){
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, FINANCIAL_ACTIVITY.ASSET_TRANSFER.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
        } else if (loanTransactionDTO.isAccountTransfer()) {
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
        } else {
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
        }
    }

    /**
     * Create a single Debit to fund source and a single credit to
     * "Income from Recovery"
     * 
     * In case the loan transaction is a reversal, all debits are turned into
     * credits and vice versa
     */
    private void createJournalEntriesForRecoveryRepayments(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final Date transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal amount = loanTransactionDTO.getAmount();
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        this.helper.createCashBasedJournalEntriesAndReversalsForLoan(office, currencyCode, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(),
                CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_RECOVERY.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                transactionDate, amount, isReversal);

    }

    /**
     * Credit loan Portfolio and Debit Suspense Account for a Transfer
     * Initiation. A Transfer acceptance would be treated the opposite i.e Debit
     * Loan Portfolio and Credit Suspense Account <br/>
     * 
     * All debits are turned into credits and vice versa in case of Transfer
     * Initiation disbursals
     * 
     * 
     * @param loanDTO
     * @param loanTransactionDTO
     * @param office
     */
    private void createJournalEntriesForTransfers(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO, final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final Date transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final boolean isReversal = loanTransactionDTO.isReversed();
        // final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        if (loanTransactionDTO.getTransactionType().isInitiateTransfer()) {
            this.helper.createCashBasedJournalEntriesAndReversalsForLoan(office, currencyCode,
                    CASH_ACCOUNTS_FOR_LOAN.TRANSFERS_SUSPENSE.getValue(), CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(), loanProductId,
                    null, loanId, transactionId, transactionDate, principalAmount, isReversal);
        } else if (loanTransactionDTO.getTransactionType().isApproveTransfer()
                || loanTransactionDTO.getTransactionType().isWithdrawTransfer()) {
            this.helper.createCashBasedJournalEntriesAndReversalsForLoan(office, currencyCode,
                    CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue(), CASH_ACCOUNTS_FOR_LOAN.TRANSFERS_SUSPENSE.getValue(), loanProductId,
                    null, loanId, transactionId, transactionDate, principalAmount, isReversal);
        }
    }
    
    private void createJournalEntriesForRefundForActiveLoan(LoanDTO loanDTO, LoanTransactionDTO loanTransactionDTO, Office office) {
        // TODO Auto-generated method stub
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final Date transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final BigDecimal overPaymentAmount = loanTransactionDTO.getOverPayment();
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        if (principalAmount != null && !(principalAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO,
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, principalAmount, !isReversal);
        }

        if (interestAmount != null && !(interestAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS,
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, interestAmount, !isReversal);
        }

        if (feesAmount != null && !(feesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);
            
            List<ChargePaymentDTO> chargePaymentDTOs = new ArrayList<>();
            
            for(ChargePaymentDTO chargePaymentDTO : loanTransactionDTO.getFeePayments()) {
                chargePaymentDTOs.add(new ChargePaymentDTO(chargePaymentDTO.getChargeId(), chargePaymentDTO.getLoanChargeId(), 
                        chargePaymentDTO.getAmount().floatValue() < 0 ? chargePaymentDTO.getAmount().multiply(new BigDecimal(-1)):chargePaymentDTO.getAmount() ));
            }
            this.helper.createCreditJournalEntryOrReversalForLoanCharges(office, currencyCode,
                    CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue(), loanProductId, loanId, transactionId, transactionDate, feesAmount,
                    !isReversal, chargePaymentDTOs);
        }

        if (penaltiesAmount != null && !(penaltiesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            List<ChargePaymentDTO> chargePaymentDTOs = new ArrayList<>();
            
            for(ChargePaymentDTO chargePaymentDTO : loanTransactionDTO.getPenaltyPayments()) {
                chargePaymentDTOs.add(new ChargePaymentDTO(chargePaymentDTO.getChargeId(), chargePaymentDTO.getLoanChargeId(), 
                        chargePaymentDTO.getAmount().floatValue() < 0 ? chargePaymentDTO.getAmount().multiply(new BigDecimal(-1)):chargePaymentDTO.getAmount() ));
            }
            
            this.helper.createCreditJournalEntryOrReversalForLoanCharges(office, currencyCode,
                    CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue(), loanProductId, loanId, transactionId, transactionDate,
                    penaltiesAmount, !isReversal, chargePaymentDTOs);
        }

        if (overPaymentAmount != null && !(overPaymentAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CASH_ACCOUNTS_FOR_LOAN.OVERPAYMENT, loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, overPaymentAmount, !isReversal);
        }

        /*** create a single debit entry (or reversal) for the entire amount **/
        this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(), loanProductId,
                paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, !isReversal);
     
    }
}
