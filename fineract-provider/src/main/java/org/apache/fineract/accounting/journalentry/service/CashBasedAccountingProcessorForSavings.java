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
import java.util.Date;
import java.util.List;

import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SAVINGS;
import org.apache.fineract.accounting.common.AccountingConstants.FINANCIAL_ACTIVITY;
import org.apache.fineract.accounting.journalentry.data.ChargePaymentDTO;
import org.apache.fineract.accounting.journalentry.data.SavingsDTO;
import org.apache.fineract.accounting.journalentry.data.SavingsTransactionDTO;
import org.apache.fineract.organisation.office.domain.Office;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CashBasedAccountingProcessorForSavings implements AccountingProcessorForSavings {

    private final AccountingProcessorHelper helper;

    @Autowired
    public CashBasedAccountingProcessorForSavings(final AccountingProcessorHelper accountingProcessorHelper) {
        this.helper = accountingProcessorHelper;
    }

    @Override
    public void createJournalEntriesForSavings(final SavingsDTO savingsDTO) {
        final GLClosure latestGLClosure = this.helper.getLatestClosureByBranch(savingsDTO.getOfficeId());
        final Long savingsProductId = savingsDTO.getSavingsProductId();
        final Long savingsId = savingsDTO.getSavingsId();
        final String currencyCode = savingsDTO.getCurrencyCode();
        for (final SavingsTransactionDTO savingsTransactionDTO : savingsDTO.getNewSavingsTransactions()) {
            final Date transactionDate = savingsTransactionDTO.getTransactionDate();
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
                if (savingsTransactionDTO.isAccountTransfer()) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                            FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue(), savingsProductId, paymentTypeId, savingsId, transactionId,
                            transactionDate, overdraftAmount, isReversal);
                    if (amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) == 1) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(), FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue(),
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate,
                                amount.subtract(overdraftAmount), isReversal);
                    }
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                            CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.getValue(), savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, overdraftAmount, isReversal);
                    if (amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) == 1) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(),
                                CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.getValue(), savingsProductId, paymentTypeId, savingsId,
                                transactionId, transactionDate, amount.subtract(overdraftAmount), isReversal);
                    }
                }
            } else if (savingsTransactionDTO.getTransactionType().isDeposit() && savingsTransactionDTO.isOverdraftTransaction()) {
                if (savingsTransactionDTO.isAccountTransfer()) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue(),
                            CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, overdraftAmount, isReversal);
                    if (amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) == 1) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue(), CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(),
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate,
                                amount.subtract(overdraftAmount), isReversal);
                    }
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.getValue(),
                            CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, overdraftAmount, isReversal);
                    if (amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) == 1) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.getValue(),
                                CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                                transactionId, transactionDate, amount.subtract(overdraftAmount), isReversal);
                    }
                }
            }

            /** Handle Deposits and reversals of deposits **/
            else if (savingsTransactionDTO.getTransactionType().isDeposit()) {
                if (savingsTransactionDTO.isAccountTransfer()) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue(), CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.getValue(), CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                }
            }

            /** Handle Deposits and reversals of Dividend pay outs **/
            else if (savingsTransactionDTO.getTransactionType().isDividendPayout()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        FINANCIAL_ACTIVITY.PAYABLE_DIVIDENDS.getValue(), CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }
            /** Handle withdrawals and reversals of withdrawals **/
            else if (savingsTransactionDTO.getTransactionType().isWithdrawal()) {
                if (savingsTransactionDTO.isAccountTransfer()) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(), FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(), CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isEscheat()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                		CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(), CASH_ACCOUNTS_FOR_SAVINGS.ESCHEAT_LIABILITY.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }
            /**
             * Handle Interest Applications and reversals of Interest
             * Applications
             **/
            else if (savingsTransactionDTO.getTransactionType().isInterestPosting() && savingsTransactionDTO.isOverdraftTransaction()) {
                // Post journal entry if earned interest amount is greater than
                // zero
                if (savingsTransactionDTO.getAmount().compareTo(BigDecimal.ZERO) == 1) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.INTEREST_ON_SAVINGS.getValue(),
                            CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                            transactionId, transactionDate, overdraftAmount, isReversal);
                    if (amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) == 1) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                                CASH_ACCOUNTS_FOR_SAVINGS.INTEREST_ON_SAVINGS.getValue(),
                                CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                                transactionId, transactionDate, amount.subtract(overdraftAmount), isReversal);
                    }
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isInterestPosting()) {
                // Post journal entry if earned interest amount is greater than
                // zero
                if (savingsTransactionDTO.getAmount().compareTo(BigDecimal.ZERO) == 1) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.INTEREST_ON_SAVINGS.getValue(), CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(),
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isWithholdTax()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavingsTax(office, currencyCode,
                        CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL, CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE, savingsProductId,
                        paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal,
                        savingsTransactionDTO.getTaxPayments());
            }

            /** Handle Fees Deductions and reversals of Fees Deductions **/
            else if (savingsTransactionDTO.getTransactionType().isFeeDeduction() && savingsTransactionDTO.isOverdraftTransaction()) {
                // Is the Charge a penalty?
                if (penaltyPayments.size() > 0) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL, CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_PENALTIES,
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, overdraftAmount, isReversal,
                            penaltyPayments);
                    if (amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) == 1) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                                CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL, CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_PENALTIES,
                                savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate,
                                amount.subtract(overdraftAmount), isReversal, penaltyPayments);
                    }
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL, CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES,
                            savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, overdraftAmount, isReversal,
                            feePayments);
                    if (amount.subtract(overdraftAmount).compareTo(BigDecimal.ZERO) == 1) {
                        this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                                CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL, CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES, savingsProductId,
                                paymentTypeId, savingsId, transactionId, transactionDate, amount.subtract(overdraftAmount), isReversal,
                                feePayments);
                    }
                }
            }

            else if (savingsTransactionDTO.getTransactionType().isFeeDeduction()) {
                // Is the Charge a penalty?
                if (penaltyPayments.size() > 0) {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL, CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_PENALTIES, savingsProductId,
                            paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal, penaltyPayments);
                } else {
                    this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                            CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL, CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES, savingsProductId,
                            paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal, feePayments);
                }
            }

            /** Handle Transfers proposal **/
            else if (savingsTransactionDTO.getTransactionType().isInitiateTransfer()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(), CASH_ACCOUNTS_FOR_SAVINGS.TRANSFERS_SUSPENSE.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }

            /** Handle Transfer Withdrawal or Acceptance **/
            else if (savingsTransactionDTO.getTransactionType().isWithdrawTransfer()
                    || savingsTransactionDTO.getTransactionType().isApproveTransfer()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        CASH_ACCOUNTS_FOR_SAVINGS.TRANSFERS_SUSPENSE.getValue(), CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            }

            /** overdraft **/
            else if (savingsTransactionDTO.getTransactionType().isOverdraftInterest()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.getValue(), CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_INTEREST.getValue(),
                        savingsProductId, paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal);
            } else if (savingsTransactionDTO.getTransactionType().isWrittenoff()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavings(office, currencyCode,
                        CASH_ACCOUNTS_FOR_SAVINGS.LOSSES_WRITTEN_OFF.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingsProductId, paymentTypeId, savingsId,
                        transactionId, transactionDate, amount, isReversal);
            } else if (savingsTransactionDTO.getTransactionType().isOverdraftFee()) {
                this.helper.createCashBasedJournalEntriesAndReversalsForSavingsCharges(office, currencyCode,
                        CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE, CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES, savingsProductId,
                        paymentTypeId, savingsId, transactionId, transactionDate, amount, isReversal, feePayments);
            }
        }
    }
}
