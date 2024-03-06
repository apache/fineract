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
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForLoan;
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
public class AccrualBasedAccountingProcessorForLoan implements AccountingProcessorForLoan {

    private final AccountingProcessorHelper helper;

    @Override
    public void createJournalEntriesForLoan(final LoanDTO loanDTO) {
        final GLClosure latestGLClosure = this.helper.getLatestClosureByBranch(loanDTO.getOfficeId());
        final Office office = this.helper.getOfficeById(loanDTO.getOfficeId());
        for (final LoanTransactionDTO loanTransactionDTO : loanDTO.getNewLoanTransactions()) {
            final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
            this.helper.checkForBranchClosures(latestGLClosure, transactionDate);

            /** Handle Disbursements **/
            if (loanTransactionDTO.getTransactionType().isDisbursement()) {
                createJournalEntriesForDisbursements(loanDTO, loanTransactionDTO, office);
            }

            /*** Handle Accruals ***/
            if (loanTransactionDTO.getTransactionType().isAccrual()) {
                createJournalEntriesForAccruals(loanDTO, loanTransactionDTO, office);
            }

            /***
             * Handle repayments, loan refunds, repayments at disbursement and reversal of Repayments and Repayments at
             * disbursement (except charge adjustment)
             ***/
            else if ((loanTransactionDTO.getTransactionType().isRepaymentType()
                    && !loanTransactionDTO.getTransactionType().isChargeAdjustment())
                    || loanTransactionDTO.getTransactionType().isRepaymentAtDisbursement()
                    || loanTransactionDTO.getTransactionType().isChargePayment()) {
                createJournalEntriesForRepaymentsAndWriteOffs(loanDTO, loanTransactionDTO, office, false,
                        loanTransactionDTO.getTransactionType().isRepaymentAtDisbursement());
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

            /** Handle Write Offs, waivers and their reversals **/
            else if ((loanTransactionDTO.getTransactionType().isWriteOff() || loanTransactionDTO.getTransactionType().isWaiveInterest()
                    || loanTransactionDTO.getTransactionType().isWaiveCharges())) {
                createJournalEntriesForRepaymentsAndWriteOffs(loanDTO, loanTransactionDTO, office, true, false);
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
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                        AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), accountMapForCredit, accountMapForDebit);
            } else {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                        AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), accountMapForCredit, accountMapForDebit);
            }
        }

        // interest payment
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {

            populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(),
                    AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), accountMapForCredit, accountMapForDebit);
        }

        // handle fees payment
        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {
            populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(),
                    AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), accountMapForCredit, accountMapForDebit);
        }

        // handle penalty payment
        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {
            populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId, AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(),
                    AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), accountMapForCredit, accountMapForDebit);
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

        // handle principal payment
        if (principalAmount != null && principalAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), paymentTypeId);
            accountMap.put(account, principalAmount);
        }

        // handle interest payment
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(interestAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, interestAmount);
            }

        }

        // handle fees payment
        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(feesAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, feesAmount);
            }
        }

        // handle penalty payment
        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), paymentTypeId);
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
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
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
                accountMappingTypeId = AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue();
            } else {
                accountMappingTypeId = AccrualAccountsForLoan.INCOME_FROM_FEES.getValue();
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
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), paymentTypeId);
            accountMap.put(account, principalAmount);
        }

        // handle interest payment (and reversals)
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(), paymentTypeId);
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
                    AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(), paymentTypeId);
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
                    AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(), paymentTypeId);
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
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
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
                accountMappingTypeId = AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue();
            } else {
                accountMappingTypeId = AccrualAccountsForLoan.INCOME_FROM_FEES.getValue();
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
        final BigDecimal overpaidAmount = Objects.isNull(loanTransactionDTO.getOverPayment()) ? BigDecimal.ZERO
                : loanTransactionDTO.getOverPayment();

        final BigDecimal principalCredited = Objects.isNull(loanTransactionDTO.getPrincipal()) ? BigDecimal.ZERO
                : loanTransactionDTO.getPrincipal();
        final BigDecimal feeCredited = Objects.isNull(loanTransactionDTO.getFees()) ? BigDecimal.ZERO : loanTransactionDTO.getFees();
        final BigDecimal penaltyCredited = Objects.isNull(loanTransactionDTO.getPenalties()) ? BigDecimal.ZERO
                : loanTransactionDTO.getPenalties();

        final BigDecimal principalPaid = Objects.isNull(loanTransactionDTO.getPrincipalPaid()) ? BigDecimal.ZERO
                : loanTransactionDTO.getPrincipalPaid();
        final BigDecimal feePaid = Objects.isNull(loanTransactionDTO.getFeePaid()) ? BigDecimal.ZERO : loanTransactionDTO.getFeePaid();
        final BigDecimal penaltyPaid = Objects.isNull(loanTransactionDTO.getPenaltyPaid()) ? BigDecimal.ZERO
                : loanTransactionDTO.getPenaltyPaid();

        helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, AccrualAccountsForLoan.FUND_SOURCE, loanProductId,
                paymentTypeId, loanId, transactionId, transactionDate, amount, isReversal);

        if (overpaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, overpaidAmount, isReversal);
        }

        if (principalCredited.compareTo(principalPaid) > 0) {
            helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, getPrincipalAccount(loanDTO), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, principalCredited.subtract(principalPaid), isReversal);
        } else if (principalCredited.compareTo(principalPaid) < 0) {
            helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, getPrincipalAccount(loanDTO), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, principalPaid.subtract(principalCredited), isReversal);
        }

        if (feeCredited.compareTo(feePaid) > 0) {
            helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, getFeeAccount(loanDTO), loanProductId, paymentTypeId,
                    loanId, transactionId, transactionDate, feeCredited.subtract(feePaid), isReversal);
        } else if (feeCredited.compareTo(feePaid) < 0) {
            helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, getFeeAccount(loanDTO), loanProductId, paymentTypeId,
                    loanId, transactionId, transactionDate, feePaid.subtract(feeCredited), isReversal);
        }

        if (penaltyCredited.compareTo(penaltyPaid) > 0) {
            helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, getPenaltyAccount(loanDTO), loanProductId, paymentTypeId,
                    loanId, transactionId, transactionDate, penaltyCredited.subtract(penaltyPaid), isReversal);
        } else if (penaltyCredited.compareTo(penaltyPaid) < 0) {
            helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, getPenaltyAccount(loanDTO), loanProductId, paymentTypeId,
                    loanId, transactionId, transactionDate, penaltyPaid.subtract(penaltyCredited), isReversal);
        }
    }

    private Integer getFeeAccount(LoanDTO loanDTO) {
        Integer account = AccrualAccountsForLoan.FEES_RECEIVABLE.getValue();
        if (loanDTO.isMarkedAsChargeOff()) {
            account = AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue();
        }
        return account;
    }

    private Integer getPenaltyAccount(LoanDTO loanDTO) {
        Integer account = AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue();
        if (loanDTO.isMarkedAsChargeOff()) {
            account = AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue();
        }
        return account;
    }

    private Integer getPrincipalAccount(LoanDTO loanDTO) {
        if (loanDTO.isMarkedAsFraud() && loanDTO.isMarkedAsChargeOff()) {
            return AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue();
        } else if (!loanDTO.isMarkedAsFraud() && loanDTO.isMarkedAsChargeOff()) {
            return AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue();
        } else {
            return AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue();
        }
    }

    /**
     * Debit loan Portfolio and credit Fund source for Disbursement.
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
        final boolean isReversed = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        // create journal entries for the disbursement (or disbursement
        // reversal)
        if (MathUtil.isGreaterThanZero(principalPortion)) {
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, principalPortion, isReversed);

        }
        if (MathUtil.isGreaterThanZero(overpaymentPortion)) {
            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, overpaymentPortion, isReversed);
        }
        if (loanTransactionDTO.isLoanToLoanTransfer()) {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, FinancialActivity.ASSET_TRANSFER.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, loanTransactionDTO.getAmount(), isReversed);
        } else if (loanTransactionDTO.isAccountTransfer()) {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, FinancialActivity.LIABILITY_TRANSFER.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, loanTransactionDTO.getAmount(), isReversed);
        } else {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, loanTransactionDTO.getAmount(), isReversed);
        }
    }

    /**
     * Handles repayments using the following posting rules <br/>
     * <br/>
     * <br/>
     *
     * <b>Principal Repayment</b>: Debits "Fund Source" and Credits "Loan Portfolio"<br/>
     *
     * <b>Interest Repayment</b>:Debits "Fund Source" and Credits "Receivable Interest" <br/>
     *
     * <b>Fee Repayment</b>:Debits "Fund Source" (or "Interest on Loans" in case of repayment at disbursement) and
     * Credits "Receivable Fees" <br/>
     *
     * <b>Penalty Repayment</b>: Debits "Fund Source" and Credits "Receivable Penalties" <br/>
     * <br/>
     * Handles write offs using the following posting rules <br/>
     * <br/>
     * <b>Principal Write off</b>: Debits "Losses Written Off" and Credits "Loan Portfolio"<br/>
     *
     * <b>Interest Write off</b>:Debits "Losses Written off" and Credits "Receivable Interest" <br/>
     *
     * <b>Fee Write off</b>:Debits "Losses Written off" and Credits "Receivable Fees" <br/>
     *
     * <b>Penalty Write off</b>: Debits "Losses Written off" and Credits "Receivable Penalties" <br/>
     * <br/>
     * <br/>
     * In case the loan transaction has been reversed, all debits are turned into credits and vice versa
     *
     * @param loanTransactionDTO
     * @param loanDTO
     * @param office
     */
    private void createJournalEntriesForRepaymentsAndWriteOffs(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office, final boolean writeOff, final boolean isIncomeFromFee) {
        final boolean isMarkedChargeOff = loanDTO.isMarkedAsChargeOff();
        if (isMarkedChargeOff) {
            createJournalEntriesForChargeOffLoanRepaymentAndWriteOffs(loanDTO, loanTransactionDTO, office, writeOff, isIncomeFromFee);

        } else {
            createJournalEntriesForLoansRepaymentAndWriteOffs(loanDTO, loanTransactionDTO, office, writeOff, isIncomeFromFee);
        }
    }

    private void createJournalEntriesForChargeOffLoanRepaymentAndWriteOffs(LoanDTO loanDTO, LoanTransactionDTO loanTransactionDTO,
            Office office, boolean writeOff, boolean isIncomeFromFee) {
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
                            AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            accountMapForCredit, accountMapForDebit);
                } else {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                            AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            accountMapForCredit, accountMapForDebit);
                }
            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                if (isMarkedFraud) {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                            AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            accountMapForCredit, accountMapForDebit);

                } else {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                            AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            accountMapForCredit, accountMapForDebit);
                }

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), AccrualAccountsForLoan.GOODWILL_CREDIT.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);
            }

        }

        // interest payment
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);
            }

        }

        // handle fees payment
        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);

            } else {
                if (isIncomeFromFee) {
                    this.helper.createCreditJournalEntryOrReversalForLoanCharges(office, currencyCode,
                            AccrualAccountsForLoan.INCOME_FROM_FEES.getValue(), loanProductId, loanId, transactionId, transactionDate,
                            feesAmount, isReversal, loanTransactionDTO.getFeePayments());
                    Integer accountDebit = AccrualAccountsForLoan.FUND_SOURCE.getValue();
                    if (accountMapForDebit.containsKey(accountDebit)) {
                        BigDecimal amount = accountMapForDebit.get(accountDebit).add(feesAmount);
                        accountMapForDebit.put(accountDebit, amount);
                    } else {
                        accountMapForDebit.put(accountDebit, feesAmount);
                    }

                } else {
                    populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(),
                            AccrualAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);
                }

            }

        }

        // handle penalties
        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        accountMapForCredit, accountMapForDebit);

            } else {
                if (isIncomeFromFee) {
                    populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                            AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            accountMapForCredit, accountMapForDebit);
                } else {
                    populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                            AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            accountMapForCredit, accountMapForDebit);
                }
            }

        }

        // overpayment
        if (overPaymentAmount != null && overPaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        AccrualAccountsForLoan.GOODWILL_CREDIT.getValue(), accountMapForCredit, accountMapForDebit);

            } else {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), accountMapForCredit, accountMapForDebit);
            }

        }

        // create credit entries
        for (Map.Entry<GLAccount, BigDecimal> creditEntry : accountMapForCredit.entrySet()) {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                    creditEntry.getValue(), isReversal, creditEntry.getKey());
        }

        if (totalDebitAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (writeOff) {
                this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode,
                        AccrualAccountsForLoan.LOSSES_WRITTEN_OFF.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                        transactionDate, totalDebitAmount, isReversal);
            } else {
                if (loanTransactionDTO.isLoanToLoanTransfer()) {
                    this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, FinancialActivity.ASSET_TRANSFER.getValue(),
                            loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
                } else if (loanTransactionDTO.isAccountTransfer()) {
                    this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode,
                            FinancialActivity.LIABILITY_TRANSFER.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                            transactionDate, totalDebitAmount, isReversal);
                } else {
                    // create debit entries
                    for (Map.Entry<Integer, BigDecimal> debitEntry : accountMapForDebit.entrySet()) {
                        this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, debitEntry.getKey().intValue(),
                                loanProductId, paymentTypeId, loanId, transactionId, transactionDate, debitEntry.getValue(), isReversal);
                    }
                }
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
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), loanProductId, paymentTypeId, loanId, transactionId, transactionDate,
                        totalDebitAmount, isReversal);
            }
        }

    }

    private void createJournalEntriesForLoansRepaymentAndWriteOffs(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office, final boolean writeOff, final boolean isIncomeFromFee) {
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
        Map<Integer, BigDecimal> debitAccountMapForGoodwillCredit = new LinkedHashMap<>();

        // handle principal payment or writeOff (and reversals)
        if (principalAmount != null && principalAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), paymentTypeId);
            accountMap.put(account, principalAmount);
            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateDebitAccountEntry(loanProductId, principalAmount, AccrualAccountsForLoan.GOODWILL_CREDIT.getValue(),
                        debitAccountMapForGoodwillCredit, paymentTypeId);
            }
        }

        // handle interest payment of writeOff (and reversals)
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(), paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(interestAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, interestAmount);
            }
            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateDebitAccountEntry(loanProductId, interestAmount,
                        AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), debitAccountMapForGoodwillCredit,
                        paymentTypeId);
            }
        }

        // handle fees payment of writeOff (and reversals)
        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {

            totalDebitAmount = totalDebitAmount.add(feesAmount);

            if (isIncomeFromFee) {
                this.helper.createCreditJournalEntryOrReversalForLoanCharges(office, currencyCode,
                        AccrualAccountsForLoan.INCOME_FROM_FEES.getValue(), loanProductId, loanId, transactionId, transactionDate,
                        feesAmount, isReversal, loanTransactionDTO.getFeePayments());
            } else {
                GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                        AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(), paymentTypeId);
                if (accountMap.containsKey(account)) {
                    BigDecimal amount = accountMap.get(account).add(feesAmount);
                    accountMap.put(account, amount);
                } else {
                    accountMap.put(account, feesAmount);
                }
            }
            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateDebitAccountEntry(loanProductId, feesAmount, AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(),
                        debitAccountMapForGoodwillCredit, paymentTypeId);
            }
        }

        // handle penalties payment of writeOff (and reversals)
        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            if (isIncomeFromFee) {
                GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                        AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), paymentTypeId);
                if (accountMap.containsKey(account)) {
                    BigDecimal amount = accountMap.get(account).add(penaltiesAmount);
                    accountMap.put(account, amount);
                } else {
                    accountMap.put(account, penaltiesAmount);
                }
            } else {
                GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                        AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(), paymentTypeId);
                if (accountMap.containsKey(account)) {
                    BigDecimal amount = accountMap.get(account).add(penaltiesAmount);
                    accountMap.put(account, amount);
                } else {
                    accountMap.put(account, penaltiesAmount);
                }
            }

            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateDebitAccountEntry(loanProductId, penaltiesAmount,
                        AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), debitAccountMapForGoodwillCredit,
                        paymentTypeId);
            }
        }

        if (overPaymentAmount != null && overPaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                    paymentTypeId);
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(overPaymentAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, overPaymentAmount);
            }
            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateDebitAccountEntry(loanProductId, overPaymentAmount, AccrualAccountsForLoan.GOODWILL_CREDIT.getValue(),
                        debitAccountMapForGoodwillCredit, paymentTypeId);
            }
        }

        for (Map.Entry<GLAccount, BigDecimal> entry : accountMap.entrySet()) {
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                    entry.getValue(), isReversal, entry.getKey());
        }

        /**
         * Single DEBIT transaction for write-offs or Repayments (and their reversals)
         ***/
        if (totalDebitAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (writeOff) {
                this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode,
                        AccrualAccountsForLoan.LOSSES_WRITTEN_OFF.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                        transactionDate, totalDebitAmount, isReversal);
            } else {
                if (loanTransactionDTO.isLoanToLoanTransfer()) {
                    this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, FinancialActivity.ASSET_TRANSFER.getValue(),
                            loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, isReversal);
                } else if (loanTransactionDTO.isAccountTransfer()) {
                    this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode,
                            FinancialActivity.LIABILITY_TRANSFER.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                            transactionDate, totalDebitAmount, isReversal);
                } else {
                    if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                        // create debit entries
                        for (Map.Entry<Integer, BigDecimal> debitEntry : debitAccountMapForGoodwillCredit.entrySet()) {
                            this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, debitEntry.getKey().intValue(),
                                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, debitEntry.getValue(),
                                    isReversal);
                        }

                    } else {
                        this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode,
                                AccrualAccountsForLoan.FUND_SOURCE.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                                transactionDate, totalDebitAmount, isReversal);
                    }
                }
            }
        }

        /**
         * Charge Refunds (and their reversals) have an extra refund related pair of journal entries in addition to
         * those related to the repayment above
         ***/
        if (totalDebitAmount.compareTo(BigDecimal.ZERO) > 0 && loanTransactionDTO.getTransactionType().isChargeRefund()) {
            Integer incomeAccount = this.helper.getValueForFeeOrPenaltyIncomeAccount(loanTransactionDTO.getChargeRefundChargeType());
            this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, incomeAccount,
                    AccrualAccountsForLoan.FUND_SOURCE.getValue(), loanProductId, paymentTypeId, loanId, transactionId, transactionDate,
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

    private Integer returnExistingDebitAccountInMapMatchingGLAccount(Long loanProductId, Long paymentTypeId, Integer accountType,
            Map<Integer, BigDecimal> accountMap) {
        GLAccount glAccount = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, accountType, paymentTypeId);
        Integer accountEntry = accountMap.entrySet().stream().filter(account -> this.helper
                .getLinkedGLAccountForLoanProduct(loanProductId, account.getKey(), paymentTypeId).getGlCode().equals(glAccount.getGlCode()))
                .map(Map.Entry::getKey).findFirst().orElse(accountType);
        return accountEntry;
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

        this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                transactionDate, amount, isReversal);

    }

    /**
     * Recognize the receivable interest <br/>
     * Debit "Interest Receivable" and Credit "Income from Interest"
     *
     * <b>Fees:</b> Debit <i>Fees Receivable</i> and credit <i>Income from Fees</i> <br/>
     *
     * <b>Penalties:</b> Debit <i>Penalties Receivable</i> and credit <i>Income from Penalties</i>
     *
     * Also handles reversals for both fees and payment applications
     *
     * @param loanDTO
     * @param loanTransactionDTO
     * @param office
     */
    private void createJournalEntriesForAccruals(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO, final Office office) {

        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final boolean isReversed = loanTransactionDTO.isReversed();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        // create journal entries for recognizing interest (or reversal)
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(),
                    AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                    transactionDate, interestAmount, isReversed);
        }
        // create journal entries for the fees application (or reversal)
        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.helper.createJournalEntriesAndReversalsForLoanCharges(office, currencyCode,
                    AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(), AccrualAccountsForLoan.INCOME_FROM_FEES.getValue(), loanProductId,
                    loanId, transactionId, transactionDate, feesAmount, isReversed, loanTransactionDTO.getFeePayments());
        }
        // create journal entries for the penalties application (or reversal)
        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {

            this.helper.createJournalEntriesAndReversalsForLoanCharges(office, currencyCode,
                    AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(), AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue(),
                    loanProductId, loanId, transactionId, transactionDate, penaltiesAmount, isReversed,
                    loanTransactionDTO.getPenaltyPayments());
        }
    }

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
            this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                    FinancialActivity.LIABILITY_TRANSFER.getValue(), loanProductId, paymentTypeId, loanId, transactionId, transactionDate,
                    refundAmount, isReversal);
        } else {
            this.helper.createJournalEntriesAndReversalsForLoan(office, currencyCode, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                    AccrualAccountsForLoan.FUND_SOURCE.getValue(), loanProductId, paymentTypeId, loanId, transactionId, transactionDate,
                    refundAmount, isReversal);
        }
    }

    private void createJournalEntriesForCreditBalanceRefund(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        final boolean isMarkedChargeOff = loanDTO.isMarkedAsChargeOff();
        createJournalEntriesForLoanCreditBalanceRefund(loanDTO, loanTransactionDTO, office, isMarkedChargeOff);
    }

    private void createJournalEntriesForLoanCreditBalanceRefund(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office, boolean isMarkedChargeOff) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();
        final boolean isMarkedFraud = loanDTO.isMarkedAsFraud();

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
            journalAmountHolders
                    .add(new JournalAmountHolder(determineAccrualAccountForCBR(isMarkedChargeOff, isMarkedFraud, false), principalAmount));
        }
        if (overpaymentAmount != null && overpaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalAmount = totalAmount.add(overpaymentAmount);
            journalAmountHolders
                    .add(new JournalAmountHolder(determineAccrualAccountForCBR(isMarkedChargeOff, isMarkedFraud, true), overpaymentAmount));
        }

        JournalAmountHolder totalAmountHolder = new JournalAmountHolder(AccrualAccountsForLoan.FUND_SOURCE.getValue(), totalAmount);
        helper.createSplitJournalEntriesAndReversalsForLoan(office, currencyCode, journalAmountHolders, totalAmountHolder, loanProductId,
                paymentTypeId, loanId, transactionId, transactionDate, isReversal);

    }

    private Integer determineAccrualAccountForCBR(boolean isMarkedChargeOff, boolean isMarkedFraud, boolean isOverpayment) {
        if (isOverpayment) {
            return AccrualAccountsForLoan.OVERPAYMENT.getValue();
        } else {
            if (isMarkedChargeOff) {
                if (isMarkedFraud) {
                    return AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue();
                } else {
                    return AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue();
                }
            } else {
                return AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue();
            }
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
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, AccrualAccountsForLoan.LOAN_PORTFOLIO,
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, principalAmount, !isReversal);
        }

        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, AccrualAccountsForLoan.INTEREST_ON_LOANS,
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
                    AccrualAccountsForLoan.INCOME_FROM_FEES.getValue(), loanProductId, loanId, transactionId, transactionDate, feesAmount,
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
                    AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), loanProductId, loanId, transactionId, transactionDate,
                    penaltiesAmount, !isReversal, chargePaymentDTOs);
        }

        if (overPaymentAmount != null && overPaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            this.helper.createCreditJournalEntryOrReversalForLoan(office, currencyCode, AccrualAccountsForLoan.OVERPAYMENT, loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, overPaymentAmount, !isReversal);
        }

        /*** create a single debit entry (or reversal) for the entire amount **/
        this.helper.createDebitJournalEntryOrReversalForLoan(office, currencyCode, AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount, !isReversal);

    }
}
