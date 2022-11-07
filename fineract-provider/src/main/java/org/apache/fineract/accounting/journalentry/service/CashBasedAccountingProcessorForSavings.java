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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.journalentry.data.ChargePaymentDTO;
import org.apache.fineract.accounting.journalentry.data.SavingsDTO;
import org.apache.fineract.accounting.journalentry.data.SavingsTransactionDTO;
import org.apache.fineract.organisation.office.domain.Office;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CashBasedAccountingProcessorForSavings implements AccountingProcessorForSavings {

    private final AccountingProcessorHelper helper;

    @Override
    public void createJournalEntriesForSavings(final SavingsDTO savingsDTO) {
        final GLClosure latestGLClosure = this.helper.getLatestClosureByBranch(savingsDTO.getOfficeId());
        final Long savingsProductId = savingsDTO.getSavingsProductId();
        final Long savingsId = savingsDTO.getSavingsId();
        final String currencyCode = savingsDTO.getCurrencyCode();
        for (final SavingsTransactionDTO savingsTransactionDTO : savingsDTO.getNewSavingsTransactions()) {
            final LocalDate transactionDate = savingsTransactionDTO.getTransactionDate();
            final String transactionId = savingsTransactionDTO.getTransactionId();
            final Office office = this.helper.getOfficeById(savingsTransactionDTO.getOfficeId());
            final Long paymentTypeId = savingsTransactionDTO.getPaymentTypeId();
            final boolean isReversal = savingsTransactionDTO.isReversed();
            final BigDecimal amount = savingsTransactionDTO.getAmount();
            final BigDecimal overdraftAmount = savingsTransactionDTO.getOverdraftAmount();
            final List<ChargePaymentDTO> feePayments = savingsTransactionDTO.getFeePayments();
            final List<ChargePaymentDTO> penaltyPayments = savingsTransactionDTO.getPenaltyPayments();

            this.helper.checkForBranchClosures(latestGLClosure, transactionDate);

            if (savingsTransactionDTO.getTransactionType().isWithdrawal() && savingsTransactionDTO.isOverdraftTransaction()) {
                boolean isPositive = amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) > 0;
                if (savingsTransactionDTO.isAccountTransfer()) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), FinancialActivity.LIABILITY_TRANSFER.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, overdraftAmount, isReversal);
                    if (isPositive) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                CashAccountsForSavings.SAVINGS_CONTROL.getValue(), FinancialActivity.LIABILITY_TRANSFER.getValue(),
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate,
                                amount.subtract(overdraftAmount), isReversal);
                    }
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                            CashAccountsForSavings.SAVINGS_REFERENCE.getValue(), savingsProductId, paymentTypeId, savingsId, transactionId,
                            transactionDate, overdraftAmount, isReversal);
                    if (isPositive) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                CashAccountsForSavings.SAVINGS_CONTROL.getValue(), CashAccountsForSavings.SAVINGS_REFERENCE.getValue(),
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate,
                                amount.subtract(overdraftAmount), isReversal);
                    }
                }
            } else if (savingsTransactionDTO.getTransactionType().isDeposit() && savingsTransactionDTO.isOverdraftTransaction()) {
                boolean isPositive = amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) > 0;
                if (savingsTransactionDTO.isAccountTransfer()) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            FinancialActivity.LIABILITY_TRANSFER.getValue(), CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, overdraftAmount, isReversal);
                    if (isPositive) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                FinancialActivity.LIABILITY_TRANSFER.getValue(), CashAccountsForSavings.SAVINGS_CONTROL.getValue(),
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate,
                                amount.subtract(overdraftAmount), isReversal);
                    }
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CashAccountsForSavings.SAVINGS_REFERENCE.getValue(),
                            CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, overdraftAmount, isReversal);
                    if (isPositive) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                CashAccountsForSavings.SAVINGS_REFERENCE.getValue(), CashAccountsForSavings.SAVINGS_CONTROL.getValue(),
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate,
                                amount.subtract(overdraftAmount), isReversal);
                    }
                }
            }

            /** Handle Deposits and reversals of deposits **/
            else if (savingsTransactionDTO.getTransactionType().isDeposit()) {
                if (savingsTransactionDTO.isAccountTransfer()) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            FinancialActivity.LIABILITY_TRANSFER.getValue(), CashAccountsForSavings.SAVINGS_CONTROL.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CashAccountsForSavings.SAVINGS_REFERENCE.getValue(), CashAccountsForSavings.SAVINGS_CONTROL.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                }
            }

            /** Handle Deposits and reversals of Dividend pay outs **/
            else if (savingsTransactionDTO.getTransactionType().isDividendPayout()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        FinancialActivity.PAYABLE_DIVIDENDS.getValue(), CashAccountsForSavings.SAVINGS_CONTROL.getValue(), savingsProductId,
                        paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }
            /** Handle withdrawals and reversals of withdrawals **/
            else if (savingsTransactionDTO.getTransactionType().isWithdrawal()) {
                if (savingsTransactionDTO.isAccountTransfer()) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CashAccountsForSavings.SAVINGS_CONTROL.getValue(), FinancialActivity.LIABILITY_TRANSFER.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CashAccountsForSavings.SAVINGS_CONTROL.getValue(), CashAccountsForSavings.SAVINGS_REFERENCE.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isEscheat()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        CashAccountsForSavings.SAVINGS_CONTROL.getValue(), CashAccountsForSavings.ESCHEAT_LIABILITY.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }
            /**
             * Handle Interest Applications and reversals of Interest Applications
             **/
            else if (savingsTransactionDTO.getTransactionType().isInterestPosting() && savingsTransactionDTO.isOverdraftTransaction()) {
                boolean isPositive = amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) > 0;
                // Post journal entry if earned interest amount is greater than
                // zero
                if (savingsTransactionDTO.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CashAccountsForSavings.INTEREST_ON_SAVINGS.getValue(),
                            CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, overdraftAmount, isReversal);
                    if (isPositive) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                CashAccountsForSavings.INTEREST_ON_SAVINGS.getValue(), CashAccountsForSavings.SAVINGS_CONTROL.getValue(),
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate,
                                amount.subtract(overdraftAmount), isReversal);
                    }
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isInterestPosting()) {
                // Post journal entry if earned interest amount is greater than
                // zero
                if (savingsTransactionDTO.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CashAccountsForSavings.INTEREST_ON_SAVINGS.getValue(), CashAccountsForSavings.SAVINGS_CONTROL.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isWithholdTax()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavingsTax(office, currencyCode,
                        CashAccountsForSavings.SAVINGS_CONTROL, CashAccountsForSavings.SAVINGS_REFERENCE, savingsProductId, paymentTypeId,
                        savingsId, transactionId, transactionDate, amount, isReversal, savingsTransactionDTO.getTaxPayments());
            }

            /** Handle Fees Deductions and reversals of Fees Deductions **/
            else if (savingsTransactionDTO.getTransactionType().isFeeDeduction() && savingsTransactionDTO.isOverdraftTransaction()) {
                boolean isPositive = amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) > 0;
                // Is the Charge a penalty?
                if (penaltyPayments.size() > 0) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL, CashAccountsForSavings.INCOME_FROM_PENALTIES,
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, overdraftAmount, isReversal,
                            penaltyPayments);
                    if (isPositive) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                                CashAccountsForSavings.SAVINGS_CONTROL, CashAccountsForSavings.INCOME_FROM_PENALTIES, savingsProductId,
                                paymentTypeId, savingsId, transactionId, transactionDate, amount.subtract(overdraftAmount), isReversal,
                                penaltyPayments);
                    }
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL, CashAccountsForSavings.INCOME_FROM_FEES, savingsProductId,
                            paymentTypeId, savingsId, transactionId, transactionDate, overdraftAmount, isReversal, feePayments);
                    if (isPositive) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                                CashAccountsForSavings.SAVINGS_CONTROL, CashAccountsForSavings.INCOME_FROM_FEES, savingsProductId,
                                paymentTypeId, savingsId, transactionId, transactionDate, amount.subtract(overdraftAmount), isReversal,
                                feePayments);
                    }
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isFeeDeduction()) {
                // Is the Charge a penalty?
                if (penaltyPayments.size() > 0) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            CashAccountsForSavings.SAVINGS_CONTROL, CashAccountsForSavings.INCOME_FROM_PENALTIES, savingsProductId,
                            paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal, penaltyPayments);
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            CashAccountsForSavings.SAVINGS_CONTROL, CashAccountsForSavings.INCOME_FROM_FEES, savingsProductId,
                            paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal, feePayments);
                }
            }

            /** Handle Transfers proposal **/
            else if (savingsTransactionDTO.getTransactionType().isInitiateTransfer()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        CashAccountsForSavings.SAVINGS_CONTROL.getValue(), CashAccountsForSavings.TRANSFERS_SUSPENSE.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }

            /** Handle Transfer Withdrawal or Acceptance **/
            else if (savingsTransactionDTO.getTransactionType().isWithdrawTransfer()
                    || savingsTransactionDTO.getTransactionType().isApproveTransfer()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        CashAccountsForSavings.TRANSFERS_SUSPENSE.getValue(), CashAccountsForSavings.SAVINGS_CONTROL.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }

            /** overdraft **/
            else if (savingsTransactionDTO.getTransactionType().isOverdraftInterest()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        CashAccountsForSavings.SAVINGS_REFERENCE.getValue(), CashAccountsForSavings.INCOME_FROM_INTEREST.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            } else if (savingsTransactionDTO.getTransactionType().isWrittenoff()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        CashAccountsForSavings.LOSSES_WRITTEN_OFF.getValue(), CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            } else if (savingsTransactionDTO.getTransactionType().isOverdraftFee()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                        CashAccountsForSavings.SAVINGS_REFERENCE, CashAccountsForSavings.INCOME_FROM_FEES, savingsProductId, paymentTypeId,
                        savingsId, transactionId, transactionDate, amount, isReversal, feePayments);
            }
        }
    }
}
