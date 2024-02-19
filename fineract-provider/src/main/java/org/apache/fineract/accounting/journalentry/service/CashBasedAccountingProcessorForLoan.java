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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.data.ChargePaymentDTO;
import org.apache.fineract.accounting.journalentry.data.LoanDTO;
import org.apache.fineract.accounting.journalentry.data.LoanTransactionDTO;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.office.domain.Office;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CashBasedAccountingProcessorForLoan implements AccountingProcessorForLoan {

    private final AccountingProcessorHelper helper;

    @Override
    public void createJournalEntriesForLoan(final LoanDTO loanDTO) {
        final GLClosure latestGLClosure = this.helper.getLatestClosureByBranch(loanDTO.getOfficeId());
        final Long loanProductId = loanDTO.getLoanProductId();
        final String currencyCode = loanDTO.getCurrencyCode();
        for (final LoanTransactionDTO loanTransactionDTO : loanDTO.getNewLoanTransactions()) {
            final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
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
             * Logic for repayments, repayments at disbursement and reversal of Repayments and Repayments at
             * disbursement (except charge adjustment)
             ***/
            else if ((loanTransactionDTO.getTransactionType().isRepaymentType()
                    && !loanTransactionDTO.getTransactionType().isChargeAdjustment())
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

            /** Logic for Credit Balance Refunds **/
            else if (loanTransactionDTO.getTransactionType().isCreditBalanceRefund()) {
                createJournalEntriesForCreditBalanceRefund(loanDTO, loanTransactionDTO, office);
            }

            /***
             * Only principal write off affects cash based accounting (interest and fee write off need not be
             * considered). Debit losses written off and credit Loan Portfolio
             **/
            else if (loanTransactionDTO.getTransactionType().isWriteOff()) {
                final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
                if (principalAmount != null && principalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode,
                            CashAccountsForLoan.LOSSES_WRITTEN_OFF.getValue(), CashAccountsForLoan.LOAN_PORTFOLIO.getValue(), loanProductId,
                            paymentTypeId, loanId, transactionId, transactionDate, principalAmount, loanTransactionDTO.isReversed());

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
            // Logic for Chargebacks
            else if (loanTransactionDTO.getTransactionType().isChargeback()) {
                createJournalEntriesForChargeback(loanDTO, loanTransactionDTO, office);
            }
            // Logic for Charge Adjustment
            else if (loanTransactionDTO.getTransactionType().isChargeAdjustment()) {
                createJournalEntriesForChargeAdjustment(loanDTO, loanTransactionDTO, office);
            }
            // Logic for Charge-Off
            else if (loanTransactionDTO.getTransactionType().isChargeoff()) {
                createJournalEntriesForChargeOff(loanDTO, loanTransactionDTO, office);
            }
        }
    }

    private void createJournalEntriesForChargeOff(LoanDTO loanDTO, LoanTransactionDTO loanTransactionDTO, Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();
        final boolean isMarkedFraud = loanDTO.isMarkedAsFraud();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        final boolean isReversal = loanTransactionDTO.isReversed();

        Map<GLAccount, BigDecimal> accountMapForCredit = new LinkedHashMap<>();

        Map<Integer, BigDecimal> accountMapForDebit = new LinkedHashMap<>();

        // principal payment
        if (principalAmount != null && principalAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (isMarkedFraud) {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, CashAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                        CashAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), accountMapForCredit, accountMapForDebit);
            } else {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, CashAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                        CashAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), accountMapForCredit, accountMapForDebit);
            }
        }

        // interest payment
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, CashAccountsForLoan.INTEREST_ON_LOANS.getValue(),
                    CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), accountMapForCredit, accountMapForDebit);
        }

        // handle fees payment
        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {
            populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_FEES.getValue(),
                    CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), accountMapForCredit, accountMapForDebit);
        }

        // handle penalties payment
        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {
            populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue(),
                    CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), accountMapForCredit, accountMapForDebit);
        }

        // create credit entries
        for (Map.Entry<GLAccount, BigDecimal> creditEntry : accountMapForCredit.entrySet()) {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                    creditEntry.getValue(), isReversal, creditEntry.getKey());
        }

        // create debit entries
        for (Map.Entry<Integer, BigDecimal> debitEntry : accountMapForDebit.entrySet()) {
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, debitEntry.getKey().intValue(), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, debitEntry.getValue(), isReversal);
        }

    }

    private void populateCreditDebitMaps(Long loanProductId, BigDecimal transactionPartAmount, Long paymentTypeId,
            Integer creditAccountType, Integer debitAccountType, Map<GLAccount, BigDecimal> accountMapForCredit,
            Map<Integer, BigDecimal> accountMapForDebit) {
        GLAccount accountCredit = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, creditAccountType, paymentTypeId);
        if (accountMapForCredit.containsKey(accountCredit)) {
            BigDecimal amount = accountMapForCredit.get(accountCredit).add(transactionPartAmount);
            accountMapForCredit.put(accountCredit, amount);
        } else {
            accountMapForCredit.put(accountCredit, transactionPartAmount);
        }
        Integer accountDebit = returnExistingDebitAccountInMapMatchingGLAccount(loanProductId, paymentTypeId, debitAccountType,
                accountMapForDebit);
        if (accountMapForDebit.containsKey(accountDebit)) {
            BigDecimal amount = accountMapForDebit.get(accountDebit).add(transactionPartAmount);
            accountMapForDebit.put(accountDebit, amount);
        } else {
            accountMapForDebit.put(accountDebit, transactionPartAmount);
        }
    }

    private Integer returnExistingDebitAccountInMapMatchingGLAccount(Long loanProductId, Long paymentTypeId, Integer accountType,
            Map<Integer, BigDecimal> accountMap) {
        GLAccount glAccount = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, accountType, paymentTypeId);
        Integer accountEntry = accountMap.entrySet().stream().filter(account -> this.helper
                .getLinkedGLAccountForLoanProduct(loanProductId, account.getKey(), paymentTypeId).getGlCode().equals(glAccount.getGlCode()))
                .map(Map.Entry::getKey).findFirst().orElse(accountType);
        return accountEntry;
    }

    private void createJournalEntriesForChargeAdjustment(LoanDTO loanDTO, LoanTransactionDTO loanTransactionDTO, Office office) {
        final boolean isMarkedAsChargeOff = loanDTO.isMarkedAsChargeOff();
        if (isMarkedAsChargeOff) {
            createJournalEntriesForChargeOffLoanChargeAdjustment(loanDTO, loanTransactionDTO, office);
        } else {
            createJournalEntriesForLoanChargeAdjustment(loanDTO, loanTransactionDTO, office);
        }

    }

    private void createJournalEntriesForChargeOffLoanChargeAdjustment(LoanDTO loanDTO, LoanTransactionDTO loanTransactionDTO,
            Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final BigDecimal overPaymentAmount = loanTransactionDTO.getOverPayment();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        final boolean isReversal = loanTransactionDTO.isReversed();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        Map<GLAccount, BigDecimal> accountMap = new LinkedHashMap<>();

        // handle principal payment (and reversals)
        if (principalAmount != null && principalAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), paymentTypeId);
            accountMap.put(account, principalAmount);
        }

        // handle interest payment (and reversals)
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(interestAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, interestAmount);
            }
        }

        // handle fees payment (and reversals)
        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(feesAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, feesAmount);
            }
        }

        // handle penalties payment
        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(penaltiesAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, penaltiesAmount);
            }
        }

        // handle overpayment
        if (overPaymentAmount != null && overPaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, CashAccountsForLoan.OVERPAYMENT.getValue(),
                    paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(overPaymentAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, overPaymentAmount);
            }
        }

        for (Map.Entry<GLAccount, BigDecimal> entry : accountMap.entrySet()) {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                    entry.getValue(), isReversal, entry.getKey());
        }

        if (totalDebitAmount.compareTo(BigDecimal.ZERO) > 0) {
            Long chargeId = loanTransactionDTO.getLoanChargeData().getChargeId();
            Integer accountMappingTypeId;
            if (loanTransactionDTO.getLoanChargeData().isPenalty()) {
                accountMappingTypeId = CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue();
            } else {
                accountMappingTypeId = CashAccountsForLoan.INCOME_FROM_FEES.getValue();
            }
            this.helper.createDebitJournalEntryOrReversalForLoanCharges(office, currencyCode, accountMappingTypeId, loanProductId, chargeId,
                    loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
        }
    }

    private void createJournalEntriesForLoanChargeAdjustment(LoanDTO loanDTO, LoanTransactionDTO loanTransactionDTO, Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final BigDecimal overPaymentAmount = loanTransactionDTO.getOverPayment();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        final boolean isReversal = loanTransactionDTO.isReversed();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        Map<GLAccount, BigDecimal> accountMap = new LinkedHashMap<>();

        // handle principal payment (and reversals)
        if (principalAmount != null && principalAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, CashAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                    paymentTypeId);
            accountMap.put(account, principalAmount);
        }

        // handle interest payment (and reversals)
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    CashAccountsForLoan.INTEREST_ON_LOANS.getValue(), paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(interestAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, interestAmount);
            }
        }

        // handle fees payment (and reversals)
        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, CashAccountsForLoan.INCOME_FROM_FEES.getValue(),
                    paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(feesAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, feesAmount);
            }
        }

        // handle penalties payment (and reversals)
        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(penaltiesAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, penaltiesAmount);
            }
        }

        // handle overpayment
        if (overPaymentAmount != null && overPaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, CashAccountsForLoan.OVERPAYMENT.getValue(),
                    paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(overPaymentAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, overPaymentAmount);
            }
        }

        for (Map.Entry<GLAccount, BigDecimal> entry : accountMap.entrySet()) {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                    entry.getValue(), isReversal, entry.getKey());
        }

        if (totalDebitAmount.compareTo(BigDecimal.ZERO) > 0) {
            Long chargeId = loanTransactionDTO.getLoanChargeData().getChargeId();
            Integer accountMappingTypeId;
            if (loanTransactionDTO.getLoanChargeData().isPenalty()) {
                accountMappingTypeId = CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue();
            } else {
                accountMappingTypeId = CashAccountsForLoan.INCOME_FROM_FEES.getValue();
            }
            this.helper.createDebitJournalEntryOrReversalForLoanCharges(office, currencyCode, accountMappingTypeId, loanProductId, chargeId,
                    loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
        }
    }

    /**
     * Handle chargeback journal entry creation
     *
     * @param loanDTO
     * @param loanTransactionDTO
     * @param office
     */
    private void createJournalEntriesForChargeback(LoanDTO loanDTO, LoanTransactionDTO loanTransactionDTO, Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal amount = loanTransactionDTO.getAmount();
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, CashAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                CashAccountsForLoan.FUND_SOURCE.getValue(), loanProductId, paymentTypeId, loanId, transactionId, transactionDate, amount,
                isReversal);
    }

    /**
     * Debit loan Portfolio and credit Fund source for a Disbursement <br/>
     *
     * All debits are turned into credits and vice versa in case of disbursement reversals
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
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal overpaymentPortion = loanTransactionDTO.getOverPayment() != null ? loanTransactionDTO.getOverPayment()
                : BigDecimal.ZERO;
        final BigDecimal principalPortion = loanTransactionDTO.getAmount().subtract(overpaymentPortion);
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        if (MathUtil.isGreaterThanZero(principalPortion)) {
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, CashAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, principalPortion, isReversal);
        }

        if (MathUtil.isGreaterThanZero(overpaymentPortion)) {
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, CashAccountsForLoan.OVERPAYMENT.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, overpaymentPortion, isReversal);
        }
        if (loanTransactionDTO.isLoanToLoanTransfer()) {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, FinancialActivity.ASSET_TRANSFER.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, loanTransactionDTO.getAmount(), isReversal);
        } else if (loanTransactionDTO.isAccountTransfer()) {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, FinancialActivity.LIABILITY_TRANSFER.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, loanTransactionDTO.getAmount(), isReversal);
        } else {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CashAccountsForLoan.FUND_SOURCE.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, loanTransactionDTO.getAmount(), isReversal);
        }
    }

    /**
     * Debit loan Portfolio and credit Fund source for a Disbursement <br/>
     *
     * All debits are turned into credits and vice versa in case of disbursement reversals
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
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal refundAmount = loanTransactionDTO.getAmount();
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        if (loanTransactionDTO.isAccountTransfer()) {
            this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, CashAccountsForLoan.OVERPAYMENT.getValue(),
                    FinancialActivity.LIABILITY_TRANSFER.getValue(), loanProductId, paymentTypeId, loanId, transactionId, transactionDate,
                    refundAmount, isReversal);
        } else {
            this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, CashAccountsForLoan.OVERPAYMENT.getValue(),
                    CashAccountsForLoan.FUND_SOURCE.getValue(), loanProductId, paymentTypeId, loanId, transactionId, transactionDate,
                    refundAmount, isReversal);
        }
    }

    /**
     * Create a single Debit to fund source and multiple credits if applicable (loan portfolio for principal repayments,
     * Interest on loans for interest repayments, Income from fees for fees payment and Income from penalties for
     * penalty payment)
     *
     * In case the loan transaction is a reversal, all debits are turned into credits and vice versa
     */
    private void createJournalEntriesForRepayments(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {

        final boolean isMarkedChargeOff = loanDTO.isMarkedAsChargeOff();
        if (isMarkedChargeOff) {
            createJournalEntriesForChargeOffLoanRepayments(loanDTO, loanTransactionDTO, office);

        } else {
            createJournalEntriesForLoanRepayments(loanDTO, loanTransactionDTO, office);
        }

    }

    private void createJournalEntriesForChargeOffLoanRepayments(LoanDTO loanDTO, LoanTransactionDTO loanTransactionDTO, Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();
        final boolean isMarkedFraud = loanDTO.isMarkedAsFraud();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final BigDecimal overPaymentAmount = loanTransactionDTO.getOverPayment();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        final boolean isReversal = loanTransactionDTO.isReversed();

        Map<GLAccount, BigDecimal> accountMapForCredit = new LinkedHashMap<>();
        Map<Integer, BigDecimal> accountMapForDebit = new LinkedHashMap<>();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        // principal payment
        if (principalAmount != null && principalAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                if (isMarkedFraud) {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                            CashAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), CashAccountsForLoan.FUND_SOURCE.getValue(),
                            accountMapForCredit, accountMapForDebit);
                } else {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                            CashAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), CashAccountsForLoan.FUND_SOURCE.getValue(),
                            accountMapForCredit, accountMapForDebit);
                }
            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                if (isMarkedFraud) {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                            CashAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), CashAccountsForLoan.FUND_SOURCE.getValue(),
                            accountMapForCredit, accountMapForDebit);

                } else {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                            CashAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), CashAccountsForLoan.FUND_SOURCE.getValue(),
                            accountMapForCredit, accountMapForDebit);
                }

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        CashAccountsForLoan.GOODWILL_CREDIT.getValue(), accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        CashAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);

            } else {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, CashAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                        CashAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);
            }

        }

        // interest payment
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId,
                        CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), CashAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId,
                        CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), CashAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), accountMapForCredit, accountMapForDebit);
            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        CashAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);

            } else {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, CashAccountsForLoan.INTEREST_ON_LOANS.getValue(),
                        CashAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);
            }

        }

        // handle fees payment
        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId,
                        CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), CashAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId,
                        CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), CashAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        CashAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);

            } else {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_FEES.getValue(),
                        CashAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);
            }

        }

        // handle penalties payment
        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), CashAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), CashAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        CashAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);

            } else {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId, CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue(),
                        CashAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);
            }

        }

        // overpayment
        if (overPaymentAmount != null && overPaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, CashAccountsForLoan.OVERPAYMENT.getValue(),
                        CashAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, CashAccountsForLoan.OVERPAYMENT.getValue(),
                        CashAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, CashAccountsForLoan.OVERPAYMENT.getValue(),
                        CashAccountsForLoan.GOODWILL_CREDIT.getValue(), accountMapForCredit, accountMapForDebit);
            } else {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, CashAccountsForLoan.OVERPAYMENT.getValue(),
                        CashAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);
            }

        }

        // create credit entries
        for (Map.Entry<GLAccount, BigDecimal> creditEntry : accountMapForCredit.entrySet()) {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                    creditEntry.getValue(), isReversal, creditEntry.getKey());
        }

        /*** create a single debit entry (or reversal) for the entire amount **/
        if (loanTransactionDTO.isLoanToLoanTransfer()) {
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, FinancialActivity.ASSET_TRANSFER.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
        } else if (loanTransactionDTO.isAccountTransfer()) {
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, FinancialActivity.LIABILITY_TRANSFER.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
        } else {
            // create debit entries
            for (Map.Entry<Integer, BigDecimal> debitEntry : accountMapForDebit.entrySet()) {
                this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, debitEntry.getKey().intValue(), loanProductId,
                        paymentTypeId, loanId, transactionId, transactionDate, debitEntry.getValue(), isReversal);
            }
        }

        /**
         * Charge Refunds (and their reversals) have an extra refund related pair of journal entries in addition to
         * those related to the repayment above
         ***/
        if (totalDebitAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (loanTransactionDTO.getTransactionType().isChargeRefund()) {
                Integer incomeAccount = this.helper.getValueForFeeOrPenaltyIncomeAccount(loanTransactionDTO.getChargeRefundChargeType());
                this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, incomeAccount,
                        CashAccountsForLoan.FUND_SOURCE.getValue(), loanProductId, paymentTypeId, loanId, transactionId, transactionDate,
                        totalDebitAmount, isReversal);
            }
        }
    }

    private void createJournalEntriesForLoanRepayments(LoanDTO loanDTO, LoanTransactionDTO loanTransactionDTO, Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final BigDecimal overPaymentAmount = loanTransactionDTO.getOverPayment();
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        BigDecimal totalDebitAmount = new BigDecimal(0);
        Map<Integer, BigDecimal> debitAccountMapForGoodwillCredit = new LinkedHashMap<>();

        if (principalAmount != null && principalAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CashAccountsForLoan.LOAN_PORTFOLIO, loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, principalAmount, isReversal);
            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateDebitAccountEntry(loanProductId, principalAmount, CashAccountsForLoan.GOODWILL_CREDIT.getValue(),
                        debitAccountMapForGoodwillCredit, paymentTypeId);
            }
        }

        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CashAccountsForLoan.INTEREST_ON_LOANS,
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, interestAmount, isReversal);
            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateDebitAccountEntry(loanProductId, interestAmount,
                        CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), debitAccountMapForGoodwillCredit,
                        paymentTypeId);
            }
        }

        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);
            this.helper.createCreditJournalEntryOrReversalForLoanCharges(office, currencyCode,
                    CashAccountsForLoan.INCOME_FROM_FEES.getValue(), loanProductId, loanId, transactionId, transactionDate, feesAmount,
                    isReversal, loanTransactionDTO.getFeePayments());
            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateDebitAccountEntry(loanProductId, feesAmount, CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(),
                        debitAccountMapForGoodwillCredit, paymentTypeId);
            }
        }

        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            this.helper.createCreditJournalEntryOrReversalForLoanCharges(office, currencyCode,
                    CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), loanProductId, loanId, transactionId, transactionDate,
                    penaltiesAmount, isReversal, loanTransactionDTO.getPenaltyPayments());
            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateDebitAccountEntry(loanProductId, penaltiesAmount,
                        CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), debitAccountMapForGoodwillCredit,
                        paymentTypeId);
            }
        }

        if (overPaymentAmount != null && overPaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CashAccountsForLoan.OVERPAYMENT, loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, overPaymentAmount, isReversal);
            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateDebitAccountEntry(loanProductId, overPaymentAmount, CashAccountsForLoan.GOODWILL_CREDIT.getValue(),
                        debitAccountMapForGoodwillCredit, paymentTypeId);
            }
        }

        /*** create a single debit entry (or reversal) for the entire amount **/
        if (loanTransactionDTO.isLoanToLoanTransfer()) {
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, FinancialActivity.ASSET_TRANSFER.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
        } else if (loanTransactionDTO.isAccountTransfer()) {
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, FinancialActivity.LIABILITY_TRANSFER.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
        } else {
            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {

                // create debit entries
                for (Map.Entry<Integer, BigDecimal> debitEntry : debitAccountMapForGoodwillCredit.entrySet()) {
                    this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, debitEntry.getKey().intValue(),
                            loanProductId, paymentTypeId, loanId, transactionId, transactionDate, debitEntry.getValue(), isReversal);
                }

            } else {
                this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, CashAccountsForLoan.FUND_SOURCE.getValue(),
                        loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
            }
        }

        /**
         * Charge Refunds (and their reversals) have an extra refund related pair of journal entries in addition to
         * those related to the repayment above
         ***/
        if (totalDebitAmount.compareTo(BigDecimal.ZERO) > 0 && loanTransactionDTO.getTransactionType().isChargeRefund()) {
            Integer incomeAccount = this.helper.getValueForFeeOrPenaltyIncomeAccount(loanTransactionDTO.getChargeRefundChargeType());
            this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, incomeAccount,
                    CashAccountsForLoan.FUND_SOURCE.getValue(), loanProductId, paymentTypeId, loanId, transactionId, transactionDate,
                    totalDebitAmount, isReversal);
        }
    }

    private void populateDebitAccountEntry(Long loanProductId, BigDecimal transactionPartAmount, Integer debitAccountType,
            Map<Integer, BigDecimal> accountMapForDebit, Long paymentTypeId) {
        Integer accountDebit = returnExistingDebitAccountInMapMatchingGLAccount(loanProductId, paymentTypeId, debitAccountType,
                accountMapForDebit);
        if (accountMapForDebit.containsKey(accountDebit)) {
            BigDecimal amount = accountMapForDebit.get(accountDebit).add(transactionPartAmount);
            accountMapForDebit.put(accountDebit, amount);
        } else {
            accountMapForDebit.put(accountDebit, transactionPartAmount);
        }
    }

    /**
     * Create a single Debit to fund source and a single credit to "Income from Recovery"
     *
     * In case the loan transaction is a reversal, all debits are turned into credits and vice versa
     */
    private void createJournalEntriesForRecoveryRepayments(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal amount = loanTransactionDTO.getAmount();
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, CashAccountsForLoan.FUND_SOURCE.getValue(),
                CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), loanProductId, paymentTypeId, loanId, transactionId, transactionDate,
                amount, isReversal);

    }

    /**
     * Credit loan Portfolio and Debit Suspense Account for a Transfer Initiation. A Transfer acceptance would be
     * treated the opposite i.e Debit Loan Portfolio and Credit Suspense Account <br/>
     *
     * All debits are turned into credits and vice versa in case of Transfer Initiation disbursals
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
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final boolean isReversal = loanTransactionDTO.isReversed();
        // final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        if (loanTransactionDTO.getTransactionType().isInitiateTransfer()) {
            this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, CashAccountsForLoan.TRANSFERS_SUSPENSE.getValue(),
                    CashAccountsForLoan.LOAN_PORTFOLIO.getValue(), loanProductId, null, loanId, transactionId, transactionDate,
                    principalAmount, isReversal);
        } else if (loanTransactionDTO.getTransactionType().isApproveTransfer()
                || loanTransactionDTO.getTransactionType().isWithdrawTransfer()) {
            this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, CashAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                    CashAccountsForLoan.TRANSFERS_SUSPENSE.getValue(), loanProductId, null, loanId, transactionId, transactionDate,
                    principalAmount, isReversal);
        }
    }

    private void createJournalEntriesForCreditBalanceRefund(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        BigDecimal overpaymentAmount = loanTransactionDTO.getOverPayment();
        BigDecimal principalAmount = loanTransactionDTO.getPrincipal();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<JournalAmountHolder> journalAmountHolders = new ArrayList<>();

        if (principalAmount != null && principalAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalAmount = totalAmount.add(principalAmount);
            journalAmountHolders.add(new JournalAmountHolder(CashAccountsForLoan.LOAN_PORTFOLIO.getValue(), principalAmount));
        }
        if (overpaymentAmount != null && overpaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalAmount = totalAmount.add(overpaymentAmount);
            journalAmountHolders.add(new JournalAmountHolder(CashAccountsForLoan.OVERPAYMENT.getValue(), overpaymentAmount));
        }

        JournalAmountHolder totalAmountHolder = new JournalAmountHolder(CashAccountsForLoan.FUND_SOURCE.getValue(), totalAmount);
        helper.createSplitJournalEntriesAndReversalsForLoan(office, currencyCode, journalAmountHolders, totalAmountHolder, loanProductId,
                paymentTypeId, loanId, transactionId, transactionDate, isReversal);
    }

    private void createJournalEntriesForRefundForActiveLoan(LoanDTO loanDTO, LoanTransactionDTO loanTransactionDTO, Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final BigDecimal overPaymentAmount = loanTransactionDTO.getOverPayment();
        final boolean isReversal = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        if (principalAmount != null && principalAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CashAccountsForLoan.LOAN_PORTFOLIO, loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, principalAmount, !isReversal);
        }

        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CashAccountsForLoan.INTEREST_ON_LOANS,
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, interestAmount, !isReversal);
        }

        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);

            List<ChargePaymentDTO> chargePaymentDTOs = new ArrayList<>();

            for (ChargePaymentDTO chargePaymentDTO : loanTransactionDTO.getFeePayments()) {
                chargePaymentDTOs.add(new ChargePaymentDTO(chargePaymentDTO.getChargeId(),
                        chargePaymentDTO.getAmount().floatValue() < 0 ? chargePaymentDTO.getAmount().multiply(new BigDecimal(-1))
                                : chargePaymentDTO.getAmount(),
                        chargePaymentDTO.getLoanChargeId()));
            }
            this.helper.createCreditJournalEntryOrReversalForLoanCharges(office, currencyCode,
                    CashAccountsForLoan.INCOME_FROM_FEES.getValue(), loanProductId, loanId, transactionId, transactionDate, feesAmount,
                    !isReversal, chargePaymentDTOs);
        }

        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            List<ChargePaymentDTO> chargePaymentDTOs = new ArrayList<>();

            for (ChargePaymentDTO chargePaymentDTO : loanTransactionDTO.getPenaltyPayments()) {
                chargePaymentDTOs.add(new ChargePaymentDTO(chargePaymentDTO.getChargeId(),
                        chargePaymentDTO.getAmount().floatValue() < 0 ? chargePaymentDTO.getAmount().multiply(new BigDecimal(-1))
                                : chargePaymentDTO.getAmount(),
                        chargePaymentDTO.getLoanChargeId()));
            }

            this.helper.createCreditJournalEntryOrReversalForLoanCharges(office, currencyCode,
                    CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), loanProductId, loanId, transactionId, transactionDate,
                    penaltiesAmount, !isReversal, chargePaymentDTOs);
        }

        if (overPaymentAmount != null && overPaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, CashAccountsForLoan.OVERPAYMENT, loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, overPaymentAmount, !isReversal);
        }

        /*** create a single debit entry (or reversal) for the entire amount **/
        this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, CashAccountsForLoan.FUND_SOURCE.getValue(),
                loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, !isReversal);

    }
}
