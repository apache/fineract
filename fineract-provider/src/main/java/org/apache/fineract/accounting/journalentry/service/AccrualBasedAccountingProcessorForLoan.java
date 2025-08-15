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
import org.apache.fineract.accounting.journalentry.data.GLAccountBalanceHolder;
import org.apache.fineract.accounting.journalentry.data.LoanDTO;
import org.apache.fineract.accounting.journalentry.data.LoanTransactionDTO;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccrualBasedAccountingProcessorForLoan implements AccountingProcessorForLoan {

    private final AccountingProcessorHelper helper;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;

    @Override
    public void createJournalEntriesForLoan(final LoanDTO loanDTO) {
        final Long officeId = loanDTO.getOfficeId();
        final GLClosure latestGLClosure = this.helper.getLatestClosureByBranch(officeId);
        final Office office = this.helper.getOfficeById(officeId);
        for (final LoanTransactionDTO loanTransactionDTO : loanDTO.getNewLoanTransactions()) {
            final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
            this.helper.checkForBranchClosures(latestGLClosure, transactionDate);
            final LoanTransactionEnumData transactionType = loanTransactionDTO.getTransactionType();

            if (loanTransactionDTO.isReversed()) {
                journalEntryWritePlatformService.createJournalEntryForReversedLoanTransaction(transactionDate,
                        loanTransactionDTO.getTransactionId(), officeId);
                continue;
            }

            // Handle Disbursements
            if (transactionType.isDisbursement()) {
                createJournalEntriesForDisbursements(loanDTO, loanTransactionDTO, office);
            }

            // Handle Accruals
            if (transactionType.isAccrual() || transactionType.isAccrualAdjustment()) {
                createJournalEntriesForAccruals(loanDTO, loanTransactionDTO, office);
            }

            /*
             * Handle repayments, loan refunds, repayments at disbursement (except charge adjustment)
             */
            else if ((transactionType.isRepaymentType() && !transactionType.isChargeAdjustment())
                    || transactionType.isRepaymentAtDisbursement() || transactionType.isChargePayment()) {
                createJournalEntriesForRepaymentsAndWriteOffs(loanDTO, loanTransactionDTO, office, false,
                        transactionType.isRepaymentAtDisbursement());
            }

            // Logic for handling recovery payments
            else if (transactionType.isRecoveryRepayment()) {
                createJournalEntriesForRecoveryRepayments(loanDTO, loanTransactionDTO, office);
            }

            // Logic for Refunds of Overpayments
            else if (transactionType.isRefund()) {
                createJournalEntriesForRefund(loanDTO, loanTransactionDTO, office);
            }

            // Logic for Credit Balance Refunds
            else if (transactionType.isCreditBalanceRefund()) {
                createJournalEntriesForCreditBalanceRefund(loanDTO, loanTransactionDTO, office);
            }

            // Handle Write Offs
            else if ((transactionType.isWriteOff() || transactionType.isWaiveInterest() || transactionType.isWaiveCharges())) {
                createJournalEntriesForRepaymentsAndWriteOffs(loanDTO, loanTransactionDTO, office, true, false);
            }

            // Logic for Refunds of Active Loans
            else if (transactionType.isRefundForActiveLoans()) {
                createJournalEntriesForRefundForActiveLoan(loanDTO, loanTransactionDTO, office);
            }
            // Logic for Chargebacks
            else if (transactionType.isChargeback()) {
                createJournalEntriesForChargeback(loanDTO, loanTransactionDTO, office);
            }
            // Logic for Charge Adjustment
            else if (transactionType.isChargeAdjustment()) {
                createJournalEntriesForChargeAdjustment(loanDTO, loanTransactionDTO, office);
            }
            // Logic for Charge-Off
            else if (transactionType.isChargeoff()) {
                createJournalEntriesForChargeOff(loanDTO, loanTransactionDTO, office);
            }
            // Logic for Interest Payment Waiver
            else if (transactionType.isInterestPaymentWaiver() || transactionType.isInterestRefund()) {
                createJournalEntriesForInterestPaymentWaiverOrInterestRefund(loanDTO, loanTransactionDTO, office);
            }
            // Handle Capitalized Income
            if (transactionType.isCapitalizedIncome()) {
                createJournalEntriesForCapitalizedIncome(loanDTO, loanTransactionDTO, office);
            }
            // Handle Capitalized Income Amortization
            if (transactionType.isCapitalizedIncomeAmortization()) {
                createJournalEntriesForCapitalizedIncomeAmortization(loanDTO, loanTransactionDTO, office);
            }
            // Handle Capitalized Income Adjustment
            if (transactionType.isCapitalizedIncomeAdjustment()) {
                createJournalEntriesForCapitalizedIncomeAdjustment(loanDTO, loanTransactionDTO, office);
            }
            // Capitalized Income Amortization Adjustment
            if (transactionType.isCapitalizedIncomeAmortizationAdjustment()) {
                createJournalEntriesForCapitalizedIncomeAmortizationAdjustment(loanDTO, loanTransactionDTO, office);
            }
            // Handle Buy Down Fee
            if (transactionType.isBuyDownFee()) {
                createJournalEntriesForBuyDownFee(loanDTO, loanTransactionDTO, office);
            }
            // Handle Buy Down Fee Adjustment
            if (transactionType.isBuyDownFeeAdjustment()) {
                createJournalEntriesForBuyDownFeeAdjustment(loanDTO, loanTransactionDTO, office);
            }
            // Handle Buy Down Fee Amortization
            if (transactionType.isBuyDownFeeAmortization()) {
                createJournalEntriesForBuyDownFeeAmortization(loanDTO, loanTransactionDTO, office);
            }
            // Handle Buy Down Fee Amortization Adjustment
            if (transactionType.isBuyDownFeeAmortizationAdjustment()) {
                createJournalEntriesForBuyDownFeeAmortizationAdjustment(loanDTO, loanTransactionDTO, office);
            }
        }
    }

    private void createJournalEntriesForCapitalizedIncome(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();
        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        final GLAccountBalanceHolder glAccountBalanceHolder = new GLAccountBalanceHolder();

        if (MathUtil.isGreaterThanZero(principalAmount)) {
            populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                    AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                    glAccountBalanceHolder);
        }

        // create credit entries
        for (Map.Entry<Long, BigDecimal> creditEntry : glAccountBalanceHolder.getCreditBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(creditEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(creditEntry.getKey());
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        creditEntry.getValue(), glAccount);
            }
        }
        // create debit entries
        for (Map.Entry<Long, BigDecimal> debitEntry : glAccountBalanceHolder.getDebitBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(debitEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(debitEntry.getKey());
                this.helper.createDebitJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        debitEntry.getValue(), glAccount);
            }
        }
    }

    private void createJournalEntriesForCapitalizedIncomeAdjustment(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();
        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal transactionAmount = loanTransactionDTO.getAmount();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final BigDecimal overPaymentAmount = loanTransactionDTO.getOverPayment();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        final GLAccountBalanceHolder glAccountBalanceHolder = new GLAccountBalanceHolder();

        if (MathUtil.isGreaterThanZero(transactionAmount)) {
            // Resolve Credit
            // handle principal payment
            if (MathUtil.isGreaterThanZero(principalAmount)) {
                GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                        AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), paymentTypeId);
                glAccountBalanceHolder.addToCredit(account, principalAmount);
            }
            // handle interest payment
            if (MathUtil.isGreaterThanZero(interestAmount)) {
                GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                        AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(), paymentTypeId);
                glAccountBalanceHolder.addToCredit(account, interestAmount);
            }
            // handle fee payment
            if (MathUtil.isGreaterThanZero(feesAmount)) {
                GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                        AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(), paymentTypeId);
                glAccountBalanceHolder.addToCredit(account, feesAmount);
            }
            // handle penalty payment
            if (MathUtil.isGreaterThanZero(penaltiesAmount)) {
                GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                        AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(), paymentTypeId);
                glAccountBalanceHolder.addToCredit(account, penaltiesAmount);
            }
            // handle overpayment
            if (MathUtil.isGreaterThanZero(overPaymentAmount)) {
                GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                        AccrualAccountsForLoan.OVERPAYMENT.getValue(), paymentTypeId);
                glAccountBalanceHolder.addToCredit(account, overPaymentAmount);
            }

            // Resolve Debit
            GLAccount accountDeferredIncome = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), paymentTypeId);

            glAccountBalanceHolder.addToDebit(accountDeferredIncome, transactionAmount);
        }

        // create credit entries
        for (Map.Entry<Long, BigDecimal> creditEntry : glAccountBalanceHolder.getCreditBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(creditEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(creditEntry.getKey());
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        creditEntry.getValue(), glAccount);
            }
        }
        // create debit entries
        for (Map.Entry<Long, BigDecimal> debitEntry : glAccountBalanceHolder.getDebitBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(debitEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(debitEntry.getKey());
                this.helper.createDebitJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        debitEntry.getValue(), glAccount);
            }
        }
    }

    private void createJournalEntriesForCapitalizedIncomeAmortization(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        final boolean isMarkedAsChargeOff = loanDTO.isMarkedAsChargeOff();
        if (isMarkedAsChargeOff) {
            createJournalEntriesForChargeOffLoanCapitalizedIncomeAmortization(loanDTO, loanTransactionDTO, office);
        } else {
            createJournalEntriesForLoanCapitalizedIncomeAmortization(loanDTO, loanTransactionDTO, office);
        }
    }

    private void createJournalEntriesForLoanCapitalizedIncomeAmortization(final LoanDTO loanDTO,
            final LoanTransactionDTO loanTransactionDTO, final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();
        final boolean isLoanWrittenOff = loanDTO.isMarkedAsWrittenOff();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        final GLAccountBalanceHolder glAccountBalanceHolder = new GLAccountBalanceHolder();

        // interest payment
        final AccrualAccountsForLoan creditAccountType = isLoanWrittenOff ? AccrualAccountsForLoan.LOSSES_WRITTEN_OFF
                : AccrualAccountsForLoan.INCOME_FROM_CAPITALIZATION;
        if (MathUtil.isGreaterThanZero(interestAmount)) {
            populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, creditAccountType.getValue(),
                    AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), glAccountBalanceHolder);
        }
        // handle fees payment
        if (MathUtil.isGreaterThanZero(feesAmount)) {
            populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, creditAccountType.getValue(),
                    AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), glAccountBalanceHolder);
        }

        // create credit entries
        for (Map.Entry<Long, BigDecimal> creditEntry : glAccountBalanceHolder.getCreditBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(creditEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(creditEntry.getKey());
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        creditEntry.getValue(), glAccount);
            }
        }
        // create debit entries
        for (Map.Entry<Long, BigDecimal> debitEntry : glAccountBalanceHolder.getDebitBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(debitEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(debitEntry.getKey());
                this.helper.createDebitJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        debitEntry.getValue(), glAccount);
            }
        }
    }

    private void createJournalEntriesForChargeOffLoanCapitalizedIncomeAmortization(final LoanDTO loanDTO,
            final LoanTransactionDTO loanTransactionDTO, final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final boolean isMarkedFraud = loanDTO.isMarkedAsFraud();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        final GLAccountBalanceHolder glAccountBalanceHolder = new GLAccountBalanceHolder();
        final Long chargeOffReasonCodeValue = loanDTO.getChargeOffReasonCodeValue();

        final ProductToGLAccountMapping mapping = chargeOffReasonCodeValue != null
                ? helper.getChargeOffMappingByCodeValue(loanProductId, PortfolioProductType.LOAN, chargeOffReasonCodeValue)
                : null;

        if (mapping != null) {
            final GLAccount accountDebit = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), paymentTypeId);
            // handle interest payment
            if (MathUtil.isGreaterThanZero(interestAmount)) {
                glAccountBalanceHolder.addToCredit(mapping.getGlAccount(), interestAmount);
                glAccountBalanceHolder.addToDebit(accountDebit, interestAmount);
            }
            // handle fees payment
            if (MathUtil.isGreaterThanZero(feesAmount)) {
                glAccountBalanceHolder.addToCredit(mapping.getGlAccount(), feesAmount);
                glAccountBalanceHolder.addToDebit(accountDebit, feesAmount);
            }
        } else {
            final AccrualAccountsForLoan creditAccountType = isMarkedFraud ? AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE
                    : AccrualAccountsForLoan.CHARGE_OFF_EXPENSE;
            // handle interest payment
            if (MathUtil.isGreaterThanZero(interestAmount)) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, creditAccountType.getValue(),
                        AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), glAccountBalanceHolder);
            }
            // handle fees payment
            if (MathUtil.isGreaterThanZero(feesAmount)) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, creditAccountType.getValue(),
                        AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), glAccountBalanceHolder);
            }
        }

        // create credit entries
        for (Map.Entry<Long, BigDecimal> creditEntry : glAccountBalanceHolder.getCreditBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(creditEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(creditEntry.getKey());
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        creditEntry.getValue(), glAccount);
            }
        }
        // create debit entries
        for (Map.Entry<Long, BigDecimal> debitEntry : glAccountBalanceHolder.getDebitBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(debitEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(debitEntry.getKey());
                this.helper.createDebitJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        debitEntry.getValue(), glAccount);
            }
        }
    }

    private void createJournalEntriesForCapitalizedIncomeAmortizationAdjustment(final LoanDTO loanDTO,
            final LoanTransactionDTO loanTransactionDTO, final Office office) {
        GLAccountBalanceHolder glAccountBalanceHolder = new GLAccountBalanceHolder();
        if (MathUtil.isGreaterThanZero(loanTransactionDTO.getAmount())) {
            populateCreditDebitMaps(loanDTO.getLoanProductId(), loanTransactionDTO.getAmount(), loanTransactionDTO.getPaymentTypeId(),
                    AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(),
                    AccrualAccountsForLoan.INCOME_FROM_CAPITALIZATION.getValue(), glAccountBalanceHolder);
        }

        // create credit entries
        for (Map.Entry<Long, BigDecimal> creditEntry : glAccountBalanceHolder.getCreditBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(creditEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(creditEntry.getKey());
                this.helper.createCreditJournalEntryForLoan(office, loanDTO.getCurrencyCode(), loanDTO.getLoanId(),
                        loanTransactionDTO.getTransactionId(), loanTransactionDTO.getTransactionDate(), creditEntry.getValue(), glAccount);
            }
        }
        // create debit entries
        for (Map.Entry<Long, BigDecimal> debitEntry : glAccountBalanceHolder.getDebitBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(debitEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(debitEntry.getKey());
                this.helper.createDebitJournalEntryForLoan(office, loanDTO.getCurrencyCode(), loanDTO.getLoanId(),
                        loanTransactionDTO.getTransactionId(), loanTransactionDTO.getTransactionDate(), debitEntry.getValue(), glAccount);
            }
        }
    }

    private void createJournalEntriesForBuyDownFee(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal amount = loanTransactionDTO.getAmount();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        final AccrualAccountsForLoan debitAccountType = loanDTO.isMerchantBuyDownFee() ? AccrualAccountsForLoan.BUY_DOWN_EXPENSE
                : AccrualAccountsForLoan.FUND_SOURCE;
        if (MathUtil.isGreaterThanZero(amount)) {
            this.helper.createJournalEntriesForLoan(office, currencyCode, debitAccountType.getValue(),
                    AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                    transactionDate, amount);
        }
    }

    private void createJournalEntriesForBuyDownFeeAdjustment(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal amount = loanTransactionDTO.getAmount();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        if (MathUtil.isGreaterThanZero(amount)) {
            // Mirror of Buy Down Fee entries (as per PS-2574 requirements)
            // Debit: Deferred Income Liability, Credit: Buy Down Expense
            this.helper.createJournalEntriesForLoan(office, currencyCode, AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(),
                    AccrualAccountsForLoan.BUY_DOWN_EXPENSE.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                    transactionDate, amount);
        }
    }

    private void createJournalEntriesForBuyDownFeeAmortization(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        final boolean isMarkedAsChargeOff = loanDTO.isMarkedAsChargeOff();
        if (isMarkedAsChargeOff) {
            createJournalEntriesForChargeOffLoanBuyDownFeeAmortization(loanDTO, loanTransactionDTO, office);
        } else {
            createJournalEntriesForLoanBuyDownFeeAmortization(loanDTO, loanTransactionDTO, office);
        }
    }

    private void createJournalEntriesForLoanBuyDownFeeAmortization(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();
        final boolean isLoanWrittenOff = loanDTO.isMarkedAsWrittenOff();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        final GLAccountBalanceHolder glAccountBalanceHolder = new GLAccountBalanceHolder();

        // interest payment
        final AccrualAccountsForLoan creditAccountType = isLoanWrittenOff ? AccrualAccountsForLoan.LOSSES_WRITTEN_OFF
                : AccrualAccountsForLoan.INCOME_FROM_BUY_DOWN;
        if (MathUtil.isGreaterThanZero(interestAmount)) {
            populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, creditAccountType.getValue(),
                    AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), glAccountBalanceHolder);
        }
        // handle fees payment
        if (MathUtil.isGreaterThanZero(feesAmount)) {
            populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, creditAccountType.getValue(),
                    AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), glAccountBalanceHolder);
        }

        // create credit entries
        for (Map.Entry<Long, BigDecimal> creditEntry : glAccountBalanceHolder.getCreditBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(creditEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(creditEntry.getKey());
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        creditEntry.getValue(), glAccount);
            }
        }
        // create debit entries
        for (Map.Entry<Long, BigDecimal> debitEntry : glAccountBalanceHolder.getDebitBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(debitEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(debitEntry.getKey());
                this.helper.createDebitJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        debitEntry.getValue(), glAccount);
            }
        }
    }

    private void createJournalEntriesForChargeOffLoanBuyDownFeeAmortization(final LoanDTO loanDTO,
            final LoanTransactionDTO loanTransactionDTO, final Office office) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final boolean isMarkedFraud = loanDTO.isMarkedAsFraud();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        final GLAccountBalanceHolder glAccountBalanceHolder = new GLAccountBalanceHolder();
        final Long chargeOffReasonCodeValue = loanDTO.getChargeOffReasonCodeValue();

        final ProductToGLAccountMapping mapping = chargeOffReasonCodeValue != null
                ? helper.getChargeOffMappingByCodeValue(loanProductId, PortfolioProductType.LOAN, chargeOffReasonCodeValue)
                : null;

        if (mapping != null) {
            final GLAccount accountDebit = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), paymentTypeId);
            // handle interest payment
            if (MathUtil.isGreaterThanZero(interestAmount)) {
                glAccountBalanceHolder.addToCredit(mapping.getGlAccount(), interestAmount);
                glAccountBalanceHolder.addToDebit(accountDebit, interestAmount);
            }
            // handle fees payment
            if (MathUtil.isGreaterThanZero(feesAmount)) {
                glAccountBalanceHolder.addToCredit(mapping.getGlAccount(), feesAmount);
                glAccountBalanceHolder.addToDebit(accountDebit, feesAmount);
            }
        } else {
            final AccrualAccountsForLoan creditAccountType = isMarkedFraud ? AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE
                    : AccrualAccountsForLoan.CHARGE_OFF_EXPENSE;
            // handle interest payment
            if (MathUtil.isGreaterThanZero(interestAmount)) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, creditAccountType.getValue(),
                        AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), glAccountBalanceHolder);
            }
            // handle fees payment
            if (MathUtil.isGreaterThanZero(feesAmount)) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, creditAccountType.getValue(),
                        AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), glAccountBalanceHolder);
            }
        }

        // create credit entries
        for (Map.Entry<Long, BigDecimal> creditEntry : glAccountBalanceHolder.getCreditBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(creditEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(creditEntry.getKey());
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        creditEntry.getValue(), glAccount);
            }
        }
        // create debit entries
        for (Map.Entry<Long, BigDecimal> debitEntry : glAccountBalanceHolder.getDebitBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(debitEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(debitEntry.getKey());
                this.helper.createDebitJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        debitEntry.getValue(), glAccount);
            }
        }
    }

    private void createJournalEntriesForBuyDownFeeAmortizationAdjustment(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {
        GLAccountBalanceHolder glAccountBalanceHolder = new GLAccountBalanceHolder();
        if (MathUtil.isGreaterThanZero(loanTransactionDTO.getAmount())) {
            populateCreditDebitMaps(loanDTO.getLoanProductId(), loanTransactionDTO.getAmount(), loanTransactionDTO.getPaymentTypeId(),
                    AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), AccrualAccountsForLoan.INCOME_FROM_BUY_DOWN.getValue(),
                    glAccountBalanceHolder);
        }

        // create credit entries
        for (Map.Entry<Long, BigDecimal> creditEntry : glAccountBalanceHolder.getCreditBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(creditEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(creditEntry.getKey());
                this.helper.createCreditJournalEntryForLoan(office, loanDTO.getCurrencyCode(), loanDTO.getLoanId(),
                        loanTransactionDTO.getTransactionId(), loanTransactionDTO.getTransactionDate(), creditEntry.getValue(), glAccount);
            }
        }
        // create debit entries
        for (Map.Entry<Long, BigDecimal> debitEntry : glAccountBalanceHolder.getDebitBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(debitEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(debitEntry.getKey());
                this.helper.createDebitJournalEntryForLoan(office, loanDTO.getCurrencyCode(), loanDTO.getLoanId(),
                        loanTransactionDTO.getTransactionId(), loanTransactionDTO.getTransactionDate(), debitEntry.getValue(), glAccount);
            }
        }
    }

    private void createJournalEntriesForInterestPaymentWaiverOrInterestRefund(LoanDTO loanDTO, LoanTransactionDTO loanTransactionDTO,
            Office office) {
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();
        final String currencyCode = loanDTO.getCurrencyCode();
        final boolean isMarkedAsChargeOff = loanDTO.isMarkedAsChargeOff();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final LocalDate transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final BigDecimal overPayment = loanTransactionDTO.getOverPayment();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();
        GLAccountBalanceHolder glAccountBalanceHolder = new GLAccountBalanceHolder();

        if (isMarkedAsChargeOff) {
            // ChargeOFF
            // principal payment
            if (MathUtil.isGreaterThanZero(principalAmount)) {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(),
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), glAccountBalanceHolder);
            }
            // interest payment
            if (MathUtil.isGreaterThanZero(interestAmount)) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(),
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), glAccountBalanceHolder);
            }
            // handle fees payment
            if (MathUtil.isGreaterThanZero(feesAmount)) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(),
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), glAccountBalanceHolder);
            }

            // handle penalty payment
            if (MathUtil.isGreaterThanZero(penaltiesAmount)) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(),
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), glAccountBalanceHolder);
            }
            // handle overpayment
            if (MathUtil.isGreaterThanZero(overPayment)) {
                populateCreditDebitMaps(loanProductId, overPayment, paymentTypeId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), glAccountBalanceHolder);
            }
        } else {
            // principal payment
            if (MathUtil.isGreaterThanZero(principalAmount)) {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), glAccountBalanceHolder);
            }
            // interest payment
            if (MathUtil.isGreaterThanZero(interestAmount)) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(),
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), glAccountBalanceHolder);
            }
            // handle fees payment
            if (MathUtil.isGreaterThanZero(feesAmount)) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(),
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), glAccountBalanceHolder);
            }

            // handle penalty payment
            if (MathUtil.isGreaterThanZero(penaltiesAmount)) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(), AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(),
                        glAccountBalanceHolder);
            }
            // handle overpayment
            if (MathUtil.isGreaterThanZero(overPayment)) {
                populateCreditDebitMaps(loanProductId, overPayment, paymentTypeId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), glAccountBalanceHolder);
            }
        }
        // create credit entries
        for (Map.Entry<Long, BigDecimal> creditEntry : glAccountBalanceHolder.getCreditBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(creditEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(creditEntry.getKey());
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        creditEntry.getValue(), glAccount);
            }
        }
        // create debit entries
        for (Map.Entry<Long, BigDecimal> debitEntry : glAccountBalanceHolder.getDebitBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(debitEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(debitEntry.getKey());
                this.helper.createDebitJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        debitEntry.getValue(), glAccount);
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
        GLAccountBalanceHolder glAccountBalanceHolder = new GLAccountBalanceHolder();

        // need to fetch if there are account mappings (always one)
        Long chargeOffReasonCodeValue = loanDTO.getChargeOffReasonCodeValue();

        ProductToGLAccountMapping mapping = chargeOffReasonCodeValue != null
                ? helper.getChargeOffMappingByCodeValue(loanProductId, PortfolioProductType.LOAN, chargeOffReasonCodeValue)
                : null;

        if (MathUtil.isGreaterThanZero(principalAmount)) {
            if (mapping != null) {
                GLAccount accountCredit = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                        AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), paymentTypeId);
                glAccountBalanceHolder.addToCredit(accountCredit, principalAmount);
                glAccountBalanceHolder.addToDebit(mapping.getGlAccount(), principalAmount);
            } else {
                // principal payment
                if (isMarkedFraud) {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                            AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), glAccountBalanceHolder);
                } else {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                            AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), glAccountBalanceHolder);
                }
            }
        }
        // interest payment
        if (MathUtil.isGreaterThanZero(interestAmount)) {
            populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(),
                    AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), glAccountBalanceHolder);
        }
        // handle fees payment
        if (MathUtil.isGreaterThanZero(feesAmount)) {
            populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(),
                    AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), glAccountBalanceHolder);
        }
        // handle penalty payment
        if (MathUtil.isGreaterThanZero(penaltiesAmount)) {
            populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId, AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(),
                    AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), glAccountBalanceHolder);
        }
        // create credit entries
        for (Map.Entry<Long, BigDecimal> creditEntry : glAccountBalanceHolder.getCreditBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(creditEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(creditEntry.getKey());
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        creditEntry.getValue(), glAccount);
            }
        }
        // create debit entries
        for (Map.Entry<Long, BigDecimal> debitEntry : glAccountBalanceHolder.getDebitBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(debitEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(debitEntry.getKey());
                this.helper.createDebitJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        debitEntry.getValue(), glAccount);
            }
        }
    }

    private void populateCreditDebitMaps(Long loanProductId, BigDecimal transactionPartAmount, Long paymentTypeId,
            Integer creditAccountType, Integer debitAccountType, GLAccountBalanceHolder glAccountBalanceHolder) {
        if (MathUtil.isGreaterThanZero(transactionPartAmount)) {
            // Resolve Credit
            GLAccount accountCredit = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, creditAccountType, paymentTypeId);
            glAccountBalanceHolder.addToCredit(accountCredit, transactionPartAmount);
            // Resolve Debit
            GLAccount accountDebit = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, debitAccountType, paymentTypeId);
            glAccountBalanceHolder.addToDebit(accountDebit, transactionPartAmount);
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

        BigDecimal totalDebitAmount = new BigDecimal(0);

        Map<GLAccount, BigDecimal> accountMap = new LinkedHashMap<>();

        // handle principal payment
        if (MathUtil.isGreaterThanZero(principalAmount)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), paymentTypeId);
            accountMap.put(account, principalAmount);
        }

        // handle interest payment
        if (MathUtil.isGreaterThanZero(interestAmount)) {
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
        if (MathUtil.isGreaterThanZero(feesAmount)) {
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
        if (MathUtil.isGreaterThanZero(penaltiesAmount)) {
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
        if (MathUtil.isGreaterThanZero(overPaymentAmount)) {
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
            if (MathUtil.isGreaterThanZero(entry.getValue())) {
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate, entry.getValue(),
                        entry.getKey());
            }
        }

        if (MathUtil.isGreaterThanZero(totalDebitAmount)) {
            Long chargeId = loanTransactionDTO.getLoanChargeData().getChargeId();
            Integer accountMappingTypeId;
            if (loanTransactionDTO.getLoanChargeData().isPenalty()) {
                accountMappingTypeId = AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue();
            } else {
                accountMappingTypeId = AccrualAccountsForLoan.INCOME_FROM_FEES.getValue();
            }
            this.helper.createDebitJournalEntryForLoanCharges(office, currencyCode, accountMappingTypeId, loanProductId, chargeId, loanId,
                    transactionId, transactionDate, totalDebitAmount);
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

        BigDecimal totalDebitAmount = new BigDecimal(0);

        Map<GLAccount, BigDecimal> accountMap = new LinkedHashMap<>();

        // handle principal payment
        if (MathUtil.isGreaterThanZero(principalAmount)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), paymentTypeId);
            accountMap.put(account, principalAmount);
        }

        // handle interest payment
        if (MathUtil.isGreaterThanZero(interestAmount)) {
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

        // handle fees payment
        if (MathUtil.isGreaterThanZero(feesAmount)) {
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

        // handle penalties payment
        if (MathUtil.isGreaterThanZero(penaltiesAmount)) {
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
        if (MathUtil.isGreaterThanZero(overPaymentAmount)) {
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
            if (MathUtil.isGreaterThanZero(entry.getValue())) {
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate, entry.getValue(),
                        entry.getKey());
            }
        }

        if (MathUtil.isGreaterThanZero(totalDebitAmount)) {
            Long chargeId = loanTransactionDTO.getLoanChargeData().getChargeId();
            Integer accountMappingTypeId;
            if (loanTransactionDTO.getLoanChargeData().isPenalty()) {
                accountMappingTypeId = AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue();
            } else {
                accountMappingTypeId = AccrualAccountsForLoan.INCOME_FROM_FEES.getValue();
            }
            this.helper.createDebitJournalEntryForLoanCharges(office, currencyCode, accountMappingTypeId, loanProductId, chargeId, loanId,
                    transactionId, transactionDate, totalDebitAmount);
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

        if (MathUtil.isGreaterThanZero(amount)) {
            helper.createCreditJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.FUND_SOURCE, loanProductId, paymentTypeId,
                    loanId, transactionId, transactionDate, amount);
        }

        if (MathUtil.isGreaterThanZero(overpaidAmount)) {
            helper.createDebitJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.OVERPAYMENT.getValue(), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, overpaidAmount);
        }

        if (principalCredited.compareTo(principalPaid) > 0) {
            helper.createDebitJournalEntryForLoan(office, currencyCode, getPrincipalAccount(loanDTO), loanProductId, paymentTypeId, loanId,
                    transactionId, transactionDate, principalCredited.subtract(principalPaid));
        } else if (principalCredited.compareTo(principalPaid) < 0) {
            helper.createCreditJournalEntryForLoan(office, currencyCode, getPrincipalAccount(loanDTO), loanProductId, paymentTypeId, loanId,
                    transactionId, transactionDate, principalPaid.subtract(principalCredited));
        }

        if (feeCredited.compareTo(feePaid) > 0) {
            helper.createDebitJournalEntryForLoan(office, currencyCode, getFeeAccount(loanDTO), loanProductId, paymentTypeId, loanId,
                    transactionId, transactionDate, feeCredited.subtract(feePaid));
        } else if (feeCredited.compareTo(feePaid) < 0) {
            helper.createCreditJournalEntryForLoan(office, currencyCode, getFeeAccount(loanDTO), loanProductId, paymentTypeId, loanId,
                    transactionId, transactionDate, feePaid.subtract(feeCredited));
        }

        if (penaltyCredited.compareTo(penaltyPaid) > 0) {
            helper.createDebitJournalEntryForLoan(office, currencyCode, getPenaltyAccount(loanDTO), loanProductId, paymentTypeId, loanId,
                    transactionId, transactionDate, penaltyCredited.subtract(penaltyPaid));
        } else if (penaltyCredited.compareTo(penaltyPaid) < 0) {
            helper.createCreditJournalEntryForLoan(office, currencyCode, getPenaltyAccount(loanDTO), loanProductId, paymentTypeId, loanId,
                    transactionId, transactionDate, penaltyPaid.subtract(penaltyCredited));
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
        final BigDecimal loanTransactionDTOAmount = loanTransactionDTO.getAmount();
        final BigDecimal principalPortion = loanTransactionDTOAmount.subtract(overpaymentPortion);
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        // create journal entries for the disbursement
        if (MathUtil.isGreaterThanZero(principalPortion)) {
            this.helper.createDebitJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, principalPortion);

        }
        if (MathUtil.isGreaterThanZero(overpaymentPortion)) {
            this.helper.createDebitJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.OVERPAYMENT.getValue(), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, overpaymentPortion);
        }
        if (MathUtil.isGreaterThanZero(loanTransactionDTOAmount)) {
            if (loanTransactionDTO.isLoanToLoanTransfer()) {
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, FinancialActivity.ASSET_TRANSFER.getValue(),
                        loanProductId, paymentTypeId, loanId, transactionId, transactionDate, loanTransactionDTOAmount);
            } else if (loanTransactionDTO.isAccountTransfer()) {
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, FinancialActivity.LIABILITY_TRANSFER.getValue(),
                        loanProductId, paymentTypeId, loanId, transactionId, transactionDate, loanTransactionDTOAmount);
            } else {
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        loanProductId, paymentTypeId, loanId, transactionId, transactionDate, loanTransactionDTOAmount);
            }
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
        GLAccountBalanceHolder glAccountBalanceHolder = new GLAccountBalanceHolder();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        // principal payment
        if (MathUtil.isGreaterThanZero(principalAmount)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                if (isMarkedFraud) {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                            AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            glAccountBalanceHolder);
                } else {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                            AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            glAccountBalanceHolder);
                }
            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                if (isMarkedFraud) {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                            AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            glAccountBalanceHolder);

                } else {
                    populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                            AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            glAccountBalanceHolder);
                }

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), AccrualAccountsForLoan.GOODWILL_CREDIT.getValue(),
                        glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        glAccountBalanceHolder);

            } else {
                populateCreditDebitMaps(loanProductId, principalAmount, paymentTypeId, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), glAccountBalanceHolder);
            }

        }

        // interest payment
        if (MathUtil.isGreaterThanZero(interestAmount)) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        glAccountBalanceHolder);

            } else {
                populateCreditDebitMaps(loanProductId, interestAmount, paymentTypeId, AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), glAccountBalanceHolder);
            }

        }

        // handle fees payment
        if (MathUtil.isGreaterThanZero(feesAmount)) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), glAccountBalanceHolder);

            } else {
                if (isIncomeFromFee) {
                    this.helper.createCreditJournalEntryForLoanCharges(office, currencyCode,
                            AccrualAccountsForLoan.INCOME_FROM_FEES.getValue(), loanProductId, loanId, transactionId, transactionDate,
                            feesAmount, loanTransactionDTO.getFeePayments());
                    GLAccount debitAccount = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                            AccrualAccountsForLoan.FUND_SOURCE.getValue(), paymentTypeId);
                    glAccountBalanceHolder.addToDebit(debitAccount, feesAmount);

                } else {
                    populateCreditDebitMaps(loanProductId, feesAmount, paymentTypeId, AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(),
                            AccrualAccountsForLoan.FUND_SOURCE.getValue(), glAccountBalanceHolder);
                }

            }

        }

        // handle penalties
        if (MathUtil.isGreaterThanZero(penaltiesAmount)) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isRepayment()) {
                populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        glAccountBalanceHolder);

            } else {
                if (isIncomeFromFee) {
                    populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                            AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            glAccountBalanceHolder);
                } else {
                    populateCreditDebitMaps(loanProductId, penaltiesAmount, paymentTypeId,
                            AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(), AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                            glAccountBalanceHolder);
                }
            }

        }

        // overpayment
        if (MathUtil.isGreaterThanZero(overPaymentAmount)) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            if (loanTransactionDTO.getTransactionType().isMerchantIssuedRefund()) {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isPayoutRefund()) {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), glAccountBalanceHolder);

            } else if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        AccrualAccountsForLoan.GOODWILL_CREDIT.getValue(), glAccountBalanceHolder);

            } else {
                populateCreditDebitMaps(loanProductId, overPaymentAmount, paymentTypeId, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), glAccountBalanceHolder);
            }

        }

        // create credit entries
        for (Map.Entry<Long, BigDecimal> creditEntry : glAccountBalanceHolder.getCreditBalances().entrySet()) {
            if (MathUtil.isGreaterThanZero(creditEntry.getValue())) {
                GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(creditEntry.getKey());
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                        creditEntry.getValue(), glAccount);
            }
        }

        if (MathUtil.isGreaterThanZero(totalDebitAmount)) {
            if (writeOff) {
                this.helper.createDebitJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.LOSSES_WRITTEN_OFF.getValue(),
                        loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount);
            } else {
                if (loanTransactionDTO.isLoanToLoanTransfer()) {
                    this.helper.createDebitJournalEntryForLoan(office, currencyCode, FinancialActivity.ASSET_TRANSFER.getValue(),
                            loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount);
                } else if (loanTransactionDTO.isAccountTransfer()) {
                    this.helper.createDebitJournalEntryForLoan(office, currencyCode, FinancialActivity.LIABILITY_TRANSFER.getValue(),
                            loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount);
                } else {
                    // create debit entries
                    for (Map.Entry<Long, BigDecimal> debitEntry : glAccountBalanceHolder.getDebitBalances().entrySet()) {
                        GLAccount glAccount = glAccountBalanceHolder.getGlAccountMap().get(debitEntry.getKey());
                        this.helper.createDebitJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate,
                                debitEntry.getValue(), glAccount);
                    }
                }
            }
        }

        /**
         * Charge Refunds have an extra refund related pair of journal entries in addition to those related to the
         * repayment above
         ***/
        if (MathUtil.isGreaterThanZero(totalDebitAmount)) {
            if (loanTransactionDTO.getTransactionType().isChargeRefund()) {
                Integer incomeAccount = this.helper.getValueForFeeOrPenaltyIncomeAccount(loanTransactionDTO.getChargeRefundChargeType());
                this.helper.createJournalEntriesForLoan(office, currencyCode, incomeAccount, AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                        loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount);
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

        BigDecimal totalDebitAmount = new BigDecimal(0);

        Map<GLAccount, BigDecimal> accountMap = new LinkedHashMap<>();
        Map<Integer, BigDecimal> debitAccountMapForGoodwillCredit = new LinkedHashMap<>();

        // handle principal payment or writeOff
        if (MathUtil.isGreaterThanZero(principalAmount)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), paymentTypeId);
            accountMap.put(account, principalAmount);
            if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                populateDebitAccountEntry(loanProductId, principalAmount, AccrualAccountsForLoan.GOODWILL_CREDIT.getValue(),
                        debitAccountMapForGoodwillCredit, paymentTypeId);
            }
        }

        // handle interest payment of writeOff
        if (MathUtil.isGreaterThanZero(interestAmount)) {
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

        // handle fees payment of writeOff
        if (MathUtil.isGreaterThanZero(feesAmount)) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);
            if (isIncomeFromFee) {
                this.helper.createCreditJournalEntryForLoanCharges(office, currencyCode, AccrualAccountsForLoan.INCOME_FROM_FEES.getValue(),
                        loanProductId, loanId, transactionId, transactionDate, feesAmount, loanTransactionDTO.getFeePayments());
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

        // handle penalties payment of writeOff
        if (MathUtil.isGreaterThanZero(penaltiesAmount)) {
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

        if (MathUtil.isGreaterThanZero(overPaymentAmount)) {
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
            if (MathUtil.isGreaterThanZero(entry.getValue())) {
                this.helper.createCreditJournalEntryForLoan(office, currencyCode, loanId, transactionId, transactionDate, entry.getValue(),
                        entry.getKey());
            }
        }

        /**
         * Single DEBIT transaction for write-offs or Repayments
         ***/
        if (MathUtil.isGreaterThanZero(totalDebitAmount)) {
            if (writeOff) {
                this.helper.createDebitJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.LOSSES_WRITTEN_OFF.getValue(),
                        loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount);
            } else {
                if (loanTransactionDTO.isLoanToLoanTransfer()) {
                    this.helper.createDebitJournalEntryForLoan(office, currencyCode, FinancialActivity.ASSET_TRANSFER.getValue(),
                            loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount);
                } else if (loanTransactionDTO.isAccountTransfer()) {
                    this.helper.createDebitJournalEntryForLoan(office, currencyCode, FinancialActivity.LIABILITY_TRANSFER.getValue(),
                            loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount);
                } else {
                    if (loanTransactionDTO.getTransactionType().isGoodwillCredit()) {
                        // create debit entries
                        for (Map.Entry<Integer, BigDecimal> debitEntry : debitAccountMapForGoodwillCredit.entrySet()) {
                            this.helper.createDebitJournalEntryForLoan(office, currencyCode, debitEntry.getKey().intValue(), loanProductId,
                                    paymentTypeId, loanId, transactionId, transactionDate, debitEntry.getValue());
                        }

                    } else {
                        this.helper.createDebitJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                                loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount);
                    }
                }
            }
        }

        /**
         * Charge Refunds have an extra refund related pair of journal entries in addition to those related to the
         * repayment above
         ***/
        if (MathUtil.isGreaterThanZero(totalDebitAmount) && loanTransactionDTO.getTransactionType().isChargeRefund()) {
            Integer incomeAccount = this.helper.getValueForFeeOrPenaltyIncomeAccount(loanTransactionDTO.getChargeRefundChargeType());
            this.helper.createJournalEntriesForLoan(office, currencyCode, incomeAccount, AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount);
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
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        if (MathUtil.isGreaterThanZero(amount)) {
            this.helper.createJournalEntriesForLoan(office, currencyCode, AccrualAccountsForLoan.FUND_SOURCE.getValue(),
                    AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                    transactionDate, amount);
        }
    }

    /**
     * Recognize the receivable interest <br/>
     * Debit "Interest Receivable" and Credit "Income from Interest"
     *
     * <b>Fees:</b> Debit <i>Fees Receivable</i> and credit <i>Income from Fees</i> <br/>
     *
     * <b>Penalties:</b> Debit <i>Penalties Receivable</i> and credit <i>Income from Penalties</i>
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
        final LoanTransactionEnumData transactionType = loanTransactionDTO.getTransactionType();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        // create journal entries for recognizing interest
        if (MathUtil.isGreaterThanZero(interestAmount)) {
            if (transactionType.isAccrualAdjustment()) {
                this.helper.createJournalEntriesForLoan(office, currencyCode, AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(),
                        AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                        transactionDate, interestAmount);
            } else {
                this.helper.createJournalEntriesForLoan(office, currencyCode, AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(),
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                        transactionDate, interestAmount);
            }
        }
        // create journal entries for the fees application
        if (MathUtil.isGreaterThanZero(feesAmount)) {
            if (transactionType.isAccrualAdjustment()) {
                this.helper.createJournalEntriesForLoanCharges(office, currencyCode, AccrualAccountsForLoan.INCOME_FROM_FEES.getValue(),
                        AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(), loanProductId, loanId, transactionId, transactionDate,
                        feesAmount, loanTransactionDTO.getFeePayments());
            } else {
                this.helper.createJournalEntriesForLoanCharges(office, currencyCode, AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_FEES.getValue(), loanProductId, loanId, transactionId, transactionDate,
                        feesAmount, loanTransactionDTO.getFeePayments());
            }
        }
        // create journal entries for the penalties application
        if (MathUtil.isGreaterThanZero(penaltiesAmount)) {
            if (transactionType.isAccrualAdjustment()) {
                this.helper.createJournalEntriesForLoanCharges(office, currencyCode,
                        AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(),
                        loanProductId, loanId, transactionId, transactionDate, penaltiesAmount, loanTransactionDTO.getPenaltyPayments());
            } else {
                this.helper.createJournalEntriesForLoanCharges(office, currencyCode, AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), loanProductId, loanId, transactionId, transactionDate,
                        penaltiesAmount, loanTransactionDTO.getPenaltyPayments());
            }
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
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        if (MathUtil.isGreaterThanZero(refundAmount)) {
            if (loanTransactionDTO.isAccountTransfer()) {
                this.helper.createJournalEntriesForLoan(office, currencyCode, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        FinancialActivity.LIABILITY_TRANSFER.getValue(), loanProductId, paymentTypeId, loanId, transactionId,
                        transactionDate, refundAmount);
            } else {
                this.helper.createJournalEntriesForLoan(office, currencyCode, AccrualAccountsForLoan.OVERPAYMENT.getValue(),
                        AccrualAccountsForLoan.FUND_SOURCE.getValue(), loanProductId, paymentTypeId, loanId, transactionId, transactionDate,
                        refundAmount);
            }
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
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        BigDecimal overpaymentAmount = loanTransactionDTO.getOverPayment();
        BigDecimal principalAmount = loanTransactionDTO.getPrincipal();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<JournalAmountHolder> journalAmountHolders = new ArrayList<>();

        if (MathUtil.isGreaterThanZero(principalAmount)) {
            totalAmount = totalAmount.add(principalAmount);
            journalAmountHolders
                    .add(new JournalAmountHolder(determineAccrualAccountForCBR(isMarkedChargeOff, isMarkedFraud, false), principalAmount));
        }
        if (MathUtil.isGreaterThanZero(overpaymentAmount)) {
            totalAmount = totalAmount.add(overpaymentAmount);
            journalAmountHolders
                    .add(new JournalAmountHolder(determineAccrualAccountForCBR(isMarkedChargeOff, isMarkedFraud, true), overpaymentAmount));
        }

        JournalAmountHolder totalAmountHolder = new JournalAmountHolder(AccrualAccountsForLoan.FUND_SOURCE.getValue(), totalAmount);
        helper.createSplitJournalEntriesForLoan(office, currencyCode, journalAmountHolders, totalAmountHolder, loanProductId, paymentTypeId,
                loanId, transactionId, transactionDate);

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
        final Long paymentTypeId = loanTransactionDTO.getPaymentTypeId();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        if (MathUtil.isGreaterThanZero(principalAmount)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            this.helper.createDebitJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, principalAmount);
        }

        if (MathUtil.isGreaterThanZero(interestAmount)) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            this.helper.createDebitJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(),
                    loanProductId, paymentTypeId, loanId, transactionId, transactionDate, interestAmount);
        }

        if (MathUtil.isGreaterThanZero(feesAmount)) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);

            List<ChargePaymentDTO> chargePaymentDTOs = new ArrayList<>();

            for (ChargePaymentDTO chargePaymentDTO : loanTransactionDTO.getFeePayments()) {
                chargePaymentDTOs.add(new ChargePaymentDTO(chargePaymentDTO.getChargeId(),
                        chargePaymentDTO.getAmount().floatValue() < 0 ? chargePaymentDTO.getAmount().multiply(new BigDecimal(-1))
                                : chargePaymentDTO.getAmount(),
                        chargePaymentDTO.getLoanChargeId()));
            }
            this.helper.createDebitJournalEntryForLoanCharges(office, currencyCode, AccrualAccountsForLoan.INCOME_FROM_FEES.getValue(),
                    loanProductId, loanId, transactionId, transactionDate, feesAmount, chargePaymentDTOs);
        }

        if (MathUtil.isGreaterThanZero(penaltiesAmount)) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            List<ChargePaymentDTO> chargePaymentDTOs = new ArrayList<>();

            for (ChargePaymentDTO chargePaymentDTO : loanTransactionDTO.getPenaltyPayments()) {
                chargePaymentDTOs.add(new ChargePaymentDTO(chargePaymentDTO.getChargeId(),
                        chargePaymentDTO.getAmount().floatValue() < 0 ? chargePaymentDTO.getAmount().multiply(new BigDecimal(-1))
                                : chargePaymentDTO.getAmount(),
                        chargePaymentDTO.getLoanChargeId()));
            }
            this.helper.createDebitJournalEntryForLoanCharges(office, currencyCode, AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue(),
                    loanProductId, loanId, transactionId, transactionDate, penaltiesAmount, chargePaymentDTOs);
        }

        if (MathUtil.isGreaterThanZero(overPaymentAmount)) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            this.helper.createDebitJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.OVERPAYMENT.getValue(), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, overPaymentAmount);
        }

        if (MathUtil.isGreaterThanZero(totalDebitAmount)) {
            /*** create a single debit entry (or reversal) for the entire amount **/
            this.helper.createCreditJournalEntryForLoan(office, currencyCode, AccrualAccountsForLoan.FUND_SOURCE.getValue(), loanProductId,
                    paymentTypeId, loanId, transactionId, transactionDate, totalDebitAmount);
        }
    }
}
