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
package org.apache.fineract.investor.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.investor.accounting.journalentry.service.InvestorAccountingHelper;
import org.apache.fineract.investor.domain.ExternalAssetOwnerJournalEntryMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerJournalEntryMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferJournalEntryMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferJournalEntryMappingRepository;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountingServiceImpl implements AccountingService {

    private final InvestorAccountingHelper helper;
    private final ExternalAssetOwnerTransferJournalEntryMappingRepository externalAssetOwnerTransferJournalEntryMappingRepository;
    private final ExternalAssetOwnerJournalEntryMappingRepository externalAssetOwnerJournalEntryMappingRepository;
    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository;

    private static boolean participateInTransfer(FinancialActivityAccount financialActivityAccount, JournalEntry journalEntry,
            JournalEntryType filterType) {
        return filterType.getValue().equals(journalEntry.getType())
                && !Objects.equals(financialActivityAccount.getGlAccount().getId(), journalEntry.getGlAccount().getId());
    }

    @Override
    public void createJournalEntriesForSaleAssetTransfer(final Loan loan, final ExternalAssetOwnerTransfer transfer) {
        List<JournalEntry> journalEntryList = createJournalEntries(loan, transfer, true);
        createMappingToTransfer(transfer, journalEntryList);
        createMappingToOwner(transfer, journalEntryList, JournalEntryType.DEBIT);
    }

    @Override
    public void createJournalEntriesForBuybackAssetTransfer(final Loan loan, final ExternalAssetOwnerTransfer transfer) {
        List<JournalEntry> journalEntryList = createJournalEntries(loan, transfer, false);
        createMappingToTransfer(transfer, journalEntryList);
        createMappingToOwner(transfer, journalEntryList,
                LoanStatus.OVERPAID.equals(loan.getStatus()) ? JournalEntryType.DEBIT : JournalEntryType.CREDIT);
    }

    @NotNull
    private List<JournalEntry> createJournalEntries(Loan loan, ExternalAssetOwnerTransfer transfer, boolean isReversalOrder) {
        this.helper.checkForBranchClosures(loan.getOffice().getId(), transfer.getSettlementDate());
        // transaction properties
        final Long transactionId = transfer.getId();
        final LocalDate transactionDate = transfer.getSettlementDate();
        final BigDecimal principalAmount = loan.getSummary().getTotalPrincipalOutstanding();
        final BigDecimal interestAmount = loan.getSummary().getTotalInterestOutstanding();
        final BigDecimal feesAmount = loan.getSummary().getTotalFeeChargesOutstanding();
        final BigDecimal penaltiesAmount = loan.getSummary().getTotalPenaltyChargesOutstanding();
        final BigDecimal overPaymentAmount = loan.getTotalOverpaid();

        // Moving money to asset transfer account
        List<JournalEntry> journalEntryList = createJournalEntries(loan, transactionId, transactionDate, principalAmount, interestAmount,
                feesAmount, penaltiesAmount, overPaymentAmount, !isReversalOrder);
        // Moving money from asset transfer account
        journalEntryList.addAll(createJournalEntries(loan, transactionId, transactionDate, principalAmount, interestAmount, feesAmount,
                penaltiesAmount, overPaymentAmount, isReversalOrder));
        return journalEntryList;
    }

    private void createMappingToOwner(ExternalAssetOwnerTransfer transfer, List<JournalEntry> journalEntryList,
            JournalEntryType filterType) {
        FinancialActivityAccount financialActivityAccount = this.financialActivityAccountRepository
                .findByFinancialActivityTypeWithNotFoundDetection(AccountingConstants.FinancialActivity.ASSET_TRANSFER.getValue());
        journalEntryList.forEach(journalEntry -> {
            if (participateInTransfer(financialActivityAccount, journalEntry, filterType)) {
                ExternalAssetOwnerJournalEntryMapping mapping = new ExternalAssetOwnerJournalEntryMapping();
                mapping.setJournalEntry(journalEntry);
                mapping.setOwner(transfer.getOwner());
                externalAssetOwnerJournalEntryMappingRepository.saveAndFlush(mapping);
            }
        });
    }

    private void createMappingToTransfer(ExternalAssetOwnerTransfer transfer, List<JournalEntry> journalEntryList) {
        journalEntryList.forEach(journalEntry -> {
            ExternalAssetOwnerTransferJournalEntryMapping mapping = new ExternalAssetOwnerTransferJournalEntryMapping();
            mapping.setJournalEntry(journalEntry);
            mapping.setOwnerTransfer(transfer);
            externalAssetOwnerTransferJournalEntryMappingRepository.saveAndFlush(mapping);
        });
    }

    private List<JournalEntry> createJournalEntries(Loan loan, Long transactionId, LocalDate transactionDate, BigDecimal principalAmount,
            BigDecimal interestAmount, BigDecimal feesAmount, BigDecimal penaltiesAmount, BigDecimal overPaymentAmount,
            boolean isReversalOrder) {
        Long loanProductId = loan.productId();
        Long loanId = loan.getId();
        Office office = loan.getOffice();
        String currencyCode = loan.getCurrencyCode();
        List<JournalEntry> journalEntryList = new ArrayList<>();
        BigDecimal totalDebitAmount = BigDecimal.ZERO;
        Map<GLAccount, BigDecimal> accountMap = new LinkedHashMap<>();
        // principal entry
        if (principalAmount != null && principalAmount.compareTo(BigDecimal.ZERO) > 0) {
            AccountingConstants.AccrualAccountsForLoan accrualAccount = AccountingConstants.AccrualAccountsForLoan.LOAN_PORTFOLIO;
            if (loan.isChargedOff()) {
                if (loan.isFraud()) {
                    accrualAccount = AccountingConstants.AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE;
                } else {
                    accrualAccount = AccountingConstants.AccrualAccountsForLoan.CHARGE_OFF_EXPENSE;
                }
            }
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, accrualAccount.getValue());
            accountMap.put(account, principalAmount);
        }
        // interest entry
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            AccountingConstants.AccrualAccountsForLoan accrualAccount = AccountingConstants.AccrualAccountsForLoan.INTEREST_RECEIVABLE;
            if (loan.isChargedOff()) {
                accrualAccount = AccountingConstants.AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST;
            }
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, accrualAccount.getValue());
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(interestAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, interestAmount);
            }
        }
        // fee entry
        if (feesAmount != null && feesAmount.compareTo(BigDecimal.ZERO) > 0) {
            AccountingConstants.AccrualAccountsForLoan accrualAccount = AccountingConstants.AccrualAccountsForLoan.FEES_RECEIVABLE;
            if (loan.isChargedOff()) {
                accrualAccount = AccountingConstants.AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES;
            }
            totalDebitAmount = totalDebitAmount.add(feesAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, accrualAccount.getValue());
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(feesAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, feesAmount);
            }
        }
        // penalty entry
        if (penaltiesAmount != null && penaltiesAmount.compareTo(BigDecimal.ZERO) > 0) {
            AccountingConstants.AccrualAccountsForLoan accrualAccount = AccountingConstants.AccrualAccountsForLoan.PENALTIES_RECEIVABLE;
            if (loan.isChargedOff()) {
                accrualAccount = AccountingConstants.AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY;
            }
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId, accrualAccount.getValue());
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(penaltiesAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, penaltiesAmount);
            }
        }
        // overpaid entry
        if (overPaymentAmount != null && overPaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalDebitAmount = totalDebitAmount.add(overPaymentAmount);
            GLAccount account = this.helper.getLinkedGLAccountForLoanProduct(loanProductId,
                    AccountingConstants.AccrualAccountsForLoan.OVERPAYMENT.getValue());
            if (accountMap.containsKey(account)) {
                BigDecimal amount = accountMap.get(account).add(overPaymentAmount);
                accountMap.put(account, amount);
            } else {
                accountMap.put(account, overPaymentAmount);
            }
        }
        // asset transfer entry
        for (Map.Entry<GLAccount, BigDecimal> entry : accountMap.entrySet()) {
            journalEntryList.add(this.helper.createCreditJournalEntryOrReversalForInvestor(office, currencyCode, loanId, transactionId,
                    transactionDate, entry.getValue(), isReversalOrder, entry.getKey()));
        }
        if (totalDebitAmount.compareTo(BigDecimal.ZERO) > 0) {
            journalEntryList.add(this.helper.createDebitJournalEntryOrReversalForInvestor(office, currencyCode,
                    AccountingConstants.FinancialActivity.ASSET_TRANSFER.getValue(), loanProductId, loanId, transactionId, transactionDate,
                    totalDebitAmount, isReversalOrder));
        }
        return journalEntryList;
    }
}
